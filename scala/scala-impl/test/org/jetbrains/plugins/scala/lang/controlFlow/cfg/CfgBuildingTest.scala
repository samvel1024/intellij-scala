package org.jetbrains.plugins.scala.lang.controlFlow.cfg

import com.intellij.psi.PsiFileFactory
import org.intellij.lang.annotations.Language
import org.jetbrains.plugins.scala.ScalaFileType
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.jetbrains.plugins.scala.lang.psi.api.ScControlFlowOwner
import org.junit.Assert._

class CfgBuildingTest extends ScalaLightCodeInsightFixtureTestAdapter {

  def check(@Language("Scala") code: String, result: String): Unit = {

    val actualFile = PsiFileFactory.getInstance(getProject).createFileFromText(
      "foo.scala",
      ScalaFileType.INSTANCE,
      code
    )

    val func = actualFile.asInstanceOf[ScControlFlowOwner]
    assertEquals(result.trim, func.controlFlowGraph.asmText(lineNumbers = false))
  }

  def test_unit(): Unit = {
    check(
      "()",
      """
        |noop unit
        |end
      """.stripMargin
    )

    check(
      "val a = ()",
      """
        |a = unit
        |end
      """.stripMargin
    )
  }

  def test_if_with_else(): Unit = {
    check(
      """
        |val a = 0
        |if (a) {
        |  "then"
        |} else {
        |  "else"
        |}
      """.stripMargin,
      """
        |a = 0
        |%0 <- a
        |if! %0 -> .Lelse[5]
        |noop "then"
        |jmp .LendIf[6]
        |noop "else"
        |end
      """.stripMargin
    )

    check(
      """
        |val a = if (true) "then" else "else"
      """.stripMargin,
      """
        |if! true -> .Lelse[2]
        |%0 <- "then"
        |jmp .LendIf[4]
        |%0 <- "else"
        |a = %0
        |end
      """.stripMargin
    )
  }

  def test_if_without_else(): Unit = {
    check(
      """
        |if (true) {
        |  "then"
        |}
      """.stripMargin,
      """
        |if! true -> .LendIf[2]
        |noop "then"
        |end
      """.stripMargin
    )

    check(
      """
        |val a = if (true) "then"
      """.stripMargin,
      """
        |if! true -> .Lelse[2]
        |%0 <- "then"
        |jmp .LendIf[4]
        |%0 <- unit
        |a = %0
        |end
      """.stripMargin
    )
  }
}
