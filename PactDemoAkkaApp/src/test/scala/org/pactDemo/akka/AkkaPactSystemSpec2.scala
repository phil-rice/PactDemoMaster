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

  //val pactActor = system.actorOf(Props(new AkkaPactSystem(pactClient)), "Main-Process")
  val childPactActor = system.actorOf(Props(new CustomRequestProcessActor(pactClient)), "Child-Process")

  implicit val timeout = Timeout(5 second)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An CustomRequestProcessActor using implicit sender" should {

    "send back Sucessful response for valid request" in {
      when(pactClient.apply(CustomRequestObject(1, "validToken"))) thenReturn Future.value(CustomReplyObject(1, "validToken", true))

      val future = childPactActor ? CustomRequest(CustomRequestObject(1, "validToken"))
      val result: ProviderResponse =  Await.result(future, timeout.duration).asInstanceOf[ProviderResponse]
      result shouldBe ProviderSuccessful
    }

    "send back ProviderFailure response invalid request" in {
      when(pactClient.apply(CustomRequestObject(2, "invalidToken"))) thenReturn Future.value(CustomReplyObject(2, "invalidToken", false))

      val future = childPactActor ? CustomRequest(CustomRequestObject(2, "invalidToken"))
      val result: ProviderResponse =  Await.result(future, timeout.duration).asInstanceOf[ProviderResponse]
      result shouldBe ProviderFailure
    }

    "send back ProviderFailure response for null request" in {
      when(pactClient.apply(null)) thenReturn Future.value(CustomReplyObject(0, "", false))

      val future = childPactActor ? CustomRequest(null)
      val result: ProviderResponse =  Await.result(future, timeout.duration).asInstanceOf[ProviderResponse]
      result shouldBe ProviderFailure
    }

    "send back ProviderFailure response for Empty request" in {
      when(pactClient.apply(CustomRequestObject(0, ""))) thenReturn Future.value(CustomReplyObject(0, "", false))

      val future = childPactActor ? CustomRequestObject(0, "")
      val result: ProviderResponse =  Await.result(future, timeout.duration).asInstanceOf[ProviderResponse]
      result shouldBe ProviderFailure
    }
  }

}
