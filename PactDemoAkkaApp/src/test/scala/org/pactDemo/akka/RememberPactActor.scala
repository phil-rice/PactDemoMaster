package org.pactDemo.akka


import akka.actor.Actor
import com.twitter.util.Future

trait RememberedDetails

object RememberedDetails extends RememberedDetails

class RememberPactActor(val restClient: CustomRequestObject => Future[CustomReplyObject]) extends Actor {

  var rememberedRequest: Any = null

  override def receive: Receive = {
    case RememberedDetails =>
      sender() ! (restClient, rememberedRequest)
    case x =>
      rememberedRequest = x
      sender() ! "responseFromRememberPactActor"
  }
}