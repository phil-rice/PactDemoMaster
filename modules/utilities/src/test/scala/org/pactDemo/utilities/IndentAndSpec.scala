package org.pactDemo.utilities

class IndentAndSpec extends PactDemoSpec {

  behavior of "IndentAndString[String]"

  it should "have a asString method" in {
    IndentAnd[String](0, "").asString() shouldBe ""
    IndentAnd[String](0, "").asString('.') shouldBe ""
    IndentAnd[String](0, "abc").asString() shouldBe "abc"
    IndentAnd[String](0, "abc").asString('.') shouldBe "abc"
    IndentAnd[String](3, "abc").asString() shouldBe "   abc"
    IndentAnd[String](3, "abc").asString('.') shouldBe "...abc"
  }

  it should "explode if -ve indent" in {
    intercept[IllegalArgumentException](IndentAnd[String](-1, "someString")).getMessage shouldBe "Cannot have -ve indent for -1/someString"
  }

  it should "have an indent method" in {
    IndentAnd[String](0, "abc").indent("newString") shouldBe IndentAnd[String](1, "newString")
    IndentAnd[String](3, "abc").indent("newString") shouldBe IndentAnd[String](4, "newString")
  }

    val list = List(
      IndentAnd[String](0, "a"),
      IndentAnd[String](1, "b"),
      IndentAnd[String](2, "c"))

  it should "be able to turn a list of indent and strings into a string" in {

    list.asString(",") shouldBe  "a, b,  c"
    list.asString(",", '.') shouldBe  "a,.b,..c"
  }

  it should "be able to map over a list of indent and strings" in{
    list.mapContents{ s: String=> s+"x"} shouldBe List(
      IndentAnd[String](0, "ax"),
      IndentAnd[String](1, "bx"),
      IndentAnd[String](2, "cx"))
  }
}
