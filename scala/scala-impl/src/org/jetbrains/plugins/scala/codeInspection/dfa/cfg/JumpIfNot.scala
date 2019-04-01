package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

class JumpIfNot private[cfg](override val targetLabel: Label) extends JumpingInstruction {

  override def popCount: Int = 1
  override def asmString: String = s"jumpIfNot $targetLabel"
  override def info: Instruction.Info = JumpIfNot
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitJumpIFNot(this)
}

object JumpIfNot extends Instruction.Info(
  name = "JumpIfNot",
  hasControlFlowAfter = true,
  isJump = true
)