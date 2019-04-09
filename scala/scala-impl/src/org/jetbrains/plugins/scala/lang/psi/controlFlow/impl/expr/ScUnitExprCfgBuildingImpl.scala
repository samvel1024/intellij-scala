package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScUnitExpr
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScUnitExprCfgBuildingImpl { this: ScUnitExpr =>
  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    builder.pushUnit()

    if (!withResult) {
      builder.noop()
    }
  }
}
