package org.pactDemo.android

import com.twitter.finagle.http.Status
import com.twitter.finatra.http.EmbeddedHttpServer
import com.twitter.util.Future
import org.mockito.Mockito._
import org.pactDemo.android.{AndroidProviderController, IdAndToken, IdTokenAndValid}
import org.pactDemo.finatraUtilities.FinatraServer
import org.pactDemo.utilities.PactDemoSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class AndroidProviderSpec extends PactDemoSpec with BeforeAndAfterAll with BeforeAndAfter {

  val fakeProvider = mock[IdAndToken => Future[IdTokenAndValid]]
  val server = new EmbeddedHttpServer(new FinatraServer(0, new AndroidProviderController(fakeProvider))) //the port is ignored

  override def beforeAll(): Unit = {
    super.beforeAll()
    server.start()
  }

  override def afterAll(): Unit = {
    server.close()
    super.afterAll()
  }

  before {
    reset(fakeProvider)
  }

  behavior of "IosProvider"
  it should "Meet its response for IosProvider for Success scenario" in {
    when(fakeProvider.apply(IdAndToken(1, "validToken"))) thenReturn Future.value(IdTokenAndValid(1, "validToken", true))
    server.httpPost(
      path = "/token/android/post",
      postBody = """{"id": 1, "token":"validToken"}""",
      andExpect = Status.Ok,
      withJsonBody = """{"id":1,"token":"validToken","valid":true}"""
    )
  }

  it should "Meet its response for IosProvider for Failure scenario" in {
    when(fakeProvider.apply(IdAndToken(2, "invalidToken"))) thenReturn Future.value(IdTokenAndValid(2, "invalidToken", false))
    server.httpPost(
      path = "/token/android/post",
      postBody = """{"id": 2, "token":"invalidToken"}""",
      andExpect = Status.Ok,
      withJsonBody = """{"token":"invalidToken","id":2,"valid": false}"""
    )
  }

}
