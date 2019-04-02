package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class JumpIf private[controlFlow](override val targetLabel: Label) extends JumpingInstruction {

  override def popCount: Int = 1
  override def asmString: String = s"jumpIf $targetLabel"
  override def info: Instruction.Info = JumpIf
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitJumpIf(this)
}

object JumpIf extends Instruction.Info(
  name = "JumpIf",
  hasControlFlowAfter = true,
  isJump = true
)