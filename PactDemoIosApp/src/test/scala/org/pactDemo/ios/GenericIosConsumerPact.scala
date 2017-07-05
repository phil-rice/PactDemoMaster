package org.pactDemo.ios

import com.twitter.finagle.Http
import org.pactDemo.utilities.GenericCustomClient
import org.scalatest.{FunSpec, Matchers}
import org.pactDemo.utilities.Futures._

/**
  * Created by aban.m on 04-07-2017.
  */
class GenericIosConsumerPact extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  describe("Connecting to the Consumer service") {

    it("should be able to detect valid token - passing valid token") {
      forgePact
        .between("CustomeIos")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Validating valid request result")
            .given("token '7899-valid-for-id-1-token'  is valid for id 1")
            .uponReceiving(method = GET, path = "/token/id/1", query = None, headers = Map("Authentication" -> "token 7899-valid-for-id-1-token"), body = None, matchingRules = None)
            //.uponReceiving("/token/id/1")
            .willRespondWith(200, """{"id":"7899-valid-for-id-1-token"}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[IosCustomeAuthenticationRequest, IosCustomeAuthentication](rawHttpClient)
            val request = IosCustomeAuthenticationRequest("1", "valid")
            client(request).await shouldBe IosCustomeAuthenticationValid
        }
    }

    it("should be able to detect invalid token - passing invalid token") {
      forgePact
        .between("CustomeIos")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Validating invalid request result")
            .given("token '112233-invalid-for-id-2-token' is invalid for id 2")
            .uponReceiving(method = GET, path = "/token/id/2", query = None, headers = Map("Authentication" -> "112233-invalid-for-id-2-token"), body = None, matchingRules = None)
            //.uponReceiving("/token/id/2")
            .willRespondWith(200, """{"id":"112233-invalid-for-id-2-token"}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[IosCustomeAuthenticationRequest, IosCustomeAuthentication](rawHttpClient)
            val request = IosCustomeAuthenticationRequest("2", "invalid")
            client(request).await shouldBe IosCustomeAuthenticationInValid
        }
    }


  }

}
