package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import com.intellij.psi.{PsiMethod, PsiNamedElement}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScReferenceExpression
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFun, ScParameterOwner}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.InvocationTools.InvocationInfo
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.ScReferenceExpressionCfgBuildingImpl.ResolvesToFunction
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveResult

trait ScReferenceExpressionCfgBuildingImpl { this: ScReferenceExpression =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {
    this.bind() match {
      case Some(result) if this.refName != result.name && (result.name == "apply" || result.name == "update") =>
        val v = builder.resolveVariable(result.parentElement.get)
        rreq.satisfy(v, noop = true)

      case Some(ResolvesToFunction(func)) =>
        InvocationInfo(this.qualifier, Some(func), Seq.empty)
          .build(rreq)

      case Some(result) =>
        val v = builder.resolveVariable(result.element)
        rreq.satisfy(v, noop = true)
      case None =>
        // we have to be able to handle the error here
        ???
    }
  }
}

object ScReferenceExpressionCfgBuildingImpl {
  object ResolvesToFunction {
    def unapply(result: ScalaResolveResult): Option[PsiNamedElement] = Some(result) collect {
      case ScalaResolveResult(scalaFun: ScParameterOwner, _) => scalaFun
      case ScalaResolveResult(syntheticFun: ScFun, _) => syntheticFun
      case ScalaResolveResult(javaFun: PsiMethod, _) => javaFun
    }
  }
}