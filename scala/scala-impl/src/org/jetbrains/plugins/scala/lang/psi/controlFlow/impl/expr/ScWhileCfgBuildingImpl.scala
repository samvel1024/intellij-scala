package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScWhile
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScWhileCfgBuildingImpl { this: ScWhile =>
  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._

    val loopEntry = builder.createLabel("whileLoop")
    val loopExit = builder.createLabel("whileExit")

    builder.bindLabel(loopEntry)
    buildExpressionOrPushAny(condition)
    builder.jumpIfFalse(loopExit)
    buildExpressionWithoutResult(expression)
    builder.jumpTo(loopEntry)
    builder.bindLabel(loopExit)

    if (withResult) {
      builder.pushUnit()
    }
  }
}
