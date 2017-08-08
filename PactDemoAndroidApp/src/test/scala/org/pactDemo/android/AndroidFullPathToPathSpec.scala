package org.pactDemo.android

import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, GenericCustomClient, MutableService, NullSl4jLoggingAdapter}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class AndroidFullPathToPathSpec extends FinatraControllerSpec with BeforeAndAfterAll with BeforeAndAfter {
  implicit val loggingAdapter = NullSl4jLoggingAdapter

  val provider = new MutableService[IdAndToken, IdTokenAndValid]
import org.pactDemo.mustache.Mustache._
  def controllerUnderTest = new AndroidProviderController(provider)

  after {
    provider.delegate = null
  }

  behavior of "IosProvider"

  import com.itv.scalapact.ScalaPactForger._

  it should "should be able to detect valid token - passing valid token" in {
    forgePact
      .between("Android")
      .and("RawProvider")
      .addInteraction(
        interaction
          .description("Validating valid request result")
          .given("token '12345-valid-for-id-1-token' is valid for id 1")
          //.uponReceiving(method = POST, path = "/token/id/1", query = None, headers = Map("Authentication" -> "12345-valid-for-id-1-token"), body = None, matchingRules = None)
          .uponReceiving(method = POST, path = "/token/id/1", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"Authentication-token":"token 12345-valid-for-id-1-token"}""", matchingRules = None)
          //.uponReceiving("/token/id/1")
          .willRespondWith(200, """{"token":"12345-valid-for-id-1-token","id":"1", "valid": true}""")
      )
      .runConsumerTest {
        mockConfig =>
          val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
          val client = new GenericCustomClient[IdAndToken, IdTokenAndValid](rawHttpClient)
          provider.delegate = client
          server.httpPost(
            path = "/token/android/post",
            postBody = """{"id": 1, "token":"12345-valid-for-id-1-token"}""",
            andExpect = Status.Ok,
            withJsonBody = """{"id":1,"token":"12345-valid-for-id-1-token","valid":true}"""
          )
      }
  }

  it should "be able to detect invalid token - passing invalid token" in {
    forgePact
      .between("Android")
      .and("RawProvider")
      .addInteraction(
        interaction
          .description("Validating invalid request result")
          .given("token '54321-invalid-for-id-2-token' is valid for id 2")
          //.uponReceiving(method = GET, path = "/token/id/2", query = None, headers = Map("Authentication" -> "54321-invalid-for-id-2-token"), body = None, matchingRules = None)
          .uponReceiving(method = POST, path = "/token/id/2", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"Authentication-token":"token 54321-invalid-for-id-2-token"}""", matchingRules = None)
          //.uponReceiving("/token/id/2")
          .willRespondWith(401, """Unauthorized token 54321-invalid-for-id-2-token""")
      )
      .runConsumerTest {
        mockConfig =>
          val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
          val client = new GenericCustomClient[IdAndToken, IdTokenAndValid](rawHttpClient)
          provider.delegate = client
          server.httpPost(
            path = "/token/android/post",
            postBody = """{"id": 2, "token":"54321-invalid-for-id-2-token"}""",
            andExpect = Status.Ok,
            withJsonBody = """{"token":"54321-invalid-for-id-2-token","id":2,"valid": false}"""
          )
      }
  }


}
