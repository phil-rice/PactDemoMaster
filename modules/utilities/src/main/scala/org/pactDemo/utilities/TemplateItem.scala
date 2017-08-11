package org.pactDemo.utilities


case class TemplateItem(contents: Any)


trait Templateable[T] extends (T => TemplateItem)

trait TemplateMaker {
  def apply(name: String): (TemplateItem => String)
}

