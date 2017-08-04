package org.pactDemo.finatraUtilities

import java.util.{logging => javalog}

import com.twitter.util.Future
import org.pactDemo.utilities.{LogData, LogMe, SimpleTreeOneChild}

class LoggingClient[Req, Res](name: String, prefix: String, delegate: Req => Future[Res])(implicit reqLog: LogData[Req], resLog: LogData[Res], log: LogMe) extends (Req => Future[Res]) {
  override def apply(req: Req): Future[Res] = {
    log.enter(name, prefix, req)
    delegate(req).transform { tryResult =>
      log.exit(name, prefix, req, tryResult)
      Futures.tryToFuture(tryResult)
    }
  }
}

trait LoggingClientServiceLanguageExtension extends ServiceLanguageExtension {

  def logging[Req, Res, OldService <: Req => Future[Res]](name: String, prefix: String)(implicit reqLog: LogData[Req], resLog: LogData[Res], log: LogMe):
  ServiceDelegator[Req, Res] = { childTree =>
    DelegateTree0[Req, Res, ServiceDescriptionAndCreator[Req, Res]](
      childTree, ServiceDescriptionAndCreator(
        s"LoggingClient($name,$prefix)",
        () => new LoggingClient[Req, Res](name, prefix, childTree.payload.service)
      ))

    //    DelegateCreator0[Req, Res, OldService, LoggingClient[Req, Res]](
    //      s"LoggingClient($name,$prefix)",
    //      childTree,
    //      (delegate) => new LoggingClient[Req, Res](name, prefix, delegate))
  }
}