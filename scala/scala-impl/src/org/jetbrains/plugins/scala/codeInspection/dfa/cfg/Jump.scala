package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

/**
 * Continues control flow at the target instruction
 *
 * @param targetLabel to the instruction where the control flow should be continued
 */
class Jump private[cfg](override val targetLabel: Label) extends JumpingInstruction {

  override def asmString: String = s"jump $targetLabel"
  override def info: Instruction.Info = Jump
}

object Jump extends Instruction.Info(
  name = "Jump",
  hasControlFlowAfter = false,
  isJump = true
)