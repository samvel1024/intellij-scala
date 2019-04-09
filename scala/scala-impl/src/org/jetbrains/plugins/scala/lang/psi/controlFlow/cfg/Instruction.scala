package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.{AbstractInstructionVisitor, ControlFlowGraph}

abstract class Instruction {
  private var _index: Int = -1
  private var _graph: ControlFlowGraph = _

  def index: Int = {
    assert(_index >= 0)
    _index
  }

  private[controlFlow] def index_=(idx: Int): Unit = {
    assert(_index == -1)
    assert(idx >= 0)
    _index = idx
  }

  def graph: ControlFlowGraph = {
    assert(_graph != null)
    _graph
  }

  def asmString: String
  def asmLine: String = s"$index: $asmString"
  def info: Instruction.Info

  def accept(visitor: AbstractInstructionVisitor): Unit

  override def toString: String = asmString
}


object Instruction {
  class Info(
    val name: String,
    val hasControlFlowAfter: Boolean = true,
    val isJump: Boolean = false
  )

  private[controlFlow] def finalizeInstruction(instr: Instruction, graph: ControlFlowGraph): Unit = {
    assert(instr._index >= 0)
    assert(instr._graph == null)

    instr._graph = graph
  }
}