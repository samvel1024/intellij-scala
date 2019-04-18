package org.jetbrains.plugins.scala.lang.psi.controlFlow

import com.intellij.psi.{PsiElement, PsiNamedElement}
import org.jetbrains.plugins.scala.dfa.{DfConcreteAnyRef, DfConcreteStringRef, DfEntity, DfLocalVariable, DfRegister, DfValue, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder._
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg._
import org.jetbrains.plugins.scala.project.ProjectContext

import scala.collection.mutable

class CfgBuilder(implicit val projectContext: ProjectContext) {
  private var nextRegisterId = 0
  private val instructions = mutable.Buffer.empty[cfg.Instruction]
  private val unboundLabels = mutable.Set.empty[BuildLabel]
  private var numLabelsToNextInstr = 0
  private val stringLiteralCache = mutable.Map.empty[String, DfConcreteAnyRef]
  private val variableCache = mutable.Map.empty[PsiNamedElement, VariableRef]

  private def indexOfNextInstr: Int = instructions.length

  private def hasControlFlowFromPreviousInstruction: Boolean =
    instructions.lastOption.forall(_.info.hasControlFlowAfter)

  private def newRegisterId(): Int = {
    val nextId = nextRegisterId
    nextRegisterId += 1
    nextId
  }

  private def newInstr(instr: cfg.Instruction): cfg.Instruction = {
    instr.index = indexOfNextInstr
    instructions += instr
    numLabelsToNextInstr = 0
    instr
  }

  private def resolveVariable(anchor: PsiNamedElement): VariableRef = {
    variableCache.getOrElseUpdate(anchor, VariableRef(DfLocalVariable(anchor)))
  }

  def mov[Sink: ToSinkRef, Source: ToSourceRef](target: Sink, source: Source): this.type = {
    val targetRef = sinkRefTo(target)
    val sourceRef = sourceRefTo(source)

    if (targetRef != sourceRef) {
      ???
    }
    this
  }

  def sinkRefTo[Sink](sink: Sink)(implicit toSinkRef: ToSinkRef[Sink]): ValueSinkRef =
    toSinkRef(sink, this)

  def sourceRefTo[Source](source: Source)(implicit toSourceRef: ToSourceRef[Source]): ValueSourceRef =
    toSourceRef(source, this)

  def pin[Source: ToSourceRef](source: Source): RegisterRef = sourceRefTo(source) match {
    case reg@RegisterRef(_) => reg
    case sourceRef =>
      val reg = newRegister()
      mov(reg, sourceRef)
      reg
  }

  def newRegister(): RegisterRef =
    RegisterRef(new DfRegister(null, newRegisterId()))

  def noop(): this.type = {
    newInstr(new Noop(vpop()))
    this
  }

  def ret(): this.type = {
    newInstr(new Ret(vpop()))
    this
  }

  def end(): this.type = {
    newInstr(new End)
    this
  }

  def jumpTo(target: BuildLabel): this.type = {
    newInstr(new Jump(target))
    this
  }

  def jumpIfTrue(target: BuildLabel): this.type = {
    newInstr(new JumpIf(vpop(), target))
    this
  }

  def jumpIfFalse(target: BuildLabel): this.type = {
    newInstr(new JumpIfNot(vpop(), target))
    this
  }

  def bindLabel(label: BuildLabel): this.type = {
    if (label.isBound)
      throw new IllegalArgumentException(s"Cannot bind bound label $label")

    if (!unboundLabels.contains(label))
      throw new IllegalArgumentException(s"Label $label belongs to another builder")

    val expectedStack = registersAtLabel.getOrElseUpdate(label, vstack)
    if (vstack.size != expectedStack.size) {
      throw new IllegalArgumentException(s"Cannot bind label $label to stack size ${vstack.size}, because label expected stack size ${expectedStack.size}")
    }

    unboundLabels -= label
    numLabelsToNextInstr += 1
    label._targetIndex = indexOfNextInstr
    this
  }

  def createLabel(name: String = ""): BuildLabel = {
    val label = new BuildLabel(name)
    unboundLabels += label
    label
  }

  def build(): ControlFlowGraph = {
    val usedUnbound = registersAtLabel.keySet & unboundLabels.toSet[Label]
    if (usedUnbound.nonEmpty) {
      throw new IllegalStateException(s"Cannot build cfg with ${usedUnbound.size} unbound labels: ${usedUnbound.mkString(", ")}")
    }

    if (numLabelsToNextInstr > 0) {
      throw new IllegalStateException("Cannot build cfg with labels pointing after its end")
    }

    if (hasControlFlowFromPreviousInstruction) {
      throw new IllegalStateException("Cfg is not closed")
    }

    ControlFlowGraph(instructions.toArray)
  }
}

object CfgBuilder {
  class BuildLabel(val _name: String) extends Label {
    private[CfgBuilder] var _targetIndex = -1
    private[CfgBuilder] var _graph: ControlFlowGraph = _

    override def name: String = {
      val boundTo = if (isBound) targetIndex.toString else "<unbound>"
      val name = if (_name.nonEmpty) _name else "unknown"
      s"$name[$boundTo]"
    }

    override def targetIndex: Int = {
      assert(_targetIndex >= 0)
      _targetIndex
    }

    override def graph: ControlFlowGraph = {
      assert(_graph != null)
      _graph
    }

    def isBound: Boolean = {
      _targetIndex >= 0
    }
  }

  sealed abstract class ValueSourceRef {
    def discard(): Unit
  }

  case class EntityRef(value: DfValue) extends ValueSourceRef {
    override def discard(): Unit = ()
  }

  sealed abstract class ValueSinkRef extends ValueSourceRef

  case class RegisterRef(register: DfRegister) extends ValueSinkRef {
    override def discard(): Unit = ???
  }

  case class VariableRef(variable: DfVariable) extends ValueSinkRef {
    override def discard(): Unit = ()
  }

  sealed abstract class ToSourceRef[-From] {
    def apply(from: From, builder: CfgBuilder): ValueSourceRef
  }

  object ToSourceRef {
    implicit case object RefToRef extends ToSourceRef[ValueSourceRef] {
      override def apply(from: ValueSourceRef, builder: CfgBuilder): ValueSourceRef = from
    }
  }

  sealed abstract class ToSinkRef[-From] {
    def apply(from: From, builder: CfgBuilder): ValueSinkRef
  }

  object ToSinkRef {
    implicit case object RefToRef extends ToSinkRef[ValueSinkRef] {
      override def apply(from: ValueSinkRef, builder: CfgBuilder): ValueSinkRef = from
    }
  }

  object Implicits {
    implicit class RefTraversableOnce(val underlying: TraversableOnce[ValueSourceRef]) extends AnyVal {
      def discardAll(): Unit = underlying.foreach(_.discard())
    }
  }
}
