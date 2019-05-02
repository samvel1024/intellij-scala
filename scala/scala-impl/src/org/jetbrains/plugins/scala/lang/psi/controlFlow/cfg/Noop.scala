package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Noop private[controlFlow](val value: DfEntity) extends Instruction {
  override def sourceEntities: Seq[DfEntity] = Seq(value)
  override def variables: Seq[DfVariable] = Seq.empty
  override def asmString: String = s"noop $value"
  override def info: Instruction.Info = Noop
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitNoop(this)
}

object Noop extends Instruction.Info(
  name = "Noop",
  hasControlFlowAfter = false
)
