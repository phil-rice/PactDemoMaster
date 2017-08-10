package org.pactDemo.utilities

case class IllegalStatusCodeException(statusCode: Int, msg: String) extends Exception(s"StatusCode: $statusCode. $msg")