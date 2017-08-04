package org.pactDemo.utilities

case class IndentAnd[T](indent: Int, t: T) {
  if (indent < 0) throw new IllegalArgumentException(s"Cannot have -ve indent for $indent/$t")

  def asString(indentCh: Char = ' ') = List.fill(indent)(indentCh).mkString("", "", t.toString)

  def indent(t: T): IndentAnd[T] = IndentAnd(indent + 1, t)

  def mapContents[T1](fn: T => T1) = IndentAnd(indent, fn(t))
}

object IndentAnd {
  def asString[T](separator: String, indentCh: Char = ' ')(seq: Seq[IndentAnd[T]]) = {
    seq.map(_.asString(indentCh)).mkString(separator)
  }

  implicit class listOfIndentAndStringPimper[T](s: Seq[IndentAnd[T]]) {
    def asString(separator: String, indentCh: Char = ' ') = IndentAnd.asString(separator, indentCh)(s)

    def mapContents[T1](fn: T => T1) = s.map(_.mapContents(fn))
  }

}
