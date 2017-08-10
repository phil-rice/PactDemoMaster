package org.pactDemo.mustache

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.pactDemo.finatraUtilities._

class StatusController(templateName: String, tree: ServiceTree[_, _, ServiceDescription])(implicit templateMaker: TemplateMaker, displayStatus: Templateable[StatusReport]) extends Controller with PactArrow {
  val fullStatusService = new FullStatusService(tree)

  def toHtmlResponse(html: String) = response.ok().html(html)

  get("/status") { request: Request => StatusRequest ~> fullStatusService ~> displayStatus ~> templateMaker(templateName) ~> toHtmlResponse }
}