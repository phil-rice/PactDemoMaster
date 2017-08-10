package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

import scala.reflect.ClassTag


class GenericCustomClient[CustomRequest, CustomResponse](delegate: Request => Future[Response])
                                                        (implicit toRequest: ToRequest[CustomRequest],
                                                         fromResponse: FromResponse[CustomRequest, CustomResponse])
  extends (CustomRequest => Future[CustomResponse]) {
  override def apply(custRequest: CustomRequest): Future[CustomResponse] = {
    delegate(toRequest(custRequest)) map (fromResponse(custRequest, _))
  }
}

trait GenericCustomClientLanguageExtension extends ServiceLanguageExtension {
  def objectify[Req:ClassTag, Res:ClassTag](implicit toRequest: ToRequest[Req],
                          fromResponse: FromResponse[Req, Res]):
  ServiceTransformer[Request, Response, Req, Res] = { childTree =>
    val x: ServiceTree[Request, Response, ServiceDescription] = childTree
    TransformerTree0[Request, Response, Req, Res, ServiceDescription](
      childTree,
      ServiceDescription(s"GenericCustomClient"),
      new GenericCustomClient[Req, Res](_))
  }
}

