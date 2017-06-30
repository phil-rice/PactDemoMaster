package org.pactDemo.provider

import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import org.pactDemo.utilities.FinatraServer

case class ProviderRequest(@RouteParam id: Int)


class ProviderController extends Controller {
  private val data = Map(1 -> "Phil", 2 -> "Bob")

  get("/id/:id") { request: ProviderRequest =>
    import request._
    data.get(id) match {
      case Some(name) => response.ok(s"""{"id": "$id", "name":"$name"}""").contentType("application/json")
      case _ => response.notFound(s"id not found")
    }
  }
}


object Provider extends App {
  new FinatraServer(9000, new ProviderController).main(Array())
}