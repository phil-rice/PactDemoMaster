package org.pactDemo.utilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

/**
  * Created by aban.m on 04-07-2017.
  */

trait CustomeRequestProcessor[CustomRequest] extends (CustomRequest => Request)

trait CustomeResponseProcessor[CustomResponse] extends (Response => CustomResponse)

class GenericCustomClient[CustomRequest, CustomResponse](delegate: Request => Future[Response])
                                                        (implicit fromRequest: CustomeRequestProcessor[CustomRequest],
                                                         toResponse: CustomeResponseProcessor[CustomResponse])
  extends (CustomRequest => Future[CustomResponse]) {
  override def apply(custRequest: CustomRequest): Future[CustomResponse] = {

    delegate(fromRequest(custRequest)) map ( toResponse )

  }
}
