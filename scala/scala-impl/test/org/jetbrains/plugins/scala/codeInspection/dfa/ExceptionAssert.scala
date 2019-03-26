package org.jetbrains.plugins.scala.codeInspection.dfa

import org.junit.ComparisonFailure

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait ExceptionAssert {

  import org.junit.Assert._

  def assertExceptionMessage[E <: Throwable](expectedMessage: Option[String])
                                            (code: => Unit)
                                            (implicit classTag: ClassTag[E]): Unit = {
    val expectedException = classTag.runtimeClass
    val expectedExceptionName = expectedException.getName
    try {
      code
    } catch {
      case e: Throwable if expectedException.isAssignableFrom(e.getClass) => //if e.isInstanceOf[E] =>
        expectedMessage.foreach { expectedMsg =>
          if (expectedMsg != e.getMessage) {
            System.err.println("Wrong message in exception:")
            System.err.println(e.getMessage)
            e.printStackTrace(System.err)
            throw new ComparisonFailure("", expectedMsg, e.getMessage)
          }
        }
        return

      case e: Throwable =>
        throw new AssertionError(s"Expected exception ${expectedException.getName} but ${e.getClass.getName} was thrown", e)
    }

    expectedMessage match {
      case Some(msg) => fail(s"$expectedExceptionName with message '$msg' should have been thrown")
      case None      => fail(s"$expectedExceptionName should have been thrown")
    }

  }

  def assertExceptionMessage[E <: Throwable: TypeTag](expectedMessage: String)
                                                     (code: => Unit)
                                                     (implicit classTag: ClassTag[E]): Unit =
    assertExceptionMessage(Some(expectedMessage))(code)

  def assertException[E <: Throwable: TypeTag](code: => Unit)(implicit classTag: ClassTag[E]): Unit =
    assertExceptionMessage(None)(code)
}
