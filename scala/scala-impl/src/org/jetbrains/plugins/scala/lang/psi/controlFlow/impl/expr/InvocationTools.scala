package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.dfa.DfEntity
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.api.expr.{MethodInvocation, ScBlockExpr, ScExpression}
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScParameter
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, RequireResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.types.nonvalue.Parameter

object InvocationTools {
  /*
    TODO: We have the following cases when we encounter a method invocation
    - [x] calls to functions
    - [x] calls to methods (=> need thisref)
    - [x] calls to function objects
    - [x] calls to apply
    - [x] calls to update
    - [~] calls with assignment (i.e. +=, -=, ::=)
    - [ ] property updates (a.prop = 33 -> a.prop_=(33))
    - [x] calls that have changed precedence (::, everything with : as last char)
    - [ ] imported member methods
    - [ ] with or without implicits
    - [ ] with or without generics
    - [x] multiple parameter clauses
    - [ ] auto tupeling
    - [x] default parameter
    - [x] named arguments (potentially reordered)
    - [ ] varargs
    - [ ] dynamics
   */

  final case class ArgParamClause(argParams: Seq[(ScExpression, Parameter)], isTupled: Boolean) {
    def sortedByExprPosition: ArgParamClause =
      if (isTupled) this
      else copy(argParams = argParams.sortBy(ArgumentSorting.exprPosition))

    def build()(implicit builder: CfgBuilder): Seq[(Int, DfEntity)] = {
      if (isTupled) Seq(0 -> TupleTools.buildTupleCreation(argParams.map(_._1), RequireResult).pin)
      else argParams.map(buildArgParams)
    }
  }

  private def buildArgParams(argParam: (ScExpression, Parameter))
                            (implicit builder: CfgBuilder): (Int, DfEntity) = argParam match {
    case (blockWithCaseClause@ScBlockExpr.withCaseClauses(_), param) =>
      param.index -> CaseClausesTools.createLambdaFromCaseClausesBlock(blockWithCaseClause)
    case (expr, param) =>
      val reg = expr.buildExprControlFlow(RequireResult).pin
      param.index -> reg
  }

  case class InvocationInfo(thisExpr: Option[ScExpression],
                            funcRef: Option[PsiElement],
                            argParams: Seq[ArgParamClause]) {
    def build(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult =
      buildWithoutThis(rreq, buildThisRef())

    def buildWithoutThis(rreq: ResultRequirement, thisRef: Option[DfEntity])(implicit builder: CfgBuilder): ExprResult = {
      val (ret, result) = rreq.tryPin()
      val paramRegs = argParams.map(_.sortedByExprPosition.build())

      val args = paramRegs.map(_.sortBy(ArgumentSorting.paramPosition).map(_._2))

      builder.call(thisRef, funcRef, ret, args)

      result
    }

    def buildRightAssoc(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult = {
      assert(this.argParams.nonEmpty)
      assert(!this.argParams.head.isTupled)
      // For 'a :: b'
      // 1. evaluate a
      // 2. evaluate b
      // 3. evaluate default and implicit parameters
      val firstArgClause +: restClauses = this.argParams.map(_.sortedByExprPosition)
      val (leftExpr, _) +: defaultOrImplicitParams = firstArgClause.argParams

      val (ret, result) = rreq.tryPin()
      val actualArgRef = leftExpr.buildExprControlFlow(RequireResult).pin
      val thisRef = buildThisRef()
      val defaultOrImplicitArgRefs = defaultOrImplicitParams.map(buildArgParams).map(_._2)
      val firstArgClauseRefs = actualArgRef +: defaultOrImplicitArgRefs
      val restArgClausesRefs = restClauses.map(_.build().map(_._2))

      builder.call(thisRef, funcRef, ret, firstArgClauseRefs +: restArgClausesRefs)
      result
    }

    def buildThisRef()(implicit builder: CfgBuilder): Option[DfEntity] =
      this.thisExpr.map(_.buildExprControlFlow(RequireResult).pin)
  }

  object ArgumentSorting {
    def exprPosition(t: (ScExpression, Parameter)): (Int, Int) = {
      val (expr, param) = t
      // actually supplied arguments have to be evaluated before default parameters
      val notDefault = expr.parent.exists(!_.isInstanceOf[ScParameter])
      if (notDefault) 0 -> expr.getTextOffset
      else 1 -> param.index
    }
    def paramPosition(t: (Int, DfEntity)): Int = t._1
  }

  def invocationInfoFor(invoc: MethodInvocation): InvocationInfo = {
    val rr = invoc.target
    val isTupled = rr.exists(_.tuplingUsed)
    InvocationInfo(
      invoc.thisExpr,
      rr.map(_.element),
      Seq(ArgParamClause(invoc.matchedParameters, isTupled))
    )
  }

  object withInvocationInfo {
    def unapply(invoc: MethodInvocation): Option[InvocationInfo] =
      Some(invocationInfoFor(invoc))
  }
}
