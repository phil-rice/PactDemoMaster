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
            .uponReceiving("/token/id/1")
            .willRespondWith(200, """{"id":"998877-valid-token"}""")
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
            val client = new GenericCustomClient[IosCustomeAuthenticationRequest, IosCustomeAuthentication](rawHttpClient)
            val request = IosCustomeAuthenticationRequest("2", "invalid")
            client(request).await shouldBe IosCustomeAuthenticationInValid
        }
    }


  }

}
