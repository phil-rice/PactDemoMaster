package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.util.{Future, Return, Throw}

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

  def useClient[Req: FromRequest : ToRequest, Res: ToResponse](client: Req => Future[Res])(request: Request) = {
    request ~> fromRequest[Req] ~> client ~> toResponse
  }

  def traceClient[Req: FromRequest : ToRequest, Res: ToResponse](client: Req => Future[Res])(request: Request)(implicit loggingAdapter: LoggingAdapter, loggingMemoriser: LoggingMemoriser) = {
    def loggingReportToResponse(loggingReport: LoggingReport[Res]): Response = {
      val response = loggingReport.result match {
        case Return(res) =>
          toResponse[Res] apply (res)
        case Throw(res) =>
          val response = Response(Status.InternalServerError)
          response.contentString = res.toString
          response
      }
      response.contentString = response.contentString + "\nAND HERE ARE THE RECORDS\n" + loggingReport.records.mkString("\n")
      response
    }

    loggingMemoriser.trace(request ~> fromRequest[Req] ~> client) ~> loggingReportToResponse
  }

}