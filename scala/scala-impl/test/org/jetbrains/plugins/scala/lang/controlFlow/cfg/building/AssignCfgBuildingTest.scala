package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class AssignCfgBuildingTest extends CfgBuildingTestBase {

/*
  todo: implement this
  def test_assign_on_variable(): Unit = {
    check(
      """
        |var test = 32
        |test = 44
      """.stripMargin,
      """
        |test = 32
        |test = 44
        |end
      """.stripMargin
    )
  }
*/
  /*
  todo: implement this
  def test_assign_on_member_variable(): Unit = {
    check(
      """
        |object Test {
        |  var test = 0
        |}
        |Test.test = 99
      """.stripMargin,
      """
        |write [Test] test <- 99
        |end
      """.stripMargin
    )
  }*/

  /*
  todo: implement this
  def test_assign_on_property(): Unit = {
    check(
      """
        |object Test {
        |  def prop = 3
        |  def prop_=(i: Int) = i
        |}
        |Test.prop = 42
      """.stripMargin,
      """
        |%0 <- Test$
        |call [%0](42) Test.prop_$eq
        |end
      """.stripMargin
    )
  }*/

  def test_assign_on_indexer(): Unit = {
    check(
      """
        |object Test {
        |  def apply(idx: Int) = 3
        |  def update(idx: Int, value: Int) = value
        |}
        |val idx = 88
        |Test(idx) = 99
      """.stripMargin,
      """
        |idx = 88
        |%0 <- Test$
        |%1 <- idx
        |call [%0](%1, 99) Test$.update
        |end
      """.stripMargin
    )
  }
}
