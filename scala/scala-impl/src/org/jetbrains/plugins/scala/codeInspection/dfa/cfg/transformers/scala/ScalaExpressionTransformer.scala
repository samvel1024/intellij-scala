package org.jetbrains.plugins.scala.codeInspection.dfa.cfg.transformers.scala

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.codeInspection.dfa.cfg.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.api.ScalaElementVisitor
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScBlockExpr, ScBlockStatement, ScExpression}

/**
 * Used
 */
class ScalaExpressionTransformer(val scalaCfgTransformer: ScalaCfgTransformer, val builder: CfgBuilder)
    extends ScalaElementVisitor{

  def buildExpression(expr: ScExpression): Unit =
    scalaCfgTransformer.buildExpression(expr)

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
}
