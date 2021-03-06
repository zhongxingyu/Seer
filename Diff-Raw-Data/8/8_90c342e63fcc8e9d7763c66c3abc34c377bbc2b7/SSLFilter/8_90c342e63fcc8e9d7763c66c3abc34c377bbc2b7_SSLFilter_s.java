 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License. 
  *  
  */
 package org.apache.mina.filter;
 
 import java.net.InetSocketAddress;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLEngine;
 import javax.net.ssl.SSLException;
 import javax.net.ssl.SSLHandshakeException;
 import javax.net.ssl.SSLSession;
 
 import org.apache.mina.common.ByteBuffer;
 import org.apache.mina.common.DefaultWriteRequest;
 import org.apache.mina.common.IoFilterAdapter;
 import org.apache.mina.common.IoFilterChain;
 import org.apache.mina.common.IoFuture;
 import org.apache.mina.common.IoFutureListener;
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.common.WriteFuture;
 import org.apache.mina.common.WriteRequest;
 import org.apache.mina.common.WriteRequestWrapper;
 import org.apache.mina.common.support.DefaultWriteFuture;
 import org.apache.mina.filter.support.SSLHandler;
 import org.apache.mina.util.SessionLog;
 
 /**
  * An SSL filter that encrypts and decrypts the data exchanged in the session.
  * This filter uses an {@link SSLEngine} which was introduced in Java 5, so 
  * Java version 5 or above is mandatory to use this filter. And please note that
  * this filter only works for TCP/IP connections.
  * <p>
  * This filter logs debug information using {@link SessionLog}.
  * 
  * <h2>Implementing StartTLS</h2>
  * <p>
  * You can use {@link #DISABLE_ENCRYPTION_ONCE} attribute to implement StartTLS:
  * <pre>
  * public void messageReceived(IoSession session, Object message) {
  *    if (message instanceof MyStartTLSRequest) {
  *        // Insert SSLFilter to get ready for handshaking
  *        session.getFilterChain().addFirst(sslFilter);
  *
  *        // Disable encryption temporarilly.
  *        // This attribute will be removed by SSLFilter
  *        // inside the Session.write() call below.
  *        session.setAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE, Boolean.TRUE);
  *
  *        // Write StartTLSResponse which won't be encrypted.
  *        session.write(new MyStartTLSResponse(OK));
  *        
  *        // Now DISABLE_ENCRYPTION_ONCE attribute is cleared.
  *        assert session.getAttribute(SSLFilter.DISABLE_ENCRYPTION_ONCE) == null;
  *    }
  * }
  * </pre>
  * 
  * @author The Apache MINA Project (dev@mina.apache.org)
  * @version $Rev$, $Date$
  */
 public class SSLFilter extends IoFilterAdapter
 {
     /**
      * A session attribute key that stores underlying {@link SSLSession}
      * for each session.
      */
     public static final String SSL_SESSION = SSLFilter.class.getName() + ".SSLSession";
     
     /**
      * A session attribute key that makes next one write request bypass
      * this filter (not encrypting the data).  This is a marker attribute,
      * which means that you can put whatever as its value. ({@link Boolean#TRUE}
      * is preferred.)  The attribute is automatically removed from the session
      * attribute map as soon as {@link IoSession#write(Object)} is invoked,
      * and therefore should be put again if you want to make more messages
      * bypass this filter.  This is especially useful when you implement
      * StartTLS.   
      */
     public static final String DISABLE_ENCRYPTION_ONCE = SSLFilter.class.getName() + ".DisableEncryptionOnce";
 
     /**
      * A session attribute key that makes this filter to emit a
      * {@link IoHandler#messageReceived(IoSession, Object)} event with a
      * special message ({@link #SESSION_SECURED} or {@link #SESSION_UNSECURED}).
      * This is a marker attribute, which means that you can put whatever as its
      * value. ({@link Boolean#TRUE} is preferred.)  By default, this filter
      * doesn't emit any events related with SSL session flow control.
      */
     public static final String USE_NOTIFICATION = SSLFilter.class.getName() + ".UseNotification";
     
     /**
      * A session attribute key that should be set to an {@link InetSocketAddress}. 
      * Setting this attribute causes  
      * {@link SSLContext#createSSLEngine(String, int)} to be called passing the 
      * hostname and port of the {@link InetSocketAddress} to get an 
      * {@link SSLEngine} instance. If not set {@link SSLContext#createSSLEngine()}
      * will be called.
      * .
      * Using this feature {@link SSLSession} objects may be cached and reused 
      * when in client mode. 
      * 
      * @see SSLContext#createSSLEngine(String, int)
      */
     public static final String PEER_ADDRESS = SSLFilter.class.getName() + ".PeerAddress";
     
     /**
      * A special message object which is emitted with a {@link IoHandler#messageReceived(IoSession, Object)}
      * event when the session is secured and its {@link #USE_NOTIFICATION}
      * attribute is set.
      */
     public static final SSLFilterMessage SESSION_SECURED = new SSLFilterMessage( "SESSION_SECURED" );
     
     /**
      * A special message object which is emitted with a {@link IoHandler#messageReceived(IoSession, Object)}
      * event when the session is not secure anymore and its {@link #USE_NOTIFICATION}
      * attribute is set.
      */
     public static final SSLFilterMessage SESSION_UNSECURED = new SSLFilterMessage( "SESSION_UNSECURED" );
     
     private static final String NEXT_FILTER = SSLFilter.class.getName() + ".NextFilter";
     private static final String SSL_HANDLER = SSLFilter.class.getName() + ".SSLHandler";
 
     // SSL Context
     private SSLContext sslContext;
 
     private boolean client;
     private boolean needClientAuth;
     private boolean wantClientAuth;
     private String[] enabledCipherSuites;
     private String[] enabledProtocols;
 
     /**
      * Creates a new SSL filter using the specified {@link SSLContext}.
      */
     public SSLFilter( SSLContext sslContext )
     {
         if( sslContext == null )
         {
             throw new NullPointerException( "sslContext" );
         }
 
         this.sslContext = sslContext;
     }
     
     /**
      * Returns the underlying {@link SSLSession} for the specified session.
      * 
      * @return <tt>null</tt> if no {@link SSLSession} is initialized yet.
      */
     public SSLSession getSSLSession( IoSession session )
     {
         return ( SSLSession ) session.getAttribute( SSL_SESSION );
     }
     
     /**
      * (Re)starts SSL session for the specified <tt>session</tt> if not started yet.
      * Please note that SSL session is automatically started by default, and therefore
      * you don't need to call this method unless you've used TLS closure.
      * 
      * @return <tt>true</tt> if the SSL session has been started, <tt>false</tt> if already started.
      * @throws SSLException if failed to start the SSL session
      */
     public boolean startSSL( IoSession session ) throws SSLException
     {
         SSLHandler handler = getSSLSessionHandler( session );
         boolean started;
         synchronized( handler )
         {
             if( handler.isOutboundDone() )
             {
                 NextFilter nextFilter = ( NextFilter ) session.getAttribute( NEXT_FILTER );
                 handler.destroy();
                 handler.init();
                 handler.handshake( nextFilter );
                 started = true;
             }
             else
             {
                 started = false;
             }
         }
         
         handler.flushPostHandshakeEvents();
         return started;
     }
     
     /**
      * Returns <tt>true</tt> if and only if the specified <tt>session</tt> is
      * encrypted/decrypted over SSL/TLS currently.  This method will start
      * to retun <tt>false</tt> after TLS <tt>close_notify</tt> message
      * is sent and any messages written after then is not goinf to get encrypted.
      */
     public boolean isSSLStarted( IoSession session )
     {
         SSLHandler handler = getSSLSessionHandler0( session );
         if( handler == null )
         {
             return false;
         }
 
         synchronized( handler )
         {
             return !handler.isOutboundDone();
         }
     }
 
     /**
      * Stops the SSL session by sending TLS <tt>close_notify</tt> message to
      * initiate TLS closure.
      * 
      * @param session the {@link IoSession} to initiate TLS closure
      * @throws SSLException if failed to initiate TLS closure
      * @throws IllegalArgumentException if this filter is not managing the specified session
      */
     public WriteFuture stopSSL( IoSession session ) throws SSLException
     {
         SSLHandler handler = getSSLSessionHandler( session );
         NextFilter nextFilter = ( NextFilter ) session.getAttribute( NEXT_FILTER );
         WriteFuture future;
         synchronized( handler )
         {
             future = initiateClosure( nextFilter, session );
         }
         
         handler.flushPostHandshakeEvents();
         
         return future;
     }
 
     /**
      * Returns <tt>true</tt> if the engine is set to use client mode
      * when handshaking.
      */
     public boolean isUseClientMode()
     {
         return client;
     }
     
     /**
      * Configures the engine to use client (or server) mode when handshaking.
      */
     public void setUseClientMode( boolean clientMode )
     {
         this.client = clientMode;
     }
     
     /**
      * Returns <tt>true</tt> if the engine will <em>require</em> client authentication.
      * This option is only useful to engines in the server mode.
      */
     public boolean isNeedClientAuth()
     {
         return needClientAuth;
     }
 
     /**
      * Configures the engine to <em>require</em> client authentication.
      * This option is only useful for engines in the server mode.
      */
     public void setNeedClientAuth( boolean needClientAuth )
     {
         this.needClientAuth = needClientAuth;
     }
     
     
     /**
      * Returns <tt>true</tt> if the engine will <em>request</em> client authentication.
      * This option is only useful to engines in the server mode.
      */
     public boolean isWantClientAuth()
     {
         return wantClientAuth;
     }
     
     /**
      * Configures the engine to <em>request</em> client authentication.
      * This option is only useful for engines in the server mode.
      */
     public void setWantClientAuth( boolean wantClientAuth )
     {
         this.wantClientAuth = wantClientAuth;
     }
     
     /**
      * Returns the list of cipher suites to be enabled when {@link SSLEngine}
      * is initialized.
      * 
      * @return <tt>null</tt> means 'use {@link SSLEngine}'s default.'
      */
     public String[] getEnabledCipherSuites()
     {
         return enabledCipherSuites;
     }
     
     /**
      * Sets the list of cipher suites to be enabled when {@link SSLEngine}
      * is initialized.
      * 
      * @param cipherSuites <tt>null</tt> means 'use {@link SSLEngine}'s default.'
      */
     public void setEnabledCipherSuites( String[] cipherSuites )
     {
         this.enabledCipherSuites = cipherSuites;
     }
 
     /**
      * Returns the list of protocols to be enabled when {@link SSLEngine}
      * is initialized.
      * 
      * @return <tt>null</tt> means 'use {@link SSLEngine}'s default.'
      */
     public String[] getEnabledProtocols()
     {
         return enabledProtocols;
     }
     
     /**
      * Sets the list of protocols to be enabled when {@link SSLEngine}
      * is initialized.
      * 
      * @param protocols <tt>null</tt> means 'use {@link SSLEngine}'s default.'
      */
     public void setEnabledProtocols( String[] protocols )
     {
         this.enabledProtocols = protocols;
     }
     
     @Override
     public void onPreAdd( IoFilterChain parent, String name, NextFilter nextFilter ) throws SSLException
     {
         if( parent.contains( SSLFilter.class ) )
         {
             throw new IllegalStateException( "A filter chain cannot contain more than one SSLFilter." );
         }
 
         IoSession session = parent.getSession();
         session.setAttribute( NEXT_FILTER, nextFilter );
         
         // Create an SSL handler and start handshake.
         SSLHandler handler =
             new SSLHandler( this, sslContext, session );
         session.setAttribute( SSL_HANDLER, handler );
     }
     
     @Override
     public void onPostAdd( IoFilterChain parent, String name, NextFilter nextFilter ) throws SSLException
     {
         SSLHandler handler = getSSLSessionHandler( parent.getSession() );
         synchronized (handler) {
             handler.handshake( nextFilter );
         }
         handler.flushPostHandshakeEvents();
     }
     
     @Override
     public void onPreRemove( IoFilterChain parent, String name, NextFilter nextFilter ) throws SSLException
     {
         IoSession session = parent.getSession();
         stopSSL( session );
         session.removeAttribute( NEXT_FILTER );
         session.removeAttribute( SSL_HANDLER );
     }
 
     // IoFilter impl.
     @Override
     public void sessionClosed( NextFilter nextFilter, IoSession session ) throws SSLException
     {
         SSLHandler handler = getSSLSessionHandler( session );
         try
         {
             synchronized( handler )
             {
                 if( isSSLStarted( session ) )
                 {
                     if( SessionLog.isDebugEnabled( session ) )
                     {
                         SessionLog.debug( session, " Closed: " + getSSLSessionHandler( session ) );
                     }
                 }
                 
                 // release resources
                 handler.destroy();
             }
             
             handler.flushPostHandshakeEvents();
         }
         finally
         {
            // notify closed session
            nextFilter.sessionClosed( session );
         }
     }
    
     @Override
     public void messageReceived( NextFilter nextFilter, IoSession session,
                                  Object message ) throws SSLException
     {
         SSLHandler handler = getSSLSessionHandler( session );
         synchronized( handler )
         {
             if( !isSSLStarted( session ) && handler.isInboundDone() )
             {
                 handler.schedulePostHandshakeMessage( nextFilter, message );
             }
             else
             {
                 ByteBuffer buf = ( ByteBuffer ) message;
                 if( SessionLog.isDebugEnabled( session ) )
                 {
                     SessionLog.debug( session, " Data Read: " + handler + " (" + buf+ ')' );
                 }
     
                 try
                 {
                     // forward read encrypted data to SSL handler
                     handler.messageReceived( nextFilter, buf.buf() );
     
                     // Handle data to be forwarded to application or written to net
                     handleSSLData( nextFilter, handler );
     
                     if( handler.isInboundDone() )
                     {
                         if( handler.isOutboundDone() )
                         {
                             if( SessionLog.isDebugEnabled( session ) )
                             {
                                 SessionLog.debug(
                                         session, " SSL Session closed." );
                             }
                             
                             handler.destroy();
                         }
                         else
                         {
                             initiateClosure( nextFilter, session );
                         }
     
                         if( buf.hasRemaining() )
                         {
                             handler.schedulePostHandshakeMessage( nextFilter, buf );
                         }
                     }
                 }
                 catch( SSLException ssle )
                 {
                     if( !handler.isInitialHandshakeComplete() )
                     {
                         SSLException newSSLE = new SSLHandshakeException(
                                 "Initial SSL handshake failed." );
                         newSSLE.initCause( ssle );
                         ssle = newSSLE;
                     }
     
                     throw ssle;
                 }
             }
         }
         
         handler.flushPostHandshakeEvents();
     }
 
     @Override
     public void messageSent( NextFilter nextFilter, IoSession session,
                              WriteRequest writeRequest )
     {
         if( writeRequest instanceof EncryptedWriteRequest )
         {
             EncryptedWriteRequest wrappedRequest = ( EncryptedWriteRequest ) writeRequest;
             nextFilter.messageSent( session, wrappedRequest.getWriteRequest() );
         }
         else
         {
             // ignore extra buffers used for handshaking
         }
     }
 
     @Override
     public void filterWrite( NextFilter nextFilter, IoSession session, WriteRequest writeRequest ) throws SSLException
     {
         boolean needsFlush = true;
         SSLHandler handler = getSSLSessionHandler( session );
         synchronized( handler )
         {
             if( !isSSLStarted( session ) )
             {
                 handler.schedulePostHandshakeWriteRequest( nextFilter, writeRequest );
             }
             // Don't encrypt the data if encryption is disabled.
             else if ( session.containsAttribute( DISABLE_ENCRYPTION_ONCE ) )
             {
                 // Remove the marker attribute because it is temporary.
                 session.removeAttribute( DISABLE_ENCRYPTION_ONCE );
                 handler.schedulePostHandshakeWriteRequest( nextFilter, writeRequest );
             }
             else
             {
                 // Otherwise, encrypt the buffer.
                 ByteBuffer buf = ( ByteBuffer ) writeRequest.getMessage();
             
                 if( SessionLog.isDebugEnabled( session ) )
                 {
                     SessionLog.debug( session, " Filtered Write: " + handler );
                 }
     
                 if( handler.isWritingEncryptedData() )
                 {
                     // data already encrypted; simply return buffer
                     if( SessionLog.isDebugEnabled( session ) )
                     {
                         SessionLog.debug( session, "   already encrypted: " + buf );
                     }
                     handler.schedulePostHandshakeWriteRequest( nextFilter, writeRequest );
                 }
                 else if( handler.isInitialHandshakeComplete() )
                 {
                     // SSL encrypt
                     if( SessionLog.isDebugEnabled( session ) )
                     {
                         SessionLog.debug( session, " encrypt: " + buf );
                     }
                     
                     int pos = buf.position();
                     handler.encrypt( buf.buf() );
                     buf.position( pos );
                     ByteBuffer encryptedBuffer = SSLHandler.copy( handler.getOutNetBuffer() );
     
                     if( SessionLog.isDebugEnabled( session ) )
                     {
                         SessionLog.debug( session, " encrypted buf: " + encryptedBuffer);
                     }
                     handler.schedulePostHandshakeWriteRequest(
                             nextFilter, new EncryptedWriteRequest(writeRequest, encryptedBuffer));
                 }
                 else
                 {
                     if( !session.isConnected() )
                     {
                         if( SessionLog.isDebugEnabled( session ) )
                         {
                             SessionLog.debug( session, " Write request on closed session." );
                         }
                     }
                     else
                     {
                         if( SessionLog.isDebugEnabled( session ) )
                         {
                             SessionLog.debug( session, " Handshaking is not complete yet. Buffering write request." );
                         }
                         handler.schedulePreHandshakeWriteRequest( nextFilter, writeRequest );
                     }
                     needsFlush = false;
                 }
             }
         }
         
         if (needsFlush) {
             handler.flushPostHandshakeEvents();
         }
     }
     
     @Override
     public void filterClose( final NextFilter nextFilter, final IoSession session ) throws SSLException
     {
        SSLHandler handler = getSSLSessionHandler( session );
 
         WriteFuture future = null;
         try
         {
             synchronized( handler )
             {
                 if( isSSLStarted( session ) )
                 {
                     future = initiateClosure( nextFilter, session );
                 }
             }
             
             handler.flushPostHandshakeEvents();
         }
         finally
         {
             if( future == null )
             {
                 nextFilter.filterClose( session );
             }
             else
             {
                 future.addListener( new IoFutureListener()
                 {
                     public void operationComplete( IoFuture future )
                     {
                         nextFilter.filterClose( session );
                     }
                 });
             }
         }
     }
     
     private WriteFuture initiateClosure( NextFilter nextFilter, IoSession session ) throws SSLException
     {
         SSLHandler handler = getSSLSessionHandler( session );
         // if already shut down
         if( !handler.closeOutbound() )
         {
             return DefaultWriteFuture.newNotWrittenFuture( session );
         }
         
         // there might be data to write out here?
         WriteFuture future = handler.writeNetBuffer( nextFilter );
         
         if( handler.isInboundDone() )
         {
             handler.destroy();
         }
 
         if( session.containsAttribute( USE_NOTIFICATION ) )
         {
             handler.schedulePostHandshakeMessage( nextFilter, SESSION_UNSECURED );
         }
         
         return future;
     }
 
     // Utiliities
 
     private void handleSSLData( NextFilter nextFilter, SSLHandler handler ) throws SSLException
     {
         // Flush any buffered write requests occurred before handshaking.
         if( handler.isInitialHandshakeComplete() )
         {
             handler.flushPreHandshakeEvents();
         }
 
         // Write encrypted data to be written (if any)
         handler.writeNetBuffer( nextFilter );
 
         // handle app. data read (if any)
         handleAppDataRead( nextFilter, handler );
     }
 
     private void handleAppDataRead( NextFilter nextFilter, SSLHandler handler )
     {
         IoSession session = handler.getSession();
         if( !handler.getAppBuffer().hasRemaining() )
         {
             return;
         }
 
         if( SessionLog.isDebugEnabled( session ) )
         {
             SessionLog.debug( session, " appBuffer: " + handler.getAppBuffer() );
         }
 
         // forward read app data
         ByteBuffer readBuffer = SSLHandler.copy( handler.getAppBuffer() );
         if( SessionLog.isDebugEnabled( session ) )
         {
             SessionLog.debug( session, " app data read: " + readBuffer + " (" + readBuffer.getHexDump() + ')' );
         }
         
         handler.schedulePostHandshakeMessage( nextFilter, readBuffer );
     }
 
     private SSLHandler getSSLSessionHandler( IoSession session )
     {
         SSLHandler handler = getSSLSessionHandler0( session );
         if( handler == null )
         {
             throw new IllegalStateException();
         }
         if( handler.getParent() != this )
         {
             throw new IllegalArgumentException( "Not managed by this filter." );
         }
         return handler;
     }
 
     private SSLHandler getSSLSessionHandler0( IoSession session )
     {
         return ( SSLHandler ) session.getAttribute( SSL_HANDLER );
     }
     
     /**
      * A message that is sent from {@link SSLFilter} when the connection became
      * secure or is not secure anymore. 
      *
      * @author The Apache MINA Project (dev@mina.apache.org)
      * @version $Rev$, $Date$
      */
     public static class SSLFilterMessage
     {
         private final String name;
         
         private SSLFilterMessage( String name )
         {
             this.name = name;
         }
         
         @Override
         public String toString()
         {
             return name;
         }
     }
     
     private static class EncryptedWriteRequest extends WriteRequestWrapper
     {
         private final ByteBuffer encryptedMessage;
         
         private EncryptedWriteRequest(WriteRequest writeRequest, ByteBuffer encryptedMessage) {
             super(writeRequest);
             this.encryptedMessage = encryptedMessage;
         }
         
         @Override
         public Object getMessage() {
             return encryptedMessage;
         }
     }
 }
