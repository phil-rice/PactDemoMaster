package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Future, Return, Throw, Try}

trait StatusRequest

object StatusRequest extends StatusRequest


case class DetailedStatusRequest(description: String, url: String)

object DetailedStatusRequest {

  implicit object StatusRequestToRequest extends ToRequest[DetailedStatusRequest] {
    override def apply(v1: DetailedStatusRequest): Request = Request(v1.url)
  }

}

sealed trait StatusResponse

case class GoodStatusResponse(statusRequest: DetailedStatusRequest, statusCode: Int) extends StatusResponse

case class ExceptionStatusResponse(statusRequest: DetailedStatusRequest, exception: Throwable) extends StatusResponse

object StatusResponse {

  implicit object StatusResponseToResponse extends FromResponse[DetailedStatusRequest, StatusResponse] {
    override def apply(v1: DetailedStatusRequest, v2: Response): StatusResponse = GoodStatusResponse(v1, v2.statusCode)
  }

}

class StatusService(detailedStatusRequest: DetailedStatusRequest, delegate: Request => Future[Response]) extends (StatusRequest => Future[StatusResponse]) {

  val objectify = new GenericCustomClient[DetailedStatusRequest, StatusResponse](delegate)
  val recoverFromErrorService = RecoverFromErrorService[DetailedStatusRequest, StatusResponse](objectify) {
    case (req, Throw(e)) => Return(ExceptionStatusResponse(req, e))
  }

  override def apply(sr: StatusRequest) = recoverFromErrorService(detailedStatusRequest)
}

case class StatusReport(details: Seq[StatusResponse])

object StatusReport {

  implicit object TemplateableForStatusReport extends Templateable[StatusReport] {
    override def apply(v1: StatusReport): TemplateItem = TemplateItem(
      Map("status" -> v1.details.map {
        case sr: GoodStatusResponse => Map("description" -> sr.statusRequest.description, "url" -> sr.statusRequest.url, "statusCode" -> sr.statusCode)
        case sr: ExceptionStatusResponse => Map("description" -> sr.statusRequest.description, "url" -> sr.statusRequest.url, "exception" -> sr.exception.getClass.getName, "exceptionMsg" -> sr.exception.getMessage)
      })
    )
  }

}

class FullStatusService(tree: ServiceTree[_, _, ServiceDescription]) extends (StatusRequest => Future[StatusReport]) {
  val statusServices: Seq[StatusService] = tree.allHttpServices.map(st => new StatusService(DetailedStatusRequest(st.payload.description, "/status"), st.service))

  override def apply(v1: StatusRequest) = Future.collect(statusServices.map(_ apply v1)).map(StatusReport.apply)

}