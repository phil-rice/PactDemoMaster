package org.pactDemo.provider

import com.itv.scalapact.ScalaPactVerify._
import com.twitter.finatra.http.EmbeddedHttpServer
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, FinatraServer}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class ProviderPactVerifierSpec extends FinatraControllerSpec {

  def controllerUnderTest = new ProviderController(new AuthenticationService)


  behavior of "Provider"

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

  it should "Meet its pacts with Angular" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/AngularIOConsumer_Provider.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }
}