 /*******************************************************************************
  * Copyright 2011 John Casey
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.commonjava.web.test.fixture;
 
 import static org.apache.commons.io.IOUtils.copy;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.Type;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 
 import org.apache.http.Header;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.ProtocolException;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.client.DefaultRedirectStrategy;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.protocol.HttpContext;
 import org.commonjava.web.common.model.Listing;
 import org.commonjava.web.common.ser.JsonSerializer;
 import org.junit.rules.ExternalResource;
 
 import com.google.gson.reflect.TypeToken;
 
 public class WebFixture
     extends ExternalResource
 {
     public static final String DEFAULT_HOST = "localhost";
 
     public static final int DEFAULT_PORT = 8080;
 
     private JsonSerializer serializer;
 
     private DefaultHttpClient http;
 
     private int port = DEFAULT_PORT;
 
     private String host = DEFAULT_HOST;
 
     private String apiVersion = "1.0";
 
     public WebFixture()
     {
         this.serializer = new JsonSerializer();
     }
 
     public WebFixture( final JsonSerializer serializer )
     {
         this.serializer = serializer;
     }
 
     public void disableRedirection()
     {
         http.setRedirectStrategy( new DefaultRedirectStrategy()
         {
             @Override
             public boolean isRedirected( final HttpRequest request, final HttpResponse response,
                                          final HttpContext context )
                 throws ProtocolException
             {
                 return false;
             }
         } );
     }
 
     public void enableRedirection()
     {
         http.setRedirectStrategy( new DefaultRedirectStrategy() );
     }
 
    @Override
    protected void before()
         throws Exception
     {
         final ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager();
         ccm.setMaxTotal( 20 );
 
         http = new DefaultHttpClient( ccm );
     }
 
     public void assertLocationHeader( final HttpResponse response, final String value )
     {
         final Header[] headers = response.getHeaders( "Location" );
         assertThat( headers, notNullValue() );
         assertThat( headers.length, equalTo( 1 ) );
 
         final String header = headers[0].getValue();
         assertThat( header, equalTo( value ) );
     }
 
     public <T> T get( final String url, final Class<T> type )
         throws Exception
     {
         final HttpGet get = new HttpGet( url );
         try
         {
             return http.execute( get, new ResponseHandler<T>()
             {
                 @SuppressWarnings( "unchecked" )
                 @Override
                 public T handleResponse( final HttpResponse response )
                     throws ClientProtocolException, IOException
                 {
                     final StatusLine sl = response.getStatusLine();
                     assertThat( sl.getStatusCode(), equalTo( HttpStatus.SC_OK ) );
 
                     return serializer.fromStream( response.getEntity()
                                                           .getContent(), "UTF-8", type );
                 }
             } );
         }
         finally
         {
             get.abort();
         }
     }
 
     public void get( final String url, final int expectedStatus )
         throws Exception
     {
         final HttpGet get = new HttpGet( url );
         try
         {
             http.execute( get, new ResponseHandler<Void>()
             {
                 @Override
                 public Void handleResponse( final HttpResponse response )
                     throws ClientProtocolException, IOException
                 {
                     final StatusLine sl = response.getStatusLine();
                     assertThat( sl.getStatusCode(), equalTo( expectedStatus ) );
 
                     return null;
                 }
             } );
         }
         finally
         {
             get.abort();
         }
     }
 
     public String getString( final String url, final int expectedStatus )
         throws ClientProtocolException, IOException
     {
         final HttpResponse response = http.execute( new HttpGet( url ) );
         final StatusLine sl = response.getStatusLine();
 
         assertThat( sl.getStatusCode(), equalTo( expectedStatus ) );
         assertThat( response.getEntity(), notNullValue() );
 
         final StringWriter sw = new StringWriter();
         copy( response.getEntity()
                       .getContent(), sw );
 
         return sw.toString();
     }
 
     public HttpResponse getWithResponse( final String url, final int expectedStatus )
         throws Exception
     {
         final HttpGet get = new HttpGet( url );
         try
         {
             final HttpResponse response = http.execute( get );
             final StatusLine sl = response.getStatusLine();
             assertThat( sl.getStatusCode(), equalTo( expectedStatus ) );
 
             return response;
         }
         finally
         {
             get.abort();
         }
     }
 
     public <T> Listing<T> getListing( final String url, final TypeToken<Listing<T>> token )
         throws Exception
     {
         final HttpGet get = new HttpGet( url );
         try
         {
             return http.execute( get, new ResponseHandler<Listing<T>>()
             {
                 @SuppressWarnings( "unchecked" )
                 @Override
                 public Listing<T> handleResponse( final HttpResponse response )
                     throws ClientProtocolException, IOException
                 {
                     final StatusLine sl = response.getStatusLine();
                     assertThat( sl.getStatusCode(), equalTo( HttpStatus.SC_OK ) );
 
                     return serializer.listingFromStream( response.getEntity()
                                                                  .getContent(), "UTF-8", token );
                 }
             } );
         }
         finally
         {
             get.abort();
         }
     }
 
     public HttpResponse delete( final String url )
         throws Exception
     {
         final HttpDelete request = new HttpDelete( url );
         try
         {
             final HttpResponse response = http.execute( request );
 
             assertThat( response.getStatusLine()
                                 .getStatusCode(), equalTo( HttpStatus.SC_OK ) );
 
             return response;
         }
         finally
         {
             request.abort();
         }
     }
 
     public HttpResponse post( final String url, final Object value, final int status )
         throws Exception
     {
         final HttpPost request = new HttpPost( url );
         request.setEntity( new StringEntity( serializer.toString( value ), "application/json", "UTF-8" ) );
 
         try
         {
             final HttpResponse response = http.execute( request );
 
             assertThat( response.getStatusLine()
                                 .getStatusCode(), equalTo( status ) );
 
             return response;
         }
         finally
         {
             request.abort();
         }
     }
 
     public HttpResponse post( final String url, final Object value, final Type type, final int status )
         throws Exception
     {
         final HttpPost request = new HttpPost( url );
         request.setEntity( new StringEntity( serializer.toString( value, type ), "application/json", "UTF-8" ) );
 
         try
         {
             final HttpResponse response = http.execute( request );
 
             assertThat( response.getStatusLine()
                                 .getStatusCode(), equalTo( status ) );
 
             return response;
         }
         finally
         {
             request.abort();
         }
     }
 
     public String resourceUrl( final String... path )
         throws MalformedURLException
     {
         final String[] parts = new String[path.length + 1];
         parts[0] = getApiVersion();
         System.arraycopy( path, 0, parts, 1, path.length );
 
         return buildUrl( "http://" + host + ( port == 80 ? "" : ":" + port ) + "/test/api/", parts );
     }
 
     public String getApiVersion()
     {
         return apiVersion;
     }
 
     public void setApiVersion( final String apiVersion )
     {
         this.apiVersion = apiVersion;
     }
 
     public static String buildUrl( final String baseUrl, final String... parts )
         throws MalformedURLException
     {
         return buildUrl( baseUrl, null, parts );
     }
 
     public static String buildUrl( final String baseUrl, final Map<String, String> params, final String... parts )
         throws MalformedURLException
     {
         if ( parts == null || parts.length < 1 )
         {
             return baseUrl;
         }
 
         final StringBuilder urlBuilder = new StringBuilder();
 
         if ( !parts[0].startsWith( baseUrl ) )
         {
             urlBuilder.append( baseUrl );
         }
 
         for ( String part : parts )
         {
             if ( part.startsWith( "/" ) )
             {
                 part = part.substring( 1 );
             }
 
             if ( urlBuilder.length() > 0 && urlBuilder.charAt( urlBuilder.length() - 1 ) != '/' )
             {
                 urlBuilder.append( "/" );
             }
 
             urlBuilder.append( part );
         }
 
         if ( params != null && !params.isEmpty() )
         {
             urlBuilder.append( "?" );
             boolean first = true;
             for ( final Map.Entry<String, String> param : params.entrySet() )
             {
                 if ( first )
                 {
                     first = false;
                 }
                 else
                 {
                     urlBuilder.append( "&" );
                 }
 
                 urlBuilder.append( param.getKey() )
                           .append( "=" )
                           .append( param.getValue() );
             }
         }
 
         return new URL( urlBuilder.toString() ).toExternalForm();
     }
 
     public JsonSerializer getSerializer()
     {
         return serializer;
     }
 
     public DefaultHttpClient getHttp()
     {
         return http;
     }
 
     public int getPort()
     {
         return port;
     }
 
     public String getHost()
     {
         return host;
     }
 
     public void setSerializer( final JsonSerializer serializer )
     {
         this.serializer = serializer;
     }
 
     public void setPort( final int port )
     {
         this.port = port;
     }
 
     public void setHost( final String host )
     {
         this.host = host;
     }
 }
