package org.jetbrains.plugins.scala.lang.psi.controlFlow
package impl
package expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScAssignment, ScMethodCall}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.InvocationTools.InvocationInfo

trait ScAssignmentCfgBuildingImpl { this: ScAssignment =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import InvocationTools.invocationInfoFor

    leftExpression match {
      case invok: ScMethodCall =>
        // call to update
        val invocationInfo =
          invocationInfoFor(invok).copy(thisExpr = Some(invok.getEffectiveInvokedExpr))

        invocationInfo.build(rreq)

      case _ =>
        ???
    }
  }
}
