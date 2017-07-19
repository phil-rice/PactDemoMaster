package org.pactDemo.angulario

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.{Method, Request, Response}
import org.pactDemo.utilities.{CustomeRequestProcessor, CustomeResponseProcessor}

/**
  * Created by aban.m on 7/11/2017.
  */

case class AngularIOAuthenticationCustomRequest(id: String, token: String)

object AngularIOAuthenticationCustomRequest {

  implicit object makeCustomAngularIORequest extends CustomeRequestProcessor[AngularIOAuthenticationCustomRequest] {
    override def apply(customeRequest: AngularIOAuthenticationCustomRequest): Request = {
      val request = Request(s"/token/id/${customeRequest.id}")
      request.headerMap.add("ContentType", "application/json")
      request.method = Method.Post
      request.setContentString(s"""{"Authentication-token":"token ${customeRequest.token}"}""")
      request
    }
  }

}

sealed trait AngularIOCustomAuthentication

case object AngularIOAuthenticationValid extends AngularIOCustomAuthentication
case object AngularIOAuthenticationInValid extends AngularIOCustomAuthentication

object AngularIOCustomAuthentication {

  implicit object makeCustomAngularIOResponse extends CustomeResponseProcessor[AngularIOCustomAuthentication] {
    override def apply(response: Response): AngularIOCustomAuthentication = {
      print(s"\n\n response.contentString :: ${response.contentString} \n\n")
      response.contentString.contains("invalid") match {
        case true => AngularIOAuthenticationInValid
        case _ => AngularIOAuthenticationValid
      }

    }
  }

}


trait FromObjToCustomObject [reqObj, jsonObj] extends (reqObj => jsonObj)

case class AngularIOPostRequest ( id : String, token : String )
object AngularIOPostRequest {

  implicit object convertRequestToCustomObject extends FromObjToCustomObject [Request, AngularIOPostRequest] {
    val objMap = new ObjectMapper()
    objMap.registerModule(DefaultScalaModule)
    override def apply(request: Request): AngularIOPostRequest = {
      objMap.readValue( request.contentString, classOf[AngularIOPostRequest] )
    }
  }

}



