package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.utilities.PactDemoSpec
import Futures._
class GenericClientSpec extends PactDemoSpec {

  behavior of "GenericClient"

  val someRequest = Request("/someRequest")
  val someResponse = Response()

  def withMocks(fn: (GenericCustomClient[Int, String], Request => Future[Response], ToRequest[Int], FromResponse[Int, String]) => Unit) = {
    implicit val toRequest = mock[ToRequest[Int]]
    implicit val fromResponse = mock[FromResponse[Int, String]]
    implicit val delegate = mock[Request => Future[Response]]
    fn(new GenericCustomClient[Int, String](delegate), delegate, toRequest, fromResponse)
  }

  it should "take the request, use the CustomeRequestProcessor typeclass pass it to the delegate, get the result and transform it with the CustomeResponseProcessor" in {
    withMocks { (client, delegate, toRequest, fromResponse) =>
      when(toRequest.apply(1)) thenReturn (someRequest)
      when(delegate.apply(someRequest)) thenReturn Future.value(someResponse)
      when(fromResponse.apply(1, someResponse)) thenReturn ("someResult")

      client(1).await shouldBe "someResult"
    }
  }



}
