package org.pactDemo.utilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

import org.mockito.Mockito._

class GenericClientSpec extends PactDemoSpec {

  behavior of "GenericClient"

  val someRequest = Request("/someRequest")
  val someResponse = Response()

  def withMocks(fn: (GenericHttpClient[Int, String], Request => Future[Response], ToRequest[Int], FromResponse[String]) => Unit) = {
    implicit val toRequest = mock[ToRequest[Int]]
    implicit val fromResponse = mock[FromResponse[String]]
    implicit val delegate = mock[Request => Future[Response]]
    fn(new GenericHttpClient[Int, String](delegate), delegate, toRequest, fromResponse)
  }

  it should "take the request, use the ToRequest typeclass pass it to the delegate, get the result and transform it with the FromResponse" in {
    withMocks { (client, delegate, toRequest, fromResponse) =>
      when(toRequest.apply(1)) thenReturn (someRequest)
      when(delegate.apply(someRequest)) thenReturn Future.value(someResponse)
      when(fromResponse.apply(someResponse)) thenReturn ("someResult")

      client(1).await shouldBe "someResult"
    }
  }



}
