package org.pactDemo.finatraUtilities

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.pactDemo.utilities.{SimpleTree, SimpleTreeRoot}


object ServiceTree {

}


trait ServiceTree[Req, Res, Payload] {

  def payload: Payload
}

case class RootServiceTree[Req, Res, Payload](payload: Payload) extends ServiceTree[Req, Res, Payload] {
}

case class DelegateTree0[Req, Res, Payload]
(delegate: ServiceTree[Req, Res, Payload], payload: Payload) extends ServiceTree[Req, Res, Payload] {
}


case class TransformerTree0[OldReq, OldRes, NewReq, NewRes, Payload]
(delegate: ServiceTree[OldReq, OldRes, Payload], payload: Payload) extends ServiceTree[NewReq, NewRes, Payload] {
}


trait ServiceLanguageExtension {

  type ServiceTransformer[OldReq, OldRes, Req, Res] =
    ServiceTree[OldReq, OldRes, ServiceDescriptionAndCreator[OldReq, OldRes]] =>
      ServiceTree[Req, Res, ServiceDescriptionAndCreator[Req, Res]]
  type ServiceDelegator[Req, Res] = ServiceTransformer[Req, Res, Req, Res]

  //    => ServiceCreator[Req, Res, NewService, Payload])

  implicit class ServiceDescriptionPimper[OldReq, OldRes](sd: ServiceTree[OldReq, OldRes, ServiceDescriptionAndCreator[OldReq, OldReq]]) {
    def >--<[NewReq, NewRes](transformer: ServiceTransformer[OldReq, OldRes, NewReq, NewRes]): ServiceTree[NewReq, NewRes, ServiceDescriptionAndCreator[NewReq, NewRes]] =
      transformer(sd)
  }

}

case class ServiceDescriptionAndCreator[Req, Res](description: String, creator: () => Req => Future[Res]) {
  lazy val service = creator()
}


trait HttpServiceLanguageExtension {
  def http(hostNameAndPort: String) = RootServiceTree[Request, Response, ServiceDescriptionAndCreator[Request, Response]](
    ServiceDescriptionAndCreator("FinagleHttp($hostNameAndPort)", () => Http.newService(hostNameAndPort))
  )

  //  RootServiceCreator[Request, Response, Request => Future[Response]](s"FinagleHttp($hostNameAndPort)", () => Http.newService(hostNameAndPort))
}

trait ServiceLanguage extends HttpServiceLanguageExtension with GenericCustomClientLanguageExtension with AddHostNameServiceLanguageExtension with LoggingClientServiceLanguageExtension