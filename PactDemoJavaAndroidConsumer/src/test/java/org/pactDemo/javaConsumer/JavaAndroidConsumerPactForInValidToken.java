package org.pactDemo.javaConsumer;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import org.junit.Assert;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prasenjit.b on 7/26/2017.
 */
public class JavaAndroidConsumerPactForInValidToken extends ConsumerPactTestMk2 {
    @Override
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ContentType","application/hcl.token");
        return builder
                .uponReceiving("JavaAndriodPact Test interaction 2")
                .path("/token/android/post")
                .method("POST")
                .body("{\"id\":1,\"token\":\"token valid\"}","application/hcl.token")
                .willRespondWith()
                .status(200)
                .body("{\"id\": 1,\"token\":\"token valid\", \"valid\": true}")
                .toPact();
    }

    @Override
    protected String providerName() {
        return "AndriodProvider";
    }

    @Override
    protected String consumerName() {
        return "JavaAndroidConsumerForInValidToken";
    }

    @Override
    @PactVerification( "JavaAndroidConsumerPactForInValidToken" )
    protected void runTest(MockServer mockServer) throws IOException {
        String url = mockServer.getUrl()+"/token/android/post";
        String methodName ="POST";
        System.out.println("url2 => "+url);
        String contentType = "application/hcl.token";
        String postBody = "{\"id\":1,\"token\":\"token valid\"}";
        Assert.assertEquals( "{\"id\":1,\"token\":\"token valid\",\"valid\":true}", new CustomURLConnection().callURL( url, methodName,postBody,contentType ) );
    }
}
