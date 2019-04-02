package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class End private[controlFlow] extends Instruction {
  override def asmString: String = "end"
  override def info: Instruction.Info = End
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitEnd(this)
}

object End extends Instruction.Info(
  name = "End",
  hasControlFlowAfter = false
)
