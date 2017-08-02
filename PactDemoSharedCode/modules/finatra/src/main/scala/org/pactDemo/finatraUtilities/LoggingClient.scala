package org.pactDemo.finatraUtilities

import java.util.{logging => javalog}

import com.twitter.util.Future
import org.pactDemo.utilities.{LogData, LogMe}

class LoggingClient[Req, Res](name: String, prefix: String, delegate: Req => Future[Res])(implicit reqLog: LogData[Req], resLog: LogData[Res], log: LogMe) extends (Req => Future[Res]) {
  override def apply(req: Req): Future[Res] = {
    log.enter(name, prefix, req)
    delegate(req).transform { tryResult =>
      log.exit(name, prefix, req, tryResult)
      Futures.tryToFuture(tryResult)
    }
  }
}
