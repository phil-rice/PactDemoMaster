package org.pactDemo.utilities

trait EnvStringGetter {
  def getEnv(name: String): Option[String]
}

object EnvStringGetter {

  implicit object EnvStringGetterFromEnv extends EnvStringGetter {
    override def getEnv(name: String): Option[String] = Option(System.getenv("PORT"))
  }

}

object Heroku {

  def port(default: Int)(implicit envStringGetter: EnvStringGetter): Int = envStringGetter.getEnv("PORT") match {
    case Some(p) => p.toInt
    case _ => default
  }

  def providerHost(implicit envStringGetter: EnvStringGetter) = envStringGetter.getEnv("PORT") match {
    case Some(p) => "pact-demo-provider.herokuapp.com" // we are in the heroku world
    case _ => "localhost"
  }

  def providerPort(implicit envStringGetter: EnvStringGetter) = envStringGetter.getEnv("PORT") match {
    case Some(p) => 80 // we are in the heroku world
    case _ => 9000
  }
}
