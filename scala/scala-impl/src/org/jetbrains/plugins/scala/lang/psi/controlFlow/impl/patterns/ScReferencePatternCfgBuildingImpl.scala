package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.patterns

import org.jetbrains.plugins.scala.dfa.DfEntity
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.Label

trait ScReferencePatternCfgBuildingImpl { this: ScReferencePattern =>
  override def buildPatternControlFlow(value: DfEntity, noMatchTarget: Option[Label])
                                      (implicit builder: CfgBuilder): Unit = {
    builder.assign(this, value)
  }
}
