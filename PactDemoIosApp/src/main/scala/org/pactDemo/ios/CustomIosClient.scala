package org.pactDemo.ios

import com.twitter.finagle.http.{Request, Response}
import org.pactDemo.utilities.{CustomeRequestProcessor, CustomeResponseProcessor}

/**
  * Created by aban.m on 04-07-2017.
  */

sealed trait IosCustomeAuthentication

case object IosCustomeAuthenticationValid extends IosCustomeAuthentication

case object IosCustomeAuthenticationInValid extends IosCustomeAuthentication


case class IosCustomeAuthenticationRequest(id: String, token: String)

object IosCustomeAuthenticationRequest {

  implicit object makeIosCustomeRequest extends CustomeRequestProcessor[IosCustomeAuthenticationRequest] {
    override def apply(customeRequest: IosCustomeAuthenticationRequest): Request = {
      val request = Request(s"/token/id/${customeRequest.id}")
      request.headerMap.add("Authentication", s"token ${customeRequest.token}")
      request
    }
  }

}


object IosCustomeAuthentication {

  implicit object makeIosCustomeResponse extends CustomeResponseProcessor[IosCustomeAuthentication] {
    override def apply(response: Response): IosCustomeAuthentication = {
      response.contentString.contains("invalid") match {
        case true => IosCustomeAuthenticationInValid
        case _ => IosCustomeAuthenticationValid
      }
    }
  }

}