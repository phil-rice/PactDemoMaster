package org.pactDemo.android

import com.twitter.finagle.Http
import com.twitter.finagle.http.Status
import org.pactDemo.finatraUtilities.{FinatraControllerSpec, GenericCustomClient, MutableService}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}

class AndroidFullPathToPathSpec extends FinatraControllerSpec with BeforeAndAfterAll with BeforeAndAfter {

  val provider = new MutableService[IdAndToken, IdTokenAndValid]
  def controllerUnderTest = new AndroidProviderController(provider)

  after {
    provider.delegate = null
  }

  behavior of "IosProvider"

  import com.itv.scalapact.ScalaPactForger._

  it should "should be able to detect valid token - passing valid token" in {
    forgePact
      .between("CustomerAndroid")
      .and("Provider")
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
            postBody = """{"id": 1, "token":"validToken"}""",
            andExpect = Status.Ok,
            withJsonBody = """{"id":1,"token":"validToken","valid":true}"""
          )
      }
  }

  it should "be able to detect invalid token - passing invalid token" in {
    forgePact
      .between("CustomerAndroid")
      .and("Provider")
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
            postBody = """{"id": 2, "token":"invalidToken"}""",
            andExpect = Status.Ok,
            withJsonBody = """{"token":"invalidToken","id":2,"valid": false}"""
          )
      }
  }


}
