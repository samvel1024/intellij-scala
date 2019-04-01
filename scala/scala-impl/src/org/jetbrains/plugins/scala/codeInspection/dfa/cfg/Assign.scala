package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

/**
 * Pops two entity A and entity B from the stack and assigns B to A
 */
class Assign private[cfg] extends Instruction {

  override def popCount: Int = 2
  override def pushCount: Int = 1
  override def asmString: String = "assign"
  override def info: Instruction.Info = Assign
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitAssign(this)
}

object Assign extends Instruction.Info(name = "Assign")
