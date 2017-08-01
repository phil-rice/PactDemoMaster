package org.pactDemo.akka

import com.fasterxml.jackson.databind.JsonMappingException
import com.twitter.finagle.http.Response
import org.pactDemo.akka.CustomReplyObject.makeCustomResponse
import org.pactDemo.akka.CustomRequestObject.ToRequestForCustomRequestObject
import org.pactDemo.finatraUtilities.Json
import org.pactDemo.utilities.PactDemoSpec

class SupervisourPactActorSpec extends PactDemoSpec {

  import org.mockito.Mockito._

  val someResponse = mock[Response]

  def makeObject(s: String) = Json.fromJson[CustomRequestObject](s)

  it should "Test makeObject : return token value of CustomRequestObject" in {
    makeObject("""{"id": 1, "token":"12345-valid-for-id-1-token"}""").token shouldBe ("12345-valid-for-id-1-token")
  }

  it should "Test makeObject : return id value of CustomRequestObject" in {
    makeObject("""{"id": 1, "token":"12345-valid-for-id-1-token"}""").id shouldBe (1)
  }

  it should "Test makeObject : return empty token for empty token input" in {
    makeObject("""{"id": "", "token":""}""").token shouldBe ""
  }

  it should "Test makeObject : return 0 for empty id input" in {
    makeObject("""{"id": "", "token":""}""").id shouldBe 0
  }

  it should "Test makeObject : throws JsonMappingException for empty input" in {
    assertThrows[JsonMappingException](makeObject(""))
  }

  it should "Test makeObject : throws NullPointerException for null input" in {
    assertThrows[NullPointerException](makeObject(null))
  }


  /**
    * Test Case for makeRequest --> CustomeRequestProcessor[CustomRequest] extends (CustomRequest => Request)
    */

  it should "Test makeRequest : return proper JSON in request against CustomRequestObject" in {
    ToRequestForCustomRequestObject(CustomRequestObject(1, "12345-valid-for-id-1-token")).contentString shouldBe ("""{"id": "1", "token":"12345-valid-for-id-1-token"}""")
  }
//
//  it should "Test makeRequest : return token value from JSON in request against CustomRequestObject" in {
//    ToRequestForCustomRequestObject(CustomRequestObject(1, "12345-valid-for-id-1-token")).contentString.token shouldBe ("12345-valid-for-id-1-token")
//  }
//
//  it should "Test makeRequest : return id value from JSON in request against CustomRequestObject" in {
//    ToRequestForCustomRequestObject(CustomRequestObject(1, "12345-valid-for-id-1-token")).contentString.id shouldBe (1)
//  }
//
//  it should "Test makeRequest : return 0 as id value from JSON in request against CustomRequestObject" in {
//    ToRequestForCustomRequestObject(CustomRequestObject(0, "12345-valid-for-id-1-token")).contentString.id shouldBe (0)
//  }
//
//  it should "Test makeRequest : return empty as token value from JSON in request against CustomRequestObject" in {
//    ToRequestForCustomRequestObject(CustomRequestObject(0, "")).contentString.token shouldBe ("")
//  }
//
//  it should "Test makeRequest : return ContentType of a request as hcl.token" in {
//    ToRequestForCustomRequestObject(CustomRequestObject(0, "")).headerMap.get("ContentType").get shouldBe ("application/hcl.token")
//  }
//
//  it should "Test makeRequest : return method name of a request as post method" in {
//    ToRequestForCustomRequestObject(CustomRequestObject(0, "")).method.name shouldBe ("POST")
//  }
//
//  it should "Test makeRequest : return uri of a request as set in post method" in {
//    ToRequestForCustomRequestObject(CustomRequestObject(0, "")).uri shouldBe ("/token/android/post")
//  }
//
//  it should "Test makeRequest : throws JsonMappingException for empty CustomRequestObject" in {
//    assertThrows[JsonMappingException](ToRequestForCustomRequestObject(""))
//  }
//
//  it should "Test makeRequest : throws NullPointerException for null input" in {
//    assertThrows[NullPointerException](ToRequestForCustomRequestObject(null))
//  }
//
//
//  /**
//    * Test Case for makeCustomResponse --> CustomeResponseProcessor[CustomResponse] extends (Response => CustomResponse)
//    */
//
//  it should "Test makeCustomResponse : return token for correct JSON" in {
//    when(someResponse.contentString).thenReturn("""{"token":"12345-valid-for-id-1-token"}""")
//    makeCustomResponse(someResponse).token shouldBe ("12345-valid-for-id-1-token")
//  }
//
//  it should "Test makeCustomResponse : return id for correct JSON" in {
//    when(someResponse.contentString).thenReturn("""{"id":1}""")
//    makeCustomResponse(someResponse).id shouldBe (1)
//  }
//
//  it should "Test makeCustomResponse : return empty token for token value dose not exists" in {
//    when(someResponse.contentString).thenReturn("""{"token":""}""")
//    makeCustomResponse(someResponse).token shouldBe ("")
//  }
//
//  it should "Test makeCustomResponse : return JsonMappingException for empty request body" in {
//    when(someResponse.contentString).thenReturn("")
//    assertThrows[com.fasterxml.jackson.databind.JsonMappingException](makeCustomResponse(someResponse))
//  }
//
//  it should "Test makeCustomResponse : throws JsonMappingException for wrong JSON format" in {
//    when(someResponse.contentString).thenReturn("""{"AuthenticationToken":"12345-valid-for-id-1-token"}""")
//    assertThrows[com.fasterxml.jackson.databind.JsonMappingException](makeCustomResponse(someResponse))
//  }
}
