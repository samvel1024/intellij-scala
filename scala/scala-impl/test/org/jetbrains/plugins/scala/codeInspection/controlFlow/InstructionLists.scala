package org.jetbrains.plugins.scala.codeInspection.controlFlow

import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg._
import scala.reflect.runtime.universe._

object InstructionLists {

  private def bundle[T: TypeTag](info: Instruction.Info) =
    (typeOf[T], info)

  val allInstructions: Seq[(Type, Instruction.Info)] = Seq(
    bundle[Push](Push),
    bundle[Pop](Pop),
    bundle[Dup](Dup),
    bundle[cfg.Assign](cfg.Assign),
    bundle[Jump](Jump),
    bundle[Ret](Ret)
  )
}
