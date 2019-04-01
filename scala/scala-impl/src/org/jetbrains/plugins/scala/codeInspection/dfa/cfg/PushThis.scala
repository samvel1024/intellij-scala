package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

class PushThis private[cfg] extends Instruction {
  override def pushCount: Int = 1
  override def asmString: String = s"pushThis"
  override def info: Instruction.Info = PushThis
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitPushThis(this)
}

object PushThis extends Instruction.Info(name = "PushThis")