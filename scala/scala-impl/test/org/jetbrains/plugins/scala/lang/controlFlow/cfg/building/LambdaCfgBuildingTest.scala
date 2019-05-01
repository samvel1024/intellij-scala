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
        |call () scala.Function0.apply
        |end
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
        |f = lambda(Int)
        |call (5) scala.Function1.apply
        |end
      """.stripMargin
    )
  }
}
