package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.dfa.DfValue
import org.jetbrains.plugins.scala.lang.psi.api.base._
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}

trait ScLiteralCfgBuildingImpl { this: ScLiteral =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    import ScLiteral._

    val lit = Value(this) match {
      case NullValue           => builder.`null`
      case BooleanValue(value) => builder.boolean(value)
      case IntegerValue(value) => builder.int(value)
      case StringValue(value)  => builder.string(value)
      case _                   => builder.any
    }

    rreq.satisfy(lit, noop = true)
  }
}
