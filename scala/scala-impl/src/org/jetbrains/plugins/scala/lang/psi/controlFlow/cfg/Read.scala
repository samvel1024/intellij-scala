package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfRegister, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Read private[controlFlow](val target: DfRegister, val variable: DfVariable) extends Instruction {

  override def asmString: String = s"$target <- $variable"
  override def info: Instruction.Info = Read
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitRead(this)
}

object Read extends Instruction.Info(name = "Read")
