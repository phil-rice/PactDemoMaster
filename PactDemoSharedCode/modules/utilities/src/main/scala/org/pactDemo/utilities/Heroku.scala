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

  def providerHostAndPort(implicit envStringGetter: EnvStringGetter) =  envStringGetter.getEnv("provider").getOrElse("localhost:9000")

}
