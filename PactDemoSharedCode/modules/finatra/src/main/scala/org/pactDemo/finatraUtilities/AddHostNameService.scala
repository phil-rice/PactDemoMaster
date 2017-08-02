package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future

class AddHostNameService(hostName: String, delegate: Request => Future[Response]) extends (Request => Future[Response]) {
  override def apply(v1: Request): Future[Response] = {
    v1.host = hostName
    delegate(v1)
  }
}
