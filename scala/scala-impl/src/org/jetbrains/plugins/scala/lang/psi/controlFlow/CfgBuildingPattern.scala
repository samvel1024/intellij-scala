package org.jetbrains.plugins.scala.lang.psi.controlFlow

import org.jetbrains.plugins.scala.dfa.{DfEntity, DfValue}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.Label

trait CfgBuildingPattern {
  /**
   * Should add the instructions to `builder` to match the pattern.
   *
   * This function can assume that the value it should match is on top of the stack.
   * The created instructions must consume that top value.
   *
   * If the match should fail, the created control flow is supposed to jump to `noMatchTarget`.
   * If `noMatchTarget` is `None`, an exception must be thrown.
   *
   * @param noMatchTarget the label to jump to if the match fails
   * @param builder the builder to add the instructions to
   */
  def buildPatternControlFlow(value: DfEntity, noMatchTarget: Option[Label])(implicit builder: CfgBuilder): Unit = ???
}
