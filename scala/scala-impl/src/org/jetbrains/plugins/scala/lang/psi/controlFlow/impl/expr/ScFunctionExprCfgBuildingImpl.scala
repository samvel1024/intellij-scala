package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.dfa.DfConcreteLambdaRef
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScFunctionExpr
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScFunctionExprCfgBuildingImpl { this: ScFunctionExpr =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    val lambda = new DfConcreteLambdaRef(this, this.parameters.map(_.`type`().getOrAny))
    rreq.satisfy(lambda, noop = true)
  }
}
