package org.pactDemo.finatraUtilities

import com.twitter.util.{Future, Return, Throw}
import org.pactDemo.utilities.{MockNanoTimeService, PactDemoSpec}
import Futures._

class LoggingMemoriserSpec extends PactDemoSpec {
  val runtimeException = new RuntimeException

  val expectedLoggingMessages = List(
    LoggingRecord(1000, Info, "name1", "msg1", None),
    LoggingRecord(1100, Info, "name2", "msg2", Some(runtimeException)),
    LoggingRecord(1200, Debug, "name3", "msg3", None),
    LoggingRecord(1300, Debug, "name4", "msg4", Some(runtimeException)))

  def logSomeMessages(simpleLogMe: SimpleLogMe): Unit = {
    simpleLogMe.info("name1", "msg1")
    simpleLogMe.info("name2", "msg2", Some(runtimeException))
    simpleLogMe.debug("name3", "msg3")
    simpleLogMe.debug("name4", "msg4", Some(runtimeException))

  }

  def setUpMocks(fn: LoggingAdapter => SimpleLogMe => Unit) = {
    implicit val nanoTimeService = new MockNanoTimeService()
    implicit val loggingAdaptor = NullSl4jLoggingAdapter
    implicit val simpleLogMe = new SimpleLogMe()

  }


  behavior of "LoggingMemoriser.traceNow"
  it should "have a traceId" in {
    setUpMocks { implicit loggingAdaptor =>
      simpleLogMe =>
        val LoggingReport(Return(result), records) = implicitly[LoggingMemoriser].traceNow {
          loggingAdaptor.traceId.get //throws exception if no traceId
        }
    }
  }
  it should "remember logging events as well as a successful result" in {
    setUpMocks { implicit loggingAdaptor =>
      simpleLogMe =>
        val LoggingReport(Return(result), records) = implicitly[LoggingMemoriser].traceNow {
          logSomeMessages(simpleLogMe)
          "someResult"
        }
        result shouldBe "someResult"
        records.toList shouldBe expectedLoggingMessages
    }
  }

  it should "remember logging events as well as an exception" in {
    setUpMocks { implicit loggingAdaptor =>
      simpleLogMe =>
        val LoggingReport(Throw(result), records) = implicitly[LoggingMemoriser].traceNow[String] {
          logSomeMessages(simpleLogMe)
          throw runtimeException
        }
        result shouldBe runtimeException
        records.toList shouldBe expectedLoggingMessages
    }
  }
  behavior of "LoggingMemoriser.trace"

  it should "have a traceId" in {
    setUpMocks { implicit loggingAdaptor =>
      simpleLogMe =>
        val LoggingReport(Return(result), records) = implicitly[LoggingMemoriser].trace {
          loggingAdaptor.traceId.get //throws exception if no traceId
          Future.value("")
        }.await
    }
  }

  it should "remember logging events as well as a successful result" in {
    setUpMocks { implicit loggingAdaptor =>
      simpleLogMe =>
        val LoggingReport(Return(result), records) = implicitly[LoggingMemoriser].trace {
          logSomeMessages(simpleLogMe)
          Future.value("someResult")
        }.await
        result shouldBe "someResult"
        records.toList shouldBe expectedLoggingMessages
    }
  }

  it should "remember logging events as well as an exception" in {
    setUpMocks { implicit loggingAdaptor =>
      simpleLogMe =>
        val LoggingReport(Throw(result), records) = implicitly[LoggingMemoriser].trace[String] {
          logSomeMessages(simpleLogMe)
          throw runtimeException
        }.await
        result shouldBe runtimeException
        records.toList shouldBe expectedLoggingMessages
    }
  }

}
