package org.pactDemo.akka

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorContext, ActorRef, ActorSystem, Props}
import akka.util.Timeout
import akka.pattern.ask
import akka.routing.RoundRobinPool

import scala.concurrent.duration._
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future
import org.pactDemo.finatraUtilities._

import scala.concurrent.{Await, ExecutionContext}


object Util {
  def currentTime: String = new SimpleDateFormat("dd-MMM-yy hh:mm:ss a").format(Calendar.getInstance.getTime)

  def getMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
}

case class ProcessRequest(input: String)

case object CloseProcess

case class CustomRequestObject(id: Int, token: String)

object CustomRequestObject {

  implicit object ToRequestForCustomRequestObject extends ToRequest[CustomRequestObject] {
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

  implicit def makeCustomResponse(implicit json: Json) = new FromResponse[CustomRequestObject, CustomReplyObject] with PactArrow {
    override def apply(request: CustomRequestObject, response: Response): CustomReplyObject = {
      json.fromJson[CustomReplyObject](response.contentString)
    }
  }

}

case class CustomRequest(param: CustomRequestObject)

trait ChildActorFactory {
  def apply(context: ActorContext): ActorRef
}

object ChildActorFactory {
  //
  //  implicit object defaultChildActorFactory extends ChildActorFactory {
  //    override def apply(context: ActorContext, id: Int, restClient: CustomRequestObject => Future[CustomReplyObject]): ActorRef = {
  //      context.actorOf(Props(new CustomRequestProcessActor(restClient)), s"${id}-Processor")
  //    }
  //  }

}

class PooledACtorFactory(numberOfActors: Int, restClient: CustomRequestObject => Future[CustomReplyObject]) extends ChildActorFactory {
  val pool = RoundRobinPool.apply(numberOfActors).props(Props(new CustomRequestProcessActor(restClient)))

  def apply(context: ActorContext): ActorRef = {
    context.actorOf(pool, "child-actor-Processor")
  }
}

class SupervisoryPactActor(restClient: CustomRequestObject => Future[CustomReplyObject], childActorFactory: ChildActorFactory)(implicit json: Json) extends Actor with PactArrow {

  implicit val timeout = Timeout(5 second)

  val requestProcessor: ActorRef = childActorFactory(context)

  def processRequest(properSender: ActorRef, processRequest: ProcessRequest): ActorRef = {

    val inputRequest = processRequest.input ~> json.fromJson[CustomRequestObject]

    import context.dispatcher

    val x: concurrent.Future[Any] = (requestProcessor ? CustomRequest(inputRequest))
    x.foreach[Any] { x => properSender ! x }
    requestProcessor
  }

  def closeProcess = {
    println("System is going to shutdown @ " + Util.currentTime)
    context.system.terminate
  }

  override def receive: Receive = {
    case p: ProcessRequest =>
      processRequest(sender(), p)
    case CloseProcess => closeProcess
  }
}

trait ProviderResponse

case object ProviderSuccessful extends ProviderResponse

case object ProviderFailure extends ProviderResponse

object CustomRequestProcessActor {
  val count = new AtomicInteger()
}

class CustomRequestProcessActor(restClient: CustomRequestObject => Future[CustomReplyObject]) extends Actor with PactArrow {
  CustomRequestProcessActor.count.incrementAndGet()

  override def receive: Receive = {
    case CustomRequest(request) =>

      try {
        val pureResponse = restClient(request)
        pureResponse.onSuccess(x => {

          x.valid match {
            case true => sender ! ProviderSuccessful
            case _ => sender ! ProviderFailure
          }
          // self ! Pr
        }).onFailure(x => {
          sender ! ProviderFailure
        })
      } catch {
        case e: Exception => sender ! ProviderFailure
      }
    case x =>
      sender ! ProviderFailure

  }
}


object SupervisoryPactActor {

  def akkaProcessing(host: String, port: String, numberOfChildActors: Int) = {
    val system = ActorSystem("AkkaPactSystem")
    val rawHttpClient = Http.newService(host + ":" + port)
    val restClient = new GenericCustomClient[CustomRequestObject, CustomReplyObject](rawHttpClient)
    val childActorFactory = new PooledACtorFactory(numberOfChildActors, restClient)
    val mainProcess: ActorRef = system.actorOf(Props(new SupervisoryPactActor(restClient, childActorFactory)), "Main-Process")

    mainProcess ! ProcessRequest("""{"id": 1, "token":"12345-valid-for-id-1-token"}""")
    mainProcess ! ProcessRequest("""{"id": 2, "token":"54321-invalid-for-id-2-token"}""")

    Thread.sleep(5000)

    mainProcess ! CloseProcess
  }

  def main(args: Array[String]): Unit = {
    val host = "localhost"
    val port = "9090"
    akkaProcessing(host, port, 10)
  }

}