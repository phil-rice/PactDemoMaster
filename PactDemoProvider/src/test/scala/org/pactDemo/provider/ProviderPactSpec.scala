package org.pactDemo.provider

import com.twitter.finagle.http.Request
import org.pactDemo.provider.AuthenticationRequest.FromJsonForAuthenticationRequest
import org.pactDemo.provider.AuthenticationRequestBody.FromJsonToObjectRequestBody
import org.pactDemo.provider.GetActualToken.GetActualTokenForAuthenticationRequestBody
import org.pactDemo.utilities.PactDemoSpec

/**
  * Created by prasenjit.b on 7/7/2017.
  */
class ProviderPactSpec extends PactDemoSpec{
  import org.mockito.Mockito._
  val someRequest = mock[Request]

  it should "Test FromJsonToObject : return authentication token for correct JSON" in{
    when(someRequest.contentString).thenReturn("""{"Authentication-token":"123456"}""")
    FromJsonToObjectRequestBody(someRequest).`Authentication-token` shouldBe("123456")
  }

  it should "Test FromJsonToObject : return empty authentication token if token value does not exist" in{
    when(someRequest.contentString).thenReturn("""{"Authentication-token":""}""")
    FromJsonToObjectRequestBody(someRequest).`Authentication-token` shouldBe("")
  }

  it should "Test FromJsonToObject : throws JsonMappingException for empty request body" in{
    when(someRequest.contentString).thenReturn("")
    assertThrows[com.fasterxml.jackson.databind.JsonMappingException](FromJsonToObjectRequestBody(someRequest))
  }

  it should "Test FromJsonToObject : throws JsonMappingException for wrong JSON format" in{
    when(someRequest.contentString).thenReturn("""{"AuthenticationToken":"123456"}""")
    assertThrows[com.fasterxml.jackson.databind.JsonMappingException](FromJsonToObjectRequestBody(someRequest))
  }

  it should "Test GetActualToken : return authentication token for Authentication-token with space" in{
    GetActualTokenForAuthenticationRequestBody(AuthenticationRequestBody("token 123456")).`Authentication-token` shouldBe "123456"
  }

  it should "Test GetActualToken : return authentication token for Authentication-token with no space" in{
    GetActualTokenForAuthenticationRequestBody(AuthenticationRequestBody("token123456")).`Authentication-token` shouldBe "token123456"
  }

  it should "Test GetActualToken : return empty authentication token for empty Authentication-token" in{
    GetActualTokenForAuthenticationRequestBody(AuthenticationRequestBody("")).`Authentication-token` shouldBe ""
  }

  it should "Test GetActualToken : throws NullPointerException for null Authentication-token" in{
    assertThrows[NullPointerException]( GetActualTokenForAuthenticationRequestBody( AuthenticationRequestBody( null ) ) )
  }

  it should "Test AuthenticationRequest : returns AuthenticationRequest for given request id and content string" in{
    when(someRequest.contentString).thenReturn("""{"Authentication-token":"token 123456"}""")
    when(someRequest.getIntParam("id")).thenReturn(1)
    val authenticationRequest = FromJsonForAuthenticationRequest( someRequest )
    ( authenticationRequest.id, authenticationRequest.token ) shouldBe ( 1, "123456" )
  }

  it should "Test AuthenticationRequest : throws JsonMappingException for given request id and empty content string" in{
    when(someRequest.contentString).thenReturn("")
    when(someRequest.getIntParam("id")).thenReturn( 1 )
    assertThrows[com.fasterxml.jackson.databind.JsonMappingException]( FromJsonForAuthenticationRequest( someRequest ) )
  }
}
