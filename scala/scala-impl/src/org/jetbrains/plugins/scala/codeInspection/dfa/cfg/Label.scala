package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

abstract class Label {
  def name: String
  def targetIndex: Int
  def target: Instruction = graph.instructionAt(targetIndex)
  def graph: ControlFlowGraph

  override def toString: String = ".L" + name
}
