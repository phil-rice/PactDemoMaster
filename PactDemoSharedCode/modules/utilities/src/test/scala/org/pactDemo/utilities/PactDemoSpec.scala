package org.pactDemo.utilities

import org.mockito.ArgumentCaptor
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.reflect.ClassTag

trait PactDemoSpec extends FlatSpec with Matchers with MockitoSugar{
  def capture[T: ClassTag]: ArgumentCaptor[T] = ArgumentCaptor.forClass(implicitly[ClassTag[T]].runtimeClass).asInstanceOf[ArgumentCaptor[T]]
}