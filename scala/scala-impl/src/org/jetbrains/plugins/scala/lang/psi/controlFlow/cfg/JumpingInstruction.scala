package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

abstract class JumpingInstruction extends Instruction {
  def targetLabel: Label
}
