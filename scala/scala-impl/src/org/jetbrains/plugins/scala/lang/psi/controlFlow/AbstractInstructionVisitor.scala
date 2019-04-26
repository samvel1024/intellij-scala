package org.jetbrains.plugins.scala.lang.psi.controlFlow

abstract class AbstractInstructionVisitor {
  def visitInstruction(instruction: cfg.Instruction): Unit = ()

  def visitMov(mov: cfg.Mov): Unit = visitInstruction(mov)
  def visitWrite(write: cfg.Write): Unit = visitInstruction(write)
  def visitRead(read: cfg.Read): Unit = visitInstruction(read)
  def visitCall(call: cfg.Call): Unit = visitInstruction(call)
  def visitJump(jump: cfg.Jump): Unit = visitInstruction(jump)
  def visitJumpIf(jumpIf: cfg.JumpIf): Unit = visitInstruction(jumpIf)
  def visitJumpIfNot(jumpIfNot: cfg.JumpIfNot): Unit = visitInstruction(jumpIfNot)
  def visitRet(ret: cfg.Ret): Unit = visitInstruction(ret)
  def visitEnd(end: cfg.End): Unit = visitInstruction(end)
  def visitNoop(noop: cfg.Noop): Unit = visitInstruction(noop)
}
