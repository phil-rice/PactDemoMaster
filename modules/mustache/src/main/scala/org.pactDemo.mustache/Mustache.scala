package org.pactDemo.mustache

import java.io.StringWriter

import com.github.mustachejava.{DefaultMustacheFactory, Mustache => JMustache}
import com.twitter.mustache.ScalaObjectHandler
import org.pactDemo.utilities.{TemplateItem, TemplateMaker}

trait Mustache extends TemplateMaker {
  val mf = new DefaultMustacheFactory()
  mf.setObjectHandler(new ScalaObjectHandler)

  def apply(location: String): MustacheTemplate = new MustacheTemplate(mf.compile(location))
}

object Mustache extends Mustache {

  implicit val defaultMustache = this


}

class MustacheTemplate(mustache: JMustache) extends (TemplateItem => String) {
  def apply(scope: TemplateItem): String = {
    val sw = new StringWriter
    mustache.execute(sw, scope.contents)
    sw.close()
    sw.toString
  }
}