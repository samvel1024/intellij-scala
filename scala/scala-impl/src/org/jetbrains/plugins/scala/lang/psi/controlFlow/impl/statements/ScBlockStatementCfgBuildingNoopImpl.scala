package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.statements

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlockStatement
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScBlockStatementCfgBuildingNoopImpl { this: ScBlockStatement =>

  override def buildBlockStatementControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    if (withResult) {
      builder.pushUnit()
    }
  }
}
