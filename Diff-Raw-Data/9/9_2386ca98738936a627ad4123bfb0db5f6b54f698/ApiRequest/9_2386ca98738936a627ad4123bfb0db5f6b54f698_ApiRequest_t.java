 package com.ikkerens.apifetcher;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import oauth.signpost.OAuthConsumer;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.net.Uri;
 import android.os.AsyncTask;
 
 public class ApiRequest {
     private String                              url;
     private OAuthConsumer                       signature;
     private Map< Method, Map< String, Object >> arguments;
     private FetchResult                         resultHandler;
     private Method                              method;
 
     /**
      * Prepares a new ApiFetcher. The GET method applies all arguments to the url, specifying no body. The POST and JSON methods send data using post, putting the arguments in the body unless stated otherwise per argument.
      * 
      * @param url
      *            Base url to fetch from
      * @param method
      *            POST, GET or JSON
      */
     public ApiRequest( String url, Method method ) {
         this.url = url;
         this.arguments = new HashMap< Method, Map< String, Object > >();
         this.method = method;
     }
 
     /**
      * Puts a new string argument
      * 
      * @param method
      *            The way to send this argument as.
      * @param key
      *            Key to identify this argument
      * @param value
      *            The string value
      */
     public void putArgument( Method method, String key, String value ) {
         if ( this.method == Method.GET && method != Method.GET )
             throw new RuntimeException( "Cannot specify a body to a GET request." );
 
         if ( method != Method.GET && method != this.method )
             throw new RuntimeException( "Cannot cross-pass arguments amongst types." );
 
         if ( !arguments.containsKey( method ) )
             arguments.put( method, new HashMap< String, Object >() );
 
         this.arguments.get( method ).put( key, value );
     }
 
     /**
      * Puts a new string argument
      * 
      * @param key
      *            Key to identify this argument
      * @param value
      *            The string value
      */
     public void putArgument( String key, String value ) {
         this.putArgument( method, key, value );
     }
 
     /**
      * Puts a new generic argument
      * 
      * @param method
      *            The way to send this argument as.
      * @param key
      *            Key to identify this argument
      * @param value
      *            The value
      */
     public void putArgument( Method method, String key, Object value ) {
         putArgument( method, key, value.toString() );
     }
 
     /**
      * Puts a new generic argument
      * 
      * @param key
      *            Key to identify this argument
      * @param value
      *            The value
      */
     public void putArgument( String key, Object value ) {
         putArgument( method, key, value.toString() );
     }
 
     /**
      * Used to set a response handler, one for both success and failure
      * 
      * @param afterExecute
      */
     public void setResultHandler( FetchResult afterExecute ) {
         this.resultHandler = afterExecute;
     }
 
     /**
      * Sign this request using a consumer
      * 
      * @param signature
      *            The consumer
      */
     public void sign( OAuthConsumer signature ) {
         this.signature = signature;
     }
 
     /**
      * Starts handling, should be called after setting all arguments/parameters
      */
     public void execute() {
         GetTask spt = new GetTask();
         spt.execute();
     }
 
     /**
      * Gets the url currently used in this request
      * 
      * @return Uri
      * @throws URISyntaxException
      *             Thrown when the Uri's syntax is invalid (shouldn't happen)
      * @throws InstantiationException
      *             Thrown when the engine is unable to initiate the method (shouldn't happen)
      * @throws IllegalAccessException
      *             Thrown when the engine is unable to initiate the method (shouldn't happen)
      */
     public Uri getRequestUri() throws Exception {
         return Uri.parse( this.url + "?" + new Scanner( Method.GET.getBody( arguments.get( Method.GET ) ).getContent() ).nextLine() );
     }
 
     private class GetTask extends AsyncTask< Void, Void, String > {
         @Override
         protected String doInBackground( Void... params ) {
             URI url;
             HttpClient httpClient = null;
             try {
                 // Create connection
                 if ( arguments.containsKey( Method.GET ) )
                     url = new URI( ApiRequest.this.url + "?" + new Scanner( Method.GET.getBody( arguments.get( Method.GET ) ).getContent() ).nextLine() );
                 else
                     url = new URI( ApiRequest.this.url );
 
                 HttpUriRequest request = ApiRequest.this.method.prepare( url );
                 String header = ApiRequest.this.method.getHeader();
                 if ( header != null )
                     request.setHeader( "Content-Type", header );
 
                 if ( request instanceof HttpEntityEnclosingRequestBase && ApiRequest.this.arguments.containsKey( method ) )
                     ( (HttpEntityEnclosingRequestBase) request ).setEntity( method.getBody( arguments.get( method ) ) );
 
                if ( signature != null )
                     signature.sign( request );
 
                 httpClient = new DefaultHttpClient();
                 HttpResponse response = httpClient.execute( request );
 
                 // Get Response
                 if ( response.getEntity() == null )
                     return "";
 
                 BufferedReader in = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );
                 String line;
                 StringBuffer responseLine = new StringBuffer();
                 while ( ( line = in.readLine() ) != null )
                     responseLine.append( line );
 
                 in.close();
 
                 return responseLine.toString();
             } catch ( Exception e ) {
                 e.printStackTrace();
             }
 
             return null;
         }
 
         @Override
         protected void onPostExecute( String result ) {
             if ( ApiRequest.this.resultHandler != null ) {
                 if ( result != null )
                     try {
                         ApiRequest.this.resultHandler.onSuccess( result );
                         return;
                     } catch ( Exception e ) {
                         e.printStackTrace();
                     }
                 try {
                     ApiRequest.this.resultHandler.onFail();
                 } catch ( Exception e ) {
                     new Exception( "Failing failed.", e ).printStackTrace();
                 }
             }
         }
     }
 }
