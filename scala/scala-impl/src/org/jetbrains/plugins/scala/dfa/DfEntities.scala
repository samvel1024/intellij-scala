package org.jetbrains.plugins.scala.dfa

import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.lang.psi.types.ScType
import org.jetbrains.plugins.scala.project.ProjectContext

sealed abstract class DfEntity {
}

sealed abstract class DfVariable extends DfEntity {
  def anchor: ScNamedElement
  def name: String = anchor.name
}

case class DfLocalVariable(override val anchor: ScNamedElement) extends DfVariable
case class DfMemberVariable(override val anchor: ScNamedElement) extends DfVariable


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

