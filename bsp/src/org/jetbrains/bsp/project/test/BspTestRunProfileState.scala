package org.jetbrains.bsp.project.test

import java.io.OutputStream
import java.net.URI
import java.util.UUID
import java.util.concurrent.CompletableFuture

import ch.epfl.scala.bsp4j.TaskDataKind._
import ch.epfl.scala.bsp4j.TestStatus._
import ch.epfl.scala.bsp4j._
import com.google.gson.{Gson, JsonObject}
import com.intellij.execution.configurations.{RunProfileState, RunnerSettings}
import com.intellij.execution.process.{ProcessHandler, ProcessOutputType}
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.{DefaultExecutionResult, ExecutionResult, Executor}
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import jetbrains.buildServer.messages.serviceMessages._
import org.jetbrains.bsp.data.BspMetadata
import org.jetbrains.bsp.protocol.BspCommunicationComponent
import org.jetbrains.bsp.protocol.BspNotifications.{BspNotification, TaskFinish, TaskStart}
import org.jetbrains.bsp.protocol.session.BspSession.BspServer

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits._

class BspTestRunProfileState(val project: Project, val rc: BspTestRunConfiguration, val ex: Executor) extends RunProfileState {

  val gson = new Gson()

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
    val targetIds = targets().map(uri => new BuildTargetIdentifier(uri.toString))
    val params = new TestParams(targetIds.toList.asJava)
    params.setOriginId(UUID.randomUUID().toString)
    server.buildTargetTest(params)
  }


  def printProc(str: MessageWithAttributes)(implicit proc: ProcessHandler): Unit = {
    proc.notifyTextAvailable(str + System.lineSeparator(), ProcessOutputType.STDOUT)
  }

  class ServiceMsg(val name: String, val map: Map[String, String]) extends MessageWithAttributes(name, map.asJava) {
    def this(n: String) = this(n, Map())
  }

  case class NestedBspMsg(nested: AnyRef, msg: AnyRef)

  type HasNested = {
    def getDataKind: String
    def getData: Object
  }

  private def extractNestedMessage(bspMessage: AnyRef): AnyRef = bspMessage match {
    case x: HasNested@unchecked =>
      val kind = x.getDataKind
      val rawNested = x.getData
      rawNested match {
        case d: JsonObject => kind match {
          case TEST_FINISH => gson.fromJson(d, classOf[TestFinish])
          case TEST_TASK => gson.fromJson(d, classOf[TestTask])
          case TEST_REPORT => gson.fromJson(d, classOf[TestReport])
          case TEST_START => gson.fromJson(d, classOf[TestStart])
          case _ => rawNested
        }
        case _ => rawNested
      }
    case x => x
  }

  class BspTestSession {
    val startTime: mutable.Map[String, Long] = mutable.Map()
  }

  // TODO handle standard output of test cases
  private def onBspNotification(proc: ProcessHandler, session: BspTestSession)(n: BspNotification): Unit = {
    implicit val pr = proc
    n match {
      case TaskStart(params) => extractNestedMessage(params) match {
        case t: TestTask =>
          printProc(new ServiceMsg("enteredTheMatrix"))
          printProc(new TestSuiteStarted("BSP"))
        case t: TestStart =>
          printProc(new TestStarted(t.getDisplayName, false, null))
          session.startTime += (t.getDisplayName -> params.getEventTime)
        case _ =>
      }
      case TaskFinish(params) => extractNestedMessage(params) match {
        case t: TestReport => printProc(new TestSuiteFinished("BSP"))
        case t: TestFinish =>
          t.getStatus match {
            case PASSED =>
            case FAILED => printProc(new ServiceMsg("testFailed", Map("name" -> t.getDisplayName, "message" -> t.getMessage)))
            case _ => printProc(new TestIgnored(t.getDisplayName, "Ignored"))
          }
          printProc(new TestFinished(t.getDisplayName, session.startTime.get(t.getDisplayName)
            .map { st =>
              session.startTime -= t.getDisplayName
              (params.getEventTime - st).intValue()
            }.getOrElse(0)))
        case _ =>
      }
      case _ =>
    }

  }


  override def execute(executor: Executor, runner: ProgramRunner[_ <: RunnerSettings]): ExecutionResult = {
    val procHandler = new MProcHandler
    val console = SMTestRunnerConnectionUtil.createAndAttachConsole("BSP", procHandler, new SMTRunnerConsoleProperties(
      project, rc, "BSP", ex))
    val bspCommunication = project.getComponent(classOf[BspCommunicationComponent]).communication

    bspCommunication.run(testRequest, onBspNotification(procHandler, new BspTestSession()), _ => {
    })
      .future
      .onComplete(_ => procHandler.shutdown())
    new DefaultExecutionResult(console, procHandler)
  }

}
