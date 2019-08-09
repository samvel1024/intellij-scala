package org.jetbrains.bsp.project.test

import java.io.OutputStream
import java.net.URI
import java.util.UUID
import java.util.concurrent.CompletableFuture

import ch.epfl.scala.bsp4j
import ch.epfl.scala.bsp4j.TestResult
import com.intellij.execution.configurations.{RunProfileState, RunnerSettings}
import com.intellij.execution.impl.ConsoleViewImpl
import com.intellij.execution.process.{ProcessHandler, ProcessOutputType}
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.{DefaultExecutionResult, ExecutionResult, Executor}
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import org.jetbrains.bsp.data.BspMetadata
import org.jetbrains.bsp.protocol.BspCommunicationComponent
import org.jetbrains.bsp.protocol.BspNotifications.BspNotification
import org.jetbrains.bsp.protocol.session.BspSession.BspServer

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits._


class BspTestRunProfileState(val project: Project) extends RunProfileState {

  class MProcHandler extends ProcessHandler {
    override def destroyProcessImpl(): Unit = ()

    override def detachProcessImpl(): Unit = {}

    override def detachIsDefault(): Boolean = false

    override def getProcessInput: OutputStream = null

    def shutdown(): Unit = {
      super.notifyProcessTerminated(0)
    }
  }

  private def targets(): List[URI] = {
    ModuleManager.getInstance(project).getModules.toList
      .flatMap(BspMetadata.get(project, _))
      .flatMap(x => x.targetIds.asScala.toList)
  }

  private def testRequest(server: BspServer): CompletableFuture[TestResult] = {
    val targetIds = targets().map(uri => new bsp4j.BuildTargetIdentifier(uri.toString))
    val params = new bsp4j.TestParams(targetIds.toList.asJava)
    params.setOriginId(UUID.randomUUID().toString)
    server.buildTargetTest(params)
  }

  override def execute(executor: Executor, runner: ProgramRunner[_ <: RunnerSettings]): ExecutionResult = {
    val console = new ConsoleViewImpl(project, true)
    val procHandler = new MProcHandler
    console.attachToProcess(procHandler)
    val bspCommunication = project.getComponent(classOf[BspCommunicationComponent]).communication

    def notification(n: BspNotification): Unit = {
      procHandler.notifyTextAvailable(n.toString, ProcessOutputType.STDOUT)
    }

    bspCommunication.run(testRequest, notification, procHandler.notifyTextAvailable(_, ProcessOutputType.STDOUT))
      .future
      .onComplete(_ => procHandler.shutdown())

    new DefaultExecutionResult(console, procHandler)
  }

}
