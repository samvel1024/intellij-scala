package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr

import org.jetbrains.plugins.scala.lang.psi.api.expr.{MethodInvocation, ScInfixExpr, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, RequireResult, ResultRequirement}
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScVariable

trait ScInfixExprCfgBuildingImpl extends MethodInvocationCfgBuildingImpl { this: ScInfixExpr =>
  protected override def buildActualExpressionControlFlow(rreq: ResultRequirement)
                                                         (implicit builder: CfgBuilder): ExprResult = {

    val invocInfo = this.invocationInfo

    if (this.isAssignmentOperator && invocInfo.funcRef.exists(_.name == this.operation.getText.init)) {


      this.left match {
        case leftInvoc: MethodInvocation =>
          /*
            expr(idx) op= arg

            exprRef <- expr
            originalValue <- exprRef.apply(idx)
            resultValue <- originalValue.op(arg)
            exprRef.update(idx, resultValue)
          */
          ???
        case leftRef@ScReferenceExpression.withQualifier(qualifier) =>
          /*
            qualifier.refName op= arg

            qualifierRef <- qualifier
            propValue <- qualifierRef[refName]
            result <- propValue.op(arg)
            qualifierRef[refName] <- result

            !!!! could also be call to refName and refName_=
          */
          ???
        case ScReferenceExpression(variable: ScNamedElement) =>
          /*
            var op= arg

            varRef <- var
            originalValue <- varRef
            varRef <- originalValue.op(arg)
          */
          val varRef  = builder.resolveVariable(variable)
          invocInfo.buildWithoutThis(RequireResult(varRef), thisRef = Some(varRef))
          rreq.satisfyUnit()
        case _ =>
          // forgot something? Also react to errors
          ???
      }

      rreq.satisfyUnit()
    } else if (this.isRightAssoc) {
      invocInfo.buildRightAssoc(rreq)
    } else {
      invocInfo.build(rreq)
    }
  }
}

