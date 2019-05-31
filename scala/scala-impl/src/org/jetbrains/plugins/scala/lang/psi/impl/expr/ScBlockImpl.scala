package org.jetbrains.plugins.scala
package lang
package psi
package impl
package expr

import com.intellij.lang.ASTNode
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScBlock
import org.jetbrains.plugins.scala.lang.psi.controlFlow.impl.expr.ScBlockCfgBuildingImpl

/**
* @author ilyas
*/
class ScBlockImpl(node: ASTNode) extends ScExpressionImplBase(node) with ScBlock with ScBlockCfgBuildingImpl {
  override def toString: String = "BlockOfExpressions"
}