package org.pactDemo.finatraUtilities

import com.twitter.util.Future
import org.pactDemo.utilities.NanoTimeService

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.{Duration, _}
import scala.language.postfixOps
import scala.reflect.ClassTag

trait CheckSizeCache extends {
  def needToRemove[K, V](map: TrieMap[K, CacheData[V]]): Boolean

  def itemsToRemove[K, V](map: TrieMap[K, CacheData[V]]): Iterable[K]
}

class SimpleCheckSizeCache(maxSize: Int = 200, toRemove: Int = 50) extends CheckSizeCache {
  override def itemsToRemove[K, V](map: TrieMap[K, CacheData[V]]) = map.keys.take(toRemove)

  override def needToRemove[K, V](map: TrieMap[K, CacheData[V]]) = map.size >= maxSize
}

trait StaleStrategy[V] {
  def apply(v: CacheData[V]): Boolean
}

class DuractionStaleStrategy[V](maxDuration: Duration)(implicit nanoTimeService: NanoTimeService) extends StaleStrategy[V] {
  val durationInNanos = maxDuration.toNanos

  override def apply(v: CacheData[V]) = {
    val now = nanoTimeService()
    now > v.time + durationInNanos
  }
}

class CacheData[V](initialFuture: Future[V])(implicit timeService: NanoTimeService) {
  private val lock = new Object
  private var future = initialFuture
  private var timeCreated = timeService()

  def time = timeCreated

  def getReplacingIfStale(fn: => Future[V])(implicit staleStrategy: StaleStrategy[V]): Future[V] = {
    if (staleStrategy(this)) synchronized {
      if (staleStrategy(this)) {
        timeCreated = timeService()
        future = fn
      }
    }
    future
  }
}

class CacheService[K, V](delegate: K => Future[V])(implicit checkSizeCache: CheckSizeCache, staleStrategy: StaleStrategy[V], timeService: NanoTimeService) extends (K => Future[V]) {
  val map = TrieMap[K, CacheData[V]]()
  private val lock = new Object

  override def apply(k: K) = {
    removeExcessItems
    map.getOrElseUpdate(k, new CacheData[V](delegate(k))).getReplacingIfStale(delegate(k))
  }

  private def removeExcessItems = {
    if (checkSizeCache.needToRemove(map)) {
      lock.synchronized(if (checkSizeCache.needToRemove(map)) {
        checkSizeCache.itemsToRemove(map).foreach(map.remove)
      })
    }
  }
}

trait CacheServiceLanguage extends ServiceLanguageExtension {
  def caching[Req:ClassTag, Res:ClassTag](maxCacheSize: Int, duration: Duration = 1 minute)(implicit timeService: NanoTimeService): ServiceDelegator[Req, Res] = { childTree =>
    implicit val checkSizeCache = new SimpleCheckSizeCache(maxCacheSize, Math.max(10, maxCacheSize / 4))
    implicit val staleStrategy = new DuractionStaleStrategy[Res](duration)
    DelegateTree0[Req, Res, ServiceDescription](
      childTree,
      ServiceDescription(s"CachingService($maxCacheSize)"),
      new CacheService[Req, Res](_))
  }

}