package org.pactDemo.android

import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finatra.http.response.ResponseBuilder
import org.pactDemo.finatraUtilities._
import org.pactDemo.utilities._
import scala.language.implicitConversions

case class IdAndToken(id: Int, token: String)

object IdAndToken {
  implicit def FromRequestForIdAndToken(implicit json: Json) = new FromRequest[IdAndToken] {
    override def apply(request: Request): IdAndToken = json.fromJson[IdAndToken](request.contentString)
  }

  implicit object ToRequestForIdAndToken extends ToRequest[IdAndToken] {
    override def apply(idAndToken: IdAndToken): Request = {
      val request = Request(s"/token/id/${idAndToken.id}")
      request.headerMap.add("ContentType", "application/hcl.token")
      request.method = Method.Post
      request.setContentString(s"""{"Authentication-token":"token ${idAndToken.token}"}""")
      request
    }
  }

}

case class IdTokenAndValid(id: Int, token: String, valid: Boolean)

object IdTokenAndValid {

  implicit object ToResponseForIdTokenAndValid$ extends ToResponse[IdTokenAndValid] {
    override def apply(response: ResponseBuilder)(input: IdTokenAndValid): Response = {
      input.valid match {
        case true => response.ok(s"""{"id": ${input.id},"token":"${input.token}", "valid": ${input.valid}}""") //, "server":"android"
        case false => response.ok(s"""{"id": ${input.id},"token":"${input.token}", "valid": ${input.valid}}""") // response.unauthorized(s"Unauthorized token ${input.id} for id ${input.token}") // , "server":"android"
      }
    }
  }

  implicit def FromResponseFromIdTokenAndValid(implicit json: Json) = new FromResponse[IdAndToken, IdTokenAndValid] {
    override def apply(request: IdAndToken, response: Response): IdTokenAndValid = {
      response.statusCode match {
        case 200 => json.fromJson[IdTokenAndValid](response.contentString)
        case 401 => IdTokenAndValid(request.id, request.token, false)
        case s => throw new IllegalStatusCodeException(s, s"Response was $response")
      }
    }
  }
}