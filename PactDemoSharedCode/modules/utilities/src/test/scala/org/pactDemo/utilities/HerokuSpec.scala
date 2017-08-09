package org.pactDemo.utilities

class HerokuSpec extends PactDemoSpec {

  behavior of "Heroku"

  it should "use the provider in the environment if that is set" in {
    implicit object GetEnv extends EnvStringGetter {
      override def getEnv(name: String): Option[String] = Map("PORT" -> "333", "provider" -> "someprovider:someport").get(name)
    }

    Heroku.port(123) shouldBe 333
    Heroku.providerHostAndPort shouldBe "someprovider:someport"
  }
  it should "use the PORT in the parameter if that is set" in {
    implicit object GetEnv extends EnvStringGetter {
      override def getEnv(name: String): Option[String] = None
    }

    Heroku.port(123) shouldBe 123
    Heroku.providerHostAndPort shouldBe "localhost:9000"
  }
}
