package org.pactDemo.finatraUtilities

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.pactDemo.utilities.{SimpleTree, SimpleTreeRoot}

import scala.reflect.ClassTag


object ServiceTree {

  implicit class ServiceTreeHelper[Req, Res, Payload](tree: ServiceTree[Req, Res, Payload]) {
    def map[NewPayload](fn: Payload => NewPayload): ServiceTree[Req, Res, NewPayload] = tree.mapFromTree(x => fn(x.payload))

    def foldToList = tree.foldFromTree(List[Payload]())(_ :+ _.payload)

    def foldToListOfTrees = tree.foldFromTree(List[ServiceTree[_, _, Payload]]())(_ :+ _)

    type IndentTreeAcc = (List[(ServiceTree[_, _, Payload], Int)], Int)

    def foldToListOfTreesAndDepth: List[(ServiceTree[_, _, Payload], Int)] = tree.foldFromTree[IndentTreeAcc]((List(), 0)) { case ((list, depth), tree) => (list :+ (tree, depth), depth + 1) }._1

    def filter(acceptor: ServiceTree[_, _, Payload] => Boolean) = foldToListOfTrees.filter(acceptor)

    def findAll[X](implicit classTag: ClassTag[X]) = filter(x => x.service.getClass == classTag.runtimeClass)
  }

}


sealed abstract class ServiceTree[Req, Res, Payload](implicit val reqClassTag: ClassTag[Req], val resClassTag: ClassTag[Res]) {
  def payload: Payload

  /** Important constraints: Keep the same service even after map, and on multiple calls. This is the 'real' service */
  def service: Req => Future[Res]


  def mapFromTree[NewPayload](fn: ServiceTree[_, _, Payload] => NewPayload): ServiceTree[Req, Res, NewPayload]

  def foldFromTree[Acc](initial: Acc)(foldFn: (Acc, ServiceTree[_, _, Payload]) => Acc): Acc

}

case class RootServiceTree[Req: ClassTag, Res: ClassTag, Payload](payload: Payload, serviceMaker: () => Req => Future[Res]) extends ServiceTree[Req, Res, Payload] {
  override lazy val service: (Req) => Future[Res] = serviceMaker()

  override def mapFromTree[NewPayload](fn: (ServiceTree[_, _, Payload]) => NewPayload) = RootServiceTree(fn(this), () => service)

  override def foldFromTree[Acc](initial: Acc)(foldFn: (Acc, ServiceTree[_, _, Payload]) => Acc): Acc = foldFn(initial, this)

}

case class DelegateTree0[Req: ClassTag, Res: ClassTag, Payload](delegate: ServiceTree[Req, Res, Payload], payload: Payload, serviceMaker: (Req => Future[Res]) => Req => Future[Res]) extends ServiceTree[Req, Res, Payload] {
  override lazy val service: (Req) => Future[Res] = serviceMaker(delegate.service)

  override def mapFromTree[NewPayload](fn: (ServiceTree[_, _, Payload]) => NewPayload) = DelegateTree0(delegate.mapFromTree(fn), fn(this), _ => service)

  override def foldFromTree[Acc](initial: Acc)(foldFn: (Acc, ServiceTree[_, _, Payload]) => Acc): Acc = foldFn(delegate.foldFromTree(initial)(foldFn), this)

}


case class TransformerTree0[OldReq, OldRes, NewReq: ClassTag, NewRes: ClassTag, Payload](delegate: ServiceTree[OldReq, OldRes, Payload], payload: Payload, serviceMaker: (OldReq => Future[OldRes]) => NewReq => Future[NewRes]) extends ServiceTree[NewReq, NewRes, Payload] {
  override lazy val service: (NewReq) => Future[NewRes] = serviceMaker(delegate.service)

  override def mapFromTree[NewPayload](fn: (ServiceTree[_, _, Payload]) => NewPayload): ServiceTree[NewReq, NewRes, NewPayload] = TransformerTree0(delegate.mapFromTree(fn), fn(this), { someParam: (OldReq => Future[OldRes]) => service })

  override def foldFromTree[Acc](initial: Acc)(foldFn: (Acc, ServiceTree[_, _, Payload]) => Acc): Acc = foldFn(delegate.foldFromTree(initial)(foldFn), this)

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
    ServiceDescription(s"FinagleHttp($hostNameAndPort)"), () => Http.newService(hostNameAndPort)
  )

  //  RootServiceCreator[Request, Response, Request => Future[Response]](s"FinagleHttp($hostNameAndPort)", () => Http.newService(hostNameAndPort))
}

trait ServiceLanguage extends HttpServiceLanguageExtension with GenericCustomClientLanguageExtension with AddHostNameServiceLanguageExtension with LoggingClientServiceLanguageExtension

object ServiceLanguage extends ServiceLanguage