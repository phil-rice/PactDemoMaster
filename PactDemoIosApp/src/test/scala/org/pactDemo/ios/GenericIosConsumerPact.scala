package org.pactDemo.ios

import com.twitter.finagle.Http
import org.pactDemo.finatraUtilities.GenericCustomClient
import org.scalatest.{FunSpec, Matchers}
import org.pactDemo.finatraUtilities.Futures._
/**
  * Created by aban.m on 04-07-2017.
  */
class GenericIosConsumerPact extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  describe("Connecting to the Consumer service") {

    it("should be able to detect valid token - passing valid token") {
      forgePact
        .between("Ios")
        .and("RawProvider")
        .addInteraction(
          interaction
            .description("Validating valid request result")
            .given("token '7899-valid-for-id-1-token'  is valid for id 1")
            .uponReceiving(method = POST, path = "/token/id/1", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"Authentication-token":"token 7899-valid-for-id-1-token"}""", matchingRules = None)
            //.uponReceiving("/token/id/1")
            .willRespondWith(200, """{"token":"7899-valid-for-id-1-token", "id":"1", "valid": true}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[IosProviderRequest, IosAuthResponse](rawHttpClient)
            val request = IosProviderRequest(1, "7899-valid-for-id-1-token")
            client(request).await shouldBe IosValidAuthResponse(1, "7899-valid-for-id-1-token")
        }
    }

    it("should be able to detect invalid token - passing invalid token") {
      forgePact
        .between("Ios")
        .and("RawProvider")
        .addInteraction(
          interaction
            .description("Validating invalid request result")
            .given("token '112233-invalid-for-id-2-token' is invalid for id 2")
            .uponReceiving(method = POST, path = "/token/id/2", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"Authentication-token":"token 112233-invalid-for-id-2-token"}""", matchingRules = None)
            //.uponReceiving("/token/id/2")
            .willRespondWith(401, """Unauthorized token 112233-invalid-for-id-2-token""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[IosProviderRequest, IosAuthResponse](rawHttpClient)
            val request = IosProviderRequest(2, "112233-invalid-for-id-2-token")
            client(request).await shouldBe IosInValidAuthResponse(2, "112233-invalid-for-id-2-token")
        }
    }


  }

}
