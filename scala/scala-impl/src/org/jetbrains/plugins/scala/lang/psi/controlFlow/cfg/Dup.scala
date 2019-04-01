package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

/**
 * Duplicates the topmost element
 *
 * @param times how often the topmost element should be duplicated
 */
class Dup private[controlFlow](val times: Int) extends Instruction {
  assert(times >= 1)

  override def popCount: Int = 1
  override def pushCount: Int = times + 1
  override def asmString: String = "dup" + (if (times > 1) s" ${times}x" else "")
  override def info: Instruction.Info = Dup
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitDup(this)
}

object Dup extends Instruction.Info(
  name = "Dup"
)
