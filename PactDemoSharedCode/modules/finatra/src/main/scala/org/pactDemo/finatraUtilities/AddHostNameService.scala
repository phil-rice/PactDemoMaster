package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

class AddHostNameService(hostName: String, delegate: Request => Future[Response]) extends (Request => Future[Response]) {
  val actualHostName = hostName.split(":") match {
    case Array(host, _) => host
    case Array(host) => host
    case _ => throw new RuntimeException(s"Cannot understand hostname $hostName")
  }

  override def apply(v1: Request): Future[Response] = {
    v1.host = actualHostName
    delegate(v1)
  }
}

trait AddHostNameServiceLanguageExtension extends ServiceLanguageExtension {

  def addHostName[OldService <: Request => Future[Response]](hostName: String): ServiceDelegator[Request, Response] = { oldTree =>
    DelegateTree0[Request, Response, ServiceDescriptionAndCreator[Request, Response]](oldTree,
      ServiceDescriptionAndCreator[Request, Response](
        s"AddHostNameService($hostName)", () => new AddHostNameService(hostName, oldTree.payload.service)))
  }
}
