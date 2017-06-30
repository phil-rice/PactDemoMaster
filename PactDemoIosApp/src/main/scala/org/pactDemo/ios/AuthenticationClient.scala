package org.pactDemo.ios

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future


case class AuthenticationRequest(id: String, token: String)

sealed trait AuthenticationResponse

case object AuthenticationValid extends AuthenticationResponse

case object AuthenticationInvalid extends AuthenticationResponse

class AuthenticationClient(delegate: Request => Future[Response]) extends (AuthenticationRequest => Future[AuthenticationResponse]) {
  override def apply(v1: AuthenticationRequest): Future[AuthenticationResponse] = {
    val request = Request(s"/token/${v1.id}")
    delegate(request) map { response => if (response.contentString.contains("nvalid")) AuthenticationInvalid else AuthenticationValid }
  }
}
