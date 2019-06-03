package org.jetbrains.plugins.scala.lang.psi.controlFlow
package impl
package expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScThrow
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScThrowCfgBuildingImpl { this: ScThrow =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import CfgBuildingTools._

    val excp = buildExprOrAny(this.expression).pin
    builder.throwException(excp)
    rreq.satisfyNothing()
  }
}
