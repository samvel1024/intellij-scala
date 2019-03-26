package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

abstract class JumpingInstruction extends Instruction {
  def targetLabel: Label
}
