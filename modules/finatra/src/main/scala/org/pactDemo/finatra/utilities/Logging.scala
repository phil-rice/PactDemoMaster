package org.pactDemo.finatra.utilities

import com.twitter.inject.{Logging => TLogging}
import com.twitter.util.logging.Logger
import org.pactDemo.utilities.NanoTimeService
import org.slf4j.MDC
//import java.util.logging.{Level, Logger}


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

sealed trait PactLoggingLevel

case object Debug extends PactLoggingLevel

case object Info extends PactLoggingLevel

trait LoggingAdapter {

  def isDebugEnabled(name: String): Boolean

  def isInofEnabled(name: String): Boolean

  def log(loggingRecord: LoggingRecord)

  def setTraceId

  def clearTraceId

  def traceId: Option[String]
}


trait LogMe {
  def debug(name: String, s: => Any, e: Option[Throwable] = None)

  def info(name: String, s: => Any, e: Option[Throwable] = None)

  def traceId: Option[String]

  def enter[Req](name: String, prefix: String, t: Req)(implicit logData: LogData[Req]) = debug(name, logData.detailed(t))


  def exit[Req, Res](name: String, prefix: String, req: Req, res: Res)(implicit reqLog: LogData[Req], resLog: LogData[Res], resFailed: Failed[Res]) =
    debug(name, prefix + reqLog.detailed(req) + " => " + resLog.detailed(res), resFailed(res))
}

class SimpleLogMe(implicit loggingAdapter: LoggingAdapter, nanoTimeService: NanoTimeService, loggingMemoriser: LoggingMemoriser) extends LogMe {

  def debug(name: String, s: => Any, e: Option[Throwable] = None) =
    if (loggingAdapter.isDebugEnabled(name) || loggingAdapter.traceId.isDefined) {
      val loggingRecord = LoggingRecord(nanoTimeService(), Debug, name, s.toString, e)
      loggingMemoriser.remember(loggingRecord)
      loggingAdapter.log(loggingRecord)
    }

  def info(name: String, s: => Any, e: Option[Throwable] = None) =
    if (loggingAdapter.isInofEnabled(name) || loggingAdapter.traceId.isDefined) {
      val loggingRecord = LoggingRecord(nanoTimeService(), Info, name, s.toString, e)
      loggingMemoriser.remember(loggingRecord)
      loggingAdapter.log(loggingRecord)
    }

  override def traceId: Option[String] = loggingAdapter.traceId
}

trait Sl4jTraceId {
  def setTraceId: Unit = {
    val id = LoggingMemoriser.nextTraceId.toString
    MDC.put("traceId", id)
    if (traceId != Some(id)) throw new RuntimeException(s"Failed to set trace id to $id. Actual value is $traceId. This is $this")

  }

  def clearTraceId: Unit = {
    MDC.remove("traceId")
  }

  def traceId: Option[String] = Option(MDC.get("traceId"))
}

object NullSl4jLoggingAdapter extends LoggingAdapter with Sl4jTraceId {

  override def log(loggingRecord: LoggingRecord): Unit = {}

  override def isDebugEnabled(name: String): Boolean = false

  override def isInofEnabled(name: String): Boolean = false
}

object FinagleLoggingAdapter extends LoggingAdapter with Sl4jTraceId {


  override def log(loggingRecord: LoggingRecord): Unit = loggingRecord.level match {
    case Info => getLogger(loggingRecord.name).info(loggingRecord.msg, loggingRecord.throwable.getOrElse(null))
    case Debug => getLogger(loggingRecord.name).debug(loggingRecord.msg, loggingRecord.throwable.getOrElse(null))
  }

  private def getLogger(name: String) = Logger(name)

  override def isDebugEnabled(name: String): Boolean = getLogger(name).isDebugEnabled

  override def isInofEnabled(name: String): Boolean = getLogger(name).isInfoEnabled
}



