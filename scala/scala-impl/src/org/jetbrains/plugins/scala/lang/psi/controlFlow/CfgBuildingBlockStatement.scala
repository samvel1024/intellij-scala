package org.jetbrains.plugins.scala.lang.psi.controlFlow

trait CfgBuildingBlockStatement {
  def buildBlockStatementControlFlow(withResult: Boolean)(implicit builder: CfgBuilder): Unit
}
