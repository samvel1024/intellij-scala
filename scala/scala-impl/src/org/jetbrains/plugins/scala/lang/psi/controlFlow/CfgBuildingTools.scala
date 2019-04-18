package org.jetbrains.plugins.scala.lang.psi.controlFlow

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfRegister, DfValue}
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.{ScParenthesisedPattern, ScPattern, ScWildcardPattern}
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScBlockStatement, ScExpression}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, RequireNoResult, RequireResult, ResultRequirement}

import scala.annotation.tailrec

object CfgBuildingTools {
  /*def buildExpressionOrPushAny(exprOpt: Option[ScExpression])
                              (implicit builder: CfgBuilder): Unit =
    buildExpressionOr(exprOpt, builder.pushAny())

  def buildExpressionOrPushAnyIfNeeded(exprOpt: Option[ScExpression], withResult: Boolean)
                                      (implicit builder: CfgBuilder): Unit =
    buildExpressionOr(exprOpt, builder.pushAny(), withResult)

  def buildExpressionOrPushUnit(exprOpt: Option[ScExpression])
                               (implicit builder: CfgBuilder): Unit =
    buildExpressionOr(exprOpt, builder.pushUnit())

  def buildExpressionOrPushUnitIfNeeded(exprOpt: Option[ScExpression], withResult: Boolean)
                                       (implicit builder: CfgBuilder): Unit =
    buildExpressionOr(exprOpt, builder.pushUnit(), withResult)

  def buildExpressionOr(exprOpt: Option[ScExpression], orMakeResult: => Unit, withResult: Boolean = true)
                                 (implicit builder: CfgBuilder): Unit = exprOpt match {
    case Some(expr) => expr.buildCfg(withResult)
    case None => if (withResult) orMakeResult
  }

  def buildExpressionWithoutResult(exprOpt: Option[ScExpression])
                                  (implicit builder: CfgBuilder): Unit = exprOpt match {
    case Some(expr) => expr.buildCfg(withResult = false)
    case None =>
  }*/

  def buildExprWithResultIf(expr: ScExpression, needsResult: Boolean)(implicit builder: CfgBuilder): ExprResult =
    expr.buildExprControlFlow(RequireResult.If(needsResult))

  def buildExprOr(expr: Option[ScExpression],
                  result: => DfEntity,
                  rreq: ResultRequirement = RequireResult)(implicit builder: CfgBuilder): ExprResult =
    expr.map(_.buildExprControlFlow(rreq)).getOrElse(rreq.satisfy(result))

  def buildExprOrAny(expr: Option[ScExpression],
                     rreq: ResultRequirement = RequireResult)(implicit builder: CfgBuilder): ExprResult =
    buildExprOr(expr, builder.any, rreq)

  def buildExprOrUnit(expr: Option[ScExpression],
                      rreq: ResultRequirement = RequireResult)(implicit builder: CfgBuilder): ExprResult =
    buildExprOr(expr, builder.unit, rreq)

  def buildWithoutResult(expr: Option[ScExpression])(implicit builder: CfgBuilder): Unit =
    expr.foreach(_.buildExprControlFlow(RequireNoResult))

  def buildStatements(stmts: Seq[ScBlockStatement], rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult =
    if (stmts.isEmpty) {
      rreq.satisfyAny(noop = true)
    } else {
      stmts.init.foreach(_.buildBlockStatementControlFlow(RequireNoResult))
      stmts.last.buildBlockStatementControlFlow(rreq)
    }


  @tailrec
  final def canIgnorePattern(pattern: ScPattern): Boolean = pattern match {
    case _: ScWildcardPattern => true
    case ScParenthesisedPattern(inner) => canIgnorePattern(inner)
    case _ => false
  }
}
