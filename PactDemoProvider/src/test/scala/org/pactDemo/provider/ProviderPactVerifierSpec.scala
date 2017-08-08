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
      .withPactSource(loadFromLocal("target/pacts/Android_RawProvider.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

  it should "Meet its pacts with IOS" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/Ios_RawProvider.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

}