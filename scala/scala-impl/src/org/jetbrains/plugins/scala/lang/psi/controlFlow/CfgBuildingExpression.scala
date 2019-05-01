package org.jetbrains.plugins.scala.lang.psi.controlFlow

import org.jetbrains.plugins.scala.dfa.{DfConcreteLambdaRef, DfValue}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait CfgBuildingExpression { this: ScExpression =>
  protected def buildActualExpressionControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult =
    throw new NotImplementedError("buildActualExpressionControlFlow not implemented in " + this.getClass.getCanonicalName)

  final def buildExprControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
    builder.underscoreExpressions.get(this) match {
      case Some(params) =>
        rreq.satisfy(new DfConcreteLambdaRef(this, params.map(_.`type`().getOrAny)))
      case _ =>
        buildActualExpressionControlFlow(rreq)
    }
  }

  final override def buildBlockStatementControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
    buildExprControlFlow(rreq)
  }
}
