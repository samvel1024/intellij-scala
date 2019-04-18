package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScIf
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScIfCfgBuildingImpl { this: ScIf =>

  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._

    val hasElse = withResult || elseExpression.isDefined
    val endLabel = builder.createLabel("endIf")
    val elseLabel = if (hasElse) builder.createLabel("else") else endLabel

    buildExpressionOrPushAny(condition)
    builder.jumpIfFalse(elseLabel)
    buildExpressionOrPushAnyIfNeeded(thenExpression, withResult)
    if (hasElse) {
      builder.jumpTo(endLabel)
      if (withResult)
        builder.pop()
      builder.bindLabel(elseLabel)
      buildExpressionOrPushUnitIfNeeded(elseExpression, withResult)
    }
    builder.bindLabel(endLabel)
  }
}
