package org.jetbrains.bsp.project.resolver

import java.io.File
import java.net.URI
import java.nio.file.Path
import java.util.{Collections, Optional}

import ch.epfl.scala.bsp4j._
import com.google.gson.{Gson, JsonElement}
import com.intellij.openapi.externalSystem.model.project._
import com.intellij.openapi.externalSystem.model.{DataNode, ProjectKeys}
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.roots.DependencyScope
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.bsp.BspUtil._
import org.jetbrains.bsp.data.{BspMetadata, SbtBuildModuleDataBsp, ScalaSdkData}
import org.jetbrains.bsp.project.BspSyntheticModuleType
import org.jetbrains.bsp.project.resolver.BspResolverDescriptors._
import org.jetbrains.bsp.{BSP, BspErrorMessage}

import scala.collection.JavaConverters._

private[resolver] object BspResolverLogic {

  private def extractScalaSdkData(data: JsonElement)(implicit gson: Gson): Option[ScalaBuildTarget] =
    Option(gson.fromJson[ScalaBuildTarget](data, classOf[ScalaBuildTarget]))

  private def extractSbtData(data: JsonElement)(implicit gson: Gson): Option[SbtBuildTarget] =
    Option(gson.fromJson[SbtBuildTarget](data, classOf[SbtBuildTarget]))

  /** Find common base path of all given files */
  private[resolver] def commonBase(dirs: Iterable[File]): Option[File] = {
    val paths = dirs.map(_.toPath)
    if (paths.isEmpty) None
    else {
      val basePath = paths.foldLeft(paths.head) { case (common, it) =>
        common.iterator().asScala.zip(it.iterator().asScala)
          .takeWhile { case (c, p) => c == p }
          .map(_._1)
          .foldLeft(paths.head.getRoot) { case (base, child) => base.resolve(child) }
      }

      Some(basePath.toFile)
    }
  }


  private[resolver] def getScalaSdkData(target: ScalaBuildTarget, scalacOptionsItem: Option[ScalacOptionsItem]): ScalaSdkData = {
    val scalaOptionsStrings = scalacOptionsItem.map(item => item.getOptions).getOrElse(Collections.emptyList())
    ScalaSdkData(
      scalaOrganization = target.getScalaOrganization,
      scalaVersion = Optional.of(target.getScalaVersion),
      scalacClasspath = target.getJars.asScala.map(_.toURI.toFile).asJava,
      scalacOptions = scalaOptionsStrings
    )
  }

  private[resolver] def getSbtData(target: SbtBuildTarget, scalacOptionsItem: Option[ScalacOptionsItem]): (SbtBuildModuleDataBsp, ScalaSdkData) = {
    val buildFor = target.getChildren.asScala.map { target => new URI(target.getUri) }

    val sbtBuildModuleData = SbtBuildModuleDataBsp(
      target.getAutoImports,
      buildFor.asJava
    )
    val scalaSdkData = getScalaSdkData(target.getScalaBuildTarget, scalacOptionsItem)

    (sbtBuildModuleData, scalaSdkData)
  }

  private[resolver] def calculateModuleDescriptions(buildTargets: Seq[BuildTarget],
                                                    optionsItems: Seq[ScalacOptionsItem],
                                                    sourcesItems: Seq[SourcesItem],
                                                    dependencySourcesItems: Seq[DependencySourcesItem],
                                                    testClasses: Seq[ScalaTestClassesItem]): ProjectModules = {

    implicit val gson: Gson = new Gson()

    val idToTarget = buildTargets.map(t => (t.getId, t)).toMap
    val idToScalacOptions = optionsItems.map(item => (item.getTarget, item)).toMap
    val idToDepSources = dependencySourcesItems.map(item => (item.getTarget, item)).toMap

    def transitiveDependencyOutputs(start: BuildTarget): Seq[File] = {
      val transitiveDeps = (start +: transitiveDependencies(start)).map(_.getId)
      transitiveDeps.flatMap(idToScalacOptions.get).map(_.getClassDirectory.toURI.toFile)
    }

    def transitiveDependencies(start: BuildTarget): Seq[BuildTarget] = {
      val direct = start.getDependencies.asScala.flatMap(idToTarget.get) // TODO warning when dependencies are not in buildTargets
      val transitive = direct.flatMap(transitiveDependencies)
      (start +: (direct ++ transitive)).distinct
    }

    val idToSources = sourcesItems
      .map(item => (item.getTarget, sourceDirectories(item)))
      .toMap

    val idToTestClasses: Map[BuildTargetIdentifier, Seq[TestClassId]] = testClasses
      .map(item => (item.getTarget, item.getClasses.asScala))
      .toMap

    val sharedSources = sharedSourceDirs(idToSources)

    val moduleDescriptions = buildTargets.flatMap { target: BuildTarget =>
      val id = target.getId
      val scalacOptions = idToScalacOptions.get(id)
      val depSourcesOpt = idToDepSources.get(id)
      val sources = idToSources
        .getOrElse(id, Seq.empty)
        .filterNot(sharedSources.contains)
      val testClasses = idToTestClasses.getOrElse(id, Seq.empty)
      val dependencyOutputs = transitiveDependencyOutputs(target)

      moduleDescriptionForTarget(target, scalacOptions, depSourcesOpt, sources, dependencyOutputs, testClasses)
    }

    val idToModule = (for {
      m <- moduleDescriptions
      t <- m.data.targets
    } yield (t.getId, m)).toMap

    val allSourcesBase = commonBase(idToSources.values.flatten.map(_.directory)).map(_.toPath)

    val syntheticModules = sharedSources.map { case (src, targetIds) =>
      val targets = targetIds.map(idToTarget)
      val sharingModules = targetIds.map(idToModule)
      createSyntheticModuleDescription(allSourcesBase, targets, Seq(src), sharingModules)
    }

    // merge modules with the same module base
    val (noBase, withBase) = moduleDescriptions.partition(_.data.basePath.isEmpty)
    val mergedBase = withBase.groupBy(_.data.basePath).values.map(mergeModules)
    val modules = noBase ++ mergedBase

    ProjectModules(modules, syntheticModules.toSeq)
  }

  private def sharedSourceDirs(idToSources: Map[BuildTargetIdentifier, Seq[SourceDirectory]]): Map[SourceDirectory, Seq[BuildTargetIdentifier]] = {
    val idToSrc = for {
      (id, sources) <- idToSources.toSeq
      dir <- sources
    } yield (id, dir)

    idToSrc
      .groupBy(_._2) // TODO merge source dirs with mixed generated flag?
      .map { case (dir, derp) => (dir, derp.map(_._1)) }
      .filter(_._2.size > 1)
  }

  private def sourceDirectories(sourcesItem: SourcesItem): Seq[SourceDirectory] = {
    val sourceItems: Seq[SourceItem] = sourcesItem.getSources.asScala.distinct

    sourceItems
      .map { item =>
        val file = item.getUri.toURI.toFile
        // bsp spec used to depend on uri ending in `/` to determine directory, use both kind and uri string to determine directory
        if (item.getKind == SourceItemKind.DIRECTORY || item.getUri.endsWith("/"))
          SourceDirectory(file, item.getGenerated)
        else
        // use the file's immediate parent as best guess of source dir
        // IntelliJ project model doesn't have a concept of individual source files
          SourceDirectory(file.getParentFile, item.getGenerated)
      }
      .distinct
  }

  private[resolver] def moduleDescriptionForTarget(target: BuildTarget,
                                                   scalacOptions: Option[ScalacOptionsItem],
                                                   depSourcesOpt: Option[DependencySourcesItem],
                                                   sourceDirs: Seq[SourceDirectory],
                                                   dependencyOutputs: Seq[File],
                                                   testClasses: Seq[TestClassId]
                                                  )(implicit gson: Gson): Option[ModuleDescription] = {

    val dependencySourcePaths = for {
      depSources <- depSourcesOpt.toSeq
      depSrc <- depSources.getSources.asScala
    } yield depSrc.toURI.toFile

    // all subdirectories of a source dir are automatically source dirs
    val sourceRoots = sourceDirs.filter { dir =>
      ! sourceDirs.exists(a => FileUtil.isAncestor(a.directory, dir.directory, true))
    }

    val moduleBase = Option(target.getBaseDirectory)
      .map(_.toURI.toFile)

    val outputPath = scalacOptions.map(_.getClassDirectory.toURI.toFile)

    // classpath needs to be filtered for module dependency output paths since they are handled by IDEA module dep mechanism
    val classPath = scalacOptions.map(_.getClasspath.asScala.map(_.toURI.toFile))

    val classPathWithoutDependencyOutputs = classPath.getOrElse(Seq.empty).filterNot(dependencyOutputs.contains)

    val tags = target.getTags.asScala

    val targetData =
      if (tags.contains(BuildTargetTag.NO_IDE)) None
      else Option(target.getData).map(_.asInstanceOf[JsonElement])

    val scalaModule =
      targetData.flatMap(extractScalaSdkData)
        .map(target => getScalaSdkData(target, scalacOptions))
        .map(ScalaModule)

    // TODO there's ambiguity in the data object in BuildTarget.data
    //   there needs to be a marker for the type so that it can be deserialized to the correct class

    // TODO there's some disagreement on responsibility of handling sbt build data.
    //  specifically with bloop, the main workspace is not sbt-aware, and IntelliJ would need to start separate bloop
    //  servers for the build modules.
    //      val sbtModule =
    //        targetData.flatMap(extractSbtData)
    //          .map { data =>
    //            val (sbtModuleData, scalaSdkData) = calculateSbtData(data)
    //            SbtModule(scalaSdkData, sbtModuleData)
    //          }

    // TODO warning output when modules are skipped because of missing base or scala module data
    for {
      moduleKind <- scalaModule
    } yield {
      val moduleDescriptionData = createModuleDescriptionData(
        Seq(target), tags, moduleBase, outputPath, sourceRoots,
        classPathWithoutDependencyOutputs, dependencySourcePaths, testClasses)

      ModuleDescription(moduleDescriptionData, moduleKind)
    }
  }

  private[resolver] def createModuleDescriptionData(targets: Seq[BuildTarget],
                                                    tags: Seq[String],
                                                    moduleBase: Option[File],
                                                    outputPath: Option[File],
                                                    sourceRoots: Seq[SourceDirectory],
                                                    classPath: Seq[File],
                                                    dependencySources: Seq[File],
                                                    testClasses: Seq[TestClassId]
                                               ): ModuleDescriptionData = {
    import BuildTargetTag._

    val primaryTarget = targets.headOption
    val moduleId = primaryTarget
      .map(_.getId.getUri)
      .orElse(moduleBase.map(_.toURI.toString))
      .orElse(sourceRoots.headOption.map(_.directory.toURI.toString))
      .getOrElse(throw BspErrorMessage(s"unable to determine unique module id for module targets: $targets"))
    val moduleName = primaryTarget.flatMap(t => Option(t.getDisplayName)).getOrElse(moduleId)

    val dataBasic = ModuleDescriptionData(
      moduleId,
      moduleName,
      targets,
      Seq.empty, Seq.empty,
      moduleBase,
      None, None,
      Seq.empty, Seq.empty,
      Seq.empty, Seq.empty,
      Seq.empty, Seq.empty,
      Seq.empty
    )

    val targetDeps = targets.flatMap(_.getDependencies.asScala)

    val data1 = if (tags.contains(LIBRARY) || tags.contains(APPLICATION))
      dataBasic.copy(
        targetDependencies = targetDeps,
        output = outputPath,
        sourceDirs = sourceRoots,
        classpath = classPath,
        classpathSources = dependencySources
      ) else dataBasic

    val data2 = if(tags.contains(TEST))
      data1.copy(
        targetTestDependencies = targetDeps,
        testOutput = outputPath,
        testSourceDirs = sourceRoots,
        testClasspath = classPath,
        testClasspathSources = dependencySources,
        testClasses = testClasses
      ) else data1

    // TODO ignore and warn about unsupported build target kinds? map to special module?

    data2
  }

  /** "Inherits" data from other modules into newly created synthetic module description.
   * This is a heuristic to for sharing source directories between modules. If those modules have conflicting dependencies,
   * this mapping may break in unspecified ways.
   */
  private[resolver] def createSyntheticModuleDescription(allSourcesBase: Option[Path],
                                                         targets: Seq[BuildTarget],
                                                         sourceRoots: Seq[SourceDirectory],
                                                         ancestors: Seq[ModuleDescription]): ModuleDescription = {
    // the synthetic module "inherits" most of the "ancestors" data
    val merged = mergeModules(ancestors)
    val id = sourceRoots.headOption
      .map { dir =>
        val idPath = allSourcesBase
            .map(_.relativize(dir.directory.toPath))
            .getOrElse(dir.directory.toPath)
        "shared:" + idPath.toString
      }
      .getOrElse("shared:" + merged.data.id)

    val inheritorData = merged.data.copy(
      id = id,
      name = id,
      targets = targets,
      sourceDirs = sourceRoots,
      testSourceDirs = Seq.empty
    )
    merged.copy(data = inheritorData)
  }


  /** Merge modules assuming they have the same base path. */
  private[resolver] def mergeModules(descriptions: Seq[ModuleDescription]): ModuleDescription = {
    descriptions
      .sortBy(_.data.id)
      .reduce { (combined, next) =>
        val dataCombined = combined.data
        val dataNext = next.data
        val targets = (dataCombined.targets ++ dataNext.targets).sortBy(_.getId.getUri).distinct
        val targetDependencies = mergeBTIs(dataCombined.targetDependencies, dataNext.targetDependencies)
        val targetTestDependencies = mergeBTIs(dataCombined.targetTestDependencies, dataNext.targetTestDependencies)
        val output = dataCombined.output.orElse(dataNext.output)
        val testOutput = dataCombined.testOutput.orElse(dataNext.testOutput)
        val sourceDirs = mergeSourceDirs(dataCombined.sourceDirs, dataNext.sourceDirs)
        val testSourceDirs  = mergeSourceDirs(dataCombined.testSourceDirs, dataNext.testSourceDirs)
        val classPath = mergeFiles(dataCombined.classpath, dataNext.classpath)
        val classPathSources = mergeFiles(dataCombined.classpathSources, dataNext.classpathSources)
        val testClassPath = mergeFiles(dataCombined.testClasspath, dataNext.testClasspath)
        val testClassPathSources = mergeFiles(dataCombined.testClasspathSources, dataNext.testClasspathSources)
        val testClasses = dataCombined.testClasses ++ dataNext.testClasses

        val newData = ModuleDescriptionData(
          dataCombined.id, dataCombined.name,
          targets, targetDependencies, targetTestDependencies, dataCombined.basePath,
          output, testOutput, sourceDirs, testSourceDirs,
          classPath, classPathSources, testClassPath, testClassPathSources, testClasses
        )

        combined.copy(data = newData)
      }
  }

  private def mergeBTIs(a: Seq[BuildTargetIdentifier], b: Seq[BuildTargetIdentifier]) =
    (a++b).sortBy(_.getUri).distinct

  private def mergeSourceDirs(a: Seq[SourceDirectory], b: Seq[SourceDirectory]) =
    (a++b).sortBy(_.directory.getAbsolutePath).distinct

  private def mergeFiles(a: Seq[File], b: Seq[File]) =
    (a++b).sortBy(_.getAbsolutePath).distinct


  private[resolver] def projectNode(projectRootPath: String,
                                    moduleFilesDirectoryPath: String,
                                    projectModules: ProjectModules): DataNode[ProjectData] = {

    val projectRoot = new File(projectRootPath)
    val projectData = new ProjectData(BSP.ProjectSystemId, projectRoot.getName, projectRootPath, projectRootPath)
    val projectNode = new DataNode[ProjectData](ProjectKeys.PROJECT, projectData, null)

    // synthetic root module when no natural module is at root
    val rootModule =
      if (projectModules.modules.exists (_.data.basePath.exists(_ == projectRoot))) None
      else {
        val name = projectRoot.getName + "-root"
        val moduleData = new ModuleData(name, BSP.ProjectSystemId, BspSyntheticModuleType.Id, name, moduleFilesDirectoryPath, projectRootPath)
        val moduleNode = new DataNode[ModuleData](ProjectKeys.MODULE, moduleData, projectNode)
        val contentRootData = new ContentRootData(BSP.ProjectSystemId, projectRoot.getCanonicalPath)
        val contentRootDataNode = new DataNode[ContentRootData](ProjectKeys.CONTENT_ROOT, contentRootData, moduleNode)
        moduleNode.addChild(contentRootDataNode)

        Some(moduleNode)
      }

    def toModuleNode(moduleDescription: ModuleDescription) =
      createModuleNode(projectRootPath, moduleFilesDirectoryPath, moduleDescription, projectNode)

    val idsToTargetModule: Seq[(Seq[TargetId], DataNode[ModuleData])] =
      projectModules.modules.map { m =>
        val targetIds = m.data.targets.map(t => TargetId(t.getId.getUri))
        val node = toModuleNode(m)
        targetIds -> node
      }

    val idToRootModule = rootModule.toSeq.map(m => SynthId(m.getData.getId) -> m)
    val idToSyntheticModule = projectModules.synthetic.map { m => SynthId(m.data.id) -> toModuleNode(m) }
    val idToTargetModule = idsToTargetModule.flatMap { case (ids,m) => ids.map(_ -> m)}
    val idToModuleMap: Map[DependencyId, DataNode[ModuleData]] =
      (idToRootModule ++ idToTargetModule ++ idToSyntheticModule).toMap


    val moduleDeps = calculateModuleDependencies(projectModules)
    val synthDeps = calculateSyntheticDependencies(moduleDeps, projectModules)
    val modules = idsToTargetModule.map(_._2) ++ idToSyntheticModule.map(_._2)

    // effects
    addModuleDependencies(moduleDeps ++ synthDeps, idToModuleMap)
    modules.foreach(projectNode.addChild)

    projectNode
  }

  private def idToModule(modules: Seq[(scala.Seq[BuildTarget], DataNode[ModuleData])]) = for {
    (targets,module) <- modules
    target <- targets
  } yield {
    (TargetId(target.getId.getUri), module)
  }

  private[resolver] def createModuleNode(projectRootPath: String,
                                         moduleFilesDirectoryPath: String,
                                         moduleDescription: ModuleDescription,
                                         projectNode: DataNode[ProjectData]): DataNode[ModuleData] = {

    import ExternalSystemSourceType._

    val moduleDescriptionData = moduleDescription.data

    val moduleBase = moduleDescriptionData.basePath.map { path =>
      val base = path.getCanonicalPath
      new ContentRootData(BSP.ProjectSystemId, base)
    }
    val sourceRoots = moduleDescriptionData.sourceDirs.map { dir =>
      val sourceType = if (dir.generated) SOURCE_GENERATED else SOURCE
      (sourceType, dir)
    }

    val testRoots = moduleDescriptionData.testSourceDirs.map { dir =>
      val sourceType = if (dir.generated) TEST_GENERATED else TEST
      (sourceType, dir)
    }

    val allSourceRoots = (sourceRoots ++ testRoots).toSet

    val moduleName = moduleDescriptionData.name
    val moduleData = new ModuleData(moduleDescriptionData.id, BSP.ProjectSystemId, StdModuleTypes.JAVA.getId, moduleName, moduleFilesDirectoryPath, projectRootPath)

    moduleDescriptionData.output.foreach { outputPath =>
      moduleData.setCompileOutputPath(SOURCE, outputPath.getCanonicalPath)
    }
    moduleDescriptionData.testOutput.foreach { outputPath =>
      moduleData.setCompileOutputPath(TEST, outputPath.getCanonicalPath)
    }

    moduleData.setInheritProjectCompileOutputPath(false)

    val libraryData = new LibraryData(BSP.ProjectSystemId, s"$moduleName dependencies")
    moduleDescriptionData.classpath.foreach { path =>
      libraryData.addPath(LibraryPathType.BINARY, path.getCanonicalPath)
    }
    moduleDescriptionData.classpathSources.foreach { path =>
      libraryData.addPath(LibraryPathType.SOURCE, path.getCanonicalPath)
    }
    val libraryDependencyData = new LibraryDependencyData(moduleData, libraryData, LibraryLevel.MODULE)
    libraryDependencyData.setScope(DependencyScope.COMPILE)

    val libraryTestData = new LibraryData(BSP.ProjectSystemId, s"$moduleName test dependencies")
    moduleDescriptionData.testClasspath.foreach { path =>
      libraryTestData.addPath(LibraryPathType.BINARY, path.getCanonicalPath)
    }
    moduleDescriptionData.testClasspathSources.foreach { path =>
      libraryTestData.addPath(LibraryPathType.SOURCE, path.getCanonicalPath)
    }
    val libraryTestDependencyData = new LibraryDependencyData(moduleData, libraryTestData, LibraryLevel.MODULE)
    libraryTestDependencyData.setScope(DependencyScope.TEST)

    // data node wiring

    val moduleNode = new DataNode[ModuleData](ProjectKeys.MODULE, moduleData, projectNode)

    val libraryDependencyNode = new DataNode[LibraryDependencyData](ProjectKeys.LIBRARY_DEPENDENCY, libraryDependencyData, moduleNode)
    moduleNode.addChild(libraryDependencyNode)
    val libraryTestDependencyNode = new DataNode[LibraryDependencyData](ProjectKeys.LIBRARY_DEPENDENCY, libraryTestDependencyData, moduleNode)
    moduleNode.addChild(libraryTestDependencyNode)

    allSourceRoots.map { case (sourceType, root) =>
      val data = getContentRoot(root.directory, moduleBase)
      data.storePath(sourceType, root.directory.getCanonicalPath)
      data
    }.foreach { data =>
      // because we map on a set, `data` is deduplicated before creating content root data nodes. thus each node is only added once
      val contentRootDataNode = new DataNode[ContentRootData](ProjectKeys.CONTENT_ROOT, data, moduleNode)
      moduleNode.addChild(contentRootDataNode)
    }

    val targetIds = moduleDescriptionData.targets.map(_.getId.getUri.toURI)
    val metadata = BspMetadata(targetIds.asJava, moduleDescriptionData.testClasses.asJava)

    val metadataNode = new DataNode[BspMetadata](BspMetadata.Key, metadata, moduleNode)
    moduleNode.addChild(metadataNode)

    addNodeKindData(moduleNode, moduleDescription.moduleKindData)

    moduleNode
  }

  /** Use moduleBase content root when possible, or create a new content root if dir is not within moduleBase. */
  private[resolver] def getContentRoot(dir: File, moduleBase: Option[ContentRootData]) = {
    val baseRoot = for {
      contentRoot <- moduleBase
      if FileUtil.isAncestor(contentRoot.getRootPath, dir.getCanonicalPath, false)
    } yield contentRoot

    baseRoot.getOrElse(new ContentRootData(BSP.ProjectSystemId, dir.getCanonicalPath))
  }


  private[resolver] def calculateModuleDependencies(projectModules: ProjectModules): Seq[ModuleDep] = for {
    moduleDescription <- projectModules.modules
    moduleTargets = moduleDescription.data.targets
    aTarget <- moduleTargets.headOption.toSeq // any id will resolve the module in idToModule
    d <- {
      val moduleId = aTarget.getId.getUri
      val compileDeps = moduleDescription.data.targetDependencies.map((_, DependencyScope.COMPILE))
      val testDeps = moduleDescription.data.targetTestDependencies.map((_, DependencyScope.TEST))

      (compileDeps ++ testDeps)
        .filterNot(d => moduleTargets.exists(t => t.getId == d._1))
        .map { case (moduleDepId, scope) =>
          ModuleDep(TargetId(moduleId), TargetId(moduleDepId.getUri), scope, export = true)
        }
    }
  } yield d

  private[resolver] def calculateSyntheticDependencies(moduleDependencies: Seq[ModuleDep], projectModules: ProjectModules) = {
    // 1. synthetic module is depended on by all its parent targets
    // 2. synthetic module depends on all parent target's dependencies
    val dependencyByParent = moduleDependencies.groupBy(_.parent)

    for {
      moduleDescription <- projectModules.synthetic
      synthParent <- moduleDescription.data.targets
      dep <- {
        val synthId = SynthId(moduleDescription.data.id)
        val parentId = TargetId(synthParent.getId.getUri)

        val parentDeps = dependencyByParent.getOrElse(parentId, Seq.empty)
        val inheritedDeps = parentDeps.map { d => ModuleDep(synthId, d.child, d.scope, export = false)}

        val parentSynthDependency = ModuleDep(parentId, synthId, DependencyScope.COMPILE, export = true)
        parentSynthDependency +: inheritedDeps
      }
    } yield dep
  }

  private[resolver] def addModuleDependencies(dependencies: Seq[ModuleDep],
                                              idToModules: Map[DependencyId, DataNode[ModuleData]]): Seq[DataNode[ModuleData]] = {
    dependencies.flatMap { dep =>
      for {
        parent <- idToModules.get(dep.parent)
        child <- idToModules.get(dep.child)
      } yield {
        addDep(parent, child, dep.scope, dep.export)
      }
    }
  }

  private def addDep(parent: DataNode[ModuleData],
                     child: DataNode[ModuleData],
                     scope: DependencyScope,
                     exported: Boolean
                    ): DataNode[ModuleData] = {

    val data = new ModuleDependencyData(parent.getData, child.getData)
    data.setScope(scope)
    data.setExported(exported)

    val node = new DataNode[ModuleDependencyData](ProjectKeys.MODULE_DEPENDENCY, data, parent)
    parent.addChild(node)
    child
  }

  private[resolver] def addNodeKindData(moduleNode: DataNode[ModuleData], moduleKind: ModuleKind): Unit = {
    moduleKind match {
      case ScalaModule(scalaSdkData) =>

        val moduleData = moduleNode.getData

        val scalaSdkLibrary = new LibraryData(BSP.ProjectSystemId, ScalaSdkData.LibraryName)
        scalaSdkData.scalacClasspath.forEach { path =>
          scalaSdkLibrary.addPath(LibraryPathType.BINARY, path.getCanonicalPath)
        }
        val scalaSdkLibraryDependencyData = new LibraryDependencyData(moduleData, scalaSdkLibrary, LibraryLevel.MODULE)
        scalaSdkLibraryDependencyData.setScope(DependencyScope.COMPILE)

        val scalaSdkLibraryNode = new DataNode[LibraryDependencyData](ProjectKeys.LIBRARY_DEPENDENCY, scalaSdkLibraryDependencyData, moduleNode)
        moduleNode.addChild(scalaSdkLibraryNode)

        val scalaSdkNode = new DataNode[ScalaSdkData](ScalaSdkData.Key, scalaSdkData, moduleNode)
        moduleNode.addChild(scalaSdkNode)


      case SbtModule(scalaSdkData, sbtData) =>
        val scalaSdkNode = new DataNode[ScalaSdkData](ScalaSdkData.Key, scalaSdkData, moduleNode)
        val sbtNode = new DataNode[SbtBuildModuleDataBsp](SbtBuildModuleDataBsp.Key, sbtData, moduleNode)
        moduleNode.addChild(scalaSdkNode)
        moduleNode.addChild(sbtNode)
    }
  }

  private[resolver] sealed abstract class DependencyId(id: String)
  private[resolver] case class SynthId(id: String) extends DependencyId(id)
  private[resolver] case class TargetId(id: String) extends DependencyId(id)

  private[resolver] case class ModuleDep(parent: DependencyId, child: DependencyId, scope: DependencyScope, export: Boolean)

}

