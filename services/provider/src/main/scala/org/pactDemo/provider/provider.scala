package org.pactDemo.provider

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala._
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.finatraUtilities._
import org.pactDemo.mustache.{DisplayStructureController, Mustache, StatusController}


case class AuthenticationRequestWithPrefixBody(`Authentication-token`: String) {
  def copyWithoutPrefix = `Authentication-token`.split(" ") match {
    case Array(prefix, token) => AuthenticationRequestWithPrefixBody(token)
    case Array(token) => AuthenticationRequestWithPrefixBody(token)
  }
}

case class AuthenticationRequestBody(`Authentication-token`: String)

trait FromJsonToObject[From, To] extends (From => To)

object AuthenticationRequestBody {

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  implicit object FromJsonToObjectRequestBody extends FromJsonToObject[Request, AuthenticationRequestBody] {
    override def apply(body: Request): AuthenticationRequestBody = {
      mapper.readValue(body.contentString, classOf[AuthenticationRequestBody])
    }
  }

}

trait GetActualToken[T, B] extends (T => B)

object GetActualToken extends PactArrow {

  implicit object GetActualTokenForAuthenticationRequestBody extends GetActualToken[AuthenticationRequestBody, AuthenticationRequestWithPrefixBody] {
    override def apply(requestBody: AuthenticationRequestBody): AuthenticationRequestWithPrefixBody = {
      (requestBody.`Authentication-token`) ~> (AuthenticationRequestWithPrefixBody(_)) ~> (_.copyWithoutPrefix)
    }
  }

}

object AuthenticationRequest extends PactArrow {
  val fromJson = implicitly[FromJsonToObject[Request, AuthenticationRequestBody]]
  val GetActualToken = implicitly[GetActualToken[AuthenticationRequestBody, AuthenticationRequestWithPrefixBody]]

  implicit object FromJsonForAuthenticationRequest extends FromRequest[AuthenticationRequest] {
    override def apply(request: Request): AuthenticationRequest = {
      request ~> fromJson ~> GetActualToken ~> (_.`Authentication-token`) ~> (AuthenticationRequest(request.getIntParam("id"), _))
    }
  }

}

case class AuthenticationRequest(id: Int, token: String)

trait AuthenticationResult

case class InvalidResponse(token: String) extends AuthenticationResult

case class ValidResponse(id: Int, token: String) extends AuthenticationResult

class AuthenticationService extends (AuthenticationRequest => Future[AuthenticationResult]) {
  override def apply(v1: AuthenticationRequest): Future[AuthenticationResult] = {
    Future.value(if (v1.token.contains("invalid")) InvalidResponse(v1.token) else ValidResponse(v1.id, v1.token))
  }
}

trait AuthenticationLanguage extends ServiceLanguageExtension {
  def authenticate =
    RootServiceTree[AuthenticationRequest, AuthenticationResult, ServiceDescription](
      ServiceDescription(s"AuthenticationService"),
      () => new AuthenticationService)
}

trait FromRequest[T] extends (Request => T)

trait MakeResponse[T] extends (T => Response)

class ProviderController(authenticationService: AuthenticationRequest => Future[AuthenticationResult]) extends Controller with PactArrow {

  implicit object MakeResponseForAuthenticationResult extends MakeResponse[AuthenticationResult] {
    override def apply(v1: AuthenticationResult): Response = v1 match {
      case InvalidResponse(token) => response.unauthorized(s"Unauthorized token $token")
      case ValidResponse(id, token) => response.ok(s"""{"token":"$token","id":"$id", "valid": true}""")
    }
  }

  val fromRequest = implicitly[FromRequest[AuthenticationRequest]]
  val makeResponse = implicitly[MakeResponse[AuthenticationResult]]

  post("/token/id/:id") { request: Request => request ~> fromRequest ~> authenticationService ~> makeResponse }
  // Added options call to handle Cross-Origin problem
  options("/token/id/:*") { request: Request => response.ok }
}


object Provider extends App with ServiceLanguage with AuthenticationLanguage {
  implicit val loggerAdapter = FinagleLoggingAdapter
  implicit val logme = new SimpleLogMe
  import Mustache._
  import org.pactDemo.mustache.DisplayStructure._
  
  val clientBuilder = authenticate >--< logging("AuthenticationService", "")
  new FinatraServer(9000, new StatusController(clientBuilder), new DisplayStructureController(clientBuilder), new ProviderController(clientBuilder.service), new AssetsController).main(Array())
}