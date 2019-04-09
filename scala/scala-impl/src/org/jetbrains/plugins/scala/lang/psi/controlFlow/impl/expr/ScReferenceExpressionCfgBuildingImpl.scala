package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScReferenceExpressionCfgBuildingImpl { this: ScReferenceExpression =>

  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    this.bind() match {
      case Some(result) =>
        builder.read(result.element)
      case None =>
        // we have to be able to handle the error here
        ???
    }

    if (!withResult) {
      builder.noop()
    }
  }
}
