package org.pactDemo.utilities

trait NanoTimeService {
  def apply(): Long
}

object NanoTimeService {

  implicit object DefaultNanoTimeService extends NanoTimeService {
    override def apply(): Long = System.nanoTime()
  }

}