package org.pactDemo.ios

import com.twitter.finagle.Http
import com.twitter.util.{Await, Future}
import org.pactDemo.utilities.FinatraClient
import org.scalatest.{FunSpec, Matchers}
import org.pactDemo.utilities.Futures._

class IosConsumerPact extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._


  //  implicit val formats = DefaultFormats

  describe("Connecting to the Provider service") {
    //    Given the token is valid for that ID
    //      When I send
    //    /token/id/1
    //    With the header:
    //      Authorisation token: 1923879ladskfhsdlfkj
    //    Then I get status 200 and “{“token” : “ok”}
    //    Given the token is in valid for that ID

    it("should be able to detect valid tokens") {
      forgePact
        .between("Ios")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Fetching results")
            .given("token '7899-valid-for-id-1-token'  is valid for id 1")
            .uponReceiving("/token/1")
            //add the header
            .willRespondWith(200, """{"token":"valid"}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new AuthenticationClient(rawHttpClient)
            val request = AuthenticationRequest("1", "7899-valid-for-id-1-token")
            client(request).await shouldBe AuthenticationValid
        }
    }
  }

  it("should be able to detect invalid tokens") {
    forgePact
      .between("Ios")
      .and("Provider")
      .addInteraction(
        interaction
          .description("Fetching results")
          .given("token '123-invalid-for-id-1-token'  is invalid for id 1")
          .uponReceiving("/token/1")
          //add the header
          .willRespondWith(200, """{"token":"invalid"}""")
      )
      .runConsumerTest {
        mockConfig =>
          val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
          val client = new AuthenticationClient(rawHttpClient)
          val request = AuthenticationRequest("1", "123-invalid-for-id-1-token")
          client(request).await shouldBe AuthenticationInvalid
      }
  }
}
