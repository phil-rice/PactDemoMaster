package org.pactDemo.ios

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import org.pactDemo.utilities.FinatraServer
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

/**
  * Created by prasenjit.b on 7/11/2017.
  */
class IosProviderSpec extends FlatSpec with BeforeAndAfterAll{

  val server = new EmbeddedHttpServer(new FinatraServer(0, new IosProvider())) //the port is ignored

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  override def afterAll(): Unit = {
    server.close()
    super.afterAll()
  }

  behavior of "IosProvider"
  it should "Meet its response for IosProvider for Success scenario" in{
    server.httpPost(
      path = "/token/id/1",
      postBody = """{"Authentication-token":"token valid"}""",
      andExpect = Status.Ok,
      withJsonBody = """{"id":"1","token":"valid"}"""
    )
  }

  it should "Meet its response for IosProvider for Failure scenario" in{
    server.httpPost(
      path = "/token/id/2",
      postBody = """{"Authentication-token":"token invalid"}""",
      andExpect = Status.Unauthorized,
      withJsonBody = """Unauthorized token invalid for id 2"""
    )
  }

}
