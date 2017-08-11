package org.pactDemo.mustache

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.pactDemo.finatraUtilities._

class StatusController(tree: ServiceTree[_, _, ServiceDescription], templateName: String = "status.mustache")(implicit templateMaker: TemplateMaker, displayStatus: Templateable[FullStatusReport]) extends Controller with PactArrow {
  val fullStatusService = new FullStatusService(tree)

  def toHtmlResponse(html: String) = response.ok().html(html)

  get("/status") { request: Request => FullStatusRequest ~> fullStatusService ~> displayStatus ~> templateMaker(templateName) ~> toHtmlResponse }
}