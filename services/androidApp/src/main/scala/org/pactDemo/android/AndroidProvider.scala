package org.pactDemo.android

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.finatra._
import org.pactDemo.finatra.controller.{AssetsController, ClearCacheController, DisplayStructureController, StatusController}
import org.pactDemo.finatra.structure.ServiceLanguage
import org.pactDemo.finatra.utilities._
import org.pactDemo.mustache.Mustache
import org.pactDemo.utilities._

import scala.language.postfixOps

class AndroidProviderController(client: IdAndToken => Future[IdTokenAndValid])(implicit loggingAdapter: LoggingAdapter, templateMaker: TemplateMaker) extends Controller with RequestResponse with PactArrow {
  post("/token/android/post")(useClient[IdAndToken, IdTokenAndValid](client))
  post("/token/android/post/debug")(traceClient[IdAndToken, IdTokenAndValid]("logging.mustache", client))
  options("/token/android/post") { request: Request => response.ok }
}


object AndroidIOApp extends App with ServiceLanguage {
  implicit val adapter = FinagleLoggingAdapter
  implicit val logger = new SimpleLogMe

  import Mustache._
  import org.pactDemo.finatra.controller.DisplayStructure._

  val baseUrl = Heroku.providerHostAndPort
  println(s"Base url for provider is $baseUrl")

  val clientBuilder = http(baseUrl) >--< logging("providerHttp", "") >--< addHostName(baseUrl) >--< objectify[IdAndToken, IdTokenAndValid] >--< logging("providerIdAndToken", "") >--< caching("Provider")

  new FinatraServer(9090, new StatusController(clientBuilder), new ClearCacheController(clientBuilder), new DisplayStructureController(clientBuilder), new AndroidProviderController(clientBuilder.service), new AssetsController).main(Array())
}