package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScUnderscoreSection
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScUnderscoreSectionCfgBuildingImpl { this: ScUnderscoreSection =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    this.bindingExpr match {
      case Some(bindingExpr) => ???
      case None =>
        val param = builder.resolveUnderscore(this)
        rreq.satisfy(param.variable)
    }
  }
}
