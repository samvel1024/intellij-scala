package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.DfVariable
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Read private[controlFlow](val variable: DfVariable) extends Instruction {

  override def popCount: Int = 1
  override def pushCount: Int = 1
  override def asmString: String = "read"
  override def info: Instruction.Info = Read
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitRead(this)
}

object Read extends Instruction.Info(name = "Read")
