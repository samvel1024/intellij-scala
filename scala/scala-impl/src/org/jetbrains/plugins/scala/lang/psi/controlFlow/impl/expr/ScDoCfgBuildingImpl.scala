package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScDo
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScDoCfgBuildingImpl { this: ScDo =>
  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._

    val loopEntry = builder.createLabel("doLoop")

    builder.bindLabel(loopEntry)
    buildExpressionWithoutResult(body)
    buildExpressionOrPushAny(condition)
    builder.jumpIfTrue(loopEntry)

    if (withResult) {
      builder.pushUnit()
    }
  }
}
