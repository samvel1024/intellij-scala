package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class JumpIfNot private[controlFlow](override val targetLabel: Label) extends JumpingInstruction {

  override def popCount: Int = 1
  override def asmString: String = s"jumpIfNot $targetLabel"
  override def info: Instruction.Info = JumpIfNot
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitJumpIfNot(this)
}

object JumpIfNot extends Instruction.Info(
  name = "JumpIfNot",
  hasControlFlowAfter = true,
  isJump = true
)