package org.pactDemo.ios

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

/**
  * Created by prasenjit.b on 6/29/2017.
  */
case class AndriodAuthenticationRequest( id: String, token : String )

sealed  trait AndriodAuthentication
case object AndriodAuthenticationValid extends AndriodAuthentication
case object AndriodAuthenticationInValid extends AndriodAuthentication


class ArdroidAuthenticationClient ( delegate : Request => Future[ Response ] ) extends ( AndriodAuthenticationRequest => Future[ AndriodAuthentication ] ){
  override def apply(req: AndriodAuthenticationRequest): Future[AndriodAuthentication] = {
    val request = Request( s"/token/id/${req.id}")
    delegate( request ) map( response =>{ response.contentString.contains("invalid") match{ case true => AndriodAuthenticationInValid case _  =>  AndriodAuthenticationValid } })
  }
}
