package org.pactDemo.finatra

import com.twitter.util.Future

class MutableService[Req, Res] extends (Req => Future[Res]) {
  var delegate: (Req => Future[Res]) = null

  override def apply(v1: Req): Future[Res] = delegate(v1)
}
