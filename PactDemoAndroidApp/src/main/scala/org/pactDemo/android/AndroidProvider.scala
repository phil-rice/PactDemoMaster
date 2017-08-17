package org.pactDemo.android

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.finatraUtilities.{ServiceMakerLanguage, _}

class AndroidProviderController(client: IdAndToken => Future[IdTokenAndValid]) extends Controller with RequestResponse with PactArrow {
  post("/token/android/post")(useClient[IdAndToken, IdTokenAndValid](client))
  options("/token/android/post") { request: Request => response.ok }
}

object AndroidIOApp extends App {
  val baseUrl = Option(System.getenv("provider")).getOrElse("localhost:9000")
  val Array(host, _) = baseUrl.split(":")
  val profilingStats = new ProfilingStats

  import ServiceMakerLanguage._

  val client = Http.newService(baseUrl) >--< addHostName(baseUrl) >--< logger("someName", "somePrefix") >--< profile(profilingStats) >--< objectify[IdAndToken, IdTokenAndValid] >--< profile(profilingStats) >--< logger("someOtherName", "someOtherPrefix")

  //  val rawHttpClient = new AddHostNameService(host, new LoggingClient[Request, Response]("ProviderHttp", "", Http.newService(baseUrl)))
  //  val client = new LoggingClient[IdAndToken, IdTokenAndValid]("Provider", "", new GenericCustomClient[IdAndToken, IdTokenAndValid](rawHttpClient))

  new FinatraServer(9090, new AndroidProviderController(client), new AssetsController).main(Array())
}