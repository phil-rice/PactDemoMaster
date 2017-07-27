package org.pactDemo.utilities

case class StartNotPresentException(firstWord: String, s: String) extends Exception

object Strings {
  def removeStart(firstWord: String)(s: String) = if (s.startsWith(firstWord)) s.substring(firstWord.length) else throw new StartNotPresentException(firstWord, s)

  def lastSegmentOf(s: String): String = s.split("/").filter(_.length > 0).lastOption.getOrElse("")
}
