package org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg

import com.intellij.psi.{PsiElement, PsiMethod, PsiNamedElement}
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.dfa.{DfEntity, DfVariable}
import org.jetbrains.plugins.scala.lang.psi.api.base.{AuxiliaryConstructor, Constructor}
import org.jetbrains.plugins.scala.lang.psi.controlFlow.AbstractInstructionVisitor
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.Call._

class Call private[controlFlow](val thisRef: Option[DfEntity],
                                val func: Option[PsiElement],
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
  private def getQualifiedName(func: PsiElement): Option[String] = func match {
    case Constructor.ofClass(clazz) =>
      Some(clazz.getQualifiedName.toOption.getOrElse(clazz.name) + ".constructor")
    case method: PsiMethod =>
      method.getName.toOption.map(method.getContainingClass.toOption.flatMap(_.getQualifiedName.toOption).map(_ + ".").getOrElse("") + _)
    case func: PsiNamedElement =>
      func.getName.toOption
  }
}