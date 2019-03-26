package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

/**
 * Duplicates the topmost element
 *
 * @param times how often the topmost element should be duplicated
 */
class Dup private[cfg](val times: Int) extends Instruction {
  assert(times >= 1)

  override def popCount: Int = 1
  override def pushCount: Int = times + 1
  override def asmString: String = "dup" + (if (times > 1) s" ${times}x" else "")
  override def info: Instruction.Info = Dup
}

object Dup extends Instruction.Info(
  name = "Dup"
)
