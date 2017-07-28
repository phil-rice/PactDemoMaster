package org.pactDemo.akka

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.finatraUtilities.GenericCustomClient
import org.pactDemo.utilities.PactDemoSpec2
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._


class AkkaPactSystemSpec2 extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with BeforeAndAfter
  with Matchers
  with PactDemoSpec2 {

  val pactClient = mock[GenericCustomClient[CustomRequestObject, CustomReplyObject]]
  val pactActor = system.actorOf(Props(new AkkaPactSystem(pactClient)), "Main-Process")

  implicit val timeout = Timeout(5 second)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }



  "An AkkaPactSystem using implicit sender" should {
    "send back custom response" in {
      when(pactClient.apply(CustomRequestObject(1, "validToken"))) thenReturn Future.value(CustomReplyObject(1, "validToken", true))

      val future = pactActor ? ProcessRequest("""{"id": 1, "token":"validToken"}""")
      val result: String = Await.result(future, timeout.duration).asInstanceOf[String]
      println(s"\n\n output ::: ${result} \n\n")

    }
  }

}
