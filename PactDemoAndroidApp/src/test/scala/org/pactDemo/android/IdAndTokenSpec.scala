package org.pactDemo.android

import com.twitter.finagle.http.{Method, RequestBuilder}
import com.twitter.io.Buf
import org.pactDemo.android.IdAndToken
import org.pactDemo.finatraUtilities.{FromRequest, ToRequest}
import org.pactDemo.utilities.PactDemoSpec

class IdAndTokenSpec extends PactDemoSpec{

  behavior of "IdAndToken"

  it should "be makeable from a request" in {
    val request = RequestBuilder.apply().
      url("http://someplace/somePath").
      buildPost(Buf.ByteArray.apply("""{"id": 1, "token":"someToken"}""".getBytes("UTF-8"): _*))
    implicitly[FromRequest[IdAndToken]].apply(request) shouldBe IdAndToken(1, "someToken")
  }

  it should "create a request" in {
    val request = implicitly[ToRequest[IdAndToken]].apply(IdAndToken(1, "someToken"))
    request.method shouldBe Method.Post
    request.uri shouldBe "/token/id/1"
    request.contentType shouldBe None
    request.contentString shouldBe """{"Authentication-token":"token someToken"}"""

  }
}
