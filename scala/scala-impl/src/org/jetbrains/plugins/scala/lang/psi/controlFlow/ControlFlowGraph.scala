package org.jetbrains.plugins.scala.lang.psi.controlFlow

class ControlFlowGraph private (val instructions: Array[cfg.Instruction]) {
  assert(instructions.length > 0)

  def instructionCount: Int = instructions.length
  def instructionAt(index: Int): cfg.Instruction = instructions(index)

  def entryInstruction: cfg.Instruction = instructions.head

  def asmText(lineNumbers: Boolean = true): String = {
    if (instructions.isEmpty) {
      return "<empty-cfg>"
    }

    val numLength = (instructions.length - 1).toString.length

    instructions.zipWithIndex.map {
      case (instr, line) if lineNumbers => line.toString.padTo(numLength, ' ') + ": " + instr.asmString
      case (instr, _) => instr.asmString
    }.mkString("\n")
  }

  override def toString: String = asmText()
}

object ControlFlowGraph {
  private[controlFlow] def apply(instructions: Array[cfg.Instruction]): ControlFlowGraph = {
    val g = new ControlFlowGraph(instructions)

    for ((instr, idx) <- instructions.zipWithIndex)
      cfg.Instruction.finalizeInstruction(instr, g)

    g
  }
}