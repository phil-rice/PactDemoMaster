package org.pactDemo.ios

import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import org.pactDemo.utilities.{FinatraClient, FinatraServer, Heroku}

case class AndroidRequest(@RouteParam id: Int)


class AndroidAppController extends Controller {
  val client = new FinatraClient(Heroku.providerHost, Heroku.providerPort, _.replace("}", ""","server":"android"}"""))
  get("/id/:id") { request: AndroidRequest =>
    client(request.id).map(response.ok(_).contentType("application/json"))
  }
}

object IosApp extends App {
  new FinatraServer(9020, new AndroidAppController).main(Array())
}
