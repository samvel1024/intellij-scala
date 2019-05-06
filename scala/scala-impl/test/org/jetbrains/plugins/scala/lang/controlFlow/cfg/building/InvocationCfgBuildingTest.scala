package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class InvocationCfgBuildingTest extends CfgBuildingTestBase {

  def test_simple_call(): Unit = {
    check(
      """
        |def test(): Unit = ()
        |test()
      """.stripMargin,
      """
        |call () test
        |end
      """.stripMargin
    )
  }

  def test_simple_call_withResult(): Unit = {
    check(
      """
        |def test(): Unit = ()
        |val a = test()
      """.stripMargin,
      """
        |a = call () test
        |end
      """.stripMargin
    )
  }

  def test_call_to_object_method(): Unit = {
    check(
      """
        |object Test {
        |  def test(): Unit = ()
        |}
        |Test.test()
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0]() Test$.test
        |end
      """.stripMargin
    )
  }

  def test_call_to_renamed_object_method(): Unit = {
    check(
      """
        |object Test {
        |  def test(): Unit = ()
        |}
        |val a = Test
        |a.test()
      """.stripMargin,
      """
        |a = Test$
        |%0 <- a
        |call [%0]() Test$.test
        |end
      """.stripMargin
    )
  }

  def test_simple_args(): Unit = {
    check(
      """
        |def test(a: Int, b: String): Unit = ()
        |val a = 5
        |test(a, "test")
      """.stripMargin,
      """
        |a = 5
        |%0 <- a
        |call (%0, "test") test
        |end
      """.stripMargin
    )
  }

  def test_named_args(): Unit = {
    check(
      """
        |def test(aa: Int, bb: Int): Unit = ()
        |
        |val a = 1
        |val b = 2
        |
        |test(a, bb = b)
      """.stripMargin,
      """
        |a = 1
        |b = 2
        |%0 <- a
        |%1 <- b
        |call (%0, %1) test
        |end
      """.stripMargin
    )
  }

  def test_reordered_named_args(): Unit = {
    check(
      """
        |def test(aa: Int, bb: Int): Unit = ()
        |
        |val a = 1
        |val b = 2
        |
        |test(bb = a, aa = b)
      """.stripMargin,
      """
        |a = 1
        |b = 2
        |%0 <- a
        |%1 <- b
        |call (%1, %0) test
        |end
      """.stripMargin
    )
  }

  def test_non_context_default_args(): Unit = {
    check(
      """
        |def test(aa: Int = 5): Unit = ()
        |test()
      """.stripMargin,
      """
        |call (5) test
        |end
      """.stripMargin
    )
  }

  def test_context_default_args(): Unit = {
    check(
      """
        |object Test {
        |  val default = 5
        |  def test(aa: Int = default): Unit = ()
        |}
        |Test.test()
      """.stripMargin,
      """
        |%0 <- Test$
        |%1 <- default
        |call [%0](%1) Test$.test
        |end
      """.stripMargin
    )
  }

  def test_context_default_args_ordering(): Unit = {
    check(
      """
        |val a, b, c, arg1, arg2 = 0
        |def test(aa: Int = a, bb: Int = b, cc: Int = c): Unit = ()
        |
        |// actually supplied arguments have to be evaluated before default parameters
        |test(arg1, cc = arg2)
      """.stripMargin,
      """
        |a = 0
        |b = 0
        |c = 0
        |arg1 = 0
        |arg2 = 0
        |%0 <- arg1
        |%1 <- arg2
        |%2 <- b
        |call (%0, %2, %1) test
        |end
      """.stripMargin
    )
  }

  def test_synthetic_apply(): Unit = {
    check(
      """
        |case class Test(aa: Int)
        |
        |Test(99)
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](99) Test$.apply
        |end
      """.stripMargin
    )
  }


  def test_apply(): Unit = {
    check(
      """
        |object Test {
        |  def apply(aa: Int): Unit = ()
        |}
        |Test(99)
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](99) Test$.apply
        |end
      """.stripMargin
    )
  }

  def test_explicit_apply(): Unit = {
    check(
      """
        |object Test {
        |  def apply(aa: Int): Unit = ()
        |}
        |Test(99)
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](99) Test$.apply
        |end
      """.stripMargin
    )
  }

  def test_explicit_synthetic_apply(): Unit = {
    check(
      """
        |case class Test(aa: Int)
        |
        |Test.apply(99)
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](99) Test$.apply
        |end
      """.stripMargin
    )
  }

  def test_update(): Unit = {
    check(
      """
        |object Test {
        |  def update(aa: Int, s: String): Unit = ()
        |}
        |Test(11) = ""
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](11, "") Test$.update
        |end
      """.stripMargin
    )
  }

  def test_update_with_multiple_args(): Unit = {
    check(
      """
        |object Test {
        |  def update(aa: Int, bb: Int, s: String): Unit = ()
        |}
        |Test(11, 21) = ""
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](11, 21, "") Test$.update
        |end
      """.stripMargin
    )
  }

  def test_explicit_update(): Unit = {
    check(
      """
        |object Test {
        |  def update(aa: Int, s: String): Unit = ()
        |}
        |Test.update(11, "")
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](11, "") Test$.update
        |end
      """.stripMargin
    )
  }
}
