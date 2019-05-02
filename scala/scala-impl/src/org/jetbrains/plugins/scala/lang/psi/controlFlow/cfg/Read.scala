package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfRegister, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Read private[controlFlow](val target: DfRegister, val variable: DfVariable) extends Instruction {

  override def sourceEntities: Seq[DfEntity] = Seq(variable)
  override def variables: Seq[DfVariable] = Seq(target)
  override def asmString: String = s"$target <- $variable"
  override def info: Instruction.Info = Read
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitRead(this)
}

object Read extends Instruction.Info(name = "Read")
