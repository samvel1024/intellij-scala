package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

import com.intellij.psi.PsiFileFactory
import org.intellij.lang.annotations.Language
import org.jetbrains.plugins.scala.ScalaFileType
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.jetbrains.plugins.scala.lang.psi.api.ScControlFlowOwner
import org.junit.Assert._

abstract class CfgBuildingTestBase extends ScalaLightCodeInsightFixtureTestAdapter {

  def check(@Language("Scala") code: String, result: String): Unit = {

    val actualFile = PsiFileFactory.getInstance(getProject).createFileFromText(
      "foo.scala",
      ScalaFileType.INSTANCE,
      code
    )

    val func = actualFile.asInstanceOf[ScControlFlowOwner]
    assertEquals(result.trim, func.controlFlowGraph.asmText(lineNumbers = false, indentation = false))
  }

}
