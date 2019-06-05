package org.jetbrains.plugins.scala.lang.psi.controlFlow
package impl
package expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.ScNewTemplateDefinition
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.InvocationTools.{ArgParamClause, InvocationInfo}
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveResult

trait ScNewTemplateDefinitionCfgBuildingImpl { this: ScNewTemplateDefinition =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
    val classType = this.`type`().getOrAny

    val constrData = for {
      clazzType <- this.`type`().toOption
      constructorInvocation <- this.constructorInvocation
      ref <- constructorInvocation.reference
      resolveResult <- ref.bind()
    } yield (constructorInvocation, resolveResult)

    constrData match {
      case Some((constructorInvocation, ScalaResolveResult(target, _))) =>
        val classReg = builder.newRegister()
        builder.instantiate(classType, classReg)

        InvocationInfo(
          None, Some(target),
          constructorInvocation.matchedParametersByClauses.map(ArgParamClause(_, isTupled = false))
        ).buildWithoutThis(rreq, Some(classReg))
      case None =>
        rreq.satisfyAny(noop = true)
    }
  }
}
