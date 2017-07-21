package org.pactDemo.ios

import java.text.SimpleDateFormat
import java.util.Calendar

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.{Method, Request, Response}
import org.pactDemo.utilities.{CustomeRequestProcessor, CustomeResponseProcessor, PactArrow}

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


/**
  * For Android Provider
  */

object Util {
  def currentTime: String = new SimpleDateFormat("dd-MMM-yy hh:mm:ss a").format(Calendar.getInstance.getTime)
  def getMapper: ObjectMapper = new ObjectMapper().registerModule(DefaultScalaModule)
}


trait ToCustomObject[T, R] extends (T => R)

object ToCustomObject {

  implicit object makeObject extends ToCustomObject[Request, CustomRequestObject] {
    override def apply(request: Request): CustomRequestObject = {
      Util.getMapper.readValue(request.contentString, classOf[CustomRequestObject])
    }
  }

}

case class CustomRequestObject(id: Int, token: String)

trait ObjectConvertor[R, T] extends (R => T)

case class CustomReplyObject(id: Int, token: String, valid: Boolean)

object CustomReplyObject {

  implicit object makeCustomResponse extends ObjectConvertor[CustomRequestObject, CustomReplyObject] with PactArrow {
    override def apply(input: CustomRequestObject): CustomReplyObject = {
      input.token.contains("invalid") match {
        case true => CustomReplyObject(input.id, input.token, false)
        case _ => CustomReplyObject(input.id, input.token, true)
      }
    }
  }

}

