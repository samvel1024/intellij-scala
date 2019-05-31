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

  def buildCaseClausesControlFlow(subject: DfRegister, rreq: ResultRequirement)
                                 (implicit builder: CfgBuilder): ExprResult = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._
    import builder._

    val caseClauses =
      this.caseClauses.getOrElse(throw new AssertionError("Blocks without case clauses should be handled by buildActualExpressionControlFlow"))

    val (caseRreq, result) = rreq.derivePinned()
    val endLabel = createLabel("endCaseClause")

    var labelNum = 1
    def newNextLabel() = {
      labelNum += 1
      createLabel("case" + labelNum)
    }

    val failLabel = caseClauses.caseClauses.foldLeft(Option.empty[CfgBuilder.BuildLabel]) {
      case (prevLabel, ScCaseClause(Some(pattern), guard, expr)) =>
        tryBindLabel(prevLabel)
        val nextLabel = newNextLabel()
        pattern.buildPatternControlFlow(CfgBuildingPattern.DirectSupplier(subject), Some(nextLabel))
        guard.foreach { guard =>
          val cond = buildExprOrAny(guard.expr)
          jumpIfFalse(cond, nextLabel)
        }
        buildExprOrAny(expr, caseRreq)
        Some(nextLabel)
      case (nextLabel, _) =>
        nextLabel
    }

    tryBindLabel(failLabel)

    // todo: build exception throw here

    bindLabel(endLabel)

    result
  }
}
