package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScInfixExpr
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScInfixExprCfgBuildingImpl extends MethodInvocationCfgBuildingImpl { this: ScInfixExpr =>
  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {

    val invocInfo = this.invocationInfo

    if (this.isAssignmentOperator) {

      ???
    } else if (this.isRightAssoc) {
      invocInfo.buildRightAssoc(rreq)
    } else {
      invocInfo.build(rreq)
    }
  }
}
