package org.pactDemo.finatra.service

import java.util.{logging => javalog}

import com.twitter.util.Future
import org.pactDemo.finatra.structure.ServiceLanguageExtension
import org.pactDemo.finatra.utilities.{Futures, LogData, LogMe}

import scala.reflect.ClassTag

class LoggingClient[Req, Res](name: String, prefix: String, delegate: Req => Future[Res])(implicit reqLog: LogData[Req], resLog: LogData[Res], log: LogMe) extends (Req => Future[Res]) {
  override def apply(req: Req): Future[Res] = {
    println(s"Logging client ${req} Log: $log TraceId ${log.traceId}")
    log.enter(name, prefix, req)
    delegate(req).transform { tryResult =>
      log.exit(name, prefix, req, tryResult)
      Futures.tryToFuture(tryResult)
    }
  }
}

trait LoggingClientServiceLanguageExtension extends ServiceLanguageExtension {

  def logging[Req: ClassTag, Res: ClassTag, OldService <: Req => Future[Res]](name: String, prefix: String)(implicit reqLog: LogData[Req], resLog: LogData[Res], log: LogMe): ServiceDelegator[Req, Res] =
  { childTree => delegate(s"LoggingClient($name,$prefix)", childTree, new LoggingClient[Req, Res](name, prefix, _))
  }
}