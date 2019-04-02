package org.jetbrains.plugins.scala.lang.controlFlow.cfg

import junit.framework.TestCase
import org.jetbrains.plugins.scala.lang.psi.controlFlow.cfg.JumpingInstruction

import scala.reflect.runtime.universe._

class InstructionInfoTest extends TestCase {
  import InstructionLists.allInstructions
  import org.junit.Assert._

  def test_InstructionInfo(): Unit = {
    for ((ty, info) <- allInstructions) {
      val extendsJumpingInstruction = ty <:< typeOf[JumpingInstruction]
      assertEquals(extendsJumpingInstruction, info.isJump)
    }
  }
}
