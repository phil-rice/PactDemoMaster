package org.pactDemo.ios

import com.twitter.finagle.http.{Response, Status}
import org.pactDemo.finatra.utilities.FromResponse
import org.pactDemo.utilities.PactDemoSpec

class IosAuthResponseSpec extends PactDemoSpec {

  behavior of "IosAuthResponse"

  def makeResponse(status: Status, body: String) = {
    val response = Response(status)
    response.contentString = body
    response
  }

  val someRequest = IosProviderRequest(1, "tokenInRequest")

  it should "make a valid response with a 200 status code" in {
    val actual = implicitly[FromResponse[IosProviderRequest, IosAuthResponse]].apply(someRequest, makeResponse(Status.Ok, """"""))
    actual shouldBe IosValidAuthResponse(1, "tokenInRequest")
  }

  it should "make a valid response with a none status code" in {
    val actual = implicitly[FromResponse[IosProviderRequest, IosAuthResponse]].apply(someRequest, makeResponse(Status.EnhanceYourCalm, """"""))
    actual shouldBe IosInValidAuthResponse(1, "tokenInRequest")
  }
}
