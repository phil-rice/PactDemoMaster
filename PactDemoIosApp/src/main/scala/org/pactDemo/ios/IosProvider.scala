package org.pactDemo.ios

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.request.RouteParam
import com.twitter.util.Future
import org.pactDemo.finatraUtilities.{FinatraServer, _}
import org.pactDemo.utilities.{Heroku, Strings}

case class AuthToken(`Authentication-token`: String)

object AuthToken extends PactArrow {
  def extractTokenWithoutPrefix(authToken: AuthToken): String = Strings.removeStart("token ")(authToken.`Authentication-token`)

  def getToken(raw: String)(implicit json: Json): String = extractTokenWithoutPrefix(json.fromJson[AuthToken](raw))
}

case class IosProviderRequest(id: Int, token: String)

object IosProviderRequest extends PactArrow {

  implicit def getIosProviderRequestFromHttpRequest(implicit json: Json) = new FromRequest[IosProviderRequest] {
    override def apply(request: Request): IosProviderRequest = {

      val id = Strings.lastSegmentOf(request.uri).toInt

      val token = request ~> (_.contentString) ~> AuthToken.getToken
      IosProviderRequest(id, token)
    }
  }

  implicit def makeRequestForProviderFromIosProvider = new ToRequest[IosProviderRequest] {
    override def apply(iosProviderRequest: IosProviderRequest): Request = {
      val request = Request(s"/token/id/${iosProviderRequest.id}")

      request.headerMap.add("ContentType", "application/hcl.token")
      request.method = Method.Post
      request.setContentString(s"""{"Authentication-token":"token ${iosProviderRequest.token}"}""")
      request
    }
  }
}

sealed trait IosAuthResponse

case class IosValidAuthResponse(id: Int, token: String) extends IosAuthResponse

case class IosInValidAuthResponse(id: Int, token: String) extends IosAuthResponse


object IosAuthResponse extends PactArrow {

  implicit object ToResponseIosAuthResponse extends ToResponse[IosAuthResponse] {
    override def apply(response: ResponseBuilder)(authRespone: IosAuthResponse): Response = {
      authRespone match {
        case IosValidAuthResponse(id, token) => response.ok(s"""{"token":"$token","id":"$id"}""") ~> setResponseContentTypeToJson
        case IosInValidAuthResponse(id, token) => response.unauthorized(s"Unauthorized token $token for id $id")
      }
    }
  }

  implicit object FromResponseForAuthResponse extends FromResponse[IosProviderRequest, IosAuthResponse] {
    override def apply(request: IosProviderRequest, res: Response): IosAuthResponse = {
      res.statusCode match {
        case 200 => IosValidAuthResponse(request.id, request.token)
        case _ => IosInValidAuthResponse(request.id, request.token)
      }
    }
  }

}

//case class IosRequest(@RouteParam id: Int, request: Request)

class IosProvider(clientService: IosProviderRequest => Future[IosAuthResponse]) extends Controller with RequestResponse {

  import PactArrow._
import Futures._
  post("/token/id/:id") { request: Request =>
    request ~> fromRequest[IosProviderRequest] ~> clientService ~> toResponse
  }

  // Added options call to handle Cross-Origin problem
  options("/token/id/:*") { request: Request => response.ok }
  // Added this get call to handle index.html
  get("/:*") { request: Request => response.ok.fileOrIndex(request.getParam("*"), "index.html") }
}

object IosProvider extends App {
  val baseUrl =  Heroku.providerHostAndPort
  val rawHttpClient = Http.newService(baseUrl)
  val client = new GenericCustomClient[IosProviderRequest, IosAuthResponse](rawHttpClient)
  new FinatraServer(9030, new IosProvider(client), new AssetsController).main(Array())
}