package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Mov private[controlFlow](val target: DfVariable, val source: DfEntity) extends Instruction {

  override def sourceEntities: Seq[DfEntity] = Seq(source)
  override def variables: Seq[DfVariable] = Seq(target)

  override def asmString: String = Instruction.asmAssignmentPrefix(target) + source

  override def info: Instruction.Info = Read
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitMov(this)
}

object Mov extends Instruction.Info(name = "Mov")
