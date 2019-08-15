package org.jetbrains.bsp.data

import java.io.File
import java.net.URI
import java.util
import java.util.Optional

import com.intellij.openapi.externalSystem.model.project.{AbstractExternalEntityData, ModuleData}
import com.intellij.openapi.externalSystem.model.{DataNode, Key, ProjectKeys}
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.module.{Module, ModuleManager}
import com.intellij.openapi.project.Project
import com.intellij.serialization.PropertyMapping
import org.jetbrains.annotations.NotNull
import org.jetbrains.bsp.BSP
import org.jetbrains.bsp.data.BspEntityData._

import scala.collection.JavaConverters._

abstract class BspEntityData extends AbstractExternalEntityData(BSP.ProjectSystemId) with Product {

  // need to manually specify equals/hashCode here because it is not generated for case classes inheriting from
  // AbstractExternalEntityData
  override def equals(obj: scala.Any): Boolean = obj match {
    case data: BspEntityData =>
      //noinspection CorrespondsUnsorted
      this.canEqual(data) &&
        (this.productIterator sameElements data.productIterator)
    case _ => false
  }

  override def hashCode(): Int = runtime.ScalaRunTime._hashCode(this)
}

object BspEntityData {
  def datakey[T](clazz: Class[T],
                 weight: Int = ProjectKeys.MODULE.getProcessingWeight + 1
                ): Key[T] = new Key(clazz.getName, weight)
}


@SerialVersionUID(3)
case class ScalaSdkData @PropertyMapping(Array("scalaOrganization", "scalaVersion", "scalacClasspath", "scalacOptions"))(
  @NotNull scalaOrganization: String,
  @NotNull scalaVersion: Optional[String],
  @NotNull scalacClasspath: util.List[File],
  @NotNull scalacOptions: util.List[String]
) extends BspEntityData

object ScalaSdkData {
  val Key: Key[ScalaSdkData] = datakey(classOf[ScalaSdkData])
  val LibraryName: String = "scala-sdk"
}


/**
  * Metadata to about bsp targets that have been mapped to IntelliJ modules.
  * @param targetIds target ids mapped to module
  */
@SerialVersionUID(4)
case class BspMetadata @PropertyMapping(Array("targetIds", "scalaTestClasses"))(
                                                                                 @NotNull targetIds: util.List[URI],
                                                                                 @NotNull testClasses: util.List[String])
object BspMetadata {

  type ScalaTestClass = (URI, String)

  import com.intellij.openapi.externalSystem.util.{ExternalSystemApiUtil => ES}

  val Key: Key[BspMetadata] = datakey(classOf[BspMetadata])

  def get(project: Project, module: Module): Option[BspMetadata] = {
    val dataManager = ProjectDataManager.getInstance()
    val moduleId = ES.getExternalProjectId(module)
    def predicate(node: DataNode[ModuleData]) = node.getData.getId == moduleId
    for {
      // TODO all these options fail silently. collect errors and report something
      projectInfo <- Option(dataManager.getExternalProjectData(project, BSP.ProjectSystemId, project.getBasePath))
      projectStructure <- Option(projectInfo.getExternalProjectStructure)
      moduleDataNode <- Option(ES.find(projectStructure, ProjectKeys.MODULE, predicate))
      metadata <- Option(ES.find(moduleDataNode, BspMetadata.Key))
    } yield metadata.getData
  }

  def findScalaTestClasses(proj: Project): Seq[ScalaTestClass] = {
    ModuleManager.getInstance(proj).getModules.toList
      .flatMap(get(proj, _))
      .flatMap(m => m.targetIds.asScala
        .take(1)
        .flatMap(x => m.testClasses.asScala.map((x, _))))
  }


}

