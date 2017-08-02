package org.pactDemo.akka

import akka.actor.{ActorContext, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.finatraUtilities.{GenericCustomClient, Json, PactArrow}
import org.pactDemo.utilities.PactDemoSpec2
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import org.pactDemo.finatraUtilities.Futures._

class SupervisoryPactActorAsActorSpec extends TestKit(ActorSystem("SupervisoryPactActorSpec3"))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with BeforeAndAfter
  with Matchers
  with PactDemoSpec2 {


  val pactClient = mock[GenericCustomClient[CustomRequestObject, CustomReplyObject]]

  //val pactActor = system.actorOf(Props(new AkkaPactSystem(pactClient)), "Main-Process")

  implicit val timeout = Timeout(5 second)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  implicit val childActorFactory = new ChildActorFactory {
    var actor: ActorRef = null

    override def apply(context: ActorContext, id: Int, restClient: (CustomRequestObject) => Future[CustomReplyObject]): ActorRef = {
      actor = context.actorOf(Props(new RememberPactActor(restClient)), s"${id}-Processor")
      actor
    }
  }

  implicit val json = mock[Json]

  val superPactActor = system.actorOf(Props(new SupervisoryPactActor(pactClient)), "Supervisour-Process")

  "SupervisourPactActor create a child pact and send it the message" should {

    "send back Sucessful response for valid request" in {
      when(json.fromJson[CustomRequestObject]("some input")) thenReturn CustomRequestObject(1, "validToken")

      val future = superPactActor ? ProcessRequest("some input")
      Await.result(future, timeout.duration) shouldBe "responseFromRememberPactActor"
      val (client, request) = Await.result((childActorFactory.actor ? RememberedDetails), timeout.duration)

      request shouldBe CustomRequest(CustomRequestObject(1, "validToken"))
      client shouldBe pactClient
    }
  }
}

