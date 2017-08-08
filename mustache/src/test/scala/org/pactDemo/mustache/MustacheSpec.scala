package org.pactDemo.mustache

import org.pactDemo.finatraUtilities.TemplateItem
import org.pactDemo.utilities.PactDemoSpec


class MustacheSpec extends PactDemoSpec{

  behavior of "Mustache"

  val map = TemplateItem(Map("name" -> "someName", "value" -> "someValue"))
  it should "have a fromString method" in {
    Mustache("test.mustache")(map) shouldBe "Name someName Value someValue"
  }
}
