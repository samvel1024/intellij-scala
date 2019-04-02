package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Reorder private[controlFlow](val mapping: Array[Int]) extends Instruction {
  assert(mapping.length >= 2)
  assert(mapping.forall(idx => 0 <= idx && idx < mapping.length))
  assert(mapping.toSet.size == mapping.length)
  assert(mapping.zipWithIndex.exists { case (from, to) => from != to })

  override def popCount: Int = mapping.length
  override def pushCount: Int = mapping.length
  override def asmString: String = {
    if (mapping.length == 2) "flip"
    else "reorder " + mapping.mkString("[", ", ", "]")
  }
  override def info: Instruction.Info = Reorder
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitReorder(this)
}

object Reorder extends Instruction.Info(
  name = "Reorder"
)
