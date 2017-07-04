package org.pactDemo.ios

import com.twitter.finagle.http.{Request, Response}
import org.pactDemo.utilities.{CustomeRequestProcessor, CustomeResponseProcessor}

/**
  * Created by aban.m on 04-07-2017.
  */

sealed trait AndriodCustomeAuthentication

case object AndriodCustomeAuthenticationValid extends AndriodCustomeAuthentication

case object AndriodCustomeAuthenticationInValid extends AndriodCustomeAuthentication


case class AndriodCustomeAuthenticationRequest(id: String, token: String)

object AndriodCustomeAuthenticationRequest {

  implicit object makeAndriodCustomeRequest extends CustomeRequestProcessor[AndriodCustomeAuthenticationRequest] {
    override def apply(customeRequest: AndriodCustomeAuthenticationRequest): Request = {
      Request(s"/token/id/${customeRequest.id}")
    }
  }

}

object AndriodCustomeAuthentication {

  implicit object makeAndriodCustomeResponse extends CustomeResponseProcessor[AndriodCustomeAuthentication] {
    override def apply(response: Response): AndriodCustomeAuthentication = {
      response.contentString.contains("invalid") match {
        case true => AndriodCustomeAuthenticationInValid
        case _ => AndriodCustomeAuthenticationValid
      }
    }
  }

}