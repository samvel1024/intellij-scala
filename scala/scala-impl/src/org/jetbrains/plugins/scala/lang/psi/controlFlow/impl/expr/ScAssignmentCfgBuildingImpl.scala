package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScAssignment, ScMethodCall}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.InvocationTools.InvocationInfo

trait ScAssignmentCfgBuildingImpl { this: ScAssignment =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    leftExpression match {
      case invok: ScMethodCall =>
        // call to update
        val invocationInfo = InvocationInfo(
          Some(invok.getEffectiveInvokedExpr),
          invok.target.map(_.element),
          invok.matchedParameters
        )

        invocationInfo.build(rreq)

      case _ =>
        ???
    }
  }
}
