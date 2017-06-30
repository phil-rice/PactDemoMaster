package org.pactDemo.utilities

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.util.Future
import org.json4s.native.Serialization
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.pactDemo.utilities.Futures._


case class ExampleReq(id: String)

object ExampleReq {

  implicit object ExampleToRequestForExampleReq extends ExampleToRequest[ExampleReq] {
    override def apply(v1: ExampleReq): Request = Request(s"/id/${v1.id}")
  }

}

case class ExampleRes(id: String, name: String)

case class NotFoundStatusCode(req: Any, request: Request, response: Response) extends Exception(response.toString())

case class UnexpectedStatusCode(req: Any, request: Request, response: Response) extends Exception(response.toString())

object ExampleRes {


  implicit object ParserForExampleRes extends Parser[ExampleRes] {

    import org.json4s._

    implicit val formats = DefaultFormats + FieldSerializer[ExampleRes]() // Brings in default date formats etc.
    override def apply(res: String): ExampleRes = Serialization.read[ExampleRes](res)
  }

  implicit object ExampleFromResponseForExampleRes extends ResponseProcessor[ExampleReq, ExampleRes] {
    override def statusCode404(req: ExampleReq, request: Request, r: Response): ExampleRes = throw new NotFoundStatusCode(req, request, r)

    override def unexpectedStatusCode(req: ExampleReq, request: Request, r: Response): ExampleRes = throw new UnexpectedStatusCode(req, request, r)
  }

}

object ExampleReqResService {
  def apply[Req: ExampleToRequest, Res: Parser](rawHttpClient: Request => Future[Response])(implicit responseProcessor: ResponseProcessor[Req, Res]): (Req => Future[Res]) =
    new ExampleHttpClient[Req, Res](rawHttpClient)
}

trait ExampleReqFixture {
  val req = ExampleReq("someId")
  val res = ExampleRes("anId", "aName")
  val exampleJson = """{"id":"anId", "name": "aName"}"""
}

class ExampleResSpec extends PactDemoSpec with ExampleReqFixture {

  import ExampleReq._
  import ExampleRes._

  behavior of "ExampleReq"

  it should "have a parser" in {
    ParserForExampleRes(exampleJson) shouldBe res
  }

  it should "turn a ExampleRequest into a Finatra Request" in {
    val request = ExampleToRequestForExampleReq(req)
    request.uri shouldBe "/id/someId"
  }

  it should "throw UnexpectedStatusCode if not 200" in {
    val request = ExampleToRequestForExampleReq(req)
    val response = Response()
    intercept[NotFoundStatusCode](ExampleFromResponseForExampleRes.statusCode404(req, request, response)) shouldBe NotFoundStatusCode(req, request, response)
    intercept[UnexpectedStatusCode](ExampleFromResponseForExampleRes.unexpectedStatusCode(req, request, response)) shouldBe UnexpectedStatusCode(req, request, response)
  }
}

class ExampleHttpClientSpec extends PactDemoSpec with ExampleReqFixture {

  type FinatraService = (Request => Future[Response])
  behavior of "ExampleHttpClient"

  def setup(block: (ExampleHttpClient[ExampleReq, ExampleRes], FinatraService, ArgumentCaptor[Request]) => Unit) = {
    val delegate = mock[FinatraService]
    val exampleClient = new ExampleHttpClient[ExampleReq, ExampleRes](delegate)
    val captor = capture[Request]
    block(exampleClient, delegate, captor)
  }

  def makeResponse(status: Status) = {
    val response = Response(status)
    response.contentString = exampleJson
    response
  }

  it should "process a request and return a response with status code 200" in {
    setup { (exampleClient, delegate, requestCaptor) =>
      val response = makeResponse(Status.Ok)

      when(delegate(requestCaptor.capture())) thenReturn Future.value(response)
      exampleClient(req).await shouldBe res
    }
  }
  it should "process a request and return a NotFoundStatusCode exception with status code 404" in {
    setup { (exampleClient, delegate, requestCaptor) =>
      val response = makeResponse(Status.NotFound)

      when(delegate(requestCaptor.capture())) thenReturn Future.value(response)
      intercept[NotFoundStatusCode](exampleClient(req).await) shouldBe NotFoundStatusCode(req, requestCaptor.getValue, response)
    }
  }
  it should "process a request and return a UnexpectedStatusCode exception with other status codes" in {
    setup { (exampleClient, delegate, requestCaptor) =>
      val response = makeResponse(Status.EnhanceYourCalm)

      when(delegate(requestCaptor.capture())) thenReturn Future.value(response)
      intercept[UnexpectedStatusCode](exampleClient(req).await) shouldBe UnexpectedStatusCode(req, requestCaptor.getValue, response)
    }
  }


}