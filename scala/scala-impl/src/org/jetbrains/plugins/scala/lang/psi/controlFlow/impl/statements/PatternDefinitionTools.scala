package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.statements

import org.jetbrains.plugins.scala.lang.psi.api.base.ScPatternList
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingPattern.RawExprOrAnySupplier
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

object PatternDefinitionTools {
  def buildPatternDefinition(pList: ScPatternList,
                             expr: Option[ScExpression],
                             rreq: ResultRequirement)
                            (implicit builder: CfgBuilder): ExprResult = {

    pList.patterns.foreach(_.buildPatternControlFlow(RawExprOrAnySupplier(expr), None))

    rreq.satisfyUnit()
  }
}
