package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScTuple
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScTupleCfgBuildingImpl { this: ScTuple =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    TupleTools.buildTupleCreation(this.exprs, rreq)
  }
}
