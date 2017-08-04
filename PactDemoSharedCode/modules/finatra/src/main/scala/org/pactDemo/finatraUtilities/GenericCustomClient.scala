package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future


class GenericCustomClient[CustomRequest, CustomResponse](delegate: Request => Future[Response])
                                                        (implicit toRequest: ToRequest[CustomRequest],
                                                         fromResponse: FromResponse[CustomRequest, CustomResponse])
  extends (CustomRequest => Future[CustomResponse]) {
  override def apply(custRequest: CustomRequest): Future[CustomResponse] = {

    delegate(toRequest(custRequest)) map (fromResponse(custRequest, _))

  }
}

trait GenericCustomClientLanguageExtension extends ServiceLanguageExtension {
  def objectify[Req, Res](implicit toRequest: ToRequest[Req],
                          fromResponse: FromResponse[Req, Res]):
  ServiceTransformer[Request, Response, Req, Res] = { childTree =>
    val x: ServiceTree[Request, Response, ServiceDescriptionAndCreator[Request, Response]] = childTree
    TransformerTree0[Request, Response, Req, Res, ServiceDescriptionAndCreator[Req, Res]](
      childTree,
      ServiceDescriptionAndCreator[Req, Res](s"GenericCustomClient",
        () => new GenericCustomClient[Req, Res](childTree.payload.service)))
  }
}

