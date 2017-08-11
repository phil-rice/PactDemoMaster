package org.pactDemo.finatra

import javax.inject.Inject

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.exceptions.ExceptionMapper
import com.twitter.finatra.http.filters.{CommonFilters, ExceptionMappingFilter, LoggingMDCFilter}
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.finatra.http.{Controller, HttpServer}
import org.pactDemo.utilities.Heroku


class FinatraServer(defaultPort: Int, controllers: Controller*) extends HttpServer {


  def actualPort = Heroku.port(defaultPort)

  println(s"Finatra Server is starting on port $actualPort")
  override val modules = Seq()

  override val disableAdminHttpServer = true // see https://twitter.github.io/finatra/user-guide/twitter-server/index.html

  override val defaultFinatraHttpPort: String = s":$actualPort"

  override def defaultHttpPort: Int = actualPort

  override def configureHttp(router: HttpRouter): Unit = {
    val raw = router.filter[CommonFilters]
      .filter(new HttpFilter(Cors.UnsafePermissivePolicy)) // Added CORS dependency to handle Cross-origin problem
      .filter[ExceptionMappingFilter[Request]]
      .filter[LoggingMDCFilter[Request, Response]]
      .exceptionMapper[DebugURLExceptionMapper]
    controllers.foldLeft(raw)((acc, c) => acc.add(c))
  }
}

class DebugURLExceptionMapper @Inject()(response: ResponseBuilder) extends ExceptionMapper[Exception] {
  override def toResponse(request: Request, exception: Exception): Response = {
    exception.printStackTrace()
    response.badRequest(s"Exception - ${exception.getMessage}")
  }
}
