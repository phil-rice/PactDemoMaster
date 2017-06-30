package org.pactDemo.ios

import com.twitter.finagle.Http
import com.twitter.util.{Await, Future}
import org.pactDemo.utilities.FinatraClient
import org.scalatest.{FunSpec, Matchers}
import org.pactDemo.utilities.Futures._

class AndroidConsumerPact extends FunSpec with Matchers {

  import com.itv.scalapact.ScalaPactForger._

  /**
    * Given the token is valid for that ID
      When I send
         /token/id/1
      With the header:
         Authorisation token: 1923879ladskfhsdlfkj

      Then I get status 200 and “{“token” : “ok”}

      Given the token is in valid for that ID

      When I send
         /token/id/1
      With the header:
         Authorisation token: 1923invalidsldkfj
      And the token is not valid for that ID
      Then I get status 200 and “{“token” : “invalid”}

    */

  //  implicit val formats = DefaultFormats

  describe("Connecting to the Provider service") {

    it("should be able to detect invalid token passing invalid token") {
      forgePact
        .between("Android")
        .and("Provider")
        .addInteraction(
          interaction
            .description("Fetching results")
            //            .given("Results: Bob, Fred, Harry")
            .uponReceiving("/token/id/2")
            .willRespondWith(200, """{"id":"1923879ladskfhsdlfkj-invalid"}""")
        )
        .runConsumerTest {
          mockConfig =>
            val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
            val client = new ArdroidAuthenticationClient( rawHttpClient )
            val request = AndriodAuthenticationRequest( "2", "invalid")
            client( request ).await shouldBe AndriodAuthenticationInValid
        }
    }
  }

  it("should be able to detect valid token") {
    forgePact
      .between("Android")
      .and("Provider")
      .addInteraction(
        interaction
          .description("Fetching results")
          //            .given("Results: Bob, Fred, Harry")
          .uponReceiving("/token/id/2")
          .willRespondWith(200, """{"id":"1923879ladskfhsdlfkj-valid"}""")
      )
      .runConsumerTest {
        mockConfig =>
          val rawHttpClient = Http.newService(mockConfig.host + ":" + mockConfig.port)
          val client = new ArdroidAuthenticationClient( rawHttpClient )
          val request = AndriodAuthenticationRequest( "2", "valid")
          client( request ).await shouldBe AndriodAuthenticationValid
      }
  }
}