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

}
