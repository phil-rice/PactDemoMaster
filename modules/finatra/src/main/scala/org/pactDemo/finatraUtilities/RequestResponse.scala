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

case class TemplateItem(contents: Any)


trait Templateable[T] extends (T => TemplateItem)

trait TemplateMaker {
  def apply(name: String): (TemplateItem => String)
}


trait RequestResponse {

  import PactArrow._

  implicit object TemplateableForResponse extends Templateable[Response] {
    override def apply(response: Response): TemplateItem = TemplateItem(Map("statusCode" -> response.status.code, "contentString" -> response.contentString, "contentType" -> response.contentType))
  }

  implicit object TemplateableForException extends Templateable[Throwable] {
    override def apply(e: Throwable): TemplateItem = TemplateItem(Map("statusCode" -> "Threw exception", "contentString" -> e.toString, "contentType" -> "None"))
  }

  implicit object TemplateableForLoggingRecords extends Templateable[Iterable[LoggingRecord]] {
    override def apply(records: Iterable[LoggingRecord]): TemplateItem = {
      val firstTime = records.map(_.time).min
      TemplateItem(records.map(logginRecord => Map(
        "time" -> (logginRecord.time - firstTime) / 1000000,
        "level" -> logginRecord.level,
        "message" -> logginRecord.msg
      )))
    }
  }


  implicit def templateableForResponse(implicit templateableForResponse: Templateable[Response], templateableForException: Templateable[Throwable], templatebleForLoggingeRcord: Templateable[Iterable[LoggingRecord]]) = new Templateable[LoggingReport[Response]] {
    override def apply(v1: LoggingReport[Response]): TemplateItem = {
      val mainData: TemplateItem = v1.result match {
        case Return(response) => templateableForResponse(response)
        case Throw(e) => templateableForException(e)
      }
      val loggingRecordsData = templatebleForLoggingeRcord(v1.records)
      TemplateItem(Map("result" -> mainData.contents, "logging"-> loggingRecordsData.contents))
    }
  }


  protected def response: ResponseBuilder

  def fromRequest[T](implicit fromRequest: FromRequest[T]): (Request => T) = fromRequest

  def toRequest[T](implicit toRequest: ToRequest[T]): T => Request = toRequest

  def toResponse[T](implicit makeResponse: ToResponse[T]): T => Response = makeResponse(response)

  def useClient[Req: FromRequest : ToRequest, Res: ToResponse](client: Req => Future[Res])(request: Request) = {
    request ~> fromRequest[Req] ~> client ~> toResponse[Res]
  }


  def traceClient[Req: FromRequest : ToRequest, Res: ToResponse](templateName: String, client: Req => Future[Res])(request: Request)(implicit loggingAdapter: LoggingAdapter, loggingMemoriser: LoggingMemoriser,
                                                                                                                                     templateMaker: TemplateMaker,
                                                                                                                                     templateable: Templateable[LoggingReport[Response]]) = {
    def loggingReportToResponse(loggingReport: LoggingReport[Res]): Response = {
      val loggingReportBasedOnResponse = loggingReport.map(toResponse[Res])
      val responseString = templateMaker(templateName)(loggingReportBasedOnResponse)
      response.ok(responseString).contentType("text/html")
    }

    loggingMemoriser.trace(request ~> fromRequest[Req] ~> client) ~> loggingReportToResponse
  }

}