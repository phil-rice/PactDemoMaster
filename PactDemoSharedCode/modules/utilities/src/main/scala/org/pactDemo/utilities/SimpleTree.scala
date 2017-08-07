package org.pactDemo.utilities

trait SimpleTree[T] {
  def payload: T
  def map[T1](fn: T => T1): SimpleTree[T1]
  def fold: List[T]
}

case class SimpleTreeRoot[T](payload: T) extends SimpleTree[T] {
  override def map[T1](fn: (T) => T1): SimpleTree[T1] = SimpleTreeRoot(fn(payload))

  override def fold: List[T] = List(payload)
}

case class SimpleTreeOneChild[T](payload: T, child: SimpleTree[T]) extends SimpleTree[T] {
  override def map[T1](fn: (T) => T1): SimpleTree[T1] =SimpleTreeOneChild(fn(payload), child.map(fn))

  override def fold: List[T] = payload :: child.fold
}

