package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.DfVariable
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Write private[controlFlow](val variable: DfVariable) extends Instruction {

  override def popCount: Int = 2
  override def asmString: String = "write"
  override def info: Instruction.Info = Write
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitWrite(this)
}

object Write extends Instruction.Info(name = "Assign")
