package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScAssignment, ScExpression, ScMethodCall}
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFun, ScParameterOwner}
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
      val (call, result) :: rest = restCalls
      val target = result.map(_.mostInnerResolveResult)
      val (restArgs, followingCalls) = target match {
        case Some(ScalaResolveResult(scalaFun: ScParameterOwner, _)) => rest.splitAt(scalaFun.allClauses.length - 1)
        case Some(ScalaResolveResult(syntheticFun: ScFun, _)) => rest.splitAt(syntheticFun.paramClauses.length - 1)
        case Some(ScalaResolveResult(javaFun: PsiMethod, _)) => (Nil, rest)
        case None => rest.span(_._2.isEmpty)
      }

      val allMatchedArgs = call.matchedParameters ++ restArgs.flatMap(_._1.matchedParameters)
      val allArgExprs = call.argumentExpressions ++ restArgs.flatMap(_._1.argumentExpressions)

      // there might be more parameter then the function wants. In this case still evaluate the parameters.
      val fixedArgs = fixArguments(allArgExprs, allMatchedArgs)

      (call, target, fixedArgs) :: gatherCalls(followingCalls)
    }

    gatherCalls(chain.map(call => call -> call.target))
  }

  private def fixArguments(args: Seq[ScExpression], matched: Seq[(ScExpression, Parameter)]): Seq[(ScExpression, Parameter)] = {
    def notAlreadyMatched(arg: ScExpression): Boolean =
      !matched.exists(_._1 == arg)

    val notMatchedArgs = args.filter {
      case ScAssignment(_, Some(actualArg)) => notAlreadyMatched(actualArg)
      case arg => notAlreadyMatched(arg)
    }
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
