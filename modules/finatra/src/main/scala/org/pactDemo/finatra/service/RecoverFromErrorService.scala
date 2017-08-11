package org.pactDemo.finatra.service

import com.twitter.util.{Future, Try}
import org.pactDemo.finatra.structure.ServiceLanguageExtension
import org.pactDemo.finatra.utilities.Futures.tryToFuture

import scala.reflect.ClassTag

class RecoverFromErrorService[Req, Res, NewRes](delegate: Req => Future[Res], transform: (Req, Try[Res]) => Try[NewRes]) extends (Req => Future[NewRes]) {
  override def apply(req: Req) = delegate(req).transform(tryRes => tryToFuture(transform(req, tryRes)))
}

object RecoverFromErrorService {
  def apply[Req, Res](transform: PartialFunction[(Req, Try[Res]), Try[Res]])(delegate: Req => Future[Res]): RecoverFromErrorService[Req, Res, Res] = {
    new RecoverFromErrorService[Req, Res, Res](delegate, (req, tryRes) => transform.applyOrElse[(Req, Try[Res]), Try[Res]]((req, tryRes), _ => tryRes))
  }
}

trait RecoverFromErrorServiceLanguage extends ServiceLanguageExtension {
  def recoverFromError[Req:ClassTag, Res:ClassTag](transform: PartialFunction[(Req, Try[Res]), Try[Res]]): ServiceDelegator[Req, Res] = childTree => delegate("RecoverFromError", childTree, RecoverFromErrorService(transform))
}