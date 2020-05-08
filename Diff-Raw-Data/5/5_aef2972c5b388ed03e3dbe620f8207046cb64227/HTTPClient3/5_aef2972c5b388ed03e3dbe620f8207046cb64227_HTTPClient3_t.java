 /*
  * Copyright (C) 2008-2009 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package iudex.http.httpclient3;
 
 import iudex.http.HTTPClient;
 import iudex.http.HTTPSession;
 import iudex.http.Header;
 import iudex.http.ResponseHandler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.HeadMethod;
 
 public class HTTPClient3 implements HTTPClient
 {
 
     public HTTPClient3( HttpClient client )
     {
         _client = client;
     }
 
     @Override
     public HTTPSession createSession()
     {
         return new Session();
     }
 
     @Override
     public void request( HTTPSession session, ResponseHandler handler )
     {
         ((Session) session).execute( handler );
     }
 
     private class Session extends HTTPSession
     {
         public List<Header> requestHeaders()
         {
             org.apache.commons.httpclient.Header[] inHeaders =
                 _httpMethod.getRequestHeaders();
             List<Header> outHeaders =
                 new ArrayList<Header>( inHeaders.length + 1 );
 
             outHeaders.add( new Header( "Request-Line",
                                         reconstructRequestLine() ) );
 
             copyHeaders( inHeaders, outHeaders );
 
             return outHeaders;
 
             //FIXME: Adapter? Lazy Cache?
         }
 
        /* FIXME: Final Url?
         public URI uri() throws URIException
         {
             return _httpMethod.getURI();
         }
        */
 
         public int responseCode()
         {
             return _httpMethod.getStatusCode();
         }
 
         public String statusText()
         {
             return _httpMethod.getStatusText();
         }
 
         public List<Header> responseHeaders()
         {
             org.apache.commons.httpclient.Header[] inHeaders =
                 _httpMethod.getResponseHeaders();
 
             List<Header> outHeaders =
                 new ArrayList<Header>( inHeaders.length + 1 );
 
             outHeaders.add( new Header( "Status-Line",
                                         _httpMethod.getStatusLine() ) );
 
             copyHeaders( inHeaders, outHeaders );
 
             return outHeaders;
             //FIXME: Adapter? Lazy Cache?
         }
 
         public InputStream responseStream() throws IOException
         {
             return _httpMethod.getResponseBodyAsStream();
         }
 
         public void abort() throws IOException
         {
             _httpMethod.abort();
             close(); //FIXME: Good idea to also close?
         }
 
         public void close() throws IOException
         {
             super.close(); //FIXME: Or abstract?
 
             if( _httpMethod != null ) {
                 _httpMethod.releaseConnection();
                 _httpMethod = null;
             }
         }
 
         void execute( ResponseHandler handler )
         {
             try {
                 if( method() == Method.GET ) {
                     _httpMethod = new GetMethod( url() );
                 }
                 else if( method() == Method.HEAD ) {
                     _httpMethod = new HeadMethod( url() );
                 }
                 //FIXME: Set Headers from session?
 
                 int code = _client.executeMethod( _httpMethod );
                 if( ( code >= 200 ) && ( code < 300 ) ) {
                     handler.handleSuccess( this );
                 }
                 else {
                     handler.handleError( this, code );
                 }
             }
             catch( IOException e ) {
                 handler.handleException( this, e );
             }
         }
 
         private CharSequence reconstructRequestLine()
         {
             StringBuilder reqLine = new StringBuilder( 128 );
             reqLine.append( _httpMethod.getName() );
             reqLine.append( ' ' );
             reqLine.append( _httpMethod.getPath() );
             if( _httpMethod.getQueryString() != null ) {
                 reqLine.append( '?' );
                 reqLine.append( _httpMethod.getQueryString() );
             }
             return reqLine;
         }
 
         private List<Header>
         copyHeaders( org.apache.commons.httpclient.Header[] inHeaders,
                      List<Header> outHeaders )
         {
             for( org.apache.commons.httpclient.Header h : inHeaders  ) {
                 outHeaders.add( new Header( h.getName(), h.getValue() ) );
             }
             return outHeaders;
         }
 
         HttpMethod _httpMethod;
     }
 
     // FIXME: Conditional GET (If-Modified-Since, ETags?)
     // FIXME: Record all redirects
 
     private HttpClient _client;
 }
