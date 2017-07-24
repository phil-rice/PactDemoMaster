package org.pactDemo.javaConsumer;

import au.com.dius.pact.consumer.*;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.MockProviderConfig;
import au.com.dius.pact.model.RequestResponsePact;

import static au.com.dius.pact.consumer.ConsumerPactRunnerKt.runConsumerTest;
import static org.junit.Assert.assertEquals;

import au.com.dius.pact.model.Response;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prasenjit.b on 7/20/2017.
 */
public class JavaAndroidConsumerPact /*extends ConsumerPactTestMk2*/ {

    @Test
    public void testPactForInValidToken(){
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer("JavaAndroidConsumer")
                .hasPactWith("AndriodProvider")
                .uponReceiving("JavaAndriodPact Test interaction 1")
                .path("/token/android/post")
                .method("POST")
                .body("{\"id\":2,\"token\":\"token invalid\"}")
                .willRespondWith()
                .status(200)
                .body("{\"id\":2,\"token\":\"token invalid\",\"valid\": false}")
                .toPact();

        MockProviderConfig config = MockProviderConfig.createDefault();
        PactVerificationResult result = runConsumerTest( pact, config, mockServer -> {
            String url = mockServer.getUrl();
            String methodName = "POST";
            String postBody = "{\"id\":2,\"token\":\"token invalid\"}";
            String contentType = "application/hcl.token";
            assertEquals( "{\"id\":1,\"token\":\"token invalid\",\"valid\":false}", new CustomURLConnection().callURL( url, methodName,postBody,contentType ) );
        });

        /*if( result instanceof PactVerificationResult.Error ){
            throw new RuntimeException(((PactVerificationResult.Error)result).getError());
        }*/
        //assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }

    @Test
    public void testPactForValidToken(){
        RequestResponsePact pact = ConsumerPactBuilder
                .consumer("JavaAndroidConsumer")
                .hasPactWith("AndriodProvider")
                .uponReceiving("JavaAndriodPact Test interaction For Valid token")
                .path("/token/android/post")
                .method("POST")
                .body("{\"id\":1,\"token\":\"token valid\"}")
                .willRespondWith()
                .status(200)
                .body("{\"id\":1,\"token\":\"token valid\",\"valid\": true}")
                .toPact();

        MockProviderConfig config = MockProviderConfig.createDefault();
        PactVerificationResult result = runConsumerTest( pact, config, mockServer -> {
            String url = mockServer.getUrl();
            String methodName = "POST";
            String postBody = "{\"id\":1,\"token\":\"token valid\"}";
            String contentType = "application/hcl.token";
            assertEquals( "{\"id\":1,\"token\":\"token valid\",\"valid\":true}", new CustomURLConnection().callURL( url, methodName,postBody,contentType ) );
        });
    }

    //@Rule
    //public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("AndriodProvider", "localhost", 9090, this );

    //@Override
    /*@Pact( provider = "AndriodProvider", consumer = "JavaAndroidConsumer")
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ContentType","application/hcl.token");
        return builder
                .given("token '112233-invalid-for-id-2-token' is invalid for id 2")
                .uponReceiving("JavaAndriodPact Test interaction 1")
                .path("/token/android/post")
                .method("POST")
                .body("{\"id\":2,\"token\":\"token invalid\"}","application/hcl.token")
                .willRespondWith()
                .status(200)
                .body("{\"id\":1,\"token\":\"token invalid\",\"valid\": false}")
                *//*.given("token '112233-valid-for-id-1-token' is invalid for id 1")
                .uponReceiving("JavaAndriodPact Test interaction 2")
                .path("/token/android/post")
                .method("POST")
                .body("{\"id\":1,\"token\":\"token valid\"}","application/hcl.token")
                .willRespondWith()
                .status(200)
                .body("{\"id\": 1,\"token\":\"token valid\", \"valid\": true}")*//*
                .toPact();
    }*/

    //@Override
   /* protected String providerName() {
        return "AndriodProvider";
    }

    //@Override
    protected String consumerName() {
        return "JavaAndroidConsumer";
    }

    //@Override
    @Test
    @PactVerification( "JavaAndroidConsumerPact" )
    public void runTest(*//*MockServer mockServer*//*) throws IOException {
        String url = mockProvider.getUrl()+"/token/android/post";
        System.out.println("url1 => "+url);
        String methodName ="POST";
        String postBody ="{\"id\":2,\"token\":\"token invalid\"}";
        String contentType = "application/hcl.token";
        Assert.assertEquals( "{\"id\":1,\"token\":\"token invalid\",\"valid\":false}", new CustomURLConnection().callURL( url, methodName,postBody,contentType ) );

        url = mockProvider.getUrl()+"/token/android/post";
        System.out.println("url2 => "+url);
        postBody = "{\"id\":1,\"token\":\"token valid\"}";
        Assert.assertEquals( "{\"id\":1,\"token\":\"token valid\",\"valid\":true}", new CustomURLConnection().callURL( url, methodName,postBody,contentType ) );
    }*/

}
