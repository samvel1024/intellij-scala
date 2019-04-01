package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

/**
 * Continues control flow at the target instruction
 *
 * @param targetLabel to the instruction where the control flow should be continued
 */
class Jump private[controlFlow](override val targetLabel: Label) extends JumpingInstruction {

  override def asmString: String = s"jump $targetLabel"
  override def info: Instruction.Info = Jump
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitJump(this)
}

object Jump extends Instruction.Info(
  name = "Jump",
  hasControlFlowAfter = false,
  isJump = true
)