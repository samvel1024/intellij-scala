package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.patterns

import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingPattern.ValueSupplier
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.Label

trait ScReferencePatternCfgBuildingImpl { this: ScReferencePattern =>
  override def buildPatternControlFlow(value: ValueSupplier, noMatchTarget: Option[Label])
                                      (implicit builder: CfgBuilder): Unit = {
    value.pin(builder.resolveVariable(this))
  }
}
