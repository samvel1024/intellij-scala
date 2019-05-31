package org.jetbrains.plugins.scala.lang.psi.controlFlow
package impl
package expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlock
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScBlockCfgBuildingImpl { this: ScBlock =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import CfgBuildingTools._

    assert(!hasCaseClauses)

    buildStatements(this.statements, rreq)
  }
}
