package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScReferenceExpressionCfgBuildingImpl { this: ScReferenceExpression =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    this.bind() match {
      case Some(result) =>
        val v = builder.resolveVariable(result.element)
        rreq.satisfy(v, noop = true)
      case None =>
        // we have to be able to handle the error here
        ???
    }
  }
}
