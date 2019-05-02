package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.dfa.DfConcreteLambdaRef
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScFunctionExpr
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.{CfgBuilder, CfgBuildingTools, ControlFlowGraph}

trait ScFunctionExprCfgBuildingImpl { this: ScFunctionExpr =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    val lambdaCfg = buildLambdaCfg(builder.createSubBuilder())
    val params = this.parameters
      .map(p => new DfConcreteLambdaRef.Parameter(builder.resolveVariable(p), p.`type`().getOrAny))
    val lambdaRef = new DfConcreteLambdaRef(this, params, lambdaCfg)
    rreq.satisfy(lambdaRef, noop = true)
  }


  private[this] final def buildLambdaCfg(builder: CfgBuilder): ControlFlowGraph = {
    import CfgBuildingTools.buildExprOrAny
    implicit val implicitBuilder: CfgBuilder = builder

    val result = buildExprOrAny(this.result).pin
    builder.ret(result)
    builder.build()
  }
}
