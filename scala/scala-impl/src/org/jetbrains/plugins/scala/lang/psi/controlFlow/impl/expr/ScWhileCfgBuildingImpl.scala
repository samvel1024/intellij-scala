package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScWhile
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScWhileCfgBuildingImpl { this: ScWhile =>
  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._
    import builder._

    val loopEntry = createLabel("whileLoop")
    val loopExit = createLabel("whileExit")

    bindLabel(loopEntry)
    val cond = buildExprOrAny(condition)
    jumpIfFalse(cond, loopExit)
    buildWithoutResult(expression)
    jumpTo(loopEntry)
    bindLabel(loopExit)

    rreq.satisfyUnit()
  }
}
