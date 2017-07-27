package org.pactDemo.utilities

class HerokuSpec extends PactDemoSpec{

  behavior of "Heroku"

  it should "use the PORT in the environment if that is set" in {
    implicit object GetEnv extends EnvStringGetter {
      override def getEnv(name: String): Option[String] =Some("333")
    }

    Heroku.port(123) shouldBe 333
    Heroku.providerHost shouldBe "pact-demo-provider.herokuapp.com"
    Heroku.providerPort shouldBe 80 //real world
  }
  it should "use the PORT in the parameter if that is set" in {
    implicit object GetEnv extends EnvStringGetter {
      override def getEnv(name: String): Option[String] = None
    }

    Heroku.port(123) shouldBe  123
    Heroku.providerHost shouldBe "localhost"
    Heroku.providerPort shouldBe 9000
  }
}
