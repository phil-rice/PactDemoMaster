package org.pactDemo.javaConsumer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by prasenjit.b on 7/18/2017.
 */
public class CustomURLConnection {

    private static final String RESPONSE_OK = "OK";
    private static final String RESPONSE_UNAUTHORIZED = "UNAUTHORIZED";

    /**
     * This method is designed to create a URL connection from given a proper URL
     * @param urlStr
     * @return HttpURLConnection
     * @throws MalformedURLException
     * @throws IOException
     */
    private HttpURLConnection createConnection( String urlStr ) throws MalformedURLException,IOException {
        HttpURLConnection conn = null;
        if( urlStr != null && !("").equals( urlStr ) ){
            URL url = new URL( urlStr );
            conn = (HttpURLConnection) url.openConnection();
        }
        return conn;
    }

    /**
     * This method is designed to set specific properties of a given URL connection
     * @param urlConn
     * @param method
     * @param contentType
     * @throws ProtocolException
     */
    private void setConnectionProperties( HttpURLConnection urlConn, String method, String contentType) throws ProtocolException{
        urlConn.setDoOutput(true);
        urlConn.setRequestMethod( method );
        urlConn.setRequestProperty("Content-Type", contentType);
    }

    /**
     * This method is designed to ingest post body for a post call
     * @param urlConn
     * @param postBody
     * @throws IOException
     */
    private void ingestPostBody( HttpURLConnection urlConn, String postBody ) throws IOException{
        OutputStream os = urlConn.getOutputStream();
        os.write( postBody.getBytes() );
        os.flush();
    }

    /**
     * This method is designed to get the specific response code applicable for this requirement
     * @param urlConn
     * @return
     * @throws IOException
     */
    private String validateResponseCode( HttpURLConnection urlConn ) throws IOException  {
        String responseCode = null;
        if( urlConn.getResponseCode() == HttpURLConnection.HTTP_OK ){
            responseCode = RESPONSE_OK;
        }else if( urlConn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED ){
            responseCode = RESPONSE_UNAUTHORIZED;
        }else{
            throw new RuntimeException("Failed : HTTP Error code : "+urlConn.getResponseCode());
        }
        return responseCode;
    }

    /**
     * This method is designed get URL InputStream or ErrorStream depending on the given response code
     * @param urlConn
     * @param responseCode
     * @return
     * @throws IOException
     */
    private InputStream getProperResponseInputStream( HttpURLConnection urlConn, String responseCode ) throws IOException {
        InputStream is = null;
        if( RESPONSE_OK.equals( responseCode ) ){
            is = urlConn.getInputStream();
        }else if( RESPONSE_UNAUTHORIZED.equals( responseCode ) ){
            is = urlConn.getErrorStream();
        }
        return is;
    }

    /**
     * This method is desined to read the given InputStream
     * @param is
     * @return
     * @throws IOException
     */
    private String readResponse( InputStream is ) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader( is ));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }finally {
            if( reader != null ) reader.close();
        }
        return buffer.toString();
    }

    /**
     * This method is designed to release URL connection
     * @param urlConn
     */
    private void releaseConnection( HttpURLConnection urlConn ){
       if( urlConn != null ) {
           urlConn.disconnect();
       }
    }

    public String callURL( String urlStr, String methodName, String postBody, String contentType ) throws IOException{
        String response = null;
        HttpURLConnection urlConn = null;
        try {
            urlConn = createConnection( urlStr );
            setConnectionProperties( urlConn, methodName, contentType );
            ingestPostBody(urlConn, postBody);
            response = readResponse( getProperResponseInputStream( urlConn, validateResponseCode( urlConn ) ) );
        }finally {
            releaseConnection( urlConn );
        }
        return response;
    }

}
