package org.pactDemo.utilities

class StringsSpec extends PactDemoSpec {

  behavior of "Strings.removeFirstWordCheckingEquals"

  it should "throw an exception if the first word is not present" in {
    intercept[FirstWordNotPresent](Strings.removeFirstWordCheckingEquals("first")("someStuff")) shouldBe FirstWordNotPresent("first", "someStuff")
    intercept[FirstWordNotPresent](Strings.removeFirstWordCheckingEquals("first")("")) shouldBe FirstWordNotPresent("first", "")
  }
   it should "remove the first word if present" in {
     Strings.removeFirstWordCheckingEquals("start")("start") shouldBe ""
     Strings.removeFirstWordCheckingEquals("start")("start") shouldBe ""
     Strings.removeFirstWordCheckingEquals("start")("start value") shouldBe "value"
     Strings.removeFirstWordCheckingEquals("start")("start value1 value2") shouldBe "value1 value2"
   }

}


