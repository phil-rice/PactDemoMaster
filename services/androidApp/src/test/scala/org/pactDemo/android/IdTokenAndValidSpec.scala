package org.pactDemo.android

import com.twitter.finagle.http.{Response, Status}
import org.pactDemo.finatraUtilities.FromResponse
import org.pactDemo.utilities.PactDemoSpec

class IdTokenAndValidSpec extends PactDemoSpec {

  behavior of "IosAuthResponse"

  def makeResponse(status: Status, body: String) = {
    val response = Response(status)
    response.contentString = body
    response
  }

  val someRequest = IdAndToken(1, "tokenInRequest")

  it should "make a valid response with a 200 status code" in {
    val actual = implicitly[FromResponse[IdAndToken, IdTokenAndValid]].apply(someRequest, makeResponse(Status.Ok, """{"id":1, "token":"tokenInResponse", "valid":true}"""))
    actual shouldBe IdTokenAndValid(1, "tokenInResponse", true)
  }

  it should "make a valid response with a none status code" in {
    val actual = implicitly[FromResponse[IdAndToken, IdTokenAndValid]].apply(someRequest, makeResponse(Status.Unauthorized, """"""))
    actual shouldBe IdTokenAndValid(1, "tokenInRequest", false)
  }
}
