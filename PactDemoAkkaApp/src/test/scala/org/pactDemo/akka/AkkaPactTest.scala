package org.pactDemo.akka

import com.twitter.finagle.Http
import org.pactDemo.utilities.Futures._
import org.pactDemo.utilities.GenericCustomClient
import org.scalatest.{FunSpec, Matchers}

/**
  * Created by aban.m on 04-07-2017.
  */
class AkkaPactTest extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  describe("Connecting to the Android service") {

    it("should be able to detect valid token - passing valid token") {
      forgePact
        .between("AkkaClient")
        .and("CustomerAndroid")
        .addInteraction(
          interaction
            .description("Validating valid request result")
            .given("token '12345-valid-for-id-1-token' is valid for id 1")
            .uponReceiving(method = POST, path = "/token/id/1", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"Authentication-token":"token 12345-valid-for-id-1-token"}""", matchingRules = None)
            .willRespondWith(200, """{"token":"12345-valid-for-id-1-token","id":"1", "valid": true,"server":"android"}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[CustomRequestObject, CustomReplyObject](rawHttpClient)
            val request = CustomRequestObject(1, "12345-valid-for-id-1-token")
            client(request).await shouldBe CustomReplyObject(1,"12345-valid-for-id-1-token",true,"android")
        }
    }

    it("should be able to detect invalid token - passing invalid token") {
      forgePact
        .between("AkkaClient")
        .and("CustomerAndroid")
        .addInteraction(
          interaction
            .description("Validating invalid request result")
            .given("token '54321-invalid-for-id-2-token' is valid for id 2")
            .uponReceiving(method = POST, path = "/token/id/2", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"Authentication-token":"token 54321-invalid-for-id-2-token"}""", matchingRules = None)
            //.willRespondWith(401, """Unauthorized token 54321-invalid-for-id-2-token""")
            .willRespondWith(401, """{"token":"54321-invalid-for-id-2-token","id":"2", "valid": false,"server":"android"}""")

        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[CustomRequestObject, CustomReplyObject](rawHttpClient)
            val request = CustomRequestObject(2, "54321-invalid-for-id-2-token")
            client(request).await shouldBe CustomReplyObject(2, "54321-invalid-for-id-2-token",false,"android")
        }
    }

  }


}
