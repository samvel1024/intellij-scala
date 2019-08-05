package org.jetbrains.bsp.project.resolver

import java.io.File

import ch.epfl.scala.bsp4j._
import org.jetbrains.bsp.BspError
import org.jetbrains.bsp.data.{SbtBuildModuleDataBsp, ScalaSdkData}


private[resolver] object BspResolverDescriptors {

  private[resolver] type TestClassId = String

  private[resolver] case class ModuleDescription(data: ModuleDescriptionData,
                                                 moduleKindData: ModuleKind)

  private[resolver] case class ModuleDescriptionData(id: String,
                                                     name: String,
                                                     targets: Seq[BuildTarget],
                                                     targetDependencies: Seq[BuildTargetIdentifier],
                                                     targetTestDependencies: Seq[BuildTargetIdentifier],
                                                     basePath: Option[File],
                                                     output: Option[File],
                                                     testOutput: Option[File],
                                                     sourceDirs: Seq[SourceDirectory],
                                                     testSourceDirs: Seq[SourceDirectory],
                                                     classpath: Seq[File],
                                                     classpathSources: Seq[File],
                                                     testClasspath: Seq[File],
                                                     testClasspathSources: Seq[File],
                                                     testClasses: Seq[TestClassId])

  private[resolver] case class ProjectModules(modules: Seq[ModuleDescription], synthetic: Seq[ModuleDescription])

  private[resolver] sealed abstract class ModuleKind

  private[resolver] case class ScalaModule(scalaSdkData: ScalaSdkData) extends ModuleKind
  private[resolver] case class UnspecifiedModule() extends ModuleKind

  private[resolver] case class SbtModule(scalaSdkData: ScalaSdkData,
                                         sbtData: SbtBuildModuleDataBsp
                                        ) extends ModuleKind

  private[resolver] case class TargetData(sources: Either[BspError, SourcesResult],
                                          dependencySources: Either[BspError, DependencySourcesResult],
                                          scalacOptions: Either[BspError, ScalacOptionsResult], // TODO should be optional
                                          scalaTestClasses: Either[BspError, ScalaTestClassesResult]
                                         )

  private[resolver] case class SourceDirectory(directory: File, generated: Boolean)

}
