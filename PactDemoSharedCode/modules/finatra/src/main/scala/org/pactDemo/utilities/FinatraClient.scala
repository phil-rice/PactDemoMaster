package org.pactDemo.utilities

import com.twitter.finagle.http.Request
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Future
import com.twitter.inject.Logging




class FinatraClient(host: String, port: Int, fn: String => String) extends Service[Int, String] with com.twitter.inject.Logging {

  info(s"Trying to start with $host:$port")
  println(s"Trying to start with $host:$port - println")
  val service = Http.newService(s"$host:$port")

  override def apply(id: Int): Future[String] = {
    val request = Request(s"/id/$id")
    request.host = host
    service(request).map { res =>
      val result = fn(res.contentString)
      println(s"println inside map for FinatraClient($host:$port) request is $request ${res.contentString} => ${result} and the result was $res")
      result
    }
  }

}
