package org.pactDemo.finatraUtilities

import com.twitter.logging.Logger
import com.twitter.util.Future

import scala.util.Try

trait Pactlogger {
  def apply(msg: String)
}

object Pactlogger {

  implicit object PrintlnPactLogger extends Pactlogger {
    override def apply(msg: String): Unit = println(msg)
  }

}

trait PactArrow  {

  protected var debugArrows = false

  private def debug[T, T1](t: T, t1: => T1)(implicit pactlogger: Pactlogger): T1 = {
    val result = Try(t1)
    pactlogger(s"Arrow ${}. Input $t Output $result")
    result.get
  }

  implicit class AnyPimper[T](t: T)(implicit pactlogger: Pactlogger) {
    def ~>[T1](fn: T => T1) = debug(t, fn(t))
  }

  implicit class FutureArrowPimper[T](t: Future[T])(implicit pactlogger: Pactlogger) {
    def ~>[T1](fn: T => T1) =  t.map(x => debug(x, fn(x)))
  }

}

object PactArrow extends PactArrow