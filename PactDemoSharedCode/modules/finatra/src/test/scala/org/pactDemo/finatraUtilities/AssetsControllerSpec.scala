package org.pactDemo.finatraUtilities

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class AssetsControllerSpec extends PactDemoSpec with BeforeAndAfterAll with BeforeAndAfter {

  val server = new EmbeddedHttpServer(new FinatraServer(0, new AssetsController)) //the port is ignored

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  override def afterAll(): Unit = {
    server.close()
    super.afterAll()
  }

  behavior of "Assets controller"

  it should "return contents of index.html for '/'" in {
    server.httpGet(
      path = "/",
      andExpect = Status.Ok,
      withBody = """IndexHtmlForTest"""
    ).contentType shouldBe Some("text/html")
  }
  it should "return contents of index.html for '/index.html'" in {
    server.httpGet(
      path = "/index.html",
      andExpect = Status.Ok,
      withBody = """IndexHtmlForTest"""
    ).contentType shouldBe Some("text/html")
  }



}
