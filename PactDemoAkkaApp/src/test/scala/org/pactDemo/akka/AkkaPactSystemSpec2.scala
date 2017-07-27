package org.pactDemo.akka

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import org.pactDemo.finatraUtilities.GenericCustomClient
import org.pactDemo.utilities.PactDemoSpec2
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._


class AkkaPactSystemSpec2 extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with PactDemoSpec2 {

  val pactClient = mock[GenericCustomClient[CustomRequestObject, CustomReplyObject]]
  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An AkkaPactSystem using implicit sender " should {
    "send back custom response" in {

      val pactActor = system.actorOf(Props(new AkkaPactSystem(pactClient)), "Main-Process")

      val future = pactActor ? ProcessRequest("""{"id": 1, "token":"12345-valid-for-id-1-token"}""")
      val result: String = Await.result(future, timeout.duration).asInstanceOf[String]

      println(s"\n\n Return Result : ${result} \n\n")

    }
  }

}
