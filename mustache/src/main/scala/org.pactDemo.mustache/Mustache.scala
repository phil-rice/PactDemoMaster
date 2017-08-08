package org.pactDemo.mustache

import java.io.{StringReader, StringWriter}

import com.github.mustachejava.{DefaultMustacheFactory, Mustache => JMustache}
import com.twitter.mustache.ScalaObjectHandler

import scala.io.Source

trait Mustache {
  val mf = new DefaultMustacheFactory()
  mf.setObjectHandler(new ScalaObjectHandler)

//  def fromString(name: String, template: String): MustacheTemplate = new MustacheTemplate(mf.compile(new StringReader(template), name))

  def apply(location: String): MustacheTemplate = new MustacheTemplate(mf.compile(location))
}

object Mustache extends Mustache {

  implicit val defaultMustache = this

}

class MustacheTemplate(mustache: JMustache) extends (Any => String) {
  def apply(scope: Any): String = {
    val sw = new StringWriter
    mustache.execute(sw, scope)
    sw.close()
    sw.toString
  }
}