package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class ForComprehensionCfgBuildingTest extends CfgBuildingTestBase {

  def test_simple_for(): Unit = check(
    """
      |for (x <- Some(1)) ()
    """.stripMargin,
    """
      |%0 <- Some$
      |%1 <- call [%0](1) scala.Some$.apply
      |call [%1](lambda(block$param: Int)) scala.Option.foreach
      |end
      |
      |# lambda(block$param: Int)
      |%0 <- block$param
      |x = %0
      |%1 <- unit
      |ret %1
    """.stripMargin
  )

}
