package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

import org.jetbrains.plugins.scala.codeInspection.dfa.values.DfEntity

/**
 * Pushes an entity to the stack
 *
 * @param entity to push to the stack
 */
class Push private[cfg](entity: DfEntity) extends Instruction {
  override def pushCount: Int = 1
  override def asmString: String = s"push $entity"
  override def info: Instruction.Info = Push
}

object Push extends Instruction.Info(name = "Push")