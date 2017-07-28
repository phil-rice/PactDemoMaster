package org.pactDemo.ios

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.inject.server.{FeatureTest, FeatureTestMixin}
import com.twitter.util.Future
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, FinatraServer}
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, FlatSpec}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

class IosProviderSpec extends FinatraControllerSpec with FeatureTestMixin with BeforeAndAfter {

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
