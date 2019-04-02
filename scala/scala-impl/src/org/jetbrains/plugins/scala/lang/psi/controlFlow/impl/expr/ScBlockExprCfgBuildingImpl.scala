package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlockExpr
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScBlockExprCfgBuildingImpl { this: ScBlockExpr =>

  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._

    caseClauses match {
      case Some(caseClauses) =>
        ???
      case None =>
        buildStatements(statements, withResult)
    }
  }
}
