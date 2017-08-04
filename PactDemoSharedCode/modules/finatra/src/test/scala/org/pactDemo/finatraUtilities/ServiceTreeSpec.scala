package org.pactDemo.finatraUtilities

import org.pactDemo.utilities.{NullLogMe, PactDemoSpec}

class ServiceTreeSpec extends PactDemoSpec with ServiceLanguage {
  implicit val log = NullLogMe

  behavior of "ServiceCreator"
  it should "allow services to be built and then walked" in {
    val d = http("localhost:80") >--< logging("someName", "somePrefix") >--< addHostName("someHost")
    d.walk[String, String](_.description, "", _ + _) shouldBe "AddHostNameService(someHost)LoggingClient(someName,somePrefix)FinagleHttp(localhost:80)"
  }
}
