package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScIf
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScIfCfgBuildingImpl { this: ScIf =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._
    import builder._

    val (branchRReq, ifResult) = rreq.derivePinned()

    val hasElse = elseExpression.isDefined || rreq.needsResult
    val endLabel = createLabel("endIf")
    val elseLabel = if (hasElse) createLabel("else") else endLabel

    val cond = buildExprOrAny(condition)
    jumpIfFalse(cond, elseLabel)
    buildExprOrAny(thenExpression, branchRReq)
    if (hasElse) {
      // jump from the then branch to the end
      jumpTo(endLabel)

      bindLabel(elseLabel)
      buildExprOrUnit(elseExpression, branchRReq)
    }
    bindLabel(endLabel)

    ifResult
  }
}
