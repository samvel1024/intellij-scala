package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow
import org.jetbrains.plugins.scala.lang.psi.controlFlow.ControlFlowGraph
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

abstract class Label {
  def name: String
  def line: Int = targetIndex + 1
  def targetIndex: Int
  def target: cfg.Instruction = graph.instructionAt(targetIndex)
  def graph: ControlFlowGraph

  override def toString: String = ".L" + name
}
