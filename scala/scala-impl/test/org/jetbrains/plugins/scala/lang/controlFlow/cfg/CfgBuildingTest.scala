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
    assertEquals(func.controlFlowGraph.asmText(lineNumbers = false), result.trim)
  }

  def test_unit(): Unit = {
    check(
      "()",
      """
        |push unit
        |pop
        |end
      """.stripMargin
    )

    check(
      "val a = ()",
      """
        |push unit
        |push a
        |assign
      """.stripMargin
    )
  }

  def test_if(): Unit = {
    check(
      """
        |if (a) {
        |  "then"
        |} else {
        |  "else"
        |}
      """.stripMargin,
      ""
    )
  }
}
