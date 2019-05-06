package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.scala.dfa.DfEntity
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScMethodCall}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFun, ScFunction, ScParameterOwner}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, RequireResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.InvocationTools.InvocationInfo
import org.jetbrains.plugins.scala.lang.psi.types.api
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveResult

import scala.annotation.tailrec

trait ScMethodCallCfgBuildingImpl extends MethodInvocationCfgBuildingImpl { this: ScMethodCall =>

  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {

    val relevantCalls = splitChain(innerMethodCallChain)

    val lastIdx = relevantCalls.length - 1
    var lastResult: ExprResult = null
    for (((call, resolveResult, matchedParameters), idx) <- relevantCalls.zipWithIndex) {
      val invocInfo =
        InvocationInfo(call.thisExpr, resolveResult.map(_.element), matchedParameters)
      lastResult = idx match {
        case 0 =>
          invocInfo.build(if (lastIdx == 0) rreq else RequireResult)
        case `lastIdx` =>
          invocInfo.buildWithoutThis(rreq, Some(lastResult.pin))
        case _ =>
          invocInfo.buildWithoutThis(RequireResult, Some(lastResult.pin))
      }
    }

    lastResult
  }

  private type CallInfo = (ScMethodCall, Option[ScalaResolveResult], Seq[(ScExpression, Parameter)])
  private def splitChain(chain: List[ScMethodCall]): List[CallInfo] = {
    def gatherCalls(restCalls: List[(ScMethodCall, Option[ScalaResolveResult])]): List[CallInfo] = {
      if (restCalls.isEmpty)
        return Nil

      import ScalaResolveResult.{withActual => resolvesTo}
      val (call, result) :: rest = restCalls
      val (restArgs, followingCalls) = result match {
        case Some(resolvesTo(scalaFun: ScParameterOwner)) => rest.splitAt(scalaFun.allClauses.length - 1)
        case Some(resolvesTo(syntheticFun: ScFun)) => rest.splitAt(syntheticFun.paramClauses.length - 1)
        case Some(resolvesTo(javaFun: PsiMethod)) => (Nil, rest)
        case None => rest.span(_._2.isEmpty)
      }

      val allMatchedArgs = call.matchedParameters ++ restArgs.flatMap(_._1.matchedParameters)
      val allArgExprs = call.argumentExpressions ++ restArgs.flatMap(_._1.argumentExpressions)

      val fixedArgs = fixArguments(allArgExprs, allMatchedArgs)

      (call, result, fixedArgs) :: gatherCalls(followingCalls)
    }

    gatherCalls(chain.map(call => call -> call.target))
  }

  private def fixArguments(args: Seq[ScExpression], matched: Seq[(ScExpression, Parameter)]): Seq[(ScExpression, Parameter)] = {
    val notMatchedArgs = args.filter(arg => !matched.exists(_._1 == arg))
    matched ++ makeFakeParameters(notMatchedArgs, matched.length)
  }

  // arguments to an unresolved call should still be evaluated
  private def makeFakeParameters(args: Seq[ScExpression], initialIndex: Int): Seq[(ScExpression, Parameter)] = {
    for ((arg, idx) <- args.zipWithIndex) yield {
      arg -> Parameter(api.Any, isRepeated = false, index = idx + initialIndex)
    }
  }

  private def innerMethodCallChain: List[ScMethodCall] = {
    @tailrec
    def inner(cur: ScExpression, found: List[ScMethodCall]): List[ScMethodCall] = {
      cur match {
        case call: ScMethodCall => inner(call.getEffectiveInvokedExpr, call :: found)
        case _ => found
      }
    }

    inner(this.getEffectiveInvokedExpr, this :: Nil)
  }
}
