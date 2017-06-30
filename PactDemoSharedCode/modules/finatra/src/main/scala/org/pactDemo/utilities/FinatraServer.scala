package org.pactDemo.utilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.request.{QueryParam, RouteParam}

/** Admin port is at port +1 */
class FinatraServer(defaultPort: Int, controllers: Controller*) extends HttpServer {


  def actualPort = Heroku.port(defaultPort)
  override val modules = Seq()

  override val disableAdminHttpServer = true // see https://twitter.github.io/finatra/user-guide/twitter-server/index.html

  override val defaultFinatraHttpPort: String = s":$actualPort"
  override def defaultHttpPort: Int = actualPort


  override def configureHttp(router: HttpRouter): Unit = {
    val raw = router.filter[CommonFilters]
    controllers.foldLeft(raw)((acc, c) => acc.add(c))
  }
}

