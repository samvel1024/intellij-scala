package org.jetbrains.plugins.scala.lang.psi.controlFlow

abstract class AbstractInstructionVisitor {
  def visitInstruction(instruction: cfg.Instruction): Unit = ()

  def visitWrite(write: cfg.Write): Unit = visitInstruction(write)
  def visitRead(read: cfg.Read): Unit = visitInstruction(read)
  def visitDup(dup: cfg.Dup): Unit = visitInstruction(dup)
  def visitReorder(reorder: cfg.Reorder): Unit = visitInstruction(reorder)
  def visitJump(jump: cfg.Jump): Unit = visitInstruction(jump)
  def visitJumpIf(jumpIf: cfg.JumpIf): Unit = visitInstruction(jumpIf)
  def visitJumpIfNot(jumpIfNot: cfg.JumpIfNot): Unit = visitInstruction(jumpIfNot)
  def visitPop(pop: cfg.Pop): Unit = visitInstruction(pop)
  def visitPush(push: cfg.Push): Unit = visitInstruction(push)
  def visitPushCtx(pushCtx: cfg.PushCtx): Unit = visitInstruction(pushCtx)
  def visitPushThis(pushThis: cfg.PushThis): Unit = visitInstruction(pushThis)
  def visitRet(ret: cfg.Ret): Unit = visitInstruction(ret)
  def visitEnd(end: cfg.End): Unit = visitInstruction(end)
}
