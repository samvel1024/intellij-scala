package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

/**
 * Pops two entity A and entity B from the stack and assigns B to A
 */
class Assign private[controlFlow] extends Instruction {

  override def popCount: Int = 2
  override def pushCount: Int = 1
  override def asmString: String = "assign"
  override def info: Instruction.Info = Assign
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitAssign(this)
}

object Assign extends Instruction.Info(name = "Assign")
