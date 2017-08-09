package org.pactDemo.ios

import com.twitter.finagle.http.Status
import com.twitter.inject.server.FeatureTestMixin
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, NullSl4jLoggingAdapter}
import org.scalatest.BeforeAndAfter

class IosProviderSpec extends FinatraControllerSpec with FeatureTestMixin with BeforeAndAfter {

  import org.pactDemo.mustache.Mustache._

  implicit val loggingAdapter = NullSl4jLoggingAdapter

  val fakeProvider = mock[IosProviderRequest => Future[IosAuthResponse]]

  def controllerUnderTest = new IosProvider(fakeProvider)

  before {
    reset(fakeProvider)
  }

  behavior of "IosProvider"
  it should "Make its response for IosProvider for Success scenario" in {
    when(fakeProvider.apply(IosProviderRequest(1, "valid"))) thenReturn Future.value(IosValidAuthResponse(1, "valid"))
    server.httpPost(
      path = "/token/id/1",
      postBody = """{"Authentication-token":"token valid"}""",
      andExpect = Status.Ok,
      withJsonBody = """{"id":"1","token":"valid"}"""
    )
  }

  it should "Make its response for IosProvider for Failure scenario" in {
    when(fakeProvider.apply(IosProviderRequest(2, "invalid"))) thenReturn Future.value(IosValidAuthResponse(2, "invalid"))
    server.httpPost(
      path = "/token/id/2",
      postBody = """{"Authentication-token":"token invalid"}""",
      andExpect = Status.Ok,
      withJsonBody = """{"token":"invalid","id":"2"}"""
    )
  }

}
