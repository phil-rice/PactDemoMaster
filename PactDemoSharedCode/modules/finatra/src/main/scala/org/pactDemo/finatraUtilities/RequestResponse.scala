package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.Future

trait FromRequest[T] extends (Request => T)

trait FromResponse[Req, Res] extends ((Req, Response) => Res)

trait ToRequest[T] extends (T => Request)

trait ToResponse[T] {
  def apply(response: ResponseBuilder)(t: T): Response

  protected def setResponseContentTypeToJson(response: Response): Response = {
    response.setContentType("application/json")
    response
  }
}

trait RequestResponse {

  import PactArrow._



  protected def response: ResponseBuilder

  def fromRequest[T](implicit fromRequest: FromRequest[T]): (Request => T) = fromRequest

  def toRequest[T](implicit toRequest: ToRequest[T]): T => Request = toRequest

  def toResponse[T](implicit makeResponse: ToResponse[T]): T => Response = makeResponse(response)

  def useClient[Req: FromRequest : ToRequest, Res : ToResponse](client: Req => Future[Res])(request: Request) = {
    request ~> fromRequest[Req] ~> client ~> toResponse
  }

}