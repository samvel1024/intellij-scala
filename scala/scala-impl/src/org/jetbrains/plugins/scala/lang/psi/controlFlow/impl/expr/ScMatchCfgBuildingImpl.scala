package org.jetbrains.plugins.scala.lang.psi.controlFlow
package impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScMatch
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScMatchCfgBuildingImpl { this: ScMatch =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import CfgBuildingTools._

    val res = buildExprOrAny(this.expression).pinRegister
    CaseClausesTools.buildCaseClausesControlFlow(this.caseClauses, res, rreq)
  }
}
