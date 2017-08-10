package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class AssetsControllerSpec extends FinatraControllerSpec with BeforeAndAfterAll with BeforeAndAfter {

  def controllerUnderTest = new AssetsController

  behavior of "Assets controller"

  it should "return contents of index.html for '/'" in {
    server.httpGet(
      path = "/",
      andExpect = Status.Ok,
      withBody = """IndexHtmlForTest"""
    ).contentType shouldBe Some("text/html; charset=utf-8")
  }
  it should "return contents of index.html for '/index.html'" in {
    server.httpGet(
      path = "/index.html",
      andExpect = Status.Ok,
      withBody = """IndexHtmlForTest"""
    ).contentType shouldBe Some("text/html; charset=utf-8")
  }


}
