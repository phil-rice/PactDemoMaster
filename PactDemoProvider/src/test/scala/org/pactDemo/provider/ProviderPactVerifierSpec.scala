package org.pactDemo.provider

import com.itv.scalapact.ScalaPactVerify._
import com.twitter.finatra.http.EmbeddedHttpServer
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, FinatraServer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class ProviderPactVerifierSpec extends FinatraControllerSpec {

  def controllerUnderTest = new ProviderController(new AuthenticationService)


//  behavior of "Provider"

  it should "Meet its pacts with Android" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/CustomerAndroid_Provider.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

  it should "Meet its pacts with IOS" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/CustomerIos_Provider.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

  it should "Meet its temp pacts with Android" in {
    verifyPact
      .withPactSource(loadFromLocal("tmp/pacts/android.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

  it should "Meet its temp pacts with IOS" in {
    verifyPact
      .withPactSource(loadFromLocal("tmp/pacts/ios.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

  /*var url  = "https://hcl.pact.dius.com.au"
  val vtc = VerifyTargetConfig("https","hlHPfcw9xLlKOh6d31ZFeL37tMxk1mW:EeiQA3xoTY3fEfjf94O36zaWXsrnaGZ@hcl.pact.dius.com.au",443,10)

  it should "Meet its pacts with Android" in {
    verifyPact

      //.withPactSource(pactBroker(url,"Provider", List("Android")))
      .withPactSource(pactBrokerWithVersion(url,"1.0.0","Provider", List("Android")))

      .noSetupRequired // We did the setup in the beforeAll() function
      //.runVerificationAgainst("localhost", server.httpExternalPort)

      .runVerificationAgainst(vtc)
  }

  it should "Meet its pacts with IOS" in {
    verifyPact
      .withPactSource(pactBrokerWithVersion(url,"1.0.0","Provider", List("Ios")))

      .noSetupRequired // We did the setup in the beforeAll() function
      //.runVerificationAgainst("localhost", server.httpExternalPort)

      .runVerificationAgainst(vtc)
  }*/




}