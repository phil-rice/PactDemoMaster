package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class ClearCacheController(tree: ServiceTree[_, _, ServiceDescription]) extends Controller {
  val cachingServices = tree.findAll[CacheService[_, _]].map(_.service.asInstanceOf[CacheService[_, _]])

  post("/internal/cache/clear") { request: Request =>
    cachingServices.foreach(_.clear)
    response.temporaryRedirect.location("/status")
  }
}
