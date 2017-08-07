package org.pactDemo.android

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.finatraUtilities._
import org.pactDemo.utilities._


class AndroidProviderController(client: IdAndToken => Future[IdTokenAndValid])(implicit loggingAdapter: LoggingAdapter) extends Controller with RequestResponse with PactArrow {
  post("/token/android/post")(useClient[IdAndToken, IdTokenAndValid](client))
  post("/token/android/post/debug")(traceClient[IdAndToken, IdTokenAndValid](client))
  options("/token/android/post") { request: Request => response.ok }
}

object AndroidIOApp extends App with ServiceLanguage {
  implicit val adapter = FinagleLoggingAdapter
  implicit val logger = new SimpleLogMe
  val baseUrl = Option(System.getenv("provider")).getOrElse("localhost:9000")

  val x = http(baseUrl) >--< logging("", "") >--< addHostName(baseUrl) >--< objectify[IdAndToken, IdTokenAndValid] >--< logging("", "")


  val Array(host, _) = baseUrl.split(":")
  val rawHttpClient = new AddHostNameService(host, new LoggingClient[Request, Response]("ProviderHttp", "", Http.newService(baseUrl)))
  val client = new LoggingClient[IdAndToken, IdTokenAndValid]("Provider", "", new GenericCustomClient[IdAndToken, IdTokenAndValid](rawHttpClient))
  new FinatraServer(9090, new AndroidProviderController(x.service), new AssetsController).main(Array())
}