package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.dfa.DfEntity
import org.jetbrains.plugins.scala.lang.psi.api.expr.{MethodInvocation, ScExpression}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, RequireResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaCode._
import org.jetbrains.plugins.scala.project.ProjectContext

object TupleTools {
  def buildTupleCreation(items: Seq[ScExpression], rreq: ResultRequirement)
                        (implicit builder: CfgBuilder): ExprResult = {
    buildTupleCreationForEntities(items.map(_.buildExprControlFlow(RequireResult).pin), rreq)
  }

  def buildTupleCreationForEntities(items: Seq[DfEntity], rreq: ResultRequirement)
                                   (implicit builder: CfgBuilder): ExprResult = {
    implicit val projectContext: ProjectContext = builder.projectContext
    import InvocationTools.invocationInfoFor

    val invocInfo =
      invocationInfoFor(code"Tuple${items.length}.apply(${items.map(_ => "???").mkString(", ")})".asInstanceOf[MethodInvocation])

    val (ret, result) = rreq.tryPin()

    val thisEntity = invocInfo.buildThisRef()
     builder.call(thisEntity, invocInfo.funcRef, ret, items.map(builder.pin))

    result
  }
}
