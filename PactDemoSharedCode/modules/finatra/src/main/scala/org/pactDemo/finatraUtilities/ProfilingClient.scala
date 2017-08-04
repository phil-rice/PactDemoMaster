package org.pactDemo.finatraUtilities

import java.util.concurrent.atomic.AtomicLong

import com.twitter.util.Future

trait NanoTimeService {
  def apply(): Long
}

object NanoTimeService {

  implicit object DefaultNanoTimeService extends NanoTimeService {
    override def apply(): Long = System.nanoTime()
  }

}

class ProfilingStats {
  val durationTotal = new AtomicLong()
  val count = new AtomicLong()

  def apply(duration: Long) = {
    durationTotal.addAndGet(duration)
    count.incrementAndGet()
  }
}

class ProfilingClient[Req, Res](profilingStats: ProfilingStats, delegate: Req => Future[Res])(implicit nanoTimeService: NanoTimeService) extends (Req => Future[Res]) {


  override def apply(v1: Req): Future[Res] = {
    val startTime = nanoTimeService()
    delegate(v1).transform { tryX =>
      profilingStats(nanoTimeService() - startTime)
      Futures.tryToFuture(tryX)
    }
  }
}
