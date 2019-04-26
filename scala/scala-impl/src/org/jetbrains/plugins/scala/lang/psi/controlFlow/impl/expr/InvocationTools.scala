package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter

object InvocationTools {

  def buildMethodInvocation(invokedExpr: ScExpression,
                            matchedParameters: Seq[(ScExpression, Parameter)],
                            rreq: ResultRequirement): ExprResult = {
    ???
  }
}
