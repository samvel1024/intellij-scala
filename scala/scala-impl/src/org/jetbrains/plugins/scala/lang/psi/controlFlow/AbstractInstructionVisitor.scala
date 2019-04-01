package org.jetbrains.plugins.scala.lang.psi.controlFlow

abstract class AbstractInstructionVisitor {
  def visitInstruction(instruction: cfg.Instruction): Unit = ()

  def visitAssign(assign: cfg.Assign): Unit = visitInstruction(assign)
  def visitDup(dup: cfg.Dup): Unit = visitInstruction(dup)
  def visitJump(jump: cfg.Jump): Unit = visitInstruction(jump)
  def visitJumpIFNot(jumpIfNot: cfg.JumpIfNot): Unit = visitInstruction(jumpIfNot)
  def visitPop(pop: cfg.Pop): Unit = visitInstruction(pop)
  def visitPush(push: cfg.Push): Unit = visitInstruction(push)
  def visitPushThis(pushThis: cfg.PushThis): Unit = visitInstruction(pushThis)
  def visitRet(ret: cfg.Ret): Unit = visitInstruction(ret)
}
