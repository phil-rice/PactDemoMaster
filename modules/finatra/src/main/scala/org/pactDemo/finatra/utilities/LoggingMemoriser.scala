package org.pactDemo.finatra.utilities

import java.util.concurrent.atomic.AtomicInteger

import com.twitter.util.{Future, Return, Throw, Try}

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

case class LoggingReport[X](result: Try[X], records: mutable.MutableList[LoggingRecord]) {
  def map[T1](fn: X => T1) = LoggingReport(result.map(fn), records)
}

case class LoggingRecord(time: Long, level: PactLoggingLevel, name: String, msg: String, throwable: Option[Throwable])

trait LoggingMemoriser {

  private val id = new AtomicInteger()

  def nextTraceId = id.incrementAndGet()

  private val recorded = TrieMap[String, scala.collection.mutable.MutableList[LoggingRecord]]()

  def remember(loggingRecord: LoggingRecord)(implicit loggingAdapter: LoggingAdapter) = {
    println(s"In 'remember($loggingRecord)'")
    loggingAdapter.traceId match {
      case None =>
      case Some(traceId) =>
        recorded.getOrElseUpdate(traceId, new mutable.MutableList[LoggingRecord]) += loggingRecord
    }
  }

  def makeReport[X](tryX: Try[X])(implicit loggingAdapter: LoggingAdapter) = {
    val optionRecords = for {
      id <- loggingAdapter.traceId
      vec <- recorded.remove(id)
    } yield {
      vec
    }
    loggingAdapter.clearTraceId
    LoggingReport(tryX, optionRecords.getOrElse(mutable.MutableList()))
  }

  def traceNow[X](block: => X)(implicit loggingAdapter: LoggingAdapter): LoggingReport[X] = {
    try {
      loggingAdapter.setTraceId
      makeReport(Return(block))
    } catch {
      case e: Exception => makeReport(Throw(e))
    }
  }

  def trace[X](block: => Future[X])(implicit loggingAdapter: LoggingAdapter): Future[LoggingReport[X]] = {
    loggingAdapter.setTraceId
    println(s"In trace. Setting trace Id to ${loggingAdapter.traceId}")
    block.transform(tryx => Future.value(makeReport[X](tryx)))
  }
}

object LoggingMemoriser extends LoggingMemoriser {
  implicit val defaultMemorise = this
}


