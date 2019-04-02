package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.statements

import org.jetbrains.plugins.scala.lang.psi.controlFlow.{CfgBuilder, CfgBuildingBlockStatement}

trait ScVariableDefinitionCfgBuildingImpl extends CfgBuildingBlockStatement {

  def buildBlockStatementControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit = {
    ???
  }
}
