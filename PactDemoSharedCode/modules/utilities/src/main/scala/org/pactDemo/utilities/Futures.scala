package org.pactDemo.utilities

import com.twitter.util.{Await, Future}

object Futures {

  implicit class FuturePimper[T](f: Future[T]) {
    def await: T = Await.result(f)
  }

}