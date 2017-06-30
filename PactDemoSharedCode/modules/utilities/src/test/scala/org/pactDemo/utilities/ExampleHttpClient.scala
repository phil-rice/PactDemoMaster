package org.pactDemo.utilities

import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

trait ExampleToRequest[T] extends (T => Request)

trait Parser[T] extends (String => T)

trait ResponseProcessor[Req, Res] {
  def statusCode404(req: Req, request: Request, r: Response): Res

  def unexpectedStatusCode(req: Req, request: Request, r: Response): Res

  def apply(req: Req, request: Request)(response: Response)(implicit parser: Parser[Res]): Res = response.status.code match {
    case 200 => parser(response.contentString)
    case 404 => statusCode404(req, request, response)
    case _ => unexpectedStatusCode(req, request, response)
  }
}

class ExampleHttpClient[Req, Res: Parser](delegate: Request => Future[Response])
                                         (implicit toRequest: ExampleToRequest[Req], fromResponse: ResponseProcessor[Req, Res]) extends Service[Req, Res] {
  override def apply(req: Req): Future[Res] = {
    val httpReq = toRequest(req)
    delegate(httpReq).map(fromResponse(req, httpReq))
  }
}
class HttpClientWithHostAdded(hostName: String, delegate: Request => Future[Response]) extends Service[Request, Response] {
  override def apply(request: Request): Future[Response] = {
    request.host = hostName
    delegate(request)
  }
}
