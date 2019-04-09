package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScThisReference
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScThisReferenceCfgBuildingImpl { this: ScThisReference =>

  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {

    builder.pushThis()

    if (!withResult) {
      builder.noop()
    }
  }
}
