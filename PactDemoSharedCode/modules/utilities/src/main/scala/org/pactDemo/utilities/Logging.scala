package org.pactDemo.utilities


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

trait Failed[T] {
  def apply(t: T): Option[Throwable]
}

object Failed {
  implicit def defaultNeverFailed[T] = new Failed[T] {
    override def apply(t: T): Option[Throwable] = None
  }
}

trait LogMe {
  def debug(name: String, s: String, e: Option[Throwable] = None)

  def info(name: String, s: String, e: Option[Throwable] = None)


  def enter[Req](name: String, prefix: String, t: Req)(implicit logData: LogData[Req]) = {
    debug(name, logData.detailed(t))
  }

  def exit[Req, Res](name: String, prefix: String, req: Req, res: Res)(implicit reqLog: LogData[Req], resLog: LogData[Res], resFailed: Failed[Res]) = {
    debug(name, prefix + reqLog.detailed(req) + " => " + resLog.detailed(res), resFailed(res))
  }
}

object NullLogMe extends LogMe {
  override def debug(name: String, s: String, e: Option[Throwable]): Unit = {}

  override def info(name: String, s: String, e: Option[Throwable]): Unit = {}
}

object PrintlnLogMe extends LogMe {
  override def debug(name: String, s: String, e: Option[Throwable]): Unit = println(name + " " + s + " " + e.getOrElse(""))

  override def info(name: String, s: String, e: Option[Throwable]): Unit = debug(name, s, e)
}