package org.jetbrains.plugins.scala.lang.psi.api

import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.ScalaControlFlowBuilder
import org.jetbrains.plugins.scala.lang.psi.controlFlow.{ControlFlowGraph, Instruction}
import org.jetbrains.plugins.scala.macroAnnotations.{Cached, ModCount}

/**
 * Represents elements with control flow cached
 * @author ilyas
 */

trait ScControlFlowOwner extends ScalaPsiElement {

  @Cached(ModCount.getModificationCount, this)
  def getControlFlow: Seq[Instruction] = {
    val builder = new ScalaControlFlowBuilder(null, null)
    controlFlowScope match {
      case Some(elem) => builder.buildControlflow(elem)
      case None => Seq.empty
    }
  }

  def controlFlowScope: Option[ScalaPsiElement]

  @Cached(ModCount.getModificationCount, this)
  def controlFlowGraph: ControlFlowGraph = buildControlFlow()

  protected def buildControlFlow(): ControlFlowGraph = ???
}
