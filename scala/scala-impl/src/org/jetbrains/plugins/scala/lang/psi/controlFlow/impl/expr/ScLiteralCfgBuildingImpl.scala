package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.dfa.DfValue
import org.jetbrains.plugins.scala.lang.psi.api.base.ScLiteral
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

trait ScLiteralCfgBuildingImpl { this: ScLiteral =>

  override def buildActualExpressionControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    import ScLiteral._

    Value(this) match {
      case NullValue =>
        builder.pushNull()

      case IntegerValue(int) =>
        builder.push(DfValue.int(int))

      case StringValue(string) =>
        builder.pushString(string)

      case _ =>
        // Some error. Just push any
        builder.pushAny()
    }

    if (!withResult) {
      builder.noop()
    }
  }
}
