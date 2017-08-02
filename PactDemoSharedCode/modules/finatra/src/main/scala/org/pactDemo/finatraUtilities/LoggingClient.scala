package org.pactDemo.finatraUtilities

import com.twitter.logging.Logger
import com.twitter.util.{Future, Return, Throw, Try}
import java.util.{logging => javalog}

trait LogData[T] {
  def detailed(t: T): String

  def summary(t: T): String
}

object LogData {
  implicit def logDataDefault[T] = new LogData[T] {
    override def detailed(t: T): String = t.toString

    override def summary(t: T): String = s"${t.getClass}@${System.identityHashCode(t)}"
  }
}

trait LogMe {
  def debug(name: String, s: String)

  def debug(name: String, s: String, e: Throwable)

  def info(name: String, s: String)

  def info(name: String, s: String, e: Throwable)

  def enter[Req](name: String, prefix: String, t: Req)(implicit logData: LogData[Req]) = {
    debug(name, logData.detailed(t))
  }

  def exit[Req, Res](name: String, prefix: String, req: Req, res: Try[Res])(implicit reqLog: LogData[Req], resLog: LogData[Res]) = {
    val start = prefix + reqLog.detailed(req)
    res match {
      case Return(r) => debug(name, start + " => " + resLog.detailed(r))
      case Throw(r) => debug(name, start + " => exception " + r, r)
    }
  }
}

object PrintlnLogMe extends LogMe {
  private def print(name: String, s: String) = println(name + " " + s)

  override def debug(name: String, s: String): Unit = print(name, s)

  override def debug(name: String, s: String, e: Throwable): Unit = print(name, s)

  override def info(name: String, s: String): Unit = print(name, s)

  override def info(name: String, s: String, e: Throwable): Unit = print(name, s)
}

class LoggingClient[Req, Res](name: String, prefix: String, delegate: Req => Future[Res])(implicit reqLog: LogData[Req], resLog: LogData[Res], log: LogMe) extends (Req => Future[Res]) {
  override def apply(req: Req): Future[Res] = {
    log.enter(name, prefix, req)
    delegate(req).transform { tryResult =>
      log.exit(name, prefix, req, tryResult)
      Futures.tryToFuture(tryResult)
    }
  }
}
