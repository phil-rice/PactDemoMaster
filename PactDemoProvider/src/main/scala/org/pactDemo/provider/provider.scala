package org.pactDemo.provider

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import org.pactDemo.utilities.FinatraServer


class ProviderController extends Controller {
  private val data = Map(1 -> "Phil", 2 -> "Bob")

  post("/token/id/:id") { request: Request =>
    val token = request.contentString
    val index = token.indexOf("-token")
    val lastIndex = token.indexOf("}")
    val actualToken = token.substring(index+ 15, lastIndex-1)
    println(s"ACtual token $actualToken")
    val id = request.getIntParam("id")
    if (token.contains("invalid")) response.unauthorized(s"Unauthorized token $actualToken") else
      response.ok(s"""{"token":"$actualToken","id":"$id"}""")
  }

}


object Provider extends App {
  new FinatraServer(9000, new ProviderController).main(Array())
}