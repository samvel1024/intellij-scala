package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class End private[controlFlow] extends Instruction {
  override def sourceEntities: Seq[DfEntity] = Seq.empty
  override def variables: Seq[DfVariable] = Seq.empty
  override def asmString: String = "end"
  override def info: Instruction.Info = End
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitEnd(this)
}

object End extends Instruction.Info(
  name = "End",
  hasControlFlowAfter = false
)
