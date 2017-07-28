package org.pactDemo.provider

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.finatraUtilities.FinatraServer
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class ProviderFeatureSpec extends PactDemoSpec with BeforeAndAfterAll with BeforeAndAfter {

  val fakeAuthentication = mock[AuthenticationRequest => Future[AuthenticationResult]]
  val server = new EmbeddedHttpServer(new FinatraServer(0, new ProviderController(fakeAuthentication))) //the port is ignored

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  override def afterAll(): Unit = {
    server.close()
    super.afterAll()
  }

  before {
    reset(fakeAuthentication)
  }

  behavior of "IosProvider"
  it should "Meet its response for IosProvider for Success scenario" in {
    when(fakeAuthentication.apply(AuthenticationRequest(1, "validToken"))) thenReturn Future.value(ValidResponse(1, "validToken"))
    server.httpPost(
      path = "/token/id/1",
      postBody = """{"Authentication-token":"token validToken"}""",
      andExpect = Status.NotFound,
      withBody = """"""
    )
  }

  it should "Meet its response for IosProvider for Failure scenario" in {
    when(fakeAuthentication.apply(AuthenticationRequest(2, "invalidToken"))) thenReturn Future.value(InvalidResponse("invalidToken"))
    server.httpPost(
      path = "/token/android/post",
      postBody = """{"Authentication-token":"token invalidToken"}""",
      andExpect = Status.Ok,
      withJsonBody = """"""
    )
  }

}
