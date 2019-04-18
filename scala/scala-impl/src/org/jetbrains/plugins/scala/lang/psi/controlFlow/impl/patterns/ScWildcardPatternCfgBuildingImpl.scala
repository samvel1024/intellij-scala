package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.patterns

import org.jetbrains.plugins.scala.dfa.DfEntity
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScWildcardPattern
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.Label

trait ScWildcardPatternCfgBuildingImpl { this: ScWildcardPattern =>
  override def buildPatternControlFlow(value: DfEntity, noMatchTarget: Option[Label])
                                      (implicit builder: CfgBuilder): Unit = ()
}
