package org.pactDemo.finatraUtilities

import com.twitter.util.Future
import org.mockito.Matchers
import org.pactDemo.utilities.{MockNanoTimeService, NanoTimeService, PactDemoSpec}
import scala.language.postfixOps
import scala.concurrent.duration._

class CacheServiceSpec extends PactDemoSpec {

  import org.mockito.Mockito._
  import Futures._

  behavior of "CacheService"

  def withMocks(fn: (CacheService[Int, String], Int => Future[String], CachingMetrics, StaleStrategy[String]) => Unit): Unit = {
    val delegate = mock[Int => Future[String]]

    val cachingMetrics = new CachingMetrics
    implicit val cacheServiceSize = new SimpleCheckSizeCache(10, 2)
    implicit val timeService = new MockNanoTimeService
    implicit val staleCacheStrategy = mock[StaleStrategy[String]]
    fn(new CacheService("someName", delegate, cachingMetrics), delegate, cachingMetrics, staleCacheStrategy)
  }

  def checkMetrics(cacheService: CacheService[Int, String], queries: Long = 0, hits: Long = 0, created: Long = 0, passedThrough: Long = 0, removed: Long = 0, size: Int = 0) = {
    val metrics = cacheService.metrics
    withClue("queries")(metrics.queries shouldBe queries)
    withClue("hits")(metrics.hits shouldBe hits)
    withClue("passedThrough")(metrics.passedThrough shouldBe passedThrough)
    withClue("removed")(metrics.removed shouldBe removed)
    withClue("size")(metrics.size shouldBe size)
  }

  it should "return the delegate value the first time" in {
    withMocks { (cacheService, delegate, metrics, staleStrategy) =>
      when(delegate.apply(1)) thenReturn Future.value("one")
      when(staleStrategy.apply(Matchers.any[CacheData[String]])) thenReturn false
      cacheService(1).await shouldBe "one"
      checkMetrics(cacheService, queries = 1, passedThrough = 1, size = 1)

    }
  }
  it should "return the delegate value the first time, then cached values if not stale" in {
    withMocks { (cacheService, delegate, metrics, staleStrategy) =>
      when(delegate.apply(1)) thenReturn Future.value("one")
      when(staleStrategy.apply(Matchers.any[CacheData[String]])) thenReturn false
      cacheService(1).await shouldBe "one"
      cacheService(1).await shouldBe "one"
      cacheService(1).await shouldBe "one"
      cacheService(1).await shouldBe "one"
      checkMetrics(cacheService, queries = 4, passedThrough = 1, size = 1)
    }
  }
  it should "return the delegate value the first time, then cached values until stale then try again" in {
    withMocks { (cacheService, delegate, metrics, staleStrategy) =>
      when(delegate.apply(1)) thenReturn(Future.value("one"), Future.value("two"))
      when(staleStrategy.apply(Matchers.any[CacheData[String]])) thenReturn(false, false, true, true, false) // remembed double check if true... that's really one true
      cacheService(1).await shouldBe "one"
      cacheService(1).await shouldBe "one"
      cacheService(1).await shouldBe "two"
      cacheService(1).await shouldBe "two"
      checkMetrics(cacheService, queries = 4, passedThrough = 2, size = 1)
    }
  }

}

//class DuractionStaleStrategySpec extends PactDemoSpec {
//  behavior of "DuractionStaleStrategy"
//
//  def withMocks(fn: (DurationStaleStrategy[Int], NanoTimeService) => Unit) = {
//    implicit val nanoTimeService = mock[NanoTimeService]
//    fn(new DurationStaleStrategy[Int](1300 nanos), nanoTimeService)
//  }
//
//  it should "report not stale if 'now' is before the next stale time" in {
//    withMocks { (stategy, timeService: NanoTimeService)  =>
//      stategy(CacheData[Int]())
//    }
//  }
//}