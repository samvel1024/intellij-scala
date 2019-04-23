package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.RequireDirectResult.DirectResult

sealed abstract class ExprResult {
  def get: DfEntity
  def pin(implicit builder: CfgBuilder): DfEntity = builder.pin(get)
}

object ExprResult {
  implicit def ResultToEntity(result: ExprResult): DfEntity = result.get
}

sealed abstract class ResultRequirement {
  def satisfy(entity: DfEntity, noop: Boolean = false)(implicit builder: CfgBuilder): ExprResult
  def satisfyAny(noop: Boolean = false)(implicit builder: CfgBuilder): ExprResult = satisfy(builder.any, noop)
  def satisfyUnit(noop: Boolean = false)(implicit builder: CfgBuilder): ExprResult = satisfy(builder.unit, noop)
  def satisfyNothing(noop: Boolean = false)(implicit builder: CfgBuilder): ExprResult = satisfy(builder.any, noop)

  def pin()(implicit builder: CfgBuilder): (DfVariable, ExprResult)
  def pinOrDiscard()(implicit builder: CfgBuilder): (DfVariable, ExprResult) = pin()

  def derivePinned()(implicit builder: CfgBuilder): (ResultRequirement, ExprResult)

  def needsResult: Boolean
}

case object RequireNoResult extends ResultRequirement {
  private case object NoResult extends ExprResult {
    override def get: DfEntity =
      throw new UnsupportedOperationException("Cannot get result. No result was requested")
  }

  override def satisfy(entity: DfEntity, noop: Boolean = true)(implicit builder: CfgBuilder): ExprResult = {
    if (noop) {
      builder.noop(entity)
    }
    NoResult
  }

  override def pin()(implicit builder: CfgBuilder): (DfVariable, ExprResult) =
    throw new UnsupportedOperationException("Can't pin result, because no result is demanded.")

  override def pinOrDiscard()(implicit builder: CfgBuilder): (DfVariable, ExprResult) =
    builder.newRegister() -> NoResult

  override def derivePinned()(implicit builder: CfgBuilder): (ResultRequirement, ExprResult) =
    RequireNoResult -> NoResult

  override def needsResult: Boolean = false
}



final class RequireResultToProvidedSink(sink: DfVariable) extends ResultRequirement {
  case object SinkResult extends ExprResult {
    override def get: DfEntity = sink
  }

  override def satisfy(entity: DfEntity, noop: Boolean)(implicit builder: CfgBuilder): ExprResult = {
    builder.mov(sink, entity)
    SinkResult
  }

  override def pin()(implicit builder: CfgBuilder): (DfVariable, ExprResult) =
    sink -> SinkResult

  override def derivePinned()(implicit builder: CfgBuilder): (ResultRequirement, ExprResult) =
    this -> SinkResult

  override def needsResult: Boolean = true
}



sealed class RequireDirectResult extends ResultRequirement {
  override def satisfy(entity: DfEntity, noop: Boolean)(implicit builder: CfgBuilder): ExprResult = {
    DirectResult(entity)
  }

  override def pin()(implicit builder: CfgBuilder): (DfVariable, ExprResult) = {
    val reg = builder.newRegister()
    reg -> DirectResult(reg)
  }

  override def derivePinned()(implicit builder: CfgBuilder): (ResultRequirement, ExprResult) = {
    val reg = builder.newRegister()
    RequireResult(reg) -> DirectResult(reg)
  }

  override def needsResult: Boolean = true
}

object RequireDirectResult {
  private case class DirectResult(value: DfEntity) extends ExprResult {
    override def get: DfEntity = value
  }
}

object RequireResult extends RequireDirectResult {
  def apply(sink: DfVariable): RequireResultToProvidedSink = new RequireResultToProvidedSink(sink)

  object If {
    def apply(needsResult: Boolean): ResultRequirement = if (needsResult) RequireResult else RequireNoResult
  }
}