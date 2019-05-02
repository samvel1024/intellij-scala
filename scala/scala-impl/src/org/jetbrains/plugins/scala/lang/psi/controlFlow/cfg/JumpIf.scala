package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class JumpIf private[controlFlow](val condition: DfEntity, val targetLabel: Label) extends JumpingInstruction {

  override def sourceEntities: Seq[DfEntity] = Seq(condition)
  override def variables: Seq[DfVariable] = Seq.empty
  override def asmString: String = s"if $condition -> $targetLabel"
  override def info: Instruction.Info = JumpIf
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitJumpIf(this)
}

object JumpIf extends Instruction.Info(
  name = "JumpIf",
  hasControlFlowAfter = true,
  isJump = true
)