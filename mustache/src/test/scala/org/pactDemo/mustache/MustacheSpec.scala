package org.pactDemo.mustache

import org.pactDemo.utilities.PactDemoSpec


class MustacheSpec extends PactDemoSpec{

  behavior of "Mustache"

  val map = Map("name" -> "someName", "value" -> "someValue")
  it should "have a fromString method" in {
    Mustache("test")(map) shouldBe "hello"

  }
}
