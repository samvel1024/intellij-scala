package org.jetbrains.plugins.scala.lang.psi.controlFlow

import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait CfgBuildingExpression { this: CfgBuildingBlockStatement =>
  protected def buildActualExpressionControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = ???

  final def buildExprControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
    buildActualExpressionControlFlow(rreq)
  }

  final override def buildBlockStatementControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
    buildExprControlFlow(rreq)
  }
}
