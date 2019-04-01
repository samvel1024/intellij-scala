package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

/**
 * Pops one element from the stack, which is used as return value.
 * Afterwards, finishes control flow for the current function.
 */
class Ret private[controlFlow] extends Instruction {
  override def popCount: Int = 1
  override def asmString: String = "ret"
  override def info: Instruction.Info = Ret
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitRet(this)
}

object Ret extends Instruction.Info(
  name = "Ret",
  hasControlFlowAfter = false
)
