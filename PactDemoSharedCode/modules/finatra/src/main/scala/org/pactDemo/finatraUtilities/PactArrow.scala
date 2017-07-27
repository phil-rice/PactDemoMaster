package org.pactDemo.finatraUtilities

import com.twitter.util.Future

/**
  * Created by prasenjit.b on 7/6/2017.
  */
//trait PackArrow[T]

trait PactArrow{

  implicit class AnyPimper[T](t: T) {
    def ~>[T1](fn: T => T1) = fn(t)
  }

  implicit class FutureArrowPimper[T](t: Future[T]) {
    def ~>[T1](fn: T => T1) = t.map(fn)
  }
}

object PactArrow extends PactArrow