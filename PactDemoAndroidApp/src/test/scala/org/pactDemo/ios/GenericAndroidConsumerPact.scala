package org.pactDemo.ios

import org.pactDemo.utilities.GenericCustomClient
import com.twitter.finagle.Http
import com.twitter.util.{Await, Future}
import org.pactDemo.utilities.FinatraClient
import org.scalatest.{FunSpec, Matchers}
import org.pactDemo.utilities.Futures._

/**
  * Created by aban.m on 04-07-2017.
  */
class GenericAndroidConsumerPact extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  describe("Connecting to the Provider service") {


    it("should be able to detect valid token - passing valid token") {
      forgePact
        .between("CustomeAndroid")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Validating valid request result")
            .given("token '12345-valid-for-id-1-token' is valid for id 1")
            .uponReceiving(method = GET, path = "/token/id/1", query = None, headers = Map("Authentication" -> "12345-valid-for-id-1-token"), body = None, matchingRules = None)
            //.uponReceiving("/token/id/1")
            .willRespondWith(200, """{"id":"12345-valid-for-id-1-token"}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[AndriodCustomeAuthenticationRequest, AndriodCustomeAuthentication](rawHttpClient)
            val request = AndriodCustomeAuthenticationRequest("1", "valid")
            client(request).await shouldBe AndriodCustomeAuthenticationValid
        }
    }

    it("should be able to detect invalid token - passing invalid token") {
      forgePact
        .between("CustomeAndroid")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Validating invalid request result")
            .given("token '54321-invalid-for-id-2-token' is valid for id 2")
            .uponReceiving(method = GET, path = "/token/id/2", query = None, headers = Map("Authentication" -> "54321-invalid-for-id-2-token"), body = None, matchingRules = None)
            //.uponReceiving("/token/id/2")
            .willRespondWith(200, """{"id":"54321-invalid-for-id-2-token"}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[AndriodCustomeAuthenticationRequest, AndriodCustomeAuthentication](rawHttpClient)
            val request = AndriodCustomeAuthenticationRequest("2", "invalid")
            client(request).await shouldBe AndriodCustomeAuthenticationInValid
        }
    }

  }


}
