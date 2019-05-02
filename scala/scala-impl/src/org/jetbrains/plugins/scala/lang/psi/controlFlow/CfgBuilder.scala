package org.jetbrains.plugins.scala.lang.psi.controlFlow

import com.intellij.psi.{PsiElement, PsiNamedElement}
import org.jetbrains.plugins.scala.dfa._
import org.jetbrains.plugins.scala.lang.psi.api.expr.ScUnderScoreSectionUtil.UnderscoreMap
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScUnderscoreSection}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder._
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg._
import org.jetbrains.plugins.scala.project.ProjectContext

import scala.collection.mutable

class CfgBuilder private(val underscoreExpressions: Map[ScExpression, Seq[DfConcreteLambdaRef.Parameter]],
                         implicit val projectContext: ProjectContext,
                         _make_different_from_alternative_constructor: Any = None) {
  def this()(implicit projectContext: ProjectContext) {
    this(Map.empty, projectContext)
  }

  def this(_underscoreExpressions: UnderscoreMap)(implicit projectContext: ProjectContext) {
    this(_underscoreExpressions.mapValues(_.zipWithIndex.map((underscoreToParameter _).tupled)), projectContext)
  }

  val resolveUnderscore: Map[PsiElement, DfConcreteLambdaRef.Parameter] =
    underscoreExpressions.values.flatten.map(p => p.anchor -> p).toMap

  private var nextRegisterId = 0
  private val instructions = mutable.Buffer.empty[cfg.Instruction]
  private val unboundLabels = mutable.Set.empty[BuildLabel]
  private val boundLabels = mutable.Set.empty[BuildLabel]
  private val usedLabels = mutable.Set.empty[Label]
  private var numLabelsToNextInstr = 0
  private val stringLiteralCache = mutable.Map.empty[String, DfConcreteAnyRef]
  private val variableCache = mutable.Map.empty[PsiElement, DfVariable]

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
    use(jumpingInstr.targetLabel.asInstanceOf[BuildLabel])
    jumpingInstr
  }

  private def use(label: BuildLabel): Unit = {
    assert(unboundLabels.contains(label) || boundLabels.contains(label))
    usedLabels += label
  }

  def resolveVariable(anchor: PsiNamedElement): DfVariable =
    resolveVariable(anchor, anchor.getName)

  def resolveVariable(anchor: PsiElement, name: String): DfVariable =
    variableCache.getOrElseUpdate(anchor, DfLocalVariable(anchor, name))

  def mov(target: DfVariable, source: DfEntity): this.type = {
    if (target != source) {
      newInstr(new Mov(target, source))
    }
    this
  }

  def assign(target: PsiNamedElement, source: DfEntity): this.type =
    mov(resolveVariable(target), source)

  def pin(source: DfEntity): DfEntity = source match {
    case reg: DfRegister => reg
    case value: DfValue => value
    case nonReg =>
      val reg = newRegister()
      mov(reg, nonReg)
      reg
  }

  def newRegister(): DfRegister =
    new DfRegister(null, newRegisterId())

  def noop(entity: DfEntity): this.type = {
    if (!entity.isInstanceOf[DfRegister]) {
      newInstr(new Noop(entity))
    }
    this
  }

  def ret(entity: DfEntity = DfValue.unit): this.type = {
    newInstr(new Ret(entity))
    this
  }

  def end(): this.type = {
    newInstr(new End)
    this
  }

  def call(thisRef: Option[DfEntity], func: Option[PsiNamedElement], ret: Option[DfVariable], params: Seq[DfEntity]): this.type = {
    newInstr(new Call(thisRef, func, ret, params, false))
    this
  }

  def jumpTo(target: BuildLabel): this.type = {
    newInstr(new Jump(target))
    this
  }

  def jumpIfTrue(condition: DfEntity, target: BuildLabel): this.type = {
    newInstr(new JumpIf(condition, target))
    this
  }

  def jumpIfFalse(condition: DfEntity, target: BuildLabel): this.type = {
    newInstr(new JumpIfNot(condition, target))
    this
  }

  def `this`: DfEntity = ???

  def any: DfValue = DfValue.any
  def unit: DfValue = DfValue.unit
  def `null`: DfValue = ???
  def boolean(value: Boolean): DfValue = DfValue.boolean(value)
  def int(value: Int): DfValue = DfValue.int(value)
  def string(value: String): DfValue = stringLiteralCache.getOrElseUpdate(value, new DfConcreteStringRef(value))

  def bindLabel(label: BuildLabel): this.type = {
    if (label.isBound)
      throw new IllegalArgumentException(s"Cannot bind bound label $label")

    if (!unboundLabels.contains(label))
      throw new IllegalArgumentException(s"Label $label belongs to another builder")

    unboundLabels -= label
    boundLabels += label
    numLabelsToNextInstr += 1
    label._targetIndex = indexOfNextInstr
    this
  }

  def createLabel(name: String = ""): BuildLabel = {
    val label = new BuildLabel(name)
    unboundLabels += label
    label
  }

  def createSubBuilder(): CfgBuilder = new CfgBuilder(underscoreExpressions, projectContext)

  def build(): ControlFlowGraph = {
    val usedUnbound = usedLabels & unboundLabels.toSet[Label]
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
      val boundTo = if (isBound) line.toString else "<unbound>"
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

  private def underscoreToParameter(underscore: ScUnderscoreSection, idx: Int): DfConcreteLambdaRef.Parameter =
    new DfConcreteLambdaRef.Parameter(DfLocalVariable(underscore, "p$" + idx), underscore.`type`().getOrAny)
}
