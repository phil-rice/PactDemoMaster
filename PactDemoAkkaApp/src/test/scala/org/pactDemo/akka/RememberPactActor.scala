package org.pactDemo.akka


import akka.actor.Actor
import com.twitter.util.Future

class RememberPactActor(val restClient: CustomRequestObject => Future[CustomReplyObject]) extends Actor {

  var rememberedRequest: Any = null

  override def receive: Receive = {
    case x =>
      rememberedRequest = x
      sender() ! ("something")
  }
}