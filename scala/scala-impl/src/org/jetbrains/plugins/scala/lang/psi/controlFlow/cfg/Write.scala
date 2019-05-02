package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Write private[controlFlow](val variable: DfVariable, val value: DfEntity) extends Instruction {

  override def sourceEntities: Seq[DfEntity] = Seq(value)
  override def variables: Seq[DfVariable] = Seq(variable)
  override def asmString: String = s"$variable = $value"
  override def info: Instruction.Info = Write
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitWrite(this)
}

object Write extends Instruction.Info(name = "Write")
