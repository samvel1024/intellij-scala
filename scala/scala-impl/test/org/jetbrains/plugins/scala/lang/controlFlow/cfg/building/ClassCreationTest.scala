package org.jetbrains.plugins.scala.lang.controlFlow.cfg.building

class ClassCreationTest extends CfgBuildingTestBase {

  def test_simple_creation(): Unit = check(
    """
      |class Test
      |
      |new Test
    """.stripMargin,
    """
      |%0 <- new Test
      |call [%0]() Test.constructor
      |end
    """.stripMargin
  )

  def test_creation_with_empty_param_clause(): Unit = check(
    """
      |class Test()
      |
      |new Test()
    """.stripMargin,
    """
      |%0 <- new Test
      |call [%0]() Test.constructor
      |end
    """.stripMargin
  )

  def test_creation_with_arguments(): Unit = check(
    """
      |class Test(i: Int, j: Boolean)
      |
      |new Test(3, true)
    """.stripMargin,
    """
      |%0 <- new Test
      |call [%0](3, true) Test.constructor
      |end
    """.stripMargin
  )

  def test_creation_with_arguments_list(): Unit = check(
    """
      |class Test(i: Int, j: Boolean)(n: Int, m: Int)
      |
      |new Test(3, true)(4, 5)
    """.stripMargin,
    """
      |%0 <- new Test
      |call [%0](3, true)(4, 5) Test.constructor
      |end
    """.stripMargin
  )

  def test_creation_of_new_template(): Unit = check(
    """
      |class Test(i: Int)
      |
      |new Test(1) { def fun(): Int = 0 }
    """.stripMargin,
    """
      |%0 <- new Test {  def fun(): Int}
      |call [%0](1) Test.constructor
      |end
    """.stripMargin
  )
}
