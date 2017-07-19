package org.pactDemo.angulario

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.request.RouteParam
import com.twitter.util.Future
import org.pactDemo.utilities._

/**
  * Created by aban.m on 7/11/2017.
  */

case class AngularIORequest(@RouteParam id: Int)


trait HttpRequestToCustomRequest[CustomRequest] extends (Request => CustomRequest)
trait CustomResponseToHttpResponse[CustomResponse] extends (CustomResponse => Response)
trait converter[CustomRequest, CustomResponse] extends (CustomRequest => CustomResponse)

sealed trait AngularIOPostResponse

case class AngularValidResponse(id: String, token: String) extends AngularIOPostResponse
case class AngularInValidResponse(id: String, token: String) extends AngularIOPostResponse

class RequestConvertor () extends converter[AngularIOPostRequest,AngularIOPostResponse] {
  override def apply(custRequest: AngularIOPostRequest): AngularIOPostResponse = {
    custRequest.token.contains("invalid") match {
      case true => AngularInValidResponse(custRequest.id, custRequest.token)
      case _ => AngularValidResponse(custRequest.id, custRequest.token)

    }
  }
}

class AngularIOAppController( converter :RequestConvertor ) extends Controller with PactArrow {

  val client = new FinatraClient(Heroku.providerHost,Heroku.providerPort, _.replace("}", ""","server":"angulario"}"""))

    implicit object makeCustomeResponse extends CustomResponseToHttpResponse [AngularIOPostResponse] {
      override def apply(custResponse: AngularIOPostResponse): Response = {

        if(custResponse.isInstanceOf[AngularValidResponse]) {
          val custObj = custResponse.asInstanceOf[AngularValidResponse]
          response.ok(s"""{"token":"${custObj.token}","id":"${custObj.id}", "valid": true}""")
        } else {
          val custObj = custResponse.asInstanceOf[AngularInValidResponse]
          response.unauthorized( s"Unauthorized token ${custObj.token} for id ${custObj.id}" )
        }

      }
    }

  val fromObjToJSONImpl = implicitly [ FromObjToCustomObject[Request, AngularIOPostRequest] ]

  get("/token/id/:id") { request : AngularIORequest =>
    client(request.id) map( response.ok(_).contentType("application/json") )
  }

  post("/token/post/process") { request : Request => request ~> fromObjToJSONImpl ~> converter ~> makeCustomeResponse}
  options("/token/post/process") {request: Request => response.ok}
}

object AngularIOApp extends App {
  new FinatraServer(9090, new AngularIOAppController(new RequestConvertor)).main(Array())
}