package org.pactDemo.finatraUtilities

import com.twitter.util._

object Futures {

  implicit class FuturePimper[T](f: Future[T]) {
    def await: T = Await.result(f)
  }

  def tryToFuture[T](t: Try[T]) = t match{
    case Return(r)=> Future.value(r)
    case Throw(e) => Future.exception(e)
  }

}