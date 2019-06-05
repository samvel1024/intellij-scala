package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class MultiArgInvocationCfgBuildingTest extends CfgBuildingTestBase {

  def test_multi_arglist(): Unit = {
    check(
      """
        |def test(aa: Int)(bb: Int) = ???
        |
        |test(3)(4)
      """.stripMargin,
      """
        |call (3)(4) test
        |end
      """.stripMargin
    )
  }

  def test_returned_lambda(): Unit = {
    check(
      """
        |def test(aa: Int): Int => Unit = ???
        |
        |test(3)(4)
      """.stripMargin,
      """
        |%0 <- call (3) test
        |call [%0](4) scala.Function1.apply
        |end
      """.stripMargin
    )
  }

  def test_returned_lambda_from_multiple_arglists(): Unit = {
    check(
      """
        |def test(aa: Int)(bb: Int): Int => Unit = ???
        |
        |test(3)(4)(5)
      """.stripMargin,
      """
        |%0 <- call (3)(4) test
        |call [%0](5) scala.Function1.apply
        |end
      """.stripMargin
    )
  }

  def test_unresolved_function(): Unit = {
    check(
      """
        |test(3)(4)
      """.stripMargin,
      """
        |call (3)(4) <unknown>
        |end
      """.stripMargin
    )
  }

  def test_wrongly_called_returned_lambda(): Unit = {
    check(
      """
        |def test(aa: Int): Int => Unit = ???
        |
        |test(3)(4, 5)(6)
      """.stripMargin,
      """
        |%0 <- call (3) test
        |%1 <- call [%0](4, 5) scala.Function1.apply
        |call [%1](6) <unknown>
        |end
      """.stripMargin
    )
  }

  def test_unresolved_lambda_call(): Unit = {
    check(
      """
        |def test(aa: Int): Int = 0
        |
        |test(3)(4, 5)(6)
      """.stripMargin,
      """
        |%0 <- call (3) test
        |call [%0](4, 5)(6) <unknown>
        |end
      """.stripMargin
    )
  }

  def test_lambda_call_from_def(): Unit = {
    check(
      """
        |def test: Int => Int = ???
        |
        |test(3)
      """.stripMargin,
      """
        |%0 <- call () test
        |call [%0](3) scala.Function1.apply
        |end
      """.stripMargin
    )
  }

  def test_default_args_ordering_in_multi_args(): Unit = {
    check(
      """
        |val a, b, arg1 = 0
        |def test(aa: Int = a)(bb: Int = b): Unit = ()
        |
        |test()(arg1)
        |""".stripMargin,
      """
        |a = 0
        |b = 0
        |arg1 = 0
        |%0 <- a
        |%1 <- arg1
        |call (%0)(%1) test
        |end
        |""".stripMargin
    )
  }

  def test_multiarglist_with_infix(): Unit = {
    check(
      """
        |val arg1, arg2 = 0
        |object Test {
        |  def ++ (i: Int)(j: Int) = ()
        |}
        |(Test ++ arg1)(arg2)
        |""".stripMargin,
      """
        |arg1 = 0
        |arg2 = 0
        |%0 <- Test$
        |%1 <- arg1
        |%2 <- arg2
        |call [%0](%1)(%2) Test$.$plus$plus
        |end
        |""".stripMargin
    )
  }

  def test_multiarglist_with_right_assoc_infix(): Unit = {
    check(
      """
        |val arg1, arg2 = 0
        |object Test {
        |  def -: (i: Int)(j: Int) = ()
        |}
        |(arg1 -: Test)(arg2)
        |""".stripMargin,
      """
        |arg1 = 0
        |arg2 = 0
        |%0 <- arg1
        |%1 <- Test$
        |%2 <- arg2
        |call [%1](%0)(%2) Test$.$minus$colon
        |end
        |""".stripMargin
    )
  }
}
