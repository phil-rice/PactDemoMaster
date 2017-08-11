package org.pactDemo.finatraUtilities

import com.twitter.util.Future

import scala.reflect.ClassTag


trait AggregateTransform[From, To] extends (From => To)

trait AggregateMerger[Child1, Child2, Merged] extends (((Child1, Child2)) => Merged)

class AggregateService[ReqFull, ResFull, Child1Req, Child1Res, Child2Req, Child2Res](child1: Child1Req => Future[Child1Res], child2: Child2Req => Future[Child2Res]
                                                                                    )(implicit
                                                                                      reqToChild1Req: AggregateTransform[ReqFull, Child1Req],
                                                                                      reqToChild2Req: AggregateTransform[ReqFull, Child2Req],
                                                                                      merger: AggregateMerger[Child1Res, Child2Res, ResFull]) extends (ReqFull => Future[ResFull]) {

  override def apply(req: ReqFull) = Future.join(child1(reqToChild1Req(req)), child2(reqToChild2Req(req))).map[ResFull](merger)

}

trait AggregateServiceLanguageExtension extends ServiceLanguageExtension {

  def aggregate[ReqFull:ClassTag, ResFull:ClassTag, Child1Req:ClassTag, Child1Res:ClassTag, Child2Req:ClassTag, Child2Res:ClassTag](description: String)(implicit
                                                                                                            reqToChild1Req: AggregateTransform[ReqFull, Child1Req],
                                                                                                            reqToChild2Req: AggregateTransform[ReqFull, Child2Req],
                                                                                                            merger: AggregateMerger[Child1Res, Child2Res, ResFull]):
  ServiceAggregator[ReqFull, ResFull, Child1Req, Child1Res, Child2Req, Child2Res] = {
    (childTree1, childTree2) =>
      twoServices[ReqFull, ResFull, Child1Req, Child1Res, Child2Req, Child2Res, ServiceDescription](description, childTree1, childTree2, (t1, t2) => new AggregateService(t1, t2))
  }
}