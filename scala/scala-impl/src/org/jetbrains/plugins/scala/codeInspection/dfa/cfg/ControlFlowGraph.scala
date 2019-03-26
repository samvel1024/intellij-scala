package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

class ControlFlowGraph private (val instructions: Array[Instruction]) {
  assert(instructions.length > 0)

  def instructionCount: Int = instructions.length
  def instructionAt(index: Int): Instruction = instructions(index)

  def entryInstruction: Instruction = instructions.head
}

object ControlFlowGraph {
  private[cfg] def apply(instructions: Array[Instruction]): ControlFlowGraph = {
    val cfg = new ControlFlowGraph(instructions)

    for ((instr, idx) <- instructions.zipWithIndex)
      Instruction.finalizeInstruction(instr, cfg)

    cfg
  }
}