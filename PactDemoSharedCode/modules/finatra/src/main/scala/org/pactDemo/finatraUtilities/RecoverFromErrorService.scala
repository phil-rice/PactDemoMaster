package org.pactDemo.finatraUtilities

import com.twitter.util.{Future, Try}
import Futures._

class RecoverFromErrorService[Req, Res, NewRes](delegate: Req => Future[Res], transform: (Req, Try[Res]) => Try[NewRes]) extends (Req => Future[NewRes]) {
  override def apply(req: Req) = delegate(req).transform(tryRes => tryToFuture(transform(req, tryRes)))
}

object RecoverFromErrorService {
  def apply[Req, Res](delegate: Req => Future[Res])( transform: PartialFunction[(Req, Try[Res]), Try[Res]]): RecoverFromErrorService[Req, Res, Res] = {
    new RecoverFromErrorService[Req, Res, Res](delegate, (req, tryRes) => transform.applyOrElse[(Req, Try[Res]), Try[Res]]((req, tryRes), _ => tryRes))
  }
}