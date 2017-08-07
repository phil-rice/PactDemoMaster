package org.pactDemo.utilities

class StringsSpec extends PactDemoSpec {

  behavior of "Strings.removeStart"

  it should "throw an exception if the first word is not present" in {
    intercept[StartNotPresentException](Strings.removeStart("first")("someStuff")) shouldBe StartNotPresentException("first", "someStuff")
    intercept[StartNotPresentException](Strings.removeStart("first")("")) shouldBe StartNotPresentException("first", "")
  }
  it should "remove the first word if present" in {
    Strings.removeStart("start")("start") shouldBe ""
    Strings.removeStart("start")("start") shouldBe ""
    Strings.removeStart("start")("start value") shouldBe " value"
    Strings.removeStart("start ")("start value1 value2") shouldBe "value1 value2"
  }
  behavior of "Strings.lastSegmentof"

  it should "return the last segment " in {
    Strings.lastSegmentOf("") shouldBe ""
    Strings.lastSegmentOf("/") shouldBe ""
    Strings.lastSegmentOf("/one") shouldBe "one"
    Strings.lastSegmentOf("a/b/c/d/ef") shouldBe "ef"
    Strings.lastSegmentOf("a/b/c/d/") shouldBe "d"
  }

  "Strings.ellipse" should "return the string if the length is less than the number" in {
    Strings.ellipses(3)("") shouldBe ""
    Strings.ellipses(3)("abc") shouldBe "abc"
    Strings.ellipses(3)("abcd") shouldBe "abc.."
  }
}


