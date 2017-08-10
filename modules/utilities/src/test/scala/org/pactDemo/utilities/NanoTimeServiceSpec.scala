package org.pactDemo.utilities

import java.util.concurrent.atomic.AtomicLong


class MockNanoTimeService(start: Long = 1000, offset: Long = 100) extends NanoTimeService {
  val now = new AtomicLong(start)

  override def apply(): Long = now.getAndAdd(offset)
}

class NanoTimeServiceSpec extends PactDemoSpec {

  behavior of "DefaultNanoTimeService"

  it should "be the System nanotime" in {
    val timeService = implicitly[NanoTimeService]
    val time0 = System.nanoTime()
    val time1 = timeService()
    val time2 = System.nanoTime()
    Thread.sleep(2)
    val time3 = timeService()
    val time4 = System.nanoTime()
    time0 should be <= time1
    time1 should be <= time2
    time2 should be <= time3
    time3 should be <= time4
  }
}
