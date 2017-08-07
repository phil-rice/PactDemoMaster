package org.pactDemo.android

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, FinatraServer, NullSl4jLoggingAdapter}
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FeatureSpec}

class AndroidProviderSpec extends FinatraControllerSpec with BeforeAndAfter {
  implicit val loggingAdapter = NullSl4jLoggingAdapter

  val fakeProvider = mock[IdAndToken => Future[IdTokenAndValid]]

  def controllerUnderTest = new AndroidProviderController(fakeProvider)

  before {
    reset(fakeProvider)
  }

  behavior of "IdAndToken => Future[IdTokenAndValid] service"

  it should "process successes" in {
    when(fakeProvider.apply(IdAndToken(1, "validToken"))) thenReturn Future.value(IdTokenAndValid(1, "validToken", true))
    server.httpPost(
      path = "/token/android/post",
      postBody = """{"id": 1, "token":"validToken"}""",
      andExpect = Status.Ok,
      withJsonBody = """{"id":1,"token":"validToken","valid":true}"""
    )
  }

  it should "process failures" in {
    when(fakeProvider.apply(IdAndToken(2, "invalidToken"))) thenReturn Future.value(IdTokenAndValid(2, "invalidToken", false))
    server.httpPost(
      path = "/token/android/post",
      postBody = """{"id": 2, "token":"invalidToken"}""",
      andExpect = Status.Ok,
      withJsonBody = """{"token":"invalidToken","id":2,"valid": false}"""
    )
  }

}
