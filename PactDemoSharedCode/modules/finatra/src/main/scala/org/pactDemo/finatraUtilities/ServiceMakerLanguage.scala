package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.pactDemo.utilities.PrintlnLogMe


trait ServiceMakerLanguage[Req, Res, NewReq, NewRes] {
  def apply(service: Req => Future[Res]): (NewReq => Future[NewRes])
}

object ServiceMakerLanguage {

  implicit val logger = PrintlnLogMe

  implicit class ServiceLanguagePimper[Req, Res](s: Req => Future[Res]) {
    def >--<[NewReq, NewRes](serviceMakerLanguage: ServiceMakerLanguage[Req, Res, NewReq, NewRes]): (NewReq => Future[NewRes]) = serviceMakerLanguage(s)
  }

  def logger[Req, Res](name: String, prefix: String) = new ServiceMakerLanguage[Req, Res, Req, Res] {
    override def apply(service: (Req) => Future[Res]): (Req) => Future[Res] = new LoggingClient[Req, Res](name, prefix, service)
  }

  def addHostName(name: String) = new ServiceMakerLanguage[Request, Response, Request, Response] {
    override def apply(service: (Request) => Future[Response]): (Request) => Future[Response] = new AddHostNameService(name, service)
  }

  def objectify[CustomerReq, CustomRes](implicit toRequest: ToRequest[CustomerReq],
                                        fromResponse: FromResponse[CustomerReq, CustomRes]) = new ServiceMakerLanguage[Request, Response, CustomerReq, CustomRes] {
    override def apply(service: (Request) => Future[Response]): (CustomerReq) => Future[CustomRes] = new GenericCustomClient[CustomerReq, CustomRes](service)
  }

  def profile[Req, Res](profilingStats: ProfilingStats)= new ServiceMakerLanguage[Req, Res, Req, Res] {
    override def apply(service: (Req) => Future[Res]): (Req) => Future[Res] = new ProfilingClient[Req, Res]( profilingStats, service)
  }

}

