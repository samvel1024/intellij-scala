package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class ExprCfgBuildingTest extends CfgBuildingTestBase {

  def test_unit(): Unit = {
    check(
      "()",
      """
        |noop unit
        |end
      """.stripMargin
    )

    check(
      "val a = ()",
      """
        |a = unit
        |end
      """.stripMargin
    )
  }

  def test_if_with_else(): Unit = {
    check(
      """
        |val a = 0
        |if (a) {
        |  "then"
        |} else {
        |  "else"
        |}
      """.stripMargin,
      """
        |a = 0
        |if! a -> .Lelse[5]
        |noop "then"
        |jmp .LendIf[6]
        |.Lelse[5]:
        |noop "else"
        |.LendIf[6]:
        |end
      """.stripMargin
    )

    check(
      """
        |val a = if (true) "then" else "else"
      """.stripMargin,
      """
        |if! true -> .Lelse[4]
        |a = "then"
        |jmp .LendIf[5]
        |.Lelse[4]:
        |a = "else"
        |.LendIf[5]:
        |end
      """.stripMargin
    )
  }

  def test_if_without_else(): Unit = {
    check(
      """
        |if (true) {
        |  "then"
        |}
      """.stripMargin,
      """
        |if! true -> .LendIf[3]
        |noop "then"
        |.LendIf[3]:
        |end
      """.stripMargin
    )

    check(
      """
        |val a = if (true) "then"
      """.stripMargin,
      """
        |if! true -> .Lelse[4]
        |a = "then"
        |jmp .LendIf[5]
        |.Lelse[4]:
        |a = unit
        |.LendIf[5]:
        |end
      """.stripMargin
    )
  }

  def test_do_while(): Unit = {
    check(
      """
        |val b = true
        |do {
        |  "inner"
        |} while (b)
        |"end"
      """.stripMargin,
      """
        |b = true
        |.LdoLoop[2]:
        |noop "inner"
        |if b -> .LdoLoop[2]
        |noop "end"
        |end
      """.stripMargin
    )
  }

  def test_while(): Unit = {
    check(
      """
        |val b = true
        |while (b) {
        |  "inner"
        |}
      """.stripMargin,
      """
        |b = true
        |.LwhileLoop[2]:
        |if! b -> .LwhileExit[5]
        |noop "inner"
        |jmp .LwhileLoop[2]
        |.LwhileExit[5]:
        |end
      """.stripMargin
    )
  }

  def test_block(): Unit = {
    check(
      """
        |val a = 1
        |val c = {
        |  val b = 2
        |  b
        |}
      """.stripMargin,
      """
        |a = 1
        |b = 2
        |c = b
        |end
      """.stripMargin
    )
  }

  def test_match_with_result(): Unit = {
    check(
      """
        |val x = 42 match {
        |  case a => 10
        |  case b => 11
        |}
        |""".stripMargin,
      """
        |%0 <- 42
        |a = %0
        |x = 10
        |jmp .LendCaseClause[7]
        |b = %0
        |x = 11
        |.LendCaseClause[7]:
        |end
        |""".stripMargin
    )
  }

  def test_match_without_result(): Unit = {
    check(
      """
        |42 match {
        |  case a => 10
        |  case b => 11
        |}
        |""".stripMargin,
      """
        |%0 <- 42
        |a = %0
        |noop 10
        |jmp .LendCaseClause[7]
        |b = %0
        |noop 11
        |.LendCaseClause[7]:
        |end
        |""".stripMargin
    )
  }

  def test_match_with_guard(): Unit = {
    check(
      """
        |42 match {
        |  case a if a == 0 => 10
        |}
        |""".stripMargin,
      """
        |%0 <- 42
        |a = %0
        |%1 <- a
        |%2 <- call [%1](0) ==
        |if! %2 -> .LcaseFail[8]
        |noop 10
        |jmp .LendCaseClause[9]
        |.LcaseFail[8]:
        |throw Abstr[_root_.scala.MatchError]
        |.LendCaseClause[9]:
        |end
        |""".stripMargin
    )
  }

  def test_throw(): Unit = {
    check(
      """
        |throw new AssertionError
        |""".stripMargin,
      """
        |%0 <- new AssertionError
        |%1 <- call [%0]() java.lang.AssertionError.constructor
        |throw %1
        |end
        |""".stripMargin
    )
  }


  /*
  todo: implement this
  def test_property_access(): Unit = {
    check(
      """
        |object Test {
        |  val test = 0
        |}
        |val a = Test.test
      """.stripMargin,
      """
        |a <- read [Test] test
        |end
      """.stripMargin
    )
  }*/
}
