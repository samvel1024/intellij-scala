package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReturn
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScReturnCfgBuildingImpl { this: ScReturn =>

  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._

    buildExpressionOrPushUnit(expr)
    builder.ret()

    if (withResult) {
      builder.pushNothing()
    }
  }
}
