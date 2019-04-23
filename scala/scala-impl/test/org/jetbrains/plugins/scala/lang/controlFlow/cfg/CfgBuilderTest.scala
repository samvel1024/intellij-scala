package org.jetbrains.plugins.scala.lang.controlFlow.cfg

import org.jetbrains.plugins.scala.base.SimpleTestCase
import org.jetbrains.plugins.scala.lang.controlFlow.ExceptionAssert
import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuilder

class CfgBuilderTest extends SimpleTestCase with ExceptionAssert {

  import org.junit.Assert._

  def test_EmptyBuilder(): Unit = {
    val builder = new CfgBuilder

    assertExceptionMessage[IllegalStateException]("Cfg is not closed") {
      builder.build()
    }
  }

  /*
  def test_StackUnderflow(): Unit = {
    val builder = new CfgBuilder

    assertException[AssertionError] {
      builder.pop()
    }

    assertException[AssertionError] {
      builder.dup()
    }
  }*/

  def test_DifferentBuildersLabels(): Unit = {
    val builderA = new CfgBuilder
    val builderB = new CfgBuilder

    val labelFromBuilderA = builderA.createLabel("builderA")

    assertExceptionMessage[IllegalArgumentException](s"Label $labelFromBuilderA belongs to another builder") {
      builderB.bindLabel(labelFromBuilderA)
    }
  }


  def test_UnboundLabel(): Unit = {
    val builder = new CfgBuilder

    val label = builder.createLabel("notUsed")

    builder
      .jumpTo(label)
      .ret()

    assertExceptionMessage[IllegalStateException](s"Cannot build cfg with 1 unbound labels: $label") {
      builder.build()
    }
  }

  def test_UnusedLabel(): Unit = {
    val builder = new CfgBuilder

    val label = builder.createLabel("self")
    val unusedLabel = builder.createLabel("notUsed")

    builder
      .bindLabel(label)
      .jumpTo(label)
      .build()
  }

  def test_LabelAfterEnd(): Unit = {
    val builder = new CfgBuilder

    val label = builder.createLabel("afterEnd")

    builder.jumpTo(label)
    builder.bindLabel(label)

    assertExceptionMessage[IllegalStateException]("Cannot build cfg with labels pointing after its end") {
      builder.build()
    }
  }

  def test_doubleBoundLabel(): Unit = {
    val builder = new CfgBuilder

    val label = builder.createLabel("onlyOnce")

    builder
      .bindLabel(label)
        .noop(builder.any)

    assertExceptionMessage[IllegalArgumentException]("Cannot bind bound label .LonlyOnce[0]") {
      builder.bindLabel(label)
    }
  }

  def test_WrongStackSizeForForwardLabel(): Unit = {
    val builder = new CfgBuilder

    val label = builder.createLabel("later")

    builder
      .jumpTo(label)
      .noop(builder.any)

    assertExceptionMessage[IllegalArgumentException]("Cannot bind label .Llater[<unbound>] to stack size 1, because label expected stack size 0") {
      builder.bindLabel(label)
    }
  }

  def test_WrongStackSizeForBackwardLabel(): Unit = {
    val builder = new CfgBuilder

    val label = builder.createLabel("begin")

    builder
      .bindLabel(label)
      .noop(builder.any)

    assertExceptionMessage[IllegalStateException]("When jumping to label .Lbegin[0], stack is different") {
      builder.jumpTo(label)
    }
  }

  def test_Loop(): Unit = {
    val builder = new CfgBuilder

    val label = builder.createLabel("loop")
    builder
      .bindLabel(label)
      .noop(builder.any)
      .jumpTo(label)

    val cfg = builder.build()
    assertEquals(1, cfg.instructionCount)
  }
}
