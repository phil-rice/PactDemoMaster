package org.pactDemo.finatraUtilities

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.pactDemo.utilities.{SimpleTree, SimpleTreeRoot}


object ServiceTree {

}


sealed trait ServiceTree[Req, Res, Payload] {
  def payload: Payload

  /** Important constraints: Keep the same service even after map, and on multiple calls. This is the 'real' service */
  def service: Req => Future[Res]

  def map[NewPayload](fn: Payload => NewPayload): ServiceTree[Req, Res, NewPayload]
}

case class RootServiceTree[Req, Res, Payload](payload: Payload, serviceMaker: () => Req => Future[Res]) extends ServiceTree[Req, Res, Payload] {
  override lazy val service: (Req) => Future[Res] = serviceMaker()

  override def map[NewPayload](fn: (Payload) => NewPayload) = RootServiceTree(fn(payload), () => service)
}

case class DelegateTree0[Req, Res, Payload](delegate: ServiceTree[Req, Res, Payload], payload: Payload, serviceMaker: (Req => Future[Res]) => Req => Future[Res]) extends ServiceTree[Req, Res, Payload] {
  override lazy val service: (Req) => Future[Res] = serviceMaker(delegate.service)

  override def map[NewPayload](fn: (Payload) => NewPayload) = DelegateTree0(delegate.map(fn), fn(payload), _ => service)
}


case class TransformerTree0[OldReq, OldRes, NewReq, NewRes, Payload](delegate: ServiceTree[OldReq, OldRes, Payload], payload: Payload, serviceMaker: (OldReq => Future[OldRes]) => NewReq => Future[NewRes]) extends ServiceTree[NewReq, NewRes, Payload] {
  override lazy val service: (NewReq) => Future[NewRes] = serviceMaker(delegate.service)

  override def map[NewPayload](fn: (Payload) => NewPayload) = TransformerTree0(delegate.map(fn), fn(payload), { someParam: (OldReq => Future[OldRes]) => service })
}


trait ServiceLanguageExtension {

  type ServiceTransformer[OldReq, OldRes, Req, Res] =
    ServiceTree[OldReq, OldRes, ServiceDescription] =>
      ServiceTree[Req, Res, ServiceDescription]
  type ServiceDelegator[Req, Res] = ServiceTransformer[Req, Res, Req, Res]

  //    => ServiceCreator[Req, Res, NewService, Payload])

  implicit class ServiceDescriptionPimper[OldReq, OldRes](sd: ServiceTree[OldReq, OldRes, ServiceDescription]) {
    def >--<[NewReq, NewRes](transformer: ServiceTransformer[OldReq, OldRes, NewReq, NewRes]): ServiceTree[NewReq, NewRes, ServiceDescription] =
      transformer(sd)
  }

}

case class ServiceDescription(description: String)


trait HttpServiceLanguageExtension {
  def http(hostNameAndPort: String) = RootServiceTree[Request, Response, ServiceDescription](
    ServiceDescription("FinagleHttp($hostNameAndPort)"), () => Http.newService(hostNameAndPort)
  )

  //  RootServiceCreator[Request, Response, Request => Future[Response]](s"FinagleHttp($hostNameAndPort)", () => Http.newService(hostNameAndPort))
}

trait ServiceLanguage extends HttpServiceLanguageExtension with GenericCustomClientLanguageExtension with AddHostNameServiceLanguageExtension with LoggingClientServiceLanguageExtension