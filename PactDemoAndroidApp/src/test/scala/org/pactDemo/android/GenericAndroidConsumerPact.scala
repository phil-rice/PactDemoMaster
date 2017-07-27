package org.pactDemo.android

import com.twitter.finagle.Http
import com.twitter.util.{Await, Future}
import org.pactDemo.android.{IdAndToken, IdTokenAndValid}
import org.pactDemo.finatraUtilities.GenericCustomClient
import org.scalatest.{FunSpec, Matchers}
import org.pactDemo.finatraUtilities.Futures._

/**
  * Created by aban.m on 04-07-2017.
  */
class GenericAndroidConsumerPact extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  describe("Connecting to the Provider service") {


    it("should be able to detect valid token - passing valid token") {
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
            val request = IdAndToken(1, "12345-valid-for-id-1-token")
            client(request).await shouldBe IdTokenAndValid(1, "12345-valid-for-id-1-token", true)
        }
    }

    it("should be able to detect invalid token - passing invalid token") {
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
            val request = IdAndToken(2, "54321-invalid-for-id-2-token")
            client(request).await shouldBe IdTokenAndValid(2, "54321-invalid-for-id-2-token", false)
        }
    }

  }


}