package org.pactDemo.ios

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.util.Future
import org.pactDemo.utilities.{AssetsController, FinatraServer, PactArrow}

/**
  * Created by prasenjit.b on 7/11/2017.
  */

trait FormRequest[T] extends (Request => T)
trait MakeResponse[T] extends ( T=> Response )
trait MapRequestToResponse[Req, Res] extends ( Req => Future[Res] )
trait FromJsonToObject[FromObj, ToObj ] extends ( FromObj => ToObj )


case class AuthTokenWithOutPrefix( `Authentication-token`: String ) extends  PactArrow {
  def tokenWithOutPrefix = `Authentication-token`.split(" ") match{
    case Array( prefix, token ) =>  token
    case Array( token ) =>  token
  }
}

case class AuthToken( `Authentication-token`: String )

object AuthToken{
  implicit object GetAuthTokenFromJSONResponseBody extends FromJsonToObject[ Request, AuthToken] {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    override def apply( request: Request): AuthToken = mapper.readValue(request.contentString, classOf[AuthToken])
  }
}

case class IosProviderRequest( id: Int, token : String)

object IosProviderRequest extends PactArrow {
 implicit object getIosProviderRequestFromHttpRequest extends FormRequest[IosProviderRequest] {
   override def apply( request: Request ): IosProviderRequest ={
     val formJson = implicitly[FromJsonToObject[ Request, AuthToken ]]
     request ~> formJson ~> ( _.`Authentication-token` ) ~> ( AuthTokenWithOutPrefix(_) )~> ( _.tokenWithOutPrefix ) ~> ( IosProviderRequest( request.getIntParam("id"), _ ) )
   }
 }
}

sealed trait IosAuthResponse
case class IosValidAuthResponse( id : Int, token : String ) extends IosAuthResponse
case class IosInValidAuthResponse( id : Int, token : String ) extends IosAuthResponse


object IosAuthResponse extends  PactArrow {
  implicit object MapIosProviderRequestToIosAuthResponse extends MapRequestToResponse[ IosProviderRequest, IosAuthResponse] {
    override def apply( request: IosProviderRequest): Future[IosAuthResponse] = {
      Future.value( request ~> createIosAuthResponse )
    }
  }

  def createIosAuthResponse( request: IosProviderRequest ):IosAuthResponse = {
    if (request.token.contains("invalid")) IosInValidAuthResponse(request.id, request.token) else IosValidAuthResponse(request.id, request.token)
  }
}


class IosProvider() extends Controller with PactArrow{

  def setResponseContentTypeToJson( response: Response ): Response = { response.setContentType("application/json"); response }

  def fromCustomResponeToHttpResponse( authRespone : IosAuthResponse ):Response = authRespone match {
    case IosValidAuthResponse(id, token) => response.ok(s"""{"token":"$token","id":"$id"}""") ~> setResponseContentTypeToJson
    case IosInValidAuthResponse(id, token) => response.unauthorized( s"Unauthorized token $token for id $id" )
  }

  implicit object MakeResponseIosAuthResponse extends MakeResponse[ IosAuthResponse ] {
    override def apply( authRespone : IosAuthResponse ): Response = authRespone ~> fromCustomResponeToHttpResponse /*~> setResponseContentTypeToJson*/
  }
  val formRequest = implicitly[FormRequest[IosProviderRequest]]
  val mapRequestToResponse = implicitly[MapRequestToResponse[IosProviderRequest, IosAuthResponse]]
  val makeResponse = implicitly[MakeResponse[IosAuthResponse]]


  post("/token/id/:id"){ request : Request => request ~> formRequest ~> mapRequestToResponse ~> makeResponse }
  // Added options call to handle Cross-Origin problem
  options("/token/id/:*") {  request: Request => response.ok }
  // Added this get call to handle index.html
  get("/:*"){request : Request => response.ok.fileOrIndex(request.getParam("*"), "index.html")}
}

object IosProvider extends App{
  new FinatraServer( 9030, new IosProvider, new AssetsController).main( Array() )
}