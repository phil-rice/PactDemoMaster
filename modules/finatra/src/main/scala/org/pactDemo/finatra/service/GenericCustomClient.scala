package org.pactDemo.finatra.service

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.pactDemo.finatra.structure.ServiceLanguageExtension
import org.pactDemo.finatra.utilities.{FromResponse, ToRequest}

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
  def objectify[Req: ClassTag, Res: ClassTag](implicit toRequest: ToRequest[Req],
                                              fromResponse: FromResponse[Req, Res]):
  ServiceTransformer[Request, Response, Req, Res] = { childTree =>
    transform[Request, Response, Req, Res](s"GenericCustomClient", childTree, delegate => new GenericCustomClient[Req, Res](delegate))
  }
}

