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