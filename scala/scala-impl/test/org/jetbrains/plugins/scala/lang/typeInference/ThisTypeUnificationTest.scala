package org.jetbrains.plugins.scala.lang.typeInference

import org.jetbrains.plugins.scala.PerfCycleTests
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.junit.experimental.categories.Category

@Category(Array(classOf[PerfCycleTests]))
class ThisTypeUnificationTest extends ScalaLightCodeInsightFixtureTestAdapter {
  def testSCL15607(): Unit = checkTextHasNoErrors(
    """
      |object Test {
      | trait Ev[T]
      | implicit def f[T](t: T)(implicit ev: Ev[T]): Int = 123
      |
      | class Foo {
      |   implicit val evFoo: Ev[Foo] = ???
      |
      |   this./(123)
      | }
      |}
      |""".stripMargin
  )
}
