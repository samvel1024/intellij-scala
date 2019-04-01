package org.jetbrains.plugins.scala.codeInspection.dfa.cfg.transformers.scala

import org.jetbrains.plugins.scala.lang.psi.api.ScalaElementVisitor
import org.jetbrains.plugins.scala.lang.psi.api.expr._

trait CallExprTransformer extends ScalaElementVisitor { this: Transformer =>
  private def buildInvocation(call: MethodInvocation): Unit = {
    ???
  }

  override def visitMethodCallExpression(call: ScMethodCall): Unit = buildInvocation(call)
  override def visitInfixExpression(infix: ScInfixExpr): Unit = buildInvocation(infix)
  override def visitPostfixExpression(postfix: ScPostfixExpr): Unit = buildInvocation(postfix)
  override def visitPrefixExpression(prefix: ScPrefixExpr): Unit = buildInvocation(prefix)
}
