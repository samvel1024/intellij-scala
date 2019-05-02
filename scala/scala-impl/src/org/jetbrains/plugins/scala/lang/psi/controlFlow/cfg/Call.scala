package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import com.intellij.psi.{PsiMethod, PsiNamedElement}
import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.Call._

class Call private[controlFlow](val thisRef: Option[DfEntity],
                                val func: Option[PsiNamedElement],
                                val ret: Option[DfVariable],
                                val params: Seq[DfEntity],
                                val dyn: Boolean) extends Instruction {


  override def sourceEntities: Seq[DfEntity] = params
  override def variables: Seq[DfVariable] = ret.toSeq

  override def asmString: String = {
    val builder = new StringBuilder

    ret.foreach { ret =>
      builder.append(Instruction.asmAssignmentPrefix(ret))
    }

    if (dyn)
      builder.append("dyn")

    builder.append("call ")

    thisRef.foreach { thisRef =>
      builder.append("[")
      builder.append(thisRef)
      builder.append("]")
    }

    builder.append(params.mkString("(", ", ", ")"))

    builder.append(" ")
    builder.append(func.flatMap(getQualifiedName).getOrElse("<unknown>"))

    builder.toString()
  }

  override def info: Instruction.Info = Jump
  override def accept(visitor: AbstractInstructionVisitor): Unit = visitor.visitCall(this)
}

object Call extends Instruction.Info(
  name = "Call",
  hasControlFlowAfter = true
) {
  private def getQualifiedName(func: PsiNamedElement): Option[String] = func match {
    case method: PsiMethod =>
      Option(method.getName).map(Option(method.getContainingClass).map(_.getQualifiedName + ".").getOrElse("") + _)
    case _ =>
      Option(func.getName)
  }
}