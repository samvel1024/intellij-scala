package org.jetbrains.plugins.scala.lang.psi.controlFlow

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingPattern.ValueSupplier
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{Label, RequireNoResult, RequireResult}

trait CfgBuildingPattern {
  def buildPatternControlFlow(value: ValueSupplier, noMatchTarget: Option[Label])(implicit builder: CfgBuilder): Unit = ???
}

object CfgBuildingPattern {
  abstract class ValueSupplier {
    def pin(variable: DfVariable)(implicit builder: CfgBuilder): variable.type
    def pin()(implicit builder: CfgBuilder): DfEntity
    def discard()(implicit builder: CfgBuilder): Unit
  }

  class RawExprSupplier(expr: ScExpression) extends ValueSupplier {
    override def pin(variable: DfVariable)(implicit builder: CfgBuilder): variable.type = {
      expr.buildExprControlFlow(RequireResult(variable))
      variable
    }

    override def pin()(implicit builder: CfgBuilder): DfEntity = {
      expr.buildExprControlFlow(RequireResult).pin
    }

    override def discard()(implicit builder: CfgBuilder): Unit = {
      expr.buildExprControlFlow(RequireNoResult)
    }
  }

  object RawExprSupplier {
    def apply(expr: ScExpression): RawExprSupplier = new RawExprSupplier(expr)
  }

  object RawExprOrAnySupplier {
    def apply(expr: Option[ScExpression])(implicit builder: CfgBuilder): ValueSupplier =
      expr.map(RawExprSupplier.apply).getOrElse(DirectSupplier(builder.any))
  }

  class DirectSupplier(entity: DfEntity) extends ValueSupplier {
    override def pin(variable: DfVariable)(implicit builder: CfgBuilder): variable.type = {
      builder.mov(variable, entity)
      variable
    }

    override def pin()(implicit builder: CfgBuilder): DfEntity = entity

    override def discard()(implicit builder: CfgBuilder): Unit = {
      builder.noop(entity)
    }
  }

  object DirectSupplier {
    def apply(entity: DfEntity): DirectSupplier = new DirectSupplier(entity)
  }
}