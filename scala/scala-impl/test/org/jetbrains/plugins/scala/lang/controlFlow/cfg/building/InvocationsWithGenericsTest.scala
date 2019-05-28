package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class InvocationsWithGenericsTest extends CfgBuildingTestBase {
  def test_generic_simple(): Unit = {
    check(
      """
        |def test[A](a: A) = a
        |
        |test(100)
      """.stripMargin,
      """
        |call (100) test
        |end
      """.stripMargin
    )
  }

  def test_generic_multi_args(): Unit = {
    check(
      """
        |def test[A](a: A)(b: A) = ()
        |
        |test(100, true)
      """.stripMargin,
      """
        |call (100, true) test
        |end
      """.stripMargin
    )
  }

  /*
  todo: implement auto tupeling
  def test_autotupeling(): Unit = {
    check(
      """
        |def test[A](a: A) = ()
        |
        |test(1, true)
      """.stripMargin,
      """
        |%0 <- call (1, true) tuple
        |call (%0) test
      """.stripMargin
    )
  }
  */

  /*
    todo: implement this
  def test_typeclass(): Unit = {

    check(
      """
        |class TyClass[T]
        |implicit def ctx: TyClass[Int] = ()
        |
        |def test[X: TyClass](x: X) = ()
        |
        |test(100)
      """.stripMargin,
      """
        |%0 <- call () ctx
        |call (100, %0) test
        |end
      """.stripMargin
    )
  }
   */
}
