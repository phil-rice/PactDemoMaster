package org.pactDemo.provider

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.finatra.FinatraControllerSpec
import org.scalatest.BeforeAndAfter

class ProviderFeatureSpec extends FinatraControllerSpec with BeforeAndAfter {

  lazy val fakeAuthentication = mock[AuthenticationRequest => Future[AuthenticationResult]]

  override def controllerUnderTest: Controller = new ProviderController(fakeAuthentication)

  before {
    reset(fakeAuthentication)
  }

  behavior of "Provider"

  it should "Meet its response  for Success scenario" in {
    when(fakeAuthentication.apply(AuthenticationRequest(1, "validToken"))) thenReturn Future.value(ValidResponse(1, "validToken"))
    server.httpPost(
      path = "/token/id/1",
      postBody = """{"Authentication-token":"token validToken"}""",
      andExpect = Status.Ok,
      withBody = """{"token":"validToken","id":"1", "valid": true}"""
    )
  }

  it should "Meet its response for Failure scenario" in {
    when(fakeAuthentication.apply(AuthenticationRequest(2, "invalidToken"))) thenReturn Future.value(InvalidResponse("invalidToken"))
    server.httpPost(
      path = "/token/id/2",
      postBody = """{"Authentication-token":"token invalidToken"}""",
      andExpect = Status.Unauthorized,
      withJsonBody = """Unauthorized token invalidToken"""
    )
  }

}
