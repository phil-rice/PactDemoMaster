package org.pactDemo.ios

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import org.pactDemo.utilities._

class AndroidProviderController extends Controller with PactArrow {

  val client = new FinatraClient(Heroku.providerHost, Heroku.providerPort, _.replace("}", ""","server":"android"}"""))

  get("/token/id/:id") { request: AndroidRequest =>
    client(request.id).map(response.ok(_).contentType("application/json"))
  }

  implicit object customObjectToResponse extends ObjectConvertor[CustomReplyObject, Response] {
    override def apply(input: CustomReplyObject): Response = {
      input.valid match {
        case true => response.ok(s"""{"token":"${input.id}","id":"${input.token}", "valid": ${input.valid}}""")
        case false => response.unauthorized(s"Unauthorized token ${input.id} for id ${input.token}") //response.ok(s"""{"token":"${input.id}","id":"${input.token}", "valid": ${input.valid}""")
        case _ => response.unauthorized(s"Unknown/Invalid token ${input.id} for id ${input.token}")
      }
    }
  }

  val requestToCustomObjectConverter = implicitly[ToCustomObject[Request, CustomRequestObject]]
  val objectToCustomResponseConverter = implicitly[ObjectConvertor[CustomRequestObject, CustomReplyObject]]

  post("/token/android/post") { request: Request => request ~> requestToCustomObjectConverter ~> objectToCustomResponseConverter ~> customObjectToResponse }
  options("/token/android/post") { request: Request => response.ok }
}


object AndroidIOApp extends App {
  new FinatraServer(9090, new AndroidProviderController()).main(Array())
}