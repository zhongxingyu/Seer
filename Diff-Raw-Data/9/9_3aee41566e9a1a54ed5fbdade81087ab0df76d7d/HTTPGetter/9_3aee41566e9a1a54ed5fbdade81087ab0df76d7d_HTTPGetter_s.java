 /*
  * Copyright 2008-2012 Zuse Institute Berlin (ZIB)
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package de.zib.gndms.gndmc.utils;
 
 import de.zib.gndms.common.kit.security.CustomSSLContextRequestFactory;
 import de.zib.gndms.common.kit.security.SetupSSL;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.client.ClientHttpRequest;
 import org.springframework.http.client.ClientHttpResponse;
 import org.springframework.web.client.RequestCallback;
 import org.springframework.web.client.ResourceAccessException;
 import org.springframework.web.client.ResponseExtractor;
 import org.springframework.web.client.RestTemplate;
 
 import javax.net.ssl.SSLContext;
 import java.io.IOException;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @date: 14.03.12
  * @time: 13:06
  * @author: Jörg Bachmann
  * @email: bachmann@zib.de
  */
 public class HTTPGetter {
 
     protected final Logger logger = LoggerFactory.getLogger( this.getClass() );
     
     private int timeout = 10000; // default: 10ms
     
     private String keyStoreLocation;
     private String trustStoreLocation;
     private String password; // TODO: don't use same password for keystore and key?!
 
     private SSLContext sslContext;
 
     final private Map< Integer, EnhancedResponseExtractor > extractorMap;
 
 
     public HTTPGetter() {
         extractorMap = new HashMap< Integer, EnhancedResponseExtractor >();
         resetExtractorMap();
     }
 
 
     public void resetExtractorMap() {
         logger.trace( "Resetting ResponseExtractorMap." );
 
         extractorMap.clear();
         extractorMap.put( 3, new RedirectResponseExtractor() );
         extractorMap.put( 0, new DefaultResponseExtractor() );
     }
 
 
     public EnhancedResponseExtractor getResponseExtractor( int statusCode ) {
         return extractorMap.get( statusCode );
     }
 
 
     public void setupSSL( SetupSSL setupSSL, final String keyPassword ) throws
             IOException,
             NoSuchAlgorithmException,
             KeyStoreException,
             CertificateException,
             UnrecoverableKeyException,
             KeyManagementException {
         sslContext = setupSSL.setupSSLContext( keyPassword );
     }
 
 
     public void setExtractor( int httpStatusCode, EnhancedResponseExtractor responseExtractor ) {
         logger.trace( "Setting ResponseExtractor " + responseExtractor.getClass().getCanonicalName() + " for HTTP status code " + httpStatusCode );
 
         extractorMap.put( httpStatusCode, responseExtractor );
     }
 
 
     public int get( String url ) throws NoSuchAlgorithmException, KeyManagementException {
         return get( HttpMethod.GET, url, ( HttpHeaders )null );
     }
 
 
     public int get( String url, final HttpHeaders headers ) throws NoSuchAlgorithmException, KeyManagementException {
         return get( HttpMethod.GET, url, headers );
     }
 
 
     private EnhancedResponseExtractor get( final String url, final RequestCallback requestCallback )
             throws NoSuchAlgorithmException, KeyManagementException
     {
         return get( HttpMethod.GET, url, requestCallback );
     }
 
 
     public int head( String url ) throws NoSuchAlgorithmException, KeyManagementException {
         return get(HttpMethod.HEAD, url, (HttpHeaders) null);
     }
 
 
     public int head( String url, final HttpHeaders headers ) throws NoSuchAlgorithmException, KeyManagementException {
         return get( HttpMethod.HEAD, url, headers );
     }
 
 
     private EnhancedResponseExtractor head( final String url, final RequestCallback requestCallback )
             throws NoSuchAlgorithmException, KeyManagementException
     {
         return get( HttpMethod.HEAD, url, requestCallback );
     }
 
 
     public int get( final HttpMethod method, String url ) throws NoSuchAlgorithmException, KeyManagementException {
         return get( method, url, ( HttpHeaders )null );
     }
 
 
     public int get( final HttpMethod method, String url, final HttpHeaders headers ) throws NoSuchAlgorithmException, KeyManagementException {
         logger.debug(method.toString() + " URL " + url);
 
         EnhancedResponseExtractor responseExtractor = get( method, url, new RequestCallback() {
             @Override
             public void doWithRequest( ClientHttpRequest request ) throws IOException {
                 // add headers
                 if( headers != null )
                     request.getHeaders().putAll( headers );
             }
         } );
         int statusCode = responseExtractor.getStatusCode();
 
         int redirectionCounter = 0;
 
         // redirect as long as needed
         while( 300 <= statusCode && statusCode < 400 ) {
             final List< String > cookies = DefaultResponseExtractor.getCookies( responseExtractor.getHeaders() );
             final String location = DefaultResponseExtractor.getLocation( responseExtractor.getHeaders() );
 
             logger.debug( "Redirection " + ++redirectionCounter );
             logger.trace( "Redirecting to " + location + " with cookies " + cookies.toString() );
 
             responseExtractor = get( method, location, new RequestCallback() {
                 @Override
                 public void doWithRequest( ClientHttpRequest request ) throws IOException {
                     for( String c: cookies )
                         request.getHeaders().add( "Cookie", c.split( ";", 2 )[0] );
 
                     // add headers
                     if( headers != null )
                         request.getHeaders().putAll( headers );
                 }
             });
 
             statusCode = responseExtractor.getStatusCode();
         }
 
         logger.debug( "HTTP " + method.toString() + " Status Code " + statusCode + " after " + redirectionCounter + " redirections");
 
         return statusCode;
     }
 
 
     private EnhancedResponseExtractor get( final HttpMethod method, final String url, final RequestCallback requestCallback )
             throws NoSuchAlgorithmException, KeyManagementException
     {
         CustomSSLContextRequestFactory requestFactory = new CustomSSLContextRequestFactory( sslContext );
         RestTemplate rt = new RestTemplate( requestFactory );
         requestFactory.setConnectTimeout( getTimeout() );
         requestFactory.setReadTimeout( getTimeout() );
 
         try {
             return rt.execute( url, method, requestCallback, new ResponseExtractor< EnhancedResponseExtractor >() {
 
                 // call the EnhancedResponseExtractor registered for this response.statusCode
                 @Override
                 public EnhancedResponseExtractor extractData( ClientHttpResponse response ) throws IOException {
                     int statusCode = response.getStatusCode().value();
 
                     EnhancedResponseExtractor enhancedResponseExtractor = extractorMap.get( statusCode );
                     if( null == enhancedResponseExtractor )
                         enhancedResponseExtractor = extractorMap.get( statusCode / 100 );
                     if( null == enhancedResponseExtractor )
                         enhancedResponseExtractor = extractorMap.get( 0 );
                     if( null == enhancedResponseExtractor )
                         throw new IllegalStateException( "No default ResponseExtractor registered. THIS IS NOT HAPPENING :/" );
 
                     enhancedResponseExtractor.extractData(url, response);
 
                     return enhancedResponseExtractor;
                 }
 
             } );
         }
         catch( ResourceAccessException e ) {
            throw new RuntimeException( "Could not connect to " + url + ".", e );
         }
     }
 
 
    public String getKeyStoreLocation() {
         return keyStoreLocation;
     }
 
 
     public void setKeyStoreLocation( String keyStoreLocation ) {
         this.keyStoreLocation = keyStoreLocation;
     }
 
 
     public String getTrustStoreLocation() {
         return trustStoreLocation;
     }
 
 
     public void setTrustStoreLocation( String trustStoreLocation ) {
         this.trustStoreLocation = trustStoreLocation;
     }
 
 
     public void setPassword( String password ) {
         this.password = password;
     }
 
 
     public int getTimeout() {
         return timeout;
     }
 
 
     public void setTimeout( int timeout ) {
         this.timeout = timeout;
     }
 }
