package org.pactDemo.akka

import java.text.SimpleDateFormat
import java.util.Calendar

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finatra.http
import org.pactDemo.utilities.PactArrow


object Util {
  def currentTime: String = new SimpleDateFormat("dd-MMM-yy hh:mm:ss a").format(Calendar.getInstance.getTime)
}

case class ProcessRequest(input: String)

case object closeProcess

trait StringToObject[String, ToObj ] extends ( String => ToObj )
case class CustomRequestObject(id: Int, token: String, requestType: String)
object CustomRequestObject {
  implicit object makeObject extends StringToObject[ String, CustomRequestObject] {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    override def apply(input: String): CustomRequestObject = {
      mapper.readValue(input, classOf[CustomRequestObject])
    }
  }
}

trait CoreRequest

case class ValidRequestProcess(param: CustomRequestObject) extends CoreRequest
case class InvalidRequestProcess(param: CustomRequestObject) extends CoreRequest

class AkkaPactSystem extends Actor with PactArrow {

  override def receive: Receive = {
    case ProcessRequest(input: String) => {

      val convert = implicitly[StringToObject[String, CustomRequestObject]]
      val inputInfo = input ~> convert

      if(inputInfo.id == 1143) {
        val validProcess: ActorRef = context.actorOf(Props[ValidProcessActor], s"${inputInfo.requestType}-Process")
        validProcess ! ValidRequestProcess (inputInfo)
      } else if(inputInfo.id == 9905) {
        val invalidProcess: ActorRef = context.actorOf(Props[InvalidProcessActor], s"${inputInfo.requestType}-Process")
        invalidProcess ! InvalidRequestProcess (inputInfo)
      }
    }
    case closeProcess =>
      println("System is going to shutdown @ " + Util.currentTime)
      context.system.terminate
  }
}

class ValidProcessActor extends Actor {
  override def receive: Receive = {
    case ValidRequestProcess(inputInfo) =>
      println("---> " + inputInfo.id + " ::: " + inputInfo.requestType + " ::: " + inputInfo.token + "\n\n")


    case _ => println("None - valid")
  }
}

class InvalidProcessActor extends Actor {
  override def receive: Receive = {
    case InvalidRequestProcess(inputInfo) =>
      println("---> " + inputInfo.id + " ::: " + inputInfo.requestType + " ::: " + inputInfo.token + "\n\n")


    case _ => println("None - invalid")
  }
}

object AkkaPactSystem {

  def akkaProcessing() = {
    val system = ActorSystem("AkkaPactSystem")
    val mainProcess: ActorRef = system.actorOf(Props[AkkaPactSystem], "Main-Process")

    mainProcess ! ProcessRequest ("""{"id": 1143, "token":"1143-valid-token", "requestType":"valid" }""")
    mainProcess ! ProcessRequest ("""{"id": 9905, "token":"9905-invalid-token", "requestType":"invalid"}""")

    Thread.sleep(50)
    mainProcess ! closeProcess
  }

  def main(args: Array[String]): Unit = {
    akkaProcessing
  }


}