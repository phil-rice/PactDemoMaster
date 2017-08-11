package org.pactDemo.finatra.service

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.pactDemo.finatra.structure.{DelegateTree0, ServiceDescription, ServiceLanguageExtension}

class AddHostNameService(val hostName: String, delegate: Request => Future[Response]) extends (Request => Future[Response]) {


  override def apply(v1: Request): Future[Response] = {
    v1.host = hostName
    delegate(v1)
  }
}

trait AddHostNameServiceLanguageExtension extends ServiceLanguageExtension {

  def addHostName[OldService <: Request => Future[Response]](hostName: String): ServiceDelegator[Request, Response] = { oldTree =>
    val actualHostName = hostName.split(":") match {
      case Array(host, _) => host
      case Array(host) => host
      case _ => throw new RuntimeException(s"Cannot understand hostname $hostName")
    }
    DelegateTree0[Request, Response, ServiceDescription](oldTree,
      ServiceDescription(s"AddHostNameService($actualHostName)"), new AddHostNameService(actualHostName, _))
  }
}
