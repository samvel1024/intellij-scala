package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor

class PushCtx private[controlFlow] extends Instruction {
  override def pushCount: Int = 1
  override def asmString: String = s"pushCtx"
  override def info: Instruction.Info = PushCtx
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitPushCtx(this)
}

object PushCtx extends Instruction.Info(name = "PushCtx")
