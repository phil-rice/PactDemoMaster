package org.pactDemo.finatra.controller

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import org.pactDemo.finatra.structure.{ServiceDescription, ServiceTree}
import org.pactDemo.utilities.{TemplateItem, TemplateMaker, Templateable}


trait DisplayStructure extends Templateable[ServiceTree[_, _, ServiceDescription]] {

  override def apply(tree: ServiceTree[_, _, ServiceDescription]): TemplateItem = {

    TemplateItem(Map("structure" -> tree.foldToListOfTreesAndDepth.map { case (tree, depth) => Map("depth" -> depth, "indent" -> List.fill(depth)("&nbsp;").mkString, "description" -> tree.payload.description) }))
  }

}

object DisplayStructure extends DisplayStructure {

  implicit val defaultDisplayStructure = this
}

class DisplayStructureController(tree: ServiceTree[_, _, ServiceDescription], templateName: String = "structure.mustache")(implicit templateMaker: TemplateMaker, displayStructure: Templateable[ServiceTree[_, _, ServiceDescription]]) extends Controller {
  get("/internal/structure") { request: Request => response.ok.html(templateMaker(templateName)(displayStructure(tree))) }

}