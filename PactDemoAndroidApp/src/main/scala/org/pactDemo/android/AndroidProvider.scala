package org.pactDemo.android

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.finatraUtilities._
import org.pactDemo.utilities._


class AndroidProviderController(client: IdAndToken => Future[IdTokenAndValid]) extends Controller with RequestResponse with PactArrow {
  post("/token/android/post")(useClient[IdAndToken, IdTokenAndValid](client))
  options("/token/android/post") { request: Request => response.ok }
}

trait ServiceMaker[Req, Res, NewReq, NewRes] {
  def apply(service: Req => Future[Res]): (NewReq => Future[NewRes])
}

object AndroidIOApp extends App {
  implicit val logger = PrintlnLogMe
  val baseUrl = Option(System.getenv("provider")).getOrElse("localhost:9000")
  val Array(host, _) = baseUrl.split(":")

  implicit class ServicePimper[Req, Res](s: Req => Future[Res]) {
    def >--<[NewReq, NewRes](serviceMaker: ServiceMaker[Req, Res, NewReq, NewRes]): (NewReq => Future[NewRes]) =  serviceMaker(s)
  }

  def logger[Req, Res](name: String, prefix: String) = new ServiceMaker[Req, Res, Req, Res] {
    override def apply(service: (Req) => Future[Res]): (Req) => Future[Res] = new LoggingClient[Req, Res](name, prefix, service)
  }

  def addHostName(name: String) = new ServiceMaker[Request, Response, Request, Response] {
    override def apply(service: (Request) => Future[Response]): (Request) => Future[Response] = new AddHostNameService(name, service)
  }

  def objectify[CustomerReq, CustomRes](implicit toRequest: ToRequest[CustomerReq],
                                        fromResponse: FromResponse[CustomerReq, CustomRes]) = new ServiceMaker[Request, Response, CustomerReq, CustomRes] {
    override def apply(service: (Request) => Future[Response]): (CustomerReq) => Future[CustomRes] = new GenericCustomClient[CustomerReq, CustomRes](service)
  }

  def profile[Req, Res](profilingStats: ProfilingStats)= new ServiceMaker[Req, Res, Req, Res] {
    override def apply(service: (Req) => Future[Res]): (Req) => Future[Res] = new ProfilingClient[Req, Res]( profilingStats, service)
  }

  val profilingStats = new ProfilingStats

  val client = Http.newService(baseUrl)  >--< addHostName(baseUrl) >--< logger("someName", "somePrefix") >--< profile(profilingStats) >--< objectify[IdAndToken, IdTokenAndValid] >--< profile(profilingStats)  >--< logger("someOtherName", "someOtherPrefix")


//  val rawHttpClient = new AddHostNameService(host, new LoggingClient[Request, Response]("ProviderHttp", "", Http.newService(baseUrl)))
//  val client = new LoggingClient[IdAndToken, IdTokenAndValid]("Provider", "", new GenericCustomClient[IdAndToken, IdTokenAndValid](rawHttpClient))


  new FinatraServer(9090, new AndroidProviderController(client), new AssetsController).main(Array())
}