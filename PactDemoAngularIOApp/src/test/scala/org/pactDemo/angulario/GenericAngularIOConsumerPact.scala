package org.pactDemo.angulario

import com.twitter.finagle.Http
import org.pactDemo.utilities.GenericCustomClient
import org.scalatest.{FunSpec, Matchers}
import org.pactDemo.utilities.Futures._

/**
  * Created by aban.m on 7/11/2017.
  */
class GenericAngularIOConsumerPact extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  describe("Connecting to the Generic AngularIO Consumer service") {

    it("should be able to detect valid token - by passing token") {

      forgePact
        .between("AngularIOConsumer")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Validating valid angular request result")
            .given("token '0007-valid-for-id-1-token' is valid for id 1")
            .uponReceiving(method = POST, path = "/token/id/1", query = None, headers = Map("ContentType" -> "application/json"), body = """{"Authentication-token":"token 0007-valid-for-id-1-token"}""", matchingRules = None)
            .willRespondWith(200, """{"token":"0007-valid-for-id-1-token", "id":"1", "valid": true}""")
        )
        .runConsumerTest {
          mockConfig =>

            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[AngularIOAuthenticationCustomRequest, AngularIOCustomAuthentication] (rawHttpClient)
            val request = AngularIOAuthenticationCustomRequest ("1", "0007-valid-for-id-1-token")
            client(request).await shouldBe AngularIOAuthenticationValid
        }
    }



    it("should be able to detect invalid token - by passing token") {

      forgePact
        .between("AngularIOConsumer")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Validating invalid angular request result")
            .given("token '9007-invalid-for-id-2-token' is invalid for id 2")
            .uponReceiving(method = POST, path = "/token/id/2", query = None, headers = Map("ContentType" -> "application/json"), body = """{"Authentication-token":"token 9007-invalid-for-id-2-token"}""", matchingRules = None)
            .willRespondWith(401, """Unauthorized token 9007-invalid-for-id-2-token""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new GenericCustomClient[AngularIOAuthenticationCustomRequest, AngularIOCustomAuthentication] (rawHttpClient)
            val request = AngularIOAuthenticationCustomRequest ("2", "9007-invalid-for-id-2-token")
            client(request).await shouldBe AngularIOAuthenticationInValid
        }
    }

  }

}
