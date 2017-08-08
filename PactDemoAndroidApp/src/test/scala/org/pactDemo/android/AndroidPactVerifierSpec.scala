package org.pactDemo.android


import com.itv.scalapact.ScalaPactVerify._
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.util.Future
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, FinatraServer, NullSl4jLoggingAdapter}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class AndroidPactVerifierSpec extends FinatraControllerSpec {
  implicit val loggingAdapter = NullSl4jLoggingAdapter

  import org.pactDemo.mustache.Mustache._

  class FakeAuthenticationService extends (IdAndToken => Future[IdTokenAndValid]) {
    override def apply(v1: IdAndToken): Future[IdTokenAndValid] = {
      Future.value(if (v1.token.contains("invalid")) IdTokenAndValid(v1.id, v1.token, true) else IdTokenAndValid(v1.id, v1.token, false))
    }
  }

  def controllerUnderTest = new AndroidProviderController(new FakeAuthenticationService)


  //  behavior of "Provider"

  it should "Meet its pacts with AkkaActorClient" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/AkkaActorClient_Android.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

  it should "Meet its pacts with JavaConsumer" in {
    verifyPact
      .withPactSource(loadFromLocal("target/pacts/JavaConsumer_Android.json"))
      .noSetupRequired // We did the setup in the beforeAll() function
      .runVerificationAgainst("localhost", server.httpExternalPort)
  }

}