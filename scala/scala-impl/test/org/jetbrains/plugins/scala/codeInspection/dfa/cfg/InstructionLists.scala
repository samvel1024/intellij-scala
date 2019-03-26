package org.jetbrains.plugins.scala.codeInspection.dfa.cfg

import scala.reflect.runtime.universe._

object InstructionLists {

  private def bundle[T: TypeTag](info: Instruction.Info) =
    (typeOf[T], info)

  val allInstructions: Seq[(Type, Instruction.Info)] = Seq(
    bundle[Push](Push),
    bundle[Pop](Pop),
    bundle[Dup](Dup),
    bundle[Assign](Assign),
    bundle[Jump](Jump),
    bundle[Ret](Ret)
  )
}
