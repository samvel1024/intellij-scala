package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

import org.jetbrains.plugins.scala.codeInspection.dfa.cfg.CfgBuilder.BuildLabel
import org.jetbrains.plugins.scala.codeInspection.dfa.values.{DfEntity, DfValue}
import org.jetbrains.plugins.scala.project.ProjectContext

import scala.collection.mutable

class CfgBuilder(implicit val projectContext: ProjectContext) {
  private val instructions = mutable.Buffer.empty[Instruction]
  private val unboundLabels = mutable.Set.empty[BuildLabel]
  private val stackSizeAtLabel = mutable.Map.empty[Label, Int]
  private var numLabelsToNextInstr = 0
  private var curStackSize = 0

  private def indexOfNextInstr: Int = instructions.length

  private def hasControlFlowFromPreviousInstruction: Boolean =
    instructions.lastOption.forall(_.info.hasControlFlowAfter)

  private def newInstr(instr: Instruction): Instruction = {
    instr.index = indexOfNextInstr

    if (instr.popCount > curStackSize)
      throw new IllegalArgumentException(s"Instruction '$instr' will result in a stack underflow")

    curStackSize += instr.stackDelta
    instructions += instr
    numLabelsToNextInstr = 0
    instr
  }

  private def newInstr(jumpingInstr: JumpingInstruction): JumpingInstruction = {
    newInstr(jumpingInstr: Instruction)
    use(jumpingInstr.targetLabel)
    jumpingInstr
  }

  private def use(label: Label): Unit = {
    val targetStackSize = stackSizeAtLabel.getOrElseUpdate(label, curStackSize)

    if (curStackSize != targetStackSize) {
      throw new IllegalStateException(s"When jumping to label $label, expected stack size $targetStackSize but current stack size is $curStackSize")
    }
  }

  def jumpTo(target: BuildLabel): this.type = {
    newInstr(new Jump(target))
    this
  }

  def pushAny(): this.type = push(DfValue.any)
  def pushUnit(): this.type = push(DfValue.unit)
  def pushNull(): this.type = push(???)

  def push(value: DfEntity): this.type = {
    newInstr(new Push(value))
    this
  }

  def pop(): this.type = {
    newInstr(new Pop)
    this
  }

  def dup(times: Int = 1): this.type = {
    if (times < 0)
      throw new IllegalArgumentException("Tried to duplicate negative times, which probably indicates an error")

    if (times >= 1) {
      newInstr(new Dup(times))
    }
    this
  }

  def assign(): this.type = {
    newInstr(new Assign)
    this
  }

  def ret(): this.type = {
    newInstr(new Ret)
    this
  }

  def bindLabel(label: BuildLabel): this.type = {
    if (label.isBound)
      throw new IllegalArgumentException(s"Cannot bind bound label $label")
    if (!unboundLabels.contains(label))
      throw new IllegalArgumentException(s"Label $label belongs to another builder")

    val expectedStackSize = stackSizeAtLabel.getOrElseUpdate(label, curStackSize)
    if (expectedStackSize != curStackSize) {
      throw new IllegalArgumentException(s"Cannot bind label $label to stack size $curStackSize, because label expected stack size $expectedStackSize")
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
    if (unboundLabels.nonEmpty) {
      throw new IllegalStateException(s"Cannot build cfg with ${unboundLabels.size} unbound labels: ${unboundLabels.mkString(", ")}")
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