package org.pactDemo.finatra.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.pactDemo.finatra.service.{FullStatusReport, FullStatusRequest, FullStatusService}
import org.pactDemo.finatra.structure.{ServiceDescription, ServiceTree}
import org.pactDemo.finatra.utilities.PactArrow
import org.pactDemo.utilities.{TemplateMaker, Templateable}

class StatusController(tree: ServiceTree[_, _, ServiceDescription], templateName: String = "status.mustache")(implicit templateMaker: TemplateMaker, displayStatus: Templateable[FullStatusReport]) extends Controller with PactArrow {
  val fullStatusService = new FullStatusService(tree)

  def toHtmlResponse(html: String) = response.ok().html(html)

  any("/status") { request: Request => FullStatusRequest ~> fullStatusService ~> displayStatus ~> templateMaker(templateName) ~> toHtmlResponse }
}