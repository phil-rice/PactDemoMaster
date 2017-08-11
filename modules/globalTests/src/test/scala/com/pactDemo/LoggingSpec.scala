package com.pactDemo

import org.pactDemo.finatra.utilities.FinagleLoggingAdapter
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.BeforeAndAfter


class LoggingSpec extends PactDemoSpec with BeforeAndAfter {

//  before(FinagleLoggingAdapter.clearTraceId)

  behavior of "FinatraLoggingAdapter"

  it should "implement traceid" in {
    println("In logging spec")
    FinagleLoggingAdapter.traceId shouldBe None
    println("After traceId")
    FinagleLoggingAdapter.setTraceId
    val Some(traceId1) = FinagleLoggingAdapter.traceId

    FinagleLoggingAdapter.clearTraceId
    FinagleLoggingAdapter.traceId shouldBe None

    FinagleLoggingAdapter.setTraceId
    val Some(traceId2) = FinagleLoggingAdapter.traceId

    traceId1 shouldNot be(traceId2)
  }

}
