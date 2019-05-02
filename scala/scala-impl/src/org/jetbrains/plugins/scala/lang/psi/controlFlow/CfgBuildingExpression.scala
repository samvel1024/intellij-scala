package org.jetbrains.plugins.scala.lang.psi.controlFlow

import org.jetbrains.plugins.scala.dfa.DfConcreteLambdaRef
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, RequireResult, ResultRequirement}

trait CfgBuildingExpression { this: ScExpression =>
  protected def buildActualExpressionControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult =
    throw new NotImplementedError("buildActualExpressionControlFlow not implemented in " + this.getClass.getCanonicalName)

  final def buildExprControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
    builder.underscoreExpressions.get(this) match {
      case Some(params) =>
        val lambdaCfg = buildLambdaCfg(builder.createSubBuilder())
        val lambdaRef = new DfConcreteLambdaRef(this, params, lambdaCfg)
        rreq.satisfy(lambdaRef, noop = true)
      case _ =>
        buildActualExpressionControlFlow(rreq)
    }
  }

  private[this] final def buildLambdaCfg(builder: CfgBuilder): ControlFlowGraph = {
    implicit val implicitBuilder: CfgBuilder = builder

    val result = this.buildActualExpressionControlFlow(RequireResult).pin
    builder.ret(result)
    builder.build()
  }

  final override def buildBlockStatementControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
    buildExprControlFlow(rreq)
  }
}
