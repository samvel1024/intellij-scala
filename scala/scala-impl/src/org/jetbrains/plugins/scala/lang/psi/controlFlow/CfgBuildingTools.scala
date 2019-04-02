package org.jetbrains.plugins.scala.lang.psi.controlFlow

import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.{ScParenthesisedPattern, ScPattern, ScWildcardPattern}
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScBlockStatement, ScExpression}

import scala.annotation.tailrec

object CfgBuildingTools {
  def buildExpressionOrPushAny(exprOpt: Option[ScExpression])
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
  }

  def buildStatements(stmts: Seq[ScBlockStatement], withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    if (stmts.isEmpty) {
      if (withResult) {
        // todo: only on error? otherwise push unit?
        builder.pushAny()
      }
      return
    }

    val lastStmtIndex = stmts.size - 1
    for ((stmt, idx) <- stmts.zipWithIndex) {
      val isLast = idx == lastStmtIndex
      stmt.buildBlockStatementControlFlow(isLast && withResult)
    }
  }


  @tailrec
  final def canIgnorePattern(pattern: ScPattern): Boolean = pattern match {
    case _: ScWildcardPattern => true
    case ScParenthesisedPattern(inner) => canIgnorePattern(inner)
    case _ => false
  }
}
