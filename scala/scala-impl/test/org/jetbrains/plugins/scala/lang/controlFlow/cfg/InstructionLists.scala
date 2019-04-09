package org.jetbrains.plugins.scala.lang.controlFlow.cfg

import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg._

import scala.reflect.runtime.universe._

object InstructionLists {

  private def bundle[T: TypeTag](info: Instruction.Info) =
    (typeOf[T], info)

  val allInstructions: Seq[(Type, Instruction.Info)] = Seq(
    bundle[Read](Read),
    bundle[Write](Write),
    bundle[Jump](Jump),
    bundle[JumpIf](JumpIf),
    bundle[JumpIfNot](JumpIfNot),
    bundle[Ret](Ret),
    bundle[End](End)
  )
}
