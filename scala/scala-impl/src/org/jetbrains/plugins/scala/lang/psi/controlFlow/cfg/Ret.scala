package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

/**
 * Pops one element from the stack, which is used as return value.
 * Afterwards, finishes control flow for the current function.
 */
class Ret private[controlFlow](val returnValue: DfEntity) extends Instruction {
  override def sourceEntities: Seq[DfEntity] = Seq(returnValue)
  override def variables: Seq[DfVariable] = Seq.empty
  override def asmString: String = s"ret $returnValue"
  override def info: Instruction.Info = Ret
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitRet(this)
}

object Ret extends Instruction.Info(
  name = "Ret",
  hasControlFlowAfter = false
)
