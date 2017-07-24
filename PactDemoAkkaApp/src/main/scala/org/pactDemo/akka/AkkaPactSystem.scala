package org.pactDemo.akka

import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.duration._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future
import org.pactDemo.utilities.{CustomeRequestProcessor, CustomeResponseProcessor, GenericCustomClient, PactArrow}

import scala.concurrent.Await


object Util {
  def currentTime: String = new SimpleDateFormat("dd-MMM-yy hh:mm:ss a").format(Calendar.getInstance.getTime)

  def getMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
}

case class ProcessRequest(input: String)

case object closeProcess

trait StringToObject[String, T] extends (String => T)

object StringToObject {

  implicit object makeObject extends StringToObject[String, CustomRequestObject] {
    override def apply(input: String): CustomRequestObject = {
      Util.getMapper.readValue(input, classOf[CustomRequestObject])
    }
  }

}

case class CustomRequestObject(id: Int, token: String)

object CustomRequestObject {

  implicit object makeRequest extends CustomeRequestProcessor[CustomRequestObject] {
    override def apply(custObject: CustomRequestObject): Request = {
      val request = Request(s"/token/android/post")
      request.headerMap.add("ContentType", "application/hcl.token")
      request.method = Method.Post
      request.setContentString(s"""{"id": "${custObject.id}", "token":"${custObject.token}"}""")
      request
    }
  }

}

case class CustomReplyObject(id: Int, token: String, valid: Boolean) //, server: String

object CustomReplyObject {

  implicit object makeCustomResponse extends CustomeResponseProcessor[CustomReplyObject] with PactArrow {
    override def apply(response: Response): CustomReplyObject = {
      //println(s"\n response.contentString : ${response.contentString}\n")
      Util.getMapper.readValue(response.contentString, classOf[CustomReplyObject])
    }
  }

}

case class CustomRequest(param: CustomRequestObject)

class AkkaPactSystem(restClient: GenericCustomClient[CustomRequestObject, CustomReplyObject]) extends Actor with PactArrow {

  override def receive: Receive = {
    case ProcessRequest(input: String) => {

      val convert = implicitly[StringToObject[String, CustomRequestObject]]
      val inputRequest = input ~> convert

      val requestProcessor: ActorRef = context.actorOf(Props(new CustomRequestProcessActor(restClient)), s"${inputRequest.id}-Processor")

      requestProcessor ! CustomRequest(inputRequest)
    }

    case closeProcess =>
      println("System is going to shutdown @ " + Util.currentTime)
      context.system.terminate
  }
}

class CustomRequestProcessActor(restClient: GenericCustomClient[CustomRequestObject, CustomReplyObject]) extends Actor with PactArrow {

  override def receive: Receive = {
    case CustomRequest(request) =>

      val pureResponse = restClient(request)

     // val returnVal : Future[CustomReplyObject] = pureResponse.map(x => x )// onSuccess(println(_))
     // returnVal

      pureResponse.onSuccess(println(_))

      Thread.sleep(500)

    case _ => println("None - valid")
  }
}

object AkkaPactSystem {

  def akkaProcessing(host: String, port: String) = {
    val system = ActorSystem("AkkaPactSystem")

    val rawHttpClient = Http.newService(host + ":" + port)
    val restClient = new GenericCustomClient[CustomRequestObject, CustomReplyObject](rawHttpClient)

    val mainProcess: ActorRef = system.actorOf(Props(new AkkaPactSystem(restClient)), "Main-Process")

    mainProcess ! ProcessRequest("""{"id": 1, "token":"12345-valid-for-id-1-token"}""")
    mainProcess ! ProcessRequest("""{"id": 2, "token":"54321-invalid-for-id-2-token"}""")


  //  implicit val timeout = Timeout(5 seconds)
  //  val future = mainProcess ? ProcessRequest("""{"id": 1, "token":"12345-valid-for-id-1-token"}""") // enabled by the “ask” import
  //  val result = Await.result(future, timeout.duration).asInstanceOf[Future[CustomReplyObject]]

  //  result.onSuccess(println(_))

    Thread.sleep(5)
    mainProcess ! closeProcess
  }

  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = "9090"
    akkaProcessing(host, port)
  }

}