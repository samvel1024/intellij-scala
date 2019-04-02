package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.patterns

import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScParenthesisedPattern
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.Label

trait ScParenthesisedPatternCfgBuildingImpl { this: ScParenthesisedPattern =>
  def buildPatternControlFlow(noMatchTarget: Option[Label])(implicit builder: CfgBuilder): Unit = {
    innerElement match {
      case Some(inner) => inner.buildPatternControlFlow(noMatchTarget)
      case None => builder.pop()
    }
  }

}
