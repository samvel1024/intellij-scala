package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.statements

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScPatternDefinition
import org.jetbrains.plugins.scala.lang.psi.controlFlow.{CfgBuilder, CfgBuildingBlockStatement}

trait ScPatternDefinitionCfgBuildingImpl extends CfgBuildingBlockStatement { this: ScPatternDefinition =>

  def buildBlockStatementControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingTools._

    val patterns = pList.patterns.filter(!canIgnorePattern(_))
    buildExpressionOrPushAnyIfNeeded(expr, withResult = patterns.nonEmpty)

    // duplicate the value to
    builder.dup(patterns.length - 1)
    patterns.foreach(_.buildPatternControlFlow(None))
  }
}
