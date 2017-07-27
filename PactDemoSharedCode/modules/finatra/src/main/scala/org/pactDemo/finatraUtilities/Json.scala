package org.pactDemo.finatraUtilities

import java.text.SimpleDateFormat
import java.util.Calendar

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.ClassTag

trait Json {
  private def currentTime: String = new SimpleDateFormat("dd-MMM-yy hh:mm:ss a").format(Calendar.getInstance.getTime)

  private def getMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)

  def fromJson[T: ClassTag](s: String): T = getMapper.readValue(s, implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[T]])
}

object Json extends Json {
  implicit val defaultJson = this
}