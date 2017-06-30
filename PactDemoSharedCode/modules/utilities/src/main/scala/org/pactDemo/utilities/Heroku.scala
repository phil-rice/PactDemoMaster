package org.pactDemo.utilities

object Heroku {

  def port(default: Int): Int = Option(System.getenv("PORT")) match {
    case Some(p) => p.toInt
    case _ => default
  }

  def providerHost = Option(System.getenv("PORT")) match {
    case Some(p) => "pact-demo-provider.herokuapp.com"// we are in the heroku world
    case _ => "localhost"
  }
  def providerPort = Option(System.getenv("PORT")) match {
    case Some(p) =>80// we are in the heroku world
    case _ => 9000
  }
}
