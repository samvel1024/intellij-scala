package org.jetbrains.plugins.scala.lang
package completion
package postfix

/**
  * Created by Roman.Shein on 10.05.2016.
  */
class ScalaMatchPostfixTemplateTest extends PostfixTemplateTest {

  override def testPath(): String = super.testPath() + "match/"

  def testSimple(): Unit = doTest()

  def testInnerMatch(): Unit = doTest()

  def testInfixExpr(): Unit = doTest()

  def testInInfixExpr(): Unit = doTest()

  def testInnerMatchInfixExpr(): Unit = doTest()

  def testExhaustiveSealed(): Unit = doTest()

  def testExhaustiveJavaEnum(): Unit = doTest()

  //  def testExhaustiveScalaEnum(): Unit = doTest()

  //  def testExhaustiveScalaEnum2(): Unit = doTest()
}
