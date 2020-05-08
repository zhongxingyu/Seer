 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.maven.wagon.providers.http;
 
 import org.apache.maven.wagon.AbstractWagon;
 import org.apache.maven.wagon.ConnectionException;
 import org.apache.maven.wagon.OutputData;
 import org.apache.maven.wagon.ResourceDoesNotExistException;
 import org.apache.maven.wagon.StreamingWagon;
 import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.WagonConstants;
 import org.apache.maven.wagon.authentication.AuthenticationException;
 import org.apache.maven.wagon.authentication.AuthenticationInfo;
 import org.apache.maven.wagon.authorization.AuthorizationException;
 import org.apache.maven.wagon.events.TransferEvent;
 import org.apache.maven.wagon.proxy.ProxyInfo;
 import org.apache.maven.wagon.resource.Resource;
 import org.codehaus.plexus.util.StringUtils;
 import org.eclipse.jetty.client.Address;
 import org.eclipse.jetty.client.ContentExchange;
 import org.eclipse.jetty.client.HttpClient;
 import org.eclipse.jetty.client.HttpDestination;
 import org.eclipse.jetty.client.security.ProxyAuthorization;
 import org.eclipse.jetty.client.security.Realm;
 import org.eclipse.jetty.client.security.RealmResolver;
 import org.eclipse.jetty.http.HttpFields;
 import org.eclipse.jetty.http.HttpHeaders;
 import org.eclipse.jetty.http.HttpMethods;
 import org.eclipse.jetty.io.Buffer;
 import org.eclipse.jetty.io.BufferUtil;
 import org.eclipse.jetty.util.component.LifeCycle;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.zip.GZIPInputStream;
 
 /**
  * JettyClientHttpWagon
  */
 public class JettyClientHttpWagon
     extends AbstractWagon
     implements StreamingWagon, StreamObserver
 {
     private static final Map<String, String> _protocolMap = new HashMap<String, String>()
     {
         {
             put( "http:", "http:" );
             put( "https:", "https:" );
             put( "dav:", "http:" );
             put( "davs:", "https:" );
             put( "dav:http:", "http:" );
             put( "dav:https:", "https:" );
             put( "dav+http:", "http:" );
             put( "dav+https:", "https:" );
             put( "mttp:", "http:" );
             put( "mttps:", "https:" );
         }
     };
 
     // StreamObserver implementation
     private byte[] _buffer;
 
     private long _length = -1L;
 
     private String _lastModified;
 
     /** @plexus.configuration default="false" */
     private boolean useCache;
 
     /** @plexus.configuration default=2 */
     protected int maxConnections;
 
     /** @plexus.configuration default="10" */
     private int maxRedirections = 10;
 
     private HttpClient _httpClient;
 
     private HttpFields _httpHeaders;
 
     private Resource _resource;
 
     private RequestState _requestState;
 
     public JettyClientHttpWagon()
     {
         // needed for unit testing
         _requestState = new RequestState( TransferEvent.REQUEST_GET, null );
     }
 
     public void setAuthInfo( final AuthenticationInfo authInfo )
     {
         // needed for unit testing
         authenticationInfo = authInfo;
     }
 
     @Override
     protected void openConnectionInternal()
         throws ConnectionException, AuthenticationException
     {
         closeConnection();
 
         try
         {
             _httpClient = new FixedHttpClient();
 
             _httpClient.setConnectorType( HttpClient.CONNECTOR_SELECT_CHANNEL );
             _httpClient.setTimeout( super.getTimeout() );
             // TODO: Jetty 7.0.1: _httpClient.setConnectionTimeout( super.getTimeout() );
             if ( maxConnections > 0 )
             {
                 _httpClient.setMaxConnectionsPerAddress( maxConnections );
             }
 
             _httpClient.registerListener( "org.apache.maven.wagon.providers.http.WagonListener" );
             _httpClient.registerListener( "org.eclipse.jetty.client.webdav.WebdavListener" );
 
             WagonListener.setHelper( new HttpConnectionHelper( this ) );
 
             setupClient();
 
             _httpClient.start();
         }
         catch ( Exception ex )
         {
             _httpClient = null;
             throw new ConnectionException( ex.getLocalizedMessage() );
         }
     }
 
     @Override
     public void closeConnection()
         throws ConnectionException
     {
         if ( _httpClient != null )
         {
             try
             {
                 _httpClient.stop();
             }
             catch ( Exception e )
             {
                 e.printStackTrace();
             }
             finally
             {
                 _httpClient = null;
             }
         }
     }
 
     /**
      * Builds a complete URL string from the repository URL and the relative path passed.
      * 
      * @param path
      *            the relative path
      * @return the complete URL
      */
     private String buildUrl( final String resourceName )
     {
         StringBuilder urlBuilder = new StringBuilder();
 
         String baseUrl = getRepository().getUrl(); // get repositiory url
         int index = baseUrl.indexOf( '/' );
 
         String protocol = baseUrl.substring( 0, index );
         String mappedProtocol = _protocolMap.get( protocol ); // map server protocol
         if ( mappedProtocol != null )
         {
             urlBuilder.append( mappedProtocol );
         }
         else
         {
             urlBuilder.append( protocol );
         }
 
         urlBuilder.append( baseUrl.substring( index ) );
         if ( baseUrl.endsWith( "/" ) )
         {
             urlBuilder.deleteCharAt( urlBuilder.length() - 1 ); // avoid double slash
         }
         if ( urlBuilder.charAt( urlBuilder.length() - 1 ) == ':' )
         {
             urlBuilder.append( '/' );
         }
 
         String resourceUri = resourceName;
         resourceUri.replace( ' ', '+' ); // encode whitespace
 
         String[] parts = StringUtils.split( resourceUri, "/" );
         for ( int i = 0; i < parts.length; i++ )
         {
             urlBuilder.append( '/' ).append( URLEncoder.encode( parts[i] ) ); // encode URI
         }
 
         if ( resourceName.endsWith( "/" ) )
         {
             urlBuilder.append( '/' ); // directory URI
         }
 
         return urlBuilder.toString();
     }
 
     private void sendAndWait( WagonExchange httpExchange )
         throws IOException, InterruptedException
     {
         ExchangeStopper exchangeStopper = new ExchangeStopper( httpExchange );
 
         _httpClient.addLifeCycleListener( exchangeStopper );
         try
         {
             _httpClient.send( httpExchange );
             httpExchange.waitForDone();
         }
         finally
         {
             _httpClient.removeLifeCycleListener( exchangeStopper );
         }
     }
 
     WagonExchange newExchange()
     {
         return new WagonExchange( _httpClient );
     }
 
     public void get( final String resourceName, final File destination )
         throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
     {
         getIfNewer( resourceName, destination, 0 );
     }
 
     public boolean getIfNewer( final String resourceName, final File destination, final long timestamp )
         throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
     {
         Resource resource = new Resource( resourceName );
 
         fireGetInitiated( resource, destination );
 
         return getIfNewer( resource, null, destination, timestamp );
     }
 
     public void getToStream( final String resourceName, final OutputStream stream )
         throws ResourceDoesNotExistException, TransferFailedException, AuthorizationException
     {
         getIfNewerToStream( resourceName, stream, 0 );
     }
 
     public boolean getIfNewerToStream( final String resourceName, final OutputStream stream, final long timestamp )
         throws ResourceDoesNotExistException, TransferFailedException, AuthorizationException
     {
         Resource resource = new Resource( resourceName );
 
         fireGetInitiated( resource, null );
 
         return getIfNewer( resource, stream, null, timestamp );
     }
 
     private boolean getIfNewer( final Resource resource, final OutputStream stream, final File destination, final long timestamp )
         throws ResourceDoesNotExistException, TransferFailedException, AuthorizationException
     {
         _resource = resource;
         String resourceUrl = buildUrl( _resource.getName() );
 
         _requestState = new RequestState( TransferEvent.REQUEST_GET, destination );
         _requestState.addRequestHeader( "Accept-Encoding", "gzip" );
         if ( !useCache )
         {
             _requestState.addRequestHeader( "Pragma", "no-cache" );
             _requestState.addRequestHeader( "Cache-Control", "no-cache, no-store" );
         }
         // *TODO* add hard-coded headers for GET request
 
         try
         {
             WagonExchange httpExchange;
 
             int redirect = 0;
             do
             {
                 httpExchange = newExchange();
                 httpExchange.setURL( resourceUrl );
                 httpExchange.setMethod( HttpMethods.GET );
 
                 sendAndWait( httpExchange );
 
                 int responseStatus = httpExchange.getResponseStatus();
                 if ( responseStatus == ServerResponse.SC_MOVED_PERMANENTLY
                     || responseStatus == ServerResponse.SC_MOVED_TEMPORARILY )
                 {
                     String location = httpExchange.getLocation();
                     if ( location != null )
                     {
                         if ( location.indexOf( "://" ) > 0 )
                         {
                             resourceUrl = location;
                         }
                         else
                         {
                             resourceUrl = httpExchange.getScheme() + "://" + httpExchange.getAddress();
                             if ( !location.startsWith( "/" ) )
                             {
                                 resourceUrl += "/";
                             }
                             resourceUrl += location;
                         }
 
                         redirect++;
 
                         continue;
                     }
                 }
 
                 break;
             }
             while ( redirect <= Math.max( maxRedirections, 0 ) );
 
             int responseStatus = httpExchange.getResponseStatus();
             switch ( responseStatus )
             {
                 case ServerResponse.SC_OK:
                 case ServerResponse.SC_NOT_MODIFIED:
                     break;
 
                 case ServerResponse.SC_FORBIDDEN:
                     fireSessionConnectionRefused();
                     throw new AuthorizationException( "Transfer failed: [" + responseStatus + "] " + resourceUrl );
 
                 case ServerResponse.SC_UNAUTHORIZED:
                     fireSessionConnectionRefused();
                     throw new AuthorizationException( "Transfer failed: Not authorized" );
 
                 case ServerResponse.SC_PROXY_AUTHENTICATION_REQUIRED:
                     fireSessionConnectionRefused();
                     throw new AuthorizationException( "Transfer failed: Not authorized by proxy" );
 
                 case ServerResponse.SC_NOT_FOUND:
                     throw new ResourceDoesNotExistException( "Transfer failed: " + resourceUrl + " does not exist" );
 
                 default:
                 {
                     cleanupGetTransfer( _resource );
                     TransferFailedException ex =
                         new TransferFailedException( "Transfer failed: [" + responseStatus + "] " + resourceUrl );
                     fireTransferError( _resource, ex, TransferEvent.REQUEST_GET );
                     throw ex;
                 }
             }
 
             boolean retValue = false;
 
             _resource.setLastModified( httpExchange.getLastModified() );
             _resource.setContentLength( httpExchange.getContentLength() );
 
             // always get if timestamp is 0 (ie, target doesn't exist), otherwise only if older than the remote file
             if ( timestamp == 0 || timestamp < _resource.getLastModified() )
             {
                 retValue = true;
 
                 InputStream input = getResponseContentSource( httpExchange );
 
                 if ( stream != null )
                 {
                     fireGetStarted( _resource, destination );
                     getTransfer( _resource, stream, input );
                     fireGetCompleted( _resource, destination );
                 }
                 else if ( destination != null )
                 {
                     getTransfer( _resource, destination, input );
                 }
                 else
                 {
                     // discard the response
                 }
             }
 
             return retValue;
         }
         catch ( InterruptedException ex )
         {
             fireTransferError( _resource, ex, TransferEvent.REQUEST_GET );
 
             throw new TransferFailedException( "Transfer interrupted: " + ex.getMessage(), ex );
         }
         catch ( FileNotFoundException ex )
         {
             fireGetCompleted( _resource, null );
 
             throw new ResourceDoesNotExistException( "Transfer error: Resource not found in repository", ex );
         }
         catch ( IOException ex )
         {
             fireTransferError( _resource, ex, TransferEvent.REQUEST_GET );
 
             throw new TransferFailedException( "Transfer error: " + ex.getMessage(), ex );
         }
         catch ( IllegalStateException ex )
         {
             fireTransferError( _resource, ex, TransferEvent.REQUEST_GET );
 
             throw new TransferFailedException( "Transfer error: " + ex.getMessage(), ex );
         }
     }
 
     public void put( final File source, final String resourceName )
         throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
     {
         Resource resource = new Resource( resourceName );
 
         firePutInitiated( resource, source );
 
         resource.setContentLength( source.length() );
 
         resource.setLastModified( source.lastModified() );
 
         put( null, source, resource );
     }
 
     public void putFromStream( final InputStream stream, final String destination )
         throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
     {
         Resource resource = new Resource( destination );
 
         firePutInitiated( resource, null );
 
         put( stream, null, resource );
     }
 
     public void putFromStream( final InputStream stream, final String destination, final long contentLength, final long lastModified )
         throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
     {
         Resource resource = new Resource( destination );
 
         firePutInitiated( resource, null );
 
         resource.setContentLength( contentLength );
 
         resource.setLastModified( lastModified );
 
         put( stream, null, resource );
     }
 
     private void put( final InputStream stream, final File source, final Resource resource )
         throws TransferFailedException, AuthorizationException, ResourceDoesNotExistException
     {
         _resource = resource;
         _requestState = new RequestState( TransferEvent.REQUEST_PUT, source );
         if ( !useCache )
         {
             _requestState.addRequestHeader( "Pragma", "no-cache" );
             _requestState.addRequestHeader( "Cache-Control", "no-cache, no-store" );
         }
         // *TODO* add hard-coded headers for PUT request
 
         String resourceUrl = buildUrl( _resource.getName() );
 
         firePutStarted( _resource, source );
 
         try
         {
             WagonExchange httpExchange = newExchange();
             httpExchange.setURL( resourceUrl );
             httpExchange.setMethod( HttpMethods.PUT );
             setRequestContentSource( httpExchange, stream, source );
 
             sendAndWait( httpExchange );
 
             int responseStatus = httpExchange.getResponseStatus();
 
             switch ( responseStatus )
             {
                 // Success Codes
                 case ServerResponse.SC_OK: // 200
                 case ServerResponse.SC_CREATED: // 201
                 case ServerResponse.SC_ACCEPTED: // 202
                 case ServerResponse.SC_NO_CONTENT: // 204
                     break;
 
                 case ServerResponse.SC_FORBIDDEN:
                     fireSessionConnectionRefused();
                     throw new AuthorizationException( "Transfer failed: [" + responseStatus + "] " + resourceUrl );
 
                 case ServerResponse.SC_UNAUTHORIZED:
                     fireSessionConnectionRefused();
                     throw new AuthorizationException( "Transfer failed: Not authorized" );
 
                 case ServerResponse.SC_PROXY_AUTHENTICATION_REQUIRED:
                     fireSessionConnectionRefused();
                     throw new AuthorizationException( "Transfer failed: Not authorized by proxy" );
 
                 case ServerResponse.SC_NOT_FOUND:
                     throw new ResourceDoesNotExistException( "Transfer failed: " + resourceUrl + " does not exist" );
 
                 default:
                 {
                     TransferFailedException ex =
                         new TransferFailedException( "Transfer failed: [" + responseStatus + "] " + resourceUrl );
                     fireTransferError( _resource, ex, TransferEvent.REQUEST_PUT );
                     throw ex;
                 }
             }
 
             fireTransferDebug( resourceUrl + " [" + responseStatus + "]" );
 
             firePutCompleted( _resource, source );
         }
         catch ( InterruptedException ex )
         {
             fireTransferError( _resource, ex, TransferEvent.REQUEST_PUT );
 
             throw new TransferFailedException( "Transfer interrupted: " + ex.getMessage(), ex );
         }
         catch ( IOException ex )
         {
             fireTransferError( _resource, ex, TransferEvent.REQUEST_PUT );
 
             throw new TransferFailedException( "Transfer error: " + ex.getMessage(), ex );
         }
         catch ( IllegalStateException ex )
         {
             fireTransferError( _resource, ex, TransferEvent.REQUEST_PUT );
 
             throw new TransferFailedException( "Transfer error: " + ex.getMessage(), ex );
         }
     }
 
     @Override
     public boolean resourceExists( final String resourceName )
         throws TransferFailedException, AuthorizationException
     {
         Resource resource = new Resource( resourceName );
 
         try
         {
             return getIfNewer( resource, null, null, 0 );
         }
         catch ( ResourceDoesNotExistException ex )
         {
             return false;
         }
     }
 
     @Override
     public List getFileList( final String destinationDirectory )
         throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
     {
         throw new UnsupportedOperationException();
         /*
          * String resourceName = destinationDirectory; if (!resourceName.endsWith("/")) resourceName += "/";
          * 
          * ByteArrayOutputStream stream = new ByteArrayOutputStream();
          * 
          * getIfNewerToStream(resourceName,stream,0); return HtmlFileListParser.parseFileList( buildUrl(resourceName),
          * new ByteArrayInputStream(stream.toByteArray()) );
          */
     }
 
     protected void setRequestContentSource( final WagonExchange exchange, final InputStream srcStream, final File srcFile )
         throws IOException
     {
         InputStream source = null;
         if ( srcStream != null )
         {
             source = srcStream;
         }
         else if ( srcFile != null )
         {
             source = new FileInputStream( srcFile );
         }
 
         if ( source != null && !source.markSupported() )
         {
             BufferedInputStream bstream = new BufferedInputStream( source );
             bstream.mark( srcFile == null ? Integer.MAX_VALUE : (int) srcFile.length() );
             source = bstream;
         }
 
         if ( source != null )
         {
             ObservableInputStream observe = new ObservableInputStream( source );
             observe.addObserver( this );
             source = observe;
         }
 
         exchange.setRequestContentSource( source );
     }
 
     protected InputStream getResponseContentSource( final WagonExchange exchange )
         throws IOException
     {
         InputStream source = exchange.getResponseContentSource();
 
         if ( source != null )
         {
             String contentEncoding = exchange.getContentEncoding();
             if ( "gzip".equalsIgnoreCase( contentEncoding ) )
             {
                 source = new GZIPInputStream( source );
             }
         }
 
         return source;
     }
 
     void bytesReady( final byte[] buf, final int len )
     {
         fireTransferProgress( _requestState._transferEvent, buf, len );
     }
 
     protected void setHttpHeaders( final Properties properties )
     {
         _httpHeaders = new HttpFields();
         for ( Enumeration names = properties.propertyNames(); names.hasMoreElements(); )
         {
             String name = (String) names.nextElement();
             _httpHeaders.add( name, properties.getProperty( name ) );
         }
     }
 
     protected void setupClient()
         throws ConnectionException, IOException
     {
         ProxyInfo proxyInfo = getProxyInfo( "http", getRepository().getHost() );
         if ( proxyInfo != null && proxyInfo.getHost() != null )
         {
             String proxyType = proxyInfo.getType();
             if ( !proxyType.equalsIgnoreCase( ProxyInfo.PROXY_HTTP.toLowerCase() ) )
             {
                 throw new ConnectionException( "Connection failed: " + proxyType + " is not supported" );
             }
 
             _httpClient.setProxy( new Address( proxyInfo.getHost(), proxyInfo.getPort() ) );
 
             if ( proxyInfo.getUserName() != null )
             {
                 _httpClient.setProxyAuthentication( new ProxyAuthorization( proxyInfo.getUserName(),
                                                                             proxyInfo.getPassword() ) );
             }
         }
 
         AuthenticationInfo authInfo = getAuthenticationInfo();
         if ( authInfo != null && authInfo.getUserName() != null )
         {
             _httpClient.setRealmResolver( new RealmResolver()
             {
                 public Realm getRealm( final String realmName, final HttpDestination destination, final String path )
                     throws IOException
                 {
                     return new Realm()
                     {
                         public String getCredentials()
                         {
                             return getAuthenticationInfo().getPassword();
                         }
 
                         public String getPrincipal()
                         {
                             return getAuthenticationInfo().getUserName();
                         }
 
                         public String getId()
                         {
                             return getRepository().getHost();
                         }
                     };
                 }
             } );
         }
     }
 
     protected void mkdirs( final String dirname )
         throws IOException
     {
     }
 
     public void fillOutputData( final OutputData arg0 )
         throws TransferFailedException
     {
         throw new IllegalStateException( "Should not be using the streaming wagon for HTTP PUT" );
     }
 
     public boolean getUseCache()
     {
         return useCache;
     }
 
     public HttpFields getHttpHeaders()
     {
         return _httpHeaders;
     }
 
     @Override
     public ProxyInfo getProxyInfo( final String type, final String host )
     {
         return super.getProxyInfo( type, host );
     }
 
     public void byteReady( final int b )
         throws StreamObserverException
     {
         bytesReady( new byte[] { (byte) b }, 1 );
     }
 
     public void bytesReady( final byte[] b, final int off, final int len )
         throws StreamObserverException
     {
         if ( _buffer == null || len > _buffer.length )
         {
             _buffer = new byte[len];
         }
 
         System.arraycopy( b, off, _buffer, 0, len );
 
         bytesReady( _buffer, len );
     }
 
     public long getLength()
     {
         return _length;
     }
 
     public void setLength( final long length )
     {
         _length = length;
     }
 
     public void setLastModified( final String time )
     {
         _lastModified = time;
     }
 
     public String getLastModified()
     {
         return _lastModified;
     }
 
     class WagonExchange
         extends ContentExchange
     {
         private byte[] _responseContentBytes;
 
         private int _responseStatus;
 
        private int _contentLength = WagonConstants.UNKNOWN_LENGTH;
 
         private String _contentEncoding;
 
         private long _lastModified;
 
         private String _location;
 
         private final HttpClient _httpClient;
 
         public WagonExchange(HttpClient httpClient)
         {
             super( false );
 
             addRequestHeaders( _requestState._requestHeaders );
             addRequestHeaders( _httpHeaders );
 
             _httpClient = httpClient;
         }
 
         private void addRequestHeaders( final HttpFields headers )
         {
             if ( headers != null )
             {
                 for ( Enumeration<String> names = headers.getFieldNames(); names.hasMoreElements(); )
                 {
                     String name = names.nextElement();
                     addRequestHeader( name, headers.getStringField( name ) );
                 }
             }
         }
 
         @Override
         public boolean isDone( int status )
         {
             return super.isDone( status ) || !_httpClient.isRunning();
         }
 
         public String getLocation()
         {
             return _location;
         }
 
         public int getContentLength()
         {
             return _contentLength;
         }
 
         public void setContentLength( final int length )
         {
             _contentLength = length;
         }
 
         public String getContentEncoding()
         {
             return _contentEncoding;
         }
 
         public void setContentEncoding( final String encoding )
         {
             _contentEncoding = encoding;
         }
 
         public long getLastModified()
         {
             return _lastModified;
         }
 
         public void setLastModified( final long time )
         {
             _lastModified = time;
         }
 
         public void setResponseStatus( final int status )
         {
             _responseStatus = status;
         }
 
         @Override
         public int getResponseStatus()
         {
             if ( _responseStatus != 0 )
             {
                 return _responseStatus;
             }
             else
             {
                 return super.getResponseStatus();
             }
         }
 
         public void setResponseContentBytes( final byte[] bytes )
         {
             _responseContentBytes = bytes;
         }
 
         public InputStream getResponseContentSource()
             throws UnsupportedEncodingException
         {
             return new ByteArrayInputStream( _responseContentBytes != null ? _responseContentBytes
                             : getResponseContentBytes() );
         }
 
         @Override
         public void onResponseHeader( final Buffer name, final Buffer value )
             throws IOException
         {
             super.onResponseHeader( name, value );
             int header = HttpHeaders.CACHE.getOrdinal( name );
             switch ( header )
             {
                 case HttpHeaders.CONTENT_LENGTH_ORDINAL:
                     _contentLength = BufferUtil.toInt( value );
                     break;
                 case HttpHeaders.CONTENT_ENCODING_ORDINAL:
                     _contentEncoding = BufferUtil.to8859_1_String( value );
                     break;
                 case HttpHeaders.LAST_MODIFIED_ORDINAL:
                     String lastModifiedStr = BufferUtil.to8859_1_String( value );
                     _lastModified =
                         ( lastModifiedStr == null || lastModifiedStr.length() == 0 ? 0 : Date.parse( lastModifiedStr ) );
                     break;
                 case HttpHeaders.LOCATION_ORDINAL:
                     _location = value.toString();
                     break;
             }
         }
 
         @Override
         public void reset()
         {
             super.reset();
 
             // restart, especially to reset checksum observers
             if ( _requestState._requestType == TransferEvent.REQUEST_PUT )
             {
                 firePutStarted( _resource, _requestState._transferEvent.getLocalFile() );
             }
             else
             {
                 fireGetStarted( _resource, _requestState._transferEvent.getLocalFile() );
             }
         }
 
     }
 
     class RequestState
     {
         public int _requestType;
 
         public TransferEvent _transferEvent;
 
         public HttpFields _requestHeaders;
 
         public RequestState( final int requestType, final File localFile )
         {
             _requestType = requestType;
 
             _transferEvent =
                 new TransferEvent( JettyClientHttpWagon.this, _resource, TransferEvent.TRANSFER_PROGRESS, _requestType );
             _transferEvent.setTimestamp( System.currentTimeMillis() );
             _transferEvent.setLocalFile( localFile );
 
             _requestHeaders = new HttpFields();
         }
 
         public void addRequestHeader( final String name, final String value )
         {
             _requestHeaders.add( name, value );
         }
     }
 
     class ExchangeStopper
         implements LifeCycle.Listener
     {
 
         private WagonExchange _httpExchange;
 
         public ExchangeStopper( WagonExchange httpExchange )
         {
             _httpExchange = httpExchange;
         }
 
         public void lifeCycleFailure( LifeCycle event, Throwable cause )
         {
         }
 
         public void lifeCycleStarted( LifeCycle event )
         {
         }
 
         public void lifeCycleStarting( LifeCycle event )
         {
         }
 
         public void lifeCycleStopped( LifeCycle event )
         {
         }
 
         public void lifeCycleStopping( LifeCycle event )
         {
             synchronized ( _httpExchange )
             {
                 // WagonExchange.isDone() will notice the client has stopped, we just need to wake it up
                 _httpExchange.notifyAll();
             }
         }
 
     }
 
 }
