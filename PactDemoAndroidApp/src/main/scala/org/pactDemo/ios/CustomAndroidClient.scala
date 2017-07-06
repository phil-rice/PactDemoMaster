package org.pactDemo.ios

import com.twitter.finagle.http.{Method, Request, Response}
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
      val request = Request(s"/token/id/${customeRequest.id}")
      //request.headerMap.add("Authentication", s"token ${customeRequest.token}")
      request.headerMap.add("ContentType", "application/hcl.token")
      request.method = Method.Post
      request.setContentString(s"""{"Authentication-token":"token ${customeRequest.token}"}""")
      request
    }
  }

}

object AndriodCustomeAuthentication {

  implicit object makeAndriodCustomeResponse extends CustomeResponseProcessor[AndriodCustomeAuthentication] {
    override def apply(response: Response): AndriodCustomeAuthentication = {
      println(s"response.contentString :: ${response.contentString}")
      response.contentString.contains("invalid") match {
        case true => AndriodCustomeAuthenticationInValid
        case _ => AndriodCustomeAuthenticationValid
      }
    }
  }

}