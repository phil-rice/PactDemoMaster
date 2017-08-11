package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Future, Return, Throw, Try}

trait DependantServiceStatusRequest

object DependantServiceStatusRequest extends DependantServiceStatusRequest


case class DetailedStatusRequest(description: String, host: String, url: String)

object DetailedStatusRequest {

  implicit object StatusRequestToRequest extends ToRequest[DetailedStatusRequest] {
    override def apply(v1: DetailedStatusRequest): Request = Request(v1.url)
  }

}

sealed trait DependantServiceStatusReport

case class GoodStatusResponse(statusRequest: DetailedStatusRequest, statusCode: Int) extends DependantServiceStatusReport

case class ExceptionStatusResponse(statusRequest: DetailedStatusRequest, exception: Throwable) extends DependantServiceStatusReport

object DependantServiceStatusReport {

  implicit object StatusResponseToResponse extends FromResponse[DetailedStatusRequest, DependantServiceStatusReport] {
    override def apply(v1: DetailedStatusRequest, v2: Response): DependantServiceStatusReport = GoodStatusResponse(v1, v2.statusCode)
  }

}

class StatusService(detailedStatusRequest: DetailedStatusRequest, delegate: Request => Future[Response]) extends (DependantServiceStatusRequest => Future[DependantServiceStatusReport]) {

  import ServiceLanguage._

  val client = root[Request, Response](s"Status Http$detailedStatusRequest", () => delegate) >--< objectify[DetailedStatusRequest, DependantServiceStatusReport] >--< recoverFromError { case (req, Throw(e)) => Return(ExceptionStatusResponse(req, e)) }

  override def apply(sr: DependantServiceStatusRequest) = client.service(detailedStatusRequest)
}

case class DependantServicesStatusReport(details: Seq[DependantServiceStatusReport])

object DependantServicesStatusReport {

  implicit object TemplateableForStatusReport extends Templateable[DependantServicesStatusReport] {
    override def apply(v1: DependantServicesStatusReport): TemplateItem = TemplateItem(
      Map("status" -> v1.details.map {
        case sr: GoodStatusResponse => Map("host" -> sr.statusRequest.host, "url" -> sr.statusRequest.url, "statusCode" -> sr.statusCode)
        case sr: ExceptionStatusResponse => Map("host" -> sr.statusRequest.host, "url" -> sr.statusRequest.url, "exception" -> sr.exception.getClass.getName, "exceptionMsg" -> sr.exception.getMessage)
      })
    )
  }

}

class DependantServicesStatusService(tree: ServiceTree[_, _, ServiceDescription]) extends (DependantServiceStatusRequest => Future[DependantServicesStatusReport]) {
  val addHostNameServices = tree.findAllTreesWithServiceReqRes[Request, Response, AddHostNameService]

  val statusServices: Seq[StatusService] = addHostNameServices.map(st => new StatusService(DetailedStatusRequest(st.payload.description, st.service.asInstanceOf[AddHostNameService].hostName, "status"), st.service))

  override def apply(v1: DependantServiceStatusRequest) = Future.collect(statusServices.map(_ apply v1)).map(DependantServicesStatusReport.apply)

}

trait CacheStatusRequest

object CacheStatusRequest extends CacheStatusRequest {

}

case class NameAndCachingMetrics(name: String, metricsSnapshot: CachingMetricsSnapshot)

object NameAndCachingMetrics {

  implicit object TemplatableForNameAndCachingMetricsSnapshot extends Templateable[NameAndCachingMetrics] {
    override def apply(v1: NameAndCachingMetrics): TemplateItem = {
      import v1.metricsSnapshot._
      TemplateItem(Map(
        "name" -> v1.name,
        "queries" -> queries,
        "hits" -> hits,
        "created" -> created,
        "passedThrough" -> passedThrough,
        "removed" -> removed,
        "size" -> size))
    }
  }

}

case class CacheStatusReport(metrics: Iterable[NameAndCachingMetrics])

object CacheStatusReport {
  implicit def templateableForCacheStatusReport(implicit templateable: Templateable[NameAndCachingMetrics]) = new Templateable[CacheStatusReport] {
    override def apply(v1: CacheStatusReport): TemplateItem =
      TemplateItem(v1.metrics.map(templateable).map(_.contents))
  }
}

class CacheReportService(tree: ServiceTree[_, _, ServiceDescription]) extends (CacheStatusRequest => Future[CacheStatusReport]) {
  val cachingServices = tree.findAll[CacheService[_, _]].map(_.service.asInstanceOf[CacheService[_, _]])

  override def apply(v1: CacheStatusRequest) = Future.value(CacheStatusReport(cachingServices.map(cs => NameAndCachingMetrics(cs.name, cs.metrics))))

}

trait FullStatusRequest

object FullStatusRequest extends FullStatusRequest {

  implicit object ToDependantServiceStatusRequest extends AggregateTransform[FullStatusRequest, DependantServiceStatusRequest] {
    override def apply(v1: FullStatusRequest): DependantServiceStatusRequest = DependantServiceStatusRequest
  }

  implicit object ToCacheStatusRequest extends AggregateTransform[FullStatusRequest, CacheStatusRequest] {
    override def apply(v1: FullStatusRequest): CacheStatusRequest = CacheStatusRequest
  }

}

case class FullStatusReport(statusReport: DependantServicesStatusReport, cacheStatusReport: CacheStatusReport)

object FullStatusReport {

  implicit def templatableForFullStatusReport(implicit forDependantServicesStatusReport: Templateable[DependantServicesStatusReport], forCacheStatusReport: Templateable[CacheStatusReport]) =
    new Templateable[FullStatusReport] {
      override def apply(v1: FullStatusReport) = TemplateItem(Map(
        "dependantServices" -> forDependantServicesStatusReport(v1.statusReport).contents,
        "caches" -> forCacheStatusReport(v1.cacheStatusReport).contents))
    }

  implicit object MergerForFullStatusReport extends AggregateMerger[DependantServicesStatusReport, CacheStatusReport, FullStatusReport] {
    override def apply(v1: (DependantServicesStatusReport, CacheStatusReport)): FullStatusReport = FullStatusReport(v1._1, v1._2)
  }

}

class FullStatusService(tree: ServiceTree[_, _, ServiceDescription]) extends (FullStatusRequest => Future[FullStatusReport]) {
  val dependantServicesStatusService = new DependantServicesStatusService(tree)
  val cacheStatusService = new CacheReportService(tree)
  val merger = new AggregateService[FullStatusRequest, FullStatusReport, DependantServiceStatusRequest, DependantServicesStatusReport, CacheStatusRequest, CacheStatusReport](dependantServicesStatusService, cacheStatusService)

  override def apply(v1: FullStatusRequest) = merger(v1)
}