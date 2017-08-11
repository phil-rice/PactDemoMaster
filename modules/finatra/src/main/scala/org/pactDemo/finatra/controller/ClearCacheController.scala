package org.pactDemo.finatra.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.pactDemo.finatra.service.CacheService
import org.pactDemo.finatra.structure.{ServiceDescription, ServiceTree}

class ClearCacheController(tree: ServiceTree[_, _, ServiceDescription]) extends Controller {
  val cachingServices = tree.findAll[CacheService[_, _]].map(_.service.asInstanceOf[CacheService[_, _]])

  post("/internal/cache/clear") { request: Request =>
    cachingServices.foreach(_.clear)
    response.temporaryRedirect.location("/status")
  }
}
