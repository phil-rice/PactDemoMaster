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
public class JavaAndroidConsumerPactForInValidToken extends ConsumerPactTestMk2 {

    @Rule
    public PactProviderRuleMk2 mockProvider = new PactProviderRuleMk2("Android", "localhost", 9090, this );

    @Override
    @Pact( provider = "AndriodProvider", consumer = "JavaAndroidConsumer")
    protected RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("ContentType","application/hcl.token");
        return builder
                .uponReceiving("Invalid token")
                .path("/token/android/post")
                .method("POST")
                .body("{\"id\":2,\"token\":\"token invalid\"}","application/hcl.token")
                .willRespondWith()
                .status(200)
                .body("{\"id\":2,\"token\":\"token invalid\",\"valid\": false}")
                .toPact();
    }

    @Override
    protected String providerName() {
        return "Android";
    }

    @Override
    protected String consumerName() {
        return "JavaConsumer";
    }

    @Override
    @PactVerification( "Invalid token" )
    public void runTest( MockServer mockServer ) throws IOException {
        String url = mockServer.getUrl()+"/token/android/post";
        System.out.println("url1 => "+url);
        String methodName ="POST";
        String postBody ="{\"id\":2,\"token\":\"token invalid\"}";
        String contentType = "application/hcl.token";
        Assert.assertEquals( "{\"id\":2,\"token\":\"token invalid\",\"valid\":false}", new CustomURLConnection().callURL( url, methodName,postBody,contentType ) );
    }
}
