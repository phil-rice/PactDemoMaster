package org.pactDemo.android

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.finatraUtilities._
import org.pactDemo.mustache.Mustache
import org.pactDemo.utilities._


class AndroidProviderController(client: IdAndToken => Future[IdTokenAndValid])(implicit loggingAdapter: LoggingAdapter, templateMaker: TemplateMaker) extends Controller with RequestResponse with PactArrow {
  post("/token/android/post")(useClient[IdAndToken, IdTokenAndValid](client))
  post("/token/android/post/debug")(traceClient[IdAndToken, IdTokenAndValid]("logging.mustache", client))
  options("/token/android/post") { request: Request => response.ok }
}


object AndroidIOApp extends App with ServiceLanguage {
  implicit val adapter = FinagleLoggingAdapter
  implicit val logger = new SimpleLogMe

  import Mustache._

  val baseUrl = Heroku.providerHostAndPort

  val clientBuilder = http(baseUrl) >--< logging("", "") >--< addHostName(baseUrl) >--< objectify[IdAndToken, IdTokenAndValid] >--< logging("", "")

   new FinatraServer(9090, new AndroidProviderController(clientBuilder.service), new AssetsController).main(Array())
}