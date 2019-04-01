package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.transformers.scala

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait Transformer {
  def builder: CfgBuilder
  def buildExpression(expr: ScExpression, needResult: Boolean = true): Unit
  def buildExpressionOrPushAny(exprOpt: Option[ScExpression]): Unit =
    buildExpressionOr(exprOpt, builder.pushAny())
  def buildExpressionOrPushAnyIfNeeded(exprOpt: Option[ScExpression], needResult: Boolean): Unit =
    buildExpressionOr(exprOpt, builder.pushAny(), needResult)
  def buildExpressionOrPushUnit(exprOpt: Option[ScExpression]): Unit =
    buildExpressionOr(exprOpt, builder.pushUnit())
  def buildExpressionOrPushUnitIfNeeded(exprOpt: Option[ScExpression], needResult: Boolean): Unit =
    buildExpressionOr(exprOpt, builder.pushUnit(), needResult)
  def buildExpressionOr(exprOpt: Option[ScExpression], orMakeResult: => Unit, needResult: Boolean = true): Unit = exprOpt match {
    case Some(expr) => buildExpression(expr, needResult)
    case None => if (needResult) orMakeResult
  }
  def buildExpressionWithoutResult(exprOpt: Option[ScExpression]): Unit = exprOpt match {
    case Some(expr) => buildExpression(expr, needResult = false)
    case None =>
  }
}
