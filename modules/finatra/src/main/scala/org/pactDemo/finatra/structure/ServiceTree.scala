package org.pactDemo.finatra.structure

import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.pactDemo.finatra.service._

import scala.reflect.ClassTag


object ServiceTree {

  implicit class ServiceTreeHelper[Req, Res, Payload](tree: ServiceTree[Req, Res, Payload]) {
    def map[NewPayload](fn: Payload => NewPayload): ServiceTree[Req, Res, NewPayload] = tree.mapFromTree(x => fn(x.payload))

    def foldToList = tree.foldFromTree(List[Payload]())(_ :+ _.payload)

    def foldToListOfTrees = tree.foldFromTree(List[ServiceTree[_, _, Payload]]())(_ :+ _)

    type IndentTreeAcc = (List[(ServiceTree[_, _, Payload], Int)], Int)

    def foldToListOfTreesAndDepth: List[(ServiceTree[_, _, Payload], Int)] = {
      val resultWithWrongDepths = tree.foldFromTree[IndentTreeAcc]((List(), 0)) { case ((list, depth), tree) => ((tree, depth) :: list, depth + 1) }._1
      val maxDepth = resultWithWrongDepths.map(_._2).max
      resultWithWrongDepths.map { case (tree, depth) => (tree, maxDepth - depth) }
    }

    def filter(acceptor: ServiceTree[_, _, Payload] => Boolean) = foldToListOfTrees.filter(acceptor)

    def collect[X](acceptor: PartialFunction[ServiceTree[_, _, Payload], X]) = foldToListOfTrees.collect(acceptor)

    def findAll[X](implicit classTag: ClassTag[X]) = filter(x => classTag.runtimeClass.isAssignableFrom(x.service.getClass))

    def findAllWithReqRes[NewReq, NewRes](implicit reqTag: ClassTag[NewReq], resTag: ClassTag[NewRes]) = filter(st => st.reqClassTag == reqTag && st.resClassTag == resTag).map(_.asInstanceOf[ServiceTree[NewReq, NewRes, Payload]])

    def findAllTreesWithServiceReqRes[NewReq, NewRes, Service <: NewReq => Future[NewRes]](implicit reqTag: ClassTag[NewReq], resTag: ClassTag[NewRes], serviceClassTag: ClassTag[Service]) =
      filter(st => st.reqClassTag == reqTag && st.resClassTag == resTag && serviceClassTag.runtimeClass.isAssignableFrom(st.service.getClass)).map(_.asInstanceOf[ServiceTree[NewReq, NewRes, Payload]])

    def allHttpServices = findAllWithReqRes[Request, Response].collect { case st: RootServiceTree[Request, Response, Payload] => st }

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

case class TwoChildrenTree[ReqFull: ClassTag, ResFull: ClassTag, Child1Req, Child1Res, Child2Req, Child2Res, Payload](
                                                                                                                       delegateTree1: ServiceTree[Child1Req, Child1Res, Payload],
                                                                                                                       delegateTree2: ServiceTree[Child2Req, Child2Res, Payload],
                                                                                                                       payload: Payload,
                                                                                                                       serviceMaker: (Child1Req => Future[Child1Res], Child2Req => Future[Child2Res]) => ReqFull => Future[ResFull]
                                                                                                                     ) extends ServiceTree[ReqFull, ResFull, Payload] {

  /** Important constraints: Keep the same service even after map, and on multiple calls. This is the 'real' service */
  override lazy val service = serviceMaker(delegateTree1.service, delegateTree2.service)

  override def mapFromTree[NewPayload](fn: (ServiceTree[_, _, Payload]) => NewPayload) =
    TwoChildrenTree[ReqFull, ResFull, Child1Req, Child1Res, Child2Req, Child2Res, NewPayload](delegateTree1.mapFromTree(fn), delegateTree2.mapFromTree(fn), fn(this), (_, _) => service)

  override def foldFromTree[Acc](initial: Acc)(foldFn: (Acc, ServiceTree[_, _, Payload]) => Acc) = {
    val first = delegateTree1.foldFromTree(initial)(foldFn)
    val second = delegateTree2.foldFromTree(first)(foldFn)
    foldFn(second, this)
  }
}


trait ServiceLanguageExtension {

  type ServiceTransformer[OldReq, OldRes, Req, Res] =
    ServiceTree[OldReq, OldRes, ServiceDescription] =>
      ServiceTree[Req, Res, ServiceDescription]
  type ServiceDelegator[Req, Res] = ServiceTransformer[Req, Res, Req, Res]
  type ServiceAggregator[ReqFull, ResFull, Child1Req, Child1Res, Child2Req, Child2Res] =
    (ServiceTree[Child1Req, Child1Res, ServiceDescription], ServiceTree[Child2Req, Child2Res, ServiceDescription]) => ServiceTree[ReqFull, ResFull, ServiceDescription]


  //    => ServiceCreator[Req, Res, NewService, Payload])

  implicit class ServiceDescriptionPimper[OldReq, OldRes](sd: ServiceTree[OldReq, OldRes, ServiceDescription]) {
    def >--<[NewReq, NewRes](transformer: ServiceTransformer[OldReq, OldRes, NewReq, NewRes]): ServiceTree[NewReq, NewRes, ServiceDescription] =
      transformer(sd)

  }

  def root[Req: ClassTag, Res: ClassTag](description: String, serviceMaker: () => Req => Future[Res]) = RootServiceTree[Req, Res, ServiceDescription](ServiceDescription(description), serviceMaker)

  def delegate[Req: ClassTag, Res: ClassTag](description: String, delegate: ServiceTree[Req, Res, ServiceDescription], serviceMaker: (Req => Future[Res]) => Req => Future[Res]) =
    DelegateTree0(delegate, ServiceDescription(description), serviceMaker)

  def transform[OldReq: ClassTag, OldRes: ClassTag, NewReq: ClassTag, NewRes: ClassTag](description: String, delegate: ServiceTree[OldReq, OldRes, ServiceDescription], serviceMaker: (OldReq => Future[OldRes]) => NewReq => Future[NewRes]) =
    TransformerTree0(delegate, ServiceDescription(description), serviceMaker)

  def twoServices[ReqFull: ClassTag, ResFull: ClassTag, Child1Req, Child1Res, Child2Req, Child2Res, Payload](
                                                                                                              description: String,
                                                                                                              child1: ServiceTree[Child1Req, Child1Res, ServiceDescription],
                                                                                                              child2: ServiceTree[Child2Req, Child2Res, ServiceDescription],
                                                                                                              serviceMaker: (Child1Req => Future[Child1Res], Child2Req => Future[Child2Res]) => ReqFull => Future[ResFull]
                                                                                                            ) = {
    TwoChildrenTree(child1, child2, ServiceDescription(description), serviceMaker)
  }

}

case class ServiceDescription(description: String)


trait HttpServiceLanguageExtension extends ServiceLanguageExtension {
  def http(hostNameAndPort: String) = root(s"FinagleHttp($hostNameAndPort)", () => Http.newService(hostNameAndPort))
}

trait ServiceLanguage extends HttpServiceLanguageExtension with GenericCustomClientLanguageExtension with AddHostNameServiceLanguageExtension with LoggingClientServiceLanguageExtension with CacheServiceLanguage with RecoverFromErrorServiceLanguage

object ServiceLanguage extends ServiceLanguage