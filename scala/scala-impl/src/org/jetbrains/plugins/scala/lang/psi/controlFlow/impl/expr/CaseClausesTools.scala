package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.dfa.{DfConcreteLambdaRef, DfLocalVariable, DfRegister}
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.{ScCaseClause, ScCaseClauses}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlockExpr
import org.jetbrains.plugins.scala.lang.psi.controlFlow.{CfgBuilder, CfgBuildingPattern, ControlFlowGraph}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, RequireResult, ResultRequirement}

object CaseClausesTools {
  def createLambdaFromCaseClausesBlock(blockExpr: ScBlockExpr)
                                      (implicit builder: CfgBuilder): DfConcreteLambdaRef = {
    val caseBlock =
      blockExpr.caseClauses.getOrElse(throw new AssertionError("Cannot handle block without case clauses"))

    val caseBlockParamVar = DfLocalVariable(caseBlock, "block$param")
    val incomingType = blockExpr.caseClauseIncomingType(caseBlock.elementScope)
      .getOrElse(builder.projectContext.stdTypes.Any)
    val caseBlockParam = new DfConcreteLambdaRef.Parameter(caseBlockParamVar, incomingType)

    val caseBlockCfg =
      buildCaseClausesControlFlow(caseBlock, caseBlockParamVar, builder.createSubBuilder())

    new DfConcreteLambdaRef(blockExpr, Seq(caseBlockParam), caseBlockCfg)
  }

  private def buildCaseClausesControlFlow(caseClauses: ScCaseClauses,
                                          caseBlockParamVar: DfLocalVariable,
                                          builder: CfgBuilder): ControlFlowGraph = {
    implicit val _builder: CfgBuilder = builder

    val caseBlockParamReg = builder.pinToNewRegister(caseBlockParamVar)
    val result = buildCaseClausesControlFlow(caseClauses, caseBlockParamReg, RequireResult).pin
    builder.ret(result)
    builder.build()
  }


  def buildCaseClausesControlFlow(caseClauses: ScCaseClauses, subject: DfRegister, rreq: ResultRequirement)
                                 (implicit builder: CfgBuilder): ExprResult = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._
    import builder._
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
