package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

/**
 * Pops the topmost element from the stack
 */
class Pop private[cfg] extends Instruction {
  override def popCount: Int = 1
  override def asmString: String = "pop"
  override def info: Instruction.Info = Pop
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitPop(this)
}

object Pop extends Instruction.Info(name = "Pop")
