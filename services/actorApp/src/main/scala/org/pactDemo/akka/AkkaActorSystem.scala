package org.pactDemo.akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

trait BasicResponse

case object ValidResponse extends BasicResponse

case object InValidResponse extends BasicResponse

case object closingProcess

case class RespondeCode(code: Int)



class AkkaActorSystem extends Actor {
  override def receive: Receive = {
    case ValidResponse =>
      println(" --> This is valid response with code 200\n")
      sender ! RespondeCode(200)
    case InValidResponse =>
      println(" ~~> This is valid response with code 401\n")
      sender ! RespondeCode(401)
    case closingProcess =>
      println("\n System is going to shutdown @ " + Util.currentTime)
      context.system.terminate
  }
}

case object NewValidRequestObject

case object NewInValidRequestObject

class AnotherRequest(actor: ActorRef) extends Actor {
  override def receive: Receive = {
    case NewValidRequestObject =>
      actor ! ValidResponse
    case NewInValidRequestObject =>
      actor ! InValidResponse
    case RespondeCode(code) => println(s" In 'Another Request' - process with '$code' code\n")
  }
}


object AkkaActorSystem {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("AkkaActorSystem")
    val akkaSystem: ActorRef = system.actorOf(Props[AkkaActorSystem], "AkkaActorSystem")
    val anotherRequest: ActorRef = system.actorOf(Props(classOf[AnotherRequest], akkaSystem), "AnotherRequest")

    anotherRequest ! NewValidRequestObject
    anotherRequest ! NewInValidRequestObject
    akkaSystem ! closingProcess
  }

}