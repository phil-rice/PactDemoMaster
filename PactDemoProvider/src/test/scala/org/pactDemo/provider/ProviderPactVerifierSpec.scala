package org.pactDemo.provider

import java.nio.file.Paths

import com.itv.scalapact.ScalaPactVerify._
import com.twitter.finatra.http.EmbeddedHttpServer
import org.pactDemo.utilities.FinatraServer
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class ProviderPactVerifierSpec extends FlatSpec with BeforeAndAfterAll {

  val server = new EmbeddedHttpServer(new FinatraServer(0, new ProviderController)) //the port is ignored

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  override def afterAll(): Unit = {
    server.close()
    super.afterAll()
  }

  behavior of "Provider"

  it should "Meet its pacts with Android" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/CustomeAndroid_Provider.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

  it should "Meet its pacts with IOS" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/CustomeIos_Provider.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }
}
