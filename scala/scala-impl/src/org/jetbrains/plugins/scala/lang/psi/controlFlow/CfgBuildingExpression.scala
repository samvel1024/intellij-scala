package org.jetbrains.plugins.scala.lang.psi.controlFlow

trait CfgBuildingExpression { this: CfgBuildingBlockStatement =>
  protected def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = ???

  final def buildCfg(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    buildActualExpressionControlFlow(withResult)
  }

  final override def buildBlockStatementControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    buildCfg(withResult)
  }
}
