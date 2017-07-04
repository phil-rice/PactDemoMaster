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
            .uponReceiving("/token/id/1")
            .willRespondWith(200, """{"id":"112233-valid-token"}""")
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
            .uponReceiving("/token/id/2")
            .willRespondWith(200, """{"id":"445566-invalid-token"}""")
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
