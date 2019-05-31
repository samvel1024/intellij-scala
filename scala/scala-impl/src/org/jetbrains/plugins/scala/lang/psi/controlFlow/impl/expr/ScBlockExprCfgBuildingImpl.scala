package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.dfa.{DfRegister, DfValue}
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScCaseClause
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlockExpr
import org.jetbrains.plugins.scala.lang.psi.controlFlow.{CfgBuilder, CfgBuildingPattern}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScBlockExprCfgBuildingImpl extends ScBlockExpr {

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._

    assert(caseClauses.isEmpty, "Blocks with case clauses should be handled by buildCaseClausesControlFlow")

    buildStatements(statements, rreq)
  }
}
