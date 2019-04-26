package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.statements

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScVariableDeclaration
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.{ExprResult, ResultRequirement}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.{CfgBuilder, CfgBuildingBlockStatement}

trait ScVariableDeclarationCfgBuildingImpl extends CfgBuildingBlockStatement { this: ScVariableDeclaration =>

  override def buildBlockStatementControlFlow(rreq: ResultRequirement)(implicit builder: CfgBuilder): ExprResult =
    rreq.satisfyUnit()
}
