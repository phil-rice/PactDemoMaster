package org.pactDemo.ios

import com.twitter.finagle.http.{Method, Methods, Request, RequestBuilder}
import com.twitter.io.Buf
import org.pactDemo.finatraUtilities.{FromRequest, ToRequest}
import org.pactDemo.utilities.PactDemoSpec

class IosProviderRequestSpec extends PactDemoSpec {

  behavior of "IosProviderRequest"

  it should "generate a request" in {
    val request = implicitly[ToRequest[IosProviderRequest]].apply(IosProviderRequest(1, "someToken"))
    request.method shouldBe Method.Post
    request.uri shouldBe "/token/id/1"
    request.contentType shouldBe None
    request.contentString shouldBe """{"Authentication-token":"token someToken"}"""
  }

  it should "creatable from the original request" in {
    val request = RequestBuilder.apply().
      url("http://someplace/token/id/1").
      buildPost(Buf.ByteArray.apply("""{"Authentication-token":"token someToken"}""".getBytes("UTF-8"): _*))

    implicitly[FromRequest[IosProviderRequest]].apply(request) shouldBe IosProviderRequest(1, "someToken")
  }
}
