package org.jetbrains.plugins.scala.annotator

class FunctionLiteralParameterTest extends ScalaHighlightingTestBase {
  def testFunctionLiteralParameterTypeMismath(): Unit = {
    val code =
      """
        |object Test {
        |  trait Bar
        |  def foo(fn: String => Bar): Unit = {
        |    fn(123)
        |    fn(12, 45)
        |  }
        |}
        |""".stripMargin

    assertMessages(errorsFromScalaCode(code))(
      Error("123", "Type mismatch, expected: String, actual: Int"),
      Error("45", "Too many arguments for method apply(T1)")
    )
  }
}
