package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.transformers.scala

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.dfa.DfValue
import org.jetbrains.plugins.scala.lang.psi.api.ScalaElementVisitor
import org.jetbrains.plugins.scala.lang.psi.api.base.{ScIntLiteral, ScLiteral, ScNullLiteral}
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

/**
 * Used
 */
class ScalaExpressionTransformer(val scalaCfgTransformer: ScalaCfgTransformer, override val builder: CfgBuilder, val needResult: Boolean)
    extends ScalaElementVisitor with Transformer with CallExprTransformer {

  override def buildExpression(expr: ScExpression, needResult: Boolean = true): Unit =
    scalaCfgTransformer.buildExpression(expr, needResult)

  def buildStatements(statements: Seq[ScBlockStatement], expectResult: Boolean): Unit =
    scalaCfgTransformer.buildStatements(statements, expectResult)

  override def visitElement(element: PsiElement): Unit =
    throw new NotImplementedError(s"Only expressions should be visited by this visitor")

  override def visitExpression(expr: ScExpression): Unit =
    throw new NotImplementedError(s"No implementation to visit expression '$expr'")


  override def visitBlockExpression(block: ScBlockExpr): Unit = {
    block.caseClauses match {
      case Some(caseClauses) =>
        ???
      case None =>
        buildStatements(block.statements, expectResult = true)
    }
  }

  override def visitIfStatement(stmt: ScIf): Unit = {
    val ScIf(condition, thenExpression, elseExpression) = stmt

    val hasElse = needResult || elseExpression.isDefined
    val endLabel = builder.createLabel("endIf")
    val elseLabel = if (hasElse) builder.createLabel("else") else endLabel

    buildExpressionOrPushAny(condition)
    builder.jumpIfFalse(elseLabel)
    buildExpressionOrPushAnyIfNeeded(thenExpression, needResult)
    if (hasElse) {
      builder.jumpTo(endLabel)
      builder.bindLabel(elseLabel)
      buildExpressionOrPushUnitIfNeeded(elseExpression, needResult)
    }
    builder.bindLabel(endLabel)
  }

  override def visitWhileStatement(ws: ScWhile): Unit = {
  }

  override def visitReturnStatement(ret: ScReturn): Unit = {
    buildExpressionOrPushUnit(ret.expr)
    builder.ret()
  }

  override def visitThisReference(t: ScThisReference): Unit = {
    builder.pushThis()
    discardIfNotNeeded()
  }

  override def visitLiteral(literal: ScLiteral): Unit = {
    literal match {
      case ScNullLiteral() =>
        builder.pushNull()
      case ScIntLiteral(value) =>
        builder.push(DfValue.int(value))

      case _ =>
        ???
    }
    discardIfNotNeeded()
  }

  private def discardIfNotNeeded(): Unit = {
    if (!needResult) {
      builder.pop()
    }
  }
}
