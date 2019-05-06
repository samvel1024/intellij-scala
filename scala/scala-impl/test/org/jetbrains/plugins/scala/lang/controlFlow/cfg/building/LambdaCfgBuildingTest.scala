package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class LambdaCfgBuildingTest extends CfgBuildingTestBase {
  def test_lambda(): Unit = {
    check(
      """
        |val f = () => ()
        |f()
      """.stripMargin,
      """
        |f = lambda()
        |%0 <- f
        |call [%0]() scala.Function0.apply
        |end
        |
        |# lambda()
        |ret unit
      """.stripMargin
    )
  }

  def test_lambda_from_underscore(): Unit = {
    check(
      """
        |val f: Int => Int = _ + 10
        |f(5)
      """.stripMargin,
      """
        |f = lambda(p$0: Int)
        |%0 <- f
        |call [%0](5) scala.Function1.apply
        |end
        |
        |# lambda(p$0: Int)
        |%0 <- p$0
        |%1 <- call [%0](10) +
        |ret %1
      """.stripMargin
    )
  }
}
