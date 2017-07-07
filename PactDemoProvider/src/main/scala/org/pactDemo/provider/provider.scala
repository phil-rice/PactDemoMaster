package org.pactDemo.provider

import com.fasterxml.jackson.databind.ObjectMapper
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.utilities.{FinatraServer, PactArrow}
import com.fasterxml.jackson.module.scala._



case class AuthenticationRequestBody( `Authentication-token` : String )
trait FromJsonToObject[From, To] extends ( From => To )
object AuthenticationRequestBody{

  val mapper = new ObjectMapper()
  mapper.registerModule( DefaultScalaModule )

  implicit object FromJsonToObjectRequestBody extends FromJsonToObject[Request, AuthenticationRequestBody] {
    override def apply( body : Request): AuthenticationRequestBody = {
      mapper.readValue(body.contentString, classOf[AuthenticationRequestBody])
    }
  }
}

trait GetActualToken[T] extends (T => T)
object GetActualToken{
  implicit object GetActualTokenForAuthenticationRequestBody extends GetActualToken[ AuthenticationRequestBody ] {
    override def apply( requestBody : AuthenticationRequestBody): AuthenticationRequestBody = {
      requestBody.`Authentication-token`.split( " " ).length == 2 match{
        case true => AuthenticationRequestBody( requestBody.`Authentication-token`.split( " " )(1) )
        case false => AuthenticationRequestBody( requestBody.`Authentication-token`.split( " " )(0) )
      }
    }
  }
}

object AuthenticationRequest extends PactArrow{
  val fromJson = implicitly[FromJsonToObject[Request,AuthenticationRequestBody]]
  val GetActualToken = implicitly[GetActualToken[AuthenticationRequestBody]]

  implicit object FromJsonForAuthenticationRequest extends FromRequest[AuthenticationRequest] {
    override def apply(request: Request): AuthenticationRequest =  AuthenticationRequest( request.getIntParam("id"), (request ~> fromJson ~> GetActualToken).`Authentication-token` )
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

trait FromRequest[T] extends (Request => T)

trait MakeResponse[T] extends (T => Response)

class ProviderController(authenticationService: AuthenticationService) extends Controller with PactArrow{

  implicit object MakeResponseForAuthenticationResult extends MakeResponse[AuthenticationResult] {
    override def apply(v1: AuthenticationResult): Response = v1 match {
      case InvalidResponse(token) => response.unauthorized(s"Unauthorized token $token")
      case ValidResponse(id, token) => response.ok(s"""{"token":"$token","id":"$id"}""")
    }
  }
  val fromRequest = implicitly[FromRequest[AuthenticationRequest]]
  val makeResponse = implicitly[MakeResponse[AuthenticationResult]]

  post("/token/id/:id") { request: Request => request ~> fromRequest ~> authenticationService ~> makeResponse }
}


object Provider extends App {
  new FinatraServer(9000, new ProviderController(new AuthenticationService)).main(Array())
}