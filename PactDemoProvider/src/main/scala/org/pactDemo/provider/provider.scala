package org.pactDemo.provider

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.utilities.FinatraServer


object AuthenticationRequest {

  implicit object FromJsonForAuthenticationRequest extends FromRequest[AuthenticationRequest] {
    override def apply(request: Request): AuthenticationRequest = {
      val id = request.getIntParam("id")
      val token = request.contentString
      val index = token.indexOf("-token")
      val lastIndex = token.indexOf("}")
      val actualToken = token.substring(index + 15, lastIndex - 1)
      AuthenticationRequest(id, actualToken)
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

trait FromRequest[T] extends (Request => T)


trait MakeResponse[T] extends (T => Response)

class ProviderController(authenticationService: AuthenticationService) extends Controller {

  implicit object MakeResponseForAuthenticationResult extends MakeResponse[AuthenticationResult] {
    override def apply(v1: AuthenticationResult): Response = v1 match {
      case InvalidResponse(token) => response.unauthorized(s"Unauthorized token $token")
      case ValidResponse(id, token) => response.ok(s"""{"token":"$token","id":"$id"}""")
    }
  }

  val fromRequest = implicitly[FromRequest[AuthenticationRequest]]
  val makeResponse = implicitly[MakeResponse[AuthenticationResult]]

  implicit class AnyPimper[T](t: T) {
    def ~>[T1](fn: T => T1) = fn(t)
  }

  implicit class FuturePimper[T](t: Future[T]) {
    def ~>[T1](fn: T => T1) = t.map(fn)
  }


  post("/token/id/:id") { request: Request => request ~> fromRequest ~> authenticationService ~> makeResponse }
}


object Provider extends App {
  new FinatraServer(9000, new ProviderController(new AuthenticationService)).main(Array())
}