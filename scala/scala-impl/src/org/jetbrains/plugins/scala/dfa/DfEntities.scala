package org.jetbrains.plugins.scala.dfa

import com.intellij.psi.{PsiElement, PsiNamedElement}
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScExpression
import org.jetbrains.plugins.scala.lang.psi.controlFlow.ControlFlowGraph
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.project.ProjectContext

sealed abstract class DfEntity {
}

sealed abstract class DfVariable extends DfEntity {
  def anchor: PsiElement
  def name: String

  override def toString: String = name
}

class DfRegister(override val anchor: PsiElement, val id: Int) extends DfVariable {
  override def name: String = "%" + Integer.toUnsignedString(id, 36)
}
case class DfLocalVariable(override val anchor: PsiElement, override val name: String) extends DfVariable
case class DfMemberVariable(override val anchor: PsiNamedElement) extends DfVariable {
  override def name: String = anchor.getName
}


sealed abstract class DfValue extends DfEntity


object DfValue {
  def unit: DfValue = DfUnit

  def any(implicit pc: ProjectContext): DfValue = new DfAbstractValue(pc.stdTypes.Any)

  def anyVal(implicit pc: ProjectContext): DfValue = new DfAbstractValue(pc.stdTypes.AnyVal)
  def boolean(implicit pc: ProjectContext): DfValue = new DfAbstractValue(pc.stdTypes.Boolean)
  def boolean(value: Boolean): DfValue = DfConcreteBoolean(value)
  def int(implicit pc: ProjectContext): DfValue = new DfAbstractValue(pc.stdTypes.Int)
  def int(value: Int): DfValue = DfConcreteInt(value)

  def anyRef(implicit pc: ProjectContext): DfValue = new DfAbstractValue(pc.stdTypes.AnyRef)
}

class DfAbstractValue(upperBound: ScType) extends DfValue {

}

sealed abstract class DfConcreteValue extends DfValue

class DfConcreteAnyRef extends DfConcreteValue

class DfConcreteStringRef(value: String) extends DfConcreteAnyRef {
  override def toString: String = '\"' + value + '\"'
}

class DfConcreteLambdaRef(val lambda: ScExpression,
                          val params: Seq[DfConcreteLambdaRef.Parameter],
                          val cfg: ControlFlowGraph) extends DfConcreteAnyRef {
  override def toString: String =
    s"lambda(${params.mkString(", ")})"
}

object DfConcreteLambdaRef {
  class Parameter(val variable: DfVariable, val ty: ScType) {
    def anchor: PsiElement = variable.anchor
    def name: String = variable.name
    override def toString: String = s"$name: ${ty.presentableText}"
  }
}

sealed abstract class DfConcreteAnyVal extends DfValue {
  def value: AnyVal
}

case object DfUnit extends DfConcreteAnyVal {
  def value: Unit = ()

  override def toString: String = "unit"
}

sealed abstract class DfConcreteBoolean extends DfConcreteAnyVal {
  override def value: Boolean
}

object DfConcreteBoolean {
  def apply(value: Boolean): DfConcreteBoolean =
    if (value) DfTrue else DfFalse
}

case object DfTrue extends DfConcreteBoolean {
  override def value: Boolean = true

  override def toString: String = "true"
}

case object DfFalse extends DfConcreteBoolean {
  override def value: Boolean = false

  override def toString: String = "false"
}

sealed abstract class DfConcreteIntegral extends DfConcreteAnyVal

case class DfConcreteInt(override val value: Int) extends DfConcreteIntegral {
  override def toString: String = value.toString
}

