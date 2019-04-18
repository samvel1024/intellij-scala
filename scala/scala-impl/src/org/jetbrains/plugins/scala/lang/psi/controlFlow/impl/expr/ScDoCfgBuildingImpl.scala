package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScDo
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScDoCfgBuildingImpl { this: ScDo =>
  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._
    import builder._

    val loopEntry = createLabel("doLoop")

    bindLabel(loopEntry)
    buildWithoutResult(body)
    val cond = buildExprOrAny(condition)
    jumpIfTrue(cond, loopEntry)

    rreq.satisfyUnit()
  }
}
