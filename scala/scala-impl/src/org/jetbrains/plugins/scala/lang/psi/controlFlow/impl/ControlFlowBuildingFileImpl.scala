package org.jetbrains.plugins.scala.lang.psi.controlFlow.impl

import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.controlFlow.{CfgBuilder, CfgBuildingBlockStatement, ControlFlowGraph}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaFileImpl

trait ControlFlowBuildingFileImpl { this: ScalaFileImpl =>
  override protected def buildControlFlow(): ControlFlowGraph = {
    implicit val builder: CfgBuilder = new CfgBuilder

    this.children
      .collect { case stmt: CfgBuildingBlockStatement => stmt }
      .foreach(_.buildBlockStatementControlFlow(withResult = false))

    builder.end()
    builder.build()
  }
}
