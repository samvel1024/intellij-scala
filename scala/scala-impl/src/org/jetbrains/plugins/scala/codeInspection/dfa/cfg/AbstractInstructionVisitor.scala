package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

abstract class AbstractInstructionVisitor {
  def visitInstruction(instruction: Instruction): Unit = ()

  def visitAssign(assign: Assign): Unit = visitInstruction(assign)
  def visitDup(dup: Dup): Unit = visitInstruction(dup)
  def visitJump(jump: Jump): Unit = visitInstruction(jump)
  def visitJumpIFNot(jumpIfNot: JumpIfNot): Unit = visitInstruction(jumpIfNot)
  def visitPop(pop: Pop): Unit = visitInstruction(pop)
  def visitPush(push: Push): Unit = visitInstruction(push)
  def visitPushThis(pushThis: PushThis): Unit = visitInstruction(pushThis)
  def visitRet(ret: Ret): Unit = visitInstruction(ret)
}
