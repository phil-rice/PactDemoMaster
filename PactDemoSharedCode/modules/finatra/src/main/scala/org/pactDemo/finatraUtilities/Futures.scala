package org.pactDemo.finatraUtilities

import com.twitter.util._
import org.pactDemo.utilities.{Failed, LogData}

object Futures {

  implicit class FuturePimper[T](f: Future[T]) {
    def await: T = Await.result(f)
  }

  def tryToFuture[T](t: Try[T]) = t match {
    case Return(r) => Future.value(r)
    case Throw(e) => Future.exception(e)
  }

  implicit def tryToFailed[T] = new Failed[Try[T]] {
    override def apply(t: Try[T]): Option[Throwable] = t match {
      case Return(_) => None
      case Throw(t) => Some(t)
    }
  }

  implicit def tryLogData[T](implicit logData: LogData[T]) = new LogData[Try[T]] {
    override def detailed(t: Try[T]): String = t match {
      case Return(r) => logData.detailed(r)
      case Throw(e) => e.toString
    }

    override def summary(t: Try[T]): String =  t match {
      case Return(r) => logData.summary(r)
      case Throw(e) => e.toString
    }
  }

}