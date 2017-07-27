package org.pactDemo.akka

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.twitter.util.Future
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class AkkaPactSystemSpec2 extends TestKit(ActorSystem("MySpec"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with PactDemoSpec {

  val pactClient = mock[CustomRequestObject => Future[CustomReplyObject]]

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  //  it should "" in {
  //    val mainProcess: ActorRef = system.actorOf(Props(new AkkaPactSystem(pactClient)), "Main-Process")
  //  }

  "An AkkaPactSystem using implicit sender " should {
    "send back custom response" in {
      val pactActor = system.actorOf(Props(new AkkaPactSystem(pactClient)), "test-system")
      //      val pactActor = system.actorOf(Props[AkkaPactSystem], name = "AkkaPactSystem")
      pactActor ! ProcessRequest("""{"id": 1, "token":"12345-valid-for-id-1-token"}""")
      expectMsg("""{"id": 1, "token":"12345-valid-for-id-1-token", "valid":"true"}""")
    }
  }
}