package org.pactDemo.akka

import com.twitter.finagle.Http
import org.pactDemo.finatra.service.GenericCustomClient
import org.pactDemo.finatra.utilities.Futures._
import org.scalatest.{FunSpec, Matchers}

class AkkaPactTest extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  describe("Connecting to the Android service") {

    it("should be able to detect valid token - passing valid token") {
      forgePact
        .between("AkkaActorClient")
        .and("Android")
        .addInteraction(
          interaction
            .description("Validating valid request result")
            .given("token '12345-valid-for-id-1-token' is valid for id 1")
            .uponReceiving(method = POST, path = "/token/android/post", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"id": "1", "token":"12345-valid-for-id-1-token"}""", matchingRules = None)
            .willRespondWith(200, """{"token":"12345-valid-for-id-1-token","id":1, "valid": true}""")  // ,"server":"android"
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[CustomRequestObject, CustomReplyObject](rawHttpClient)
            val request = CustomRequestObject(1, "12345-valid-for-id-1-token")
            client(request).await shouldBe CustomReplyObject(1,"12345-valid-for-id-1-token",true) //,"android"
        }
    }

    it("should be able to detect invalid token - passing invalid token") {
      forgePact
        .between("AkkaActorClient")
        .and("Android")
        .addInteraction(
          interaction
            .description("Validating invalid request result")
            .given("token '54321-invalid-for-id-2-token' is valid for id 2")
            .uponReceiving(method = POST, path = "/token/android/post", query = None, headers = Map("ContentType" -> "application/hcl.token"), body = """{"id": "2", "token":"54321-invalid-for-id-2-token"}""", matchingRules = None)
            //.willRespondWith(401, """Unauthorized token 54321-invalid-for-id-2-token""")
            .willRespondWith(200, """{"token":"54321-invalid-for-id-2-token","id":2, "valid": false}""")  //,"server":"android"

        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[CustomRequestObject, CustomReplyObject](rawHttpClient)
            val request = CustomRequestObject(2, "54321-invalid-for-id-2-token")
            client(request).await shouldBe CustomReplyObject(2, "54321-invalid-for-id-2-token",false) //,"android"
        }
    }

  }


}
