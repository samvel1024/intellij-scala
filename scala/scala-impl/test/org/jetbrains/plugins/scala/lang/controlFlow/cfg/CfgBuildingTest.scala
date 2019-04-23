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
    assertEquals(result.trim, func.controlFlowGraph.asmText(lineNumbers = false, indentation = false))
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
        |if! a -> .Lelse[5]
        |noop "then"
        |jmp .LendIf[6]
        |.Lelse[5]:
        |noop "else"
        |.LendIf[6]:
        |end
      """.stripMargin
    )

    check(
      """
        |val a = if (true) "then" else "else"
      """.stripMargin,
      """
        |if! true -> .Lelse[4]
        |a = "then"
        |jmp .LendIf[5]
        |.Lelse[4]:
        |a = "else"
        |.LendIf[5]:
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
        |if! true -> .LendIf[3]
        |noop "then"
        |.LendIf[3]:
        |end
      """.stripMargin
    )

    check(
      """
        |val a = if (true) "then"
      """.stripMargin,
      """
        |if! true -> .Lelse[4]
        |a = "then"
        |jmp .LendIf[5]
        |.Lelse[4]:
        |a = unit
        |.LendIf[5]:
        |end
      """.stripMargin
    )
  }

  def test_do_while(): Unit = {
    check(
      """
        |val b = true
        |do {
        |  "inner"
        |} while (b)
        |"end"
      """.stripMargin,
      """
        |b = true
        |.LdoLoop[2]:
        |noop "inner"
        |if b -> .LdoLoop[2]
        |noop "end"
        |end
      """.stripMargin
    )
  }

  def test_while(): Unit = {
    check(
      """
        |val b = true
        |while (b) {
        |  "inner"
        |}
      """.stripMargin,
      """
        |b = true
        |.LwhileLoop[2]:
        |if! b -> .LwhileExit[5]
        |noop "inner"
        |jmp .LwhileLoop[2]
        |.LwhileExit[5]:
        |end
      """.stripMargin
    )
  }

  def test_block(): Unit = {
    check(
      """
        |val a = 1
        |val c = {
        |  val b = 2
        |  b
        |}
      """.stripMargin,
      """
        |a = 1
        |b = 2
        |c = b
        |end
      """.stripMargin
    )
  }
}
