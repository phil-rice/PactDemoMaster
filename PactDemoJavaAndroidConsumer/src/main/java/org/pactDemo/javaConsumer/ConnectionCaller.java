package org.pactDemo.javaConsumer;

import java.io.IOException;


/**
 * Created by prasenjit.b on 7/18/2017.
 */
public class ConnectionCaller {
    public static void main( String[] args ){
        String urlString = "http://localhost:9090/token/android/post";
        String method = "POST";
        String postBody = "{\"id\":1,\"token\":\"token valid\"}";
        String contentType = "application/json";

        CustomURLConnection urlConnection = new CustomURLConnection();
        try {
            String response = urlConnection.callURL( urlString, method, postBody, contentType );
            System.out.println("response :: "+response);
        }catch (IOException ioe ){
            ioe.printStackTrace();
        }
    }
}
