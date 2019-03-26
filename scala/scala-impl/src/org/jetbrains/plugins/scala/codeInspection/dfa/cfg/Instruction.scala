package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

abstract class Instruction {
  private var _index: Int = -1
  private var _graph: ControlFlowGraph = _

  def index: Int = {
    assert(_index >= 0)
    _index
  }

  private[cfg] def index_=(idx: Int): Unit = {
    assert(_index == -1)
    assert(idx >= 0)
    _index = idx
  }

  def graph: ControlFlowGraph = {
    assert(_graph != null)
    _graph
  }


  def popCount: Int = 0
  def pushCount: Int = 0

  def stackDelta: Int = pushCount - popCount

  def asmString: String
  def info: Instruction.Info

  override def toString: String = s"$index: $asmString"
}


object Instruction {
  class Info(
    val name: String,
    val hasControlFlowAfter: Boolean = true,
    val isJump: Boolean = false
  )

  private[cfg] def finalizeInstruction(instr: Instruction, graph: ControlFlowGraph): Unit = {
    assert(instr._index >= 0)
    assert(instr._graph == null)

    instr._graph = graph
  }
}