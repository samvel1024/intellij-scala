package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class PatternCfgBuildingTest extends CfgBuildingTestBase {
  def test_val(): Unit = {
    check(
      """
        |val a = 5
      """.stripMargin,
      """
        |a = 5
        |end
      """.stripMargin
    )
  }

  def test_multiple_val(): Unit = {
    check(
      """
        |val a, b = 1
      """.stripMargin,
      """
        |a = 1
        |b = 1
        |end
      """.stripMargin
    )
  }

  def test_var(): Unit = {
    check(
      """
        |var a = 5
      """.stripMargin,
      """
        |a = 5
        |end
      """.stripMargin
    )
  }

  def test_multiple_var(): Unit = {
    check(
      """
        |var a, b = 1
      """.stripMargin,
      """
        |a = 1
        |b = 1
        |end
      """.stripMargin
    )
  }
}
