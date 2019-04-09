package org.jetbrains.plugins.scala.lang.psi.controlFlow

import com.intellij.psi.{PsiElement, PsiNamedElement}
import org.jetbrains.plugins.scala.dfa.{DfConcreteAnyRef, DfConcreteStringRef, DfEntity, DfLocalVariable, DfRegister, DfValue, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.ScNamedElement
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder._
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg._
import org.jetbrains.plugins.scala.project.ProjectContext

import scala.collection.mutable

class CfgBuilder(implicit val projectContext: ProjectContext) {
  private type Stack = List[DfEntity]

  private var nextRegisterId = 0
  private val instructions = mutable.Buffer.empty[cfg.Instruction]
  private val unboundLabels = mutable.Set.empty[BuildLabel]
  private val stackSizeAtLabel = mutable.Map.empty[cfg.Label, Stack]
  private var numLabelsToNextInstr = 0
  private var vstack: Stack = List.empty
  private val stringLiteralCache = mutable.Map.empty[String, DfConcreteAnyRef]
  private val variableCache = mutable.Map.empty[PsiNamedElement, DfVariable]

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

  private def newInstr(jumpingInstr: JumpingInstruction): JumpingInstruction = {
    newInstr(jumpingInstr: cfg.Instruction)
    use(jumpingInstr.targetLabel)
    jumpingInstr
  }

  private def use(label: Label): Unit = {
    val lstack = stackSizeAtLabel.getOrElseUpdate(label, vstack)

    if (vstack ne lstack) {
      throw new IllegalStateException(s"When jumping to label $label, stack is different")
    }
  }

  private def resolveVariable(anchor: PsiNamedElement): DfVariable = {
    variableCache.getOrElseUpdate(anchor, DfLocalVariable(anchor))
  }

  private def vpop(): DfEntity = {
    assert(vstack.nonEmpty)
    val head :: rest = vstack
    vstack = rest
    head
  }

  private def vpush[E <: DfEntity](entity: E): E = {
    vstack ::= entity
    entity
  }

  private def vpush(anchor: PsiElement): DfRegister =
    vpush(new DfRegister(anchor, newRegisterId(), vstack.size))

  def write(variable: ScNamedElement): this.type = {
    newInstr(new Write(resolveVariable(variable), vpop()))
    this
  }

  def read(variable: PsiNamedElement): this.type = {
    newInstr(new Read(vpush(variable), resolveVariable(variable)))
    this
  }

  def pushCtx(): this.type = {
    push(???)
    this
  }

  def pushAny(): this.type = push(DfValue.any)
  def pushUnit(): this.type = push(DfValue.unit)
  def pushNull(): this.type = push(???)
  def pushNothing(): this.type = push(???)

  def pushThis(): this.type = {
    push(???)
    this
  }

  def push(value: DfEntity): this.type = {
    vpush(value)
    this
  }

  def pushString(string: String): this.type = {
    val entity = stringLiteralCache.getOrElseUpdate(string, new DfConcreteStringRef(string))
    vpush(entity)
    this
  }

  def pop(): this.type = {
    vpop()
    this
  }

  def noop(): this.type = {
    newInstr(new Noop(vpop()))
    this
  }

  def dup(times: Int = 1): this.type = {
    if (times < 0)
      throw new IllegalArgumentException("Tried to duplicate negative times, which probably indicates an error")

    if (times >= 1) {
      val entity = vpop()
      vstack :::= List.fill(times)(entity)
    }
    this
  }

  def reorder(mapping: Seq[Int]): this.type = {
    assert(mapping.forall(idx => 0 <= idx && idx < mapping.length))
    assert(mapping.toSet.size == mapping.length)
    assert(vstack.length >= mapping.length)

    val (source, vrest) = vstack.splitAt(mapping.length)
    vstack = mapping.map(source) ++: vrest

    this
  }

  def reorder(head: Int, mapping: Int*): this.type = reorder(head +: mapping)

  def flip(): this.type = reorder(1, 0)

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

    val expectedStack = stackSizeAtLabel.getOrElseUpdate(label, vstack)
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
    val usedUnbound = stackSizeAtLabel.keySet & unboundLabels.toSet[Label]
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
}
