package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class Throw private[controlFlow](val returnValue: DfEntity) extends Instruction {

  override def sourceEntities: Seq[DfEntity] = Seq(returnValue)
  override def variables: Seq[DfVariable] = Seq.empty
  override def asmString: String = s"throw $returnValue"
  override def info: Instruction.Info = Throw
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitThrow(this)
}


object Throw extends Instruction.Info(
  name = "Throw",
  hasControlFlowAfter = false
)