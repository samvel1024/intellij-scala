package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScFor
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScForCfgBuildingImpl { this: ScFor =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    this.desugared() match {
      case Some(desugared) =>
        desugared.buildExprControlFlow(rreq)
      case None =>
        rreq.satisfyAny()
    }
  }
}
