 package org.jivesoftware.smack;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.cert.CertificateException;
 import java.util.Vector;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.net.ssl.SSLSocket;
 import javax.xml.parsers.DocumentBuilder;
 
 import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
 import org.jivesoftware.smack.packet.XMPPError;
 import org.jivesoftware.smack.proxy.SocketConnectorFactory;
 import org.jivesoftware.smack.util.DNSUtil;
 import org.jivesoftware.smack.util.ObservableReader;
 import org.jivesoftware.smack.util.ObservableWriter;
 import org.jivesoftware.smack.util.ThreadUtil;
 import org.jivesoftware.smack.util.XmlUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.kenai.jbosh.AbstractBody;
 import com.kenai.jbosh.BOSHClient;
 import com.kenai.jbosh.BOSHClientConfig;
 import com.kenai.jbosh.BOSHClientConnEvent;
 import com.kenai.jbosh.BOSHClientConnListener;
 import com.kenai.jbosh.BOSHClientRequestListener;
 import com.kenai.jbosh.BOSHClientResponseListener;
 import com.kenai.jbosh.BOSHClientSocketConnectorFactory;
 import com.kenai.jbosh.BOSHException;
 import com.kenai.jbosh.BOSHMessageEvent;
 import com.kenai.jbosh.BodyQName;
 import com.kenai.jbosh.ComposableBody;
 
 public class XMPPStreamBOSH extends XMPPStream
 {
     private URI uri = null;
 
     private final ReentrantLock lock = new ReentrantLock();
     private final Condition cond = lock.newCondition();
     
     // bosh_client must only be accessed while locked, and only when connectionClosed is false.
     private BOSHClient bosh_client;
     private boolean connectionClosed = false;
 
     /** Return true if the connection is secure (encrypted with a verified certificate). */
     public boolean isSecureConnection() { return usingSecureConnection; }
     private boolean usingSecureConnection = false;
 
     public void writeData(ComposableBody body) throws XMPPException {
         assertNotLocked();
 
         BOSHClient boshClientCopy;
         lock.lock();
         try {
             if(connectionClosed)
                 throw new XMPPException("Wrote a packet while the connection was closed");
             boshClientCopy = bosh_client;
         } finally {
             lock.unlock();
         }
 
         try {
             // Note that this will block if the packet can't be sent immediately.
             boshClientCopy.send(body);
         } catch(BOSHException e) {
             throw new XMPPException("Error writing BOSH packet", e);
         }
     }
 
     public void writePacket(String packet) throws XMPPException {
         assertNotLocked();
 
         writeData(createBoshPacket(packet).build());
     }
 
     // Although compression may or may not be in use by the HTTP stream, that can
     // vary from connection to connection, and we won't have any meaningful response
     // if the connection is inactive.  Just return false.
     public boolean isUsingCompression() { return false; }
 
     private ObservableReader.ReadEvent readEvent;
     private ObservableWriter.WriteEvent writeEvent;
 
     /** This is null, our SetupPacketCallback, or the user's callback from setPacketCallback. */
     private PacketCallback callback;
     
     String authID;
     public String getConnectionID() { return authID; }
 
     /** The thread performing connection recovery, if any. */
     private RecoveryTask recoveryTask = null;
 
     public void setReadWriteEvents(ObservableReader.ReadEvent readEvent, ObservableWriter.WriteEvent writeEvent) {
         this.writeEvent = writeEvent;
         this.readEvent = readEvent;
     }
 
     ConnectionConfiguration config;
 
     /**
      * This may be accessed or set while locked, but only the creator can clear
      * this to null.
      */
     private DNSUtil.CancellableLookup initialLookup;
 
     public XMPPStreamBOSH(ConnectionConfiguration config)
     {
         this.uri = config.getBoshURI();
         this.config = config;
     }
 
     class ConnectDataBOSH extends ConnectData {
         Vector<URI> addresses = new Vector<URI>();
 
         int connectionAttempts() {
             return addresses.size();
         }
     };
 
     public ConnectDataBOSH getDefaultConnectData() {
         assertNotLocked();
 
         lock.lock();
         try {
             ConnectDataBOSH data = new ConnectDataBOSH();
             if(!this.uri.equals(ConnectionConfiguration.AUTO_DETECT_BOSH))
                 data.addresses.add(this.uri);
             return data;
         } finally {
             lock.unlock();
         }
     }
     
     public ConnectDataBOSH getConnectData() throws XMPPException {
         assertNotLocked();
 
        if(!this.config.getBoshURI().equals(ConnectionConfiguration.AUTO_DETECT_BOSH))
            return getDefaultConnectData();
        
         lock.lock();
         try {
             if(bosh_client != null)
                 throw new RuntimeException("The connection has already been initialized");
             if(this.uri == null)
                 throw new XMPPException("BOSH is disabled");
             if(connectionClosed)
                 throw new XMPPException("Connection permanently closed");
 
             ConnectDataBOSH data = new ConnectDataBOSH();
 
             // This will return the same results each time, because the weight
             // shuffling is cached.
             // XXX: Figure out how BOSH discovery is supposed to be secured.
             DNSUtil.XMPPConnectLookup txtLookup;
             txtLookup = new DNSUtil.XMPPConnectLookup(config.getServiceName(), "_xmpp-client-xbosh");
             initialLookup = txtLookup;
 
             Vector<String> urls;
             lock.unlock();
             assertNotLocked();
             try {
                 urls = txtLookup.run();
             } finally {
                 lock.lock();
             }
 
             initialLookup = null;
 
             if(urls == null || connectionClosed)
                 throw new XMPPException("Connection cancelled");
 
             for(String url: urls) {
                 try {
                     URI uri = new URI(url);
 
                     // Only allow HTTP and HTTPS.
                     if(uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))
                         data.addresses.add(uri);
                 } catch (URISyntaxException e) {
                     // throw new XMPPException("Discovered BOSH server has bad URL: " + url);
                 }
             }
 
             return data;
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * An adaptor to convert our SocketConnectorFactory to jbosh's JBOSHSocketConnectorFactory.
      * These have essentially the same interface. 
      */
     static private class JBOSHSocketConnectorFactory extends BOSHClientSocketConnectorFactory {
         final SocketConnectorFactory actualConnectorFactory;
         JBOSHSocketConnectorFactory(SocketConnectorFactory actualConnectorFactory) {
             this.actualConnectorFactory = actualConnectorFactory;
         }
 
         public SocketConnector createConnector(final Socket socket) {
             final SocketConnectorFactory.SocketConnector socketConnector = actualConnectorFactory.createConnector(socket); 
             return new SocketConnector() {
                 public void connectSocket(String host, int port) throws IOException {
                     try {
                         socketConnector.connectSocket(host, port);
                     } catch(XMPPException e) {
                         IOException io = new IOException();
                         io.initCause(e);
                         throw io;
                     }
                 }
 
                 public void cancel() {
                     socketConnector.cancel();
                 }
             };
         }
     };
 
     public void initializeConnection(ConnectData data, int attempt) throws XMPPException
     {
         assertNotLocked();
 
         if(bosh_client != null)
             throw new RuntimeException("The connection has already been initialized");
 
         // If BOSH is disabled, then stop attempting this transport.
         if(this.uri == null)
             throw new XMPPException("BOSH is disabled", XMPPError.Condition.remote_server_not_found);
 
         if(!(data instanceof ConnectDataBOSH))
             throw new IllegalArgumentException("data argument was not created with XMPPStreamBOSH.getConnectData");
         ConnectDataBOSH dataBOSH = (ConnectDataBOSH) data;
 
         if(attempt >= dataBOSH.addresses.size())
             throw new IllegalArgumentException();
 
         uri = dataBOSH.addresses.get(attempt);
 
         // We'll catch this down below, but we throw a different message here.
         if(config.getSecurityMode() == SecurityMode.required && !uri.getScheme().equalsIgnoreCase("https"))
             throw new XMPPException("Discovered BOSH server is not HTTPS, but security required by connection configuration.",
                     XMPPError.Condition.forbidden);
 
         SetupPacketCallback setupCallback;
         final XMPPSSLSocketFactory xmppSocketFactory = new XMPPSSLSocketFactory(config, config.getServiceName());
 
         lock.lock();
         try {
             // Check whether disconnect() has already been called.
             if(connectionClosed)
                 throw new XMPPException("Connection cancelled");
 
             BOSHClientConfig.Builder cfgBuilder = BOSHClientConfig.Builder.create(uri, config.getServiceName());
 
             // The BOSH wait time is its keepalive time.  BOSH doesn't support disabling keepalive;
             // if no keepalives are requested, use one hour.
             int keepAliveInterval = SmackConfiguration.getKeepAliveInterval();
             if(keepAliveInterval == -1)
                 keepAliveInterval = 60*60*1000;
             cfgBuilder.setWaitTime(keepAliveInterval / 1000);
 
             // If we've been given a ScheduledExecutorService, use it for jbosh scheduling as well.
             cfgBuilder.setExecutorService(config.getExecutorService());
 
             // Give jbosh our SocketConnectorFactory, so it uses our proxy support and DNS cancellation.
             SocketConnectorFactory connectorFactory = config.getProxyInfo().getSocketConnectorFactory();
             JBOSHSocketConnectorFactory boshConnectorFactory = new JBOSHSocketConnectorFactory(connectorFactory);
             cfgBuilder.setSocketConnectorFactory(boshConnectorFactory);
 
             cfgBuilder.setSSLConnector(new com.kenai.jbosh.SSLConnector() {
                 public SSLSocket attachSSLConnection(Socket socket, String host, int port) throws IOException {
                     return xmppSocketFactory.attachSSLConnection(socket, host, port);
                 }
             });
 
             if(uri.getScheme().equals("http")) {
                 if(config.getSecurityMode() == SecurityMode.required)
                     throw new XMPPException("BOSH server is not HTTPS, but security required by connection configuration.",
                             XMPPError.Condition.forbidden);
                 usingSecureConnection = false;
             }
 
             bosh_client = BOSHClient.create(cfgBuilder.build());
             bosh_client.addBOSHClientResponseListener(new ResponseListener());
             bosh_client.addBOSHClientConnListener(new ConnectionListener());
 
             // Pass on requests and responses to observers.
             bosh_client.addBOSHClientResponseListener(new BOSHClientResponseListener() {
                 public void responseReceived(BOSHMessageEvent event) {
                     if (event.getBody() == null)
                         return;
                     readEvent.notifyListeners(event.getBody().toXML());
                 }
             });
 
             bosh_client.addBOSHClientRequestListener(new BOSHClientRequestListener() {
                 public void requestSent(BOSHMessageEvent event) {
                     if (event.getBody() == null)
                         return;
                     writeEvent.notifyListeners(event.getBody().toXML());
                 }
             });
 
             setupCallback = new SetupPacketCallback();
             callback = setupCallback;
         } finally {
             lock.unlock();
         }
 
         // At this point, a call to disconnect() can close bosh_client.
 
         try {
             // Send the session creation request and receive the response.
             writeData(ComposableBody.builder()
                         .setNamespaceDefinition("xmpp", "urn:xmpp:xbosh")
                         .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "version", "xmpp"), "1.0")
                         .build());
 
             setupCallback.waitForCompletion();
         } catch(XMPPException e) {
             /* If an exception occurs while we're starting the connection, close it.
              * The connection should only be running on a successful return. */
             // android.util.Log.w("SMACK", "XMPPStreamBOSH: initializeConnection: shutting down client due to error");
             disconnect();
             throw e;
         }
 
         // We've received the first response, so the first connection has been established.
         // We can now tell if the connection was made insecurely.  We don't need to check
         // SecurityMode here: if it's set to required, an exception was thrown when we first
         // connected the socket.  Note that usingSecureConnection may already be false for
         // other reasons.
         if(xmppSocketFactory != null) {
             CertificateException detail = xmppSocketFactory.getSeenInsecureConnection();
             if(detail != null)
                 usingSecureConnection = false;
         }
     }
 
     public void setPacketCallbacks(PacketCallback userCallbacks) {
         if(userCallbacks == null)
             throw new IllegalArgumentException("userCallbacks can not be null");
 
         assertNotLocked();
         lock.lock();
         try {
             if(callback != null)
                 throw new IllegalStateException("PacketCallbacks is already set");
 
             // Set the user's callback.
             this.callback = userCallbacks;
 
             // If PacketReaderThread is waiting for callbacks to be set, wake it up.
             cond.signalAll();
         } finally {
             lock.unlock();
         }
     }    
 
     class SetupPacketCallback extends PacketCallback {
         boolean complete = false;
         XMPPException error = null;
 
         public void onBody(AbstractBody body) {
             assertNotLocked();
 
             try {
                 handleFirstResponse(body);
             } catch(XMPPException e) {
                 onError(e);
                 return;
             }
 
             lock.lock();
             try {
                 // Clear ourself as the callback.
                 if(callback != this)
                     throw new IllegalStateException("unexpected value for callback");
                 callback = null;
                 
                 // Wake up waitForCompletion.
                 complete = true;
                 cond.signalAll();
             } finally {
                 lock.unlock();
             }
         }
 
         // This is never called, since we change to userCallbacks first.
         public void onPacket(Element packet) { }
 
         public void onError(XMPPException error) {
             assertNotLocked();
             lock.lock();
             try {
                 this.error = error;
                 cond.signalAll();
             } finally {
                 lock.unlock();
             }
         }
 
         public void onRecoverableError(XMPPException error) {
             onError(error);
         }
 
         // Connections are never recovered while still being set up.
         public void onRecovered() { throw new RuntimeException(); }
 
         /** Wait until stream negotiation is complete. */
         public void waitForCompletion() throws XMPPException {
             assertNotLocked();
             lock.lock();
             try {
                 while(!complete && error == null) {
                     try {
                         cond.await();
                     } catch(InterruptedException e) {
                         Thread.currentThread().interrupt();
                     }
                 }
             } finally {
                 lock.unlock();
             }
 
             if(error != null) {
                 throw new XMPPException(error);
             }
         }
     };
 
     private void handleFirstResponse(AbstractBody packet) throws XMPPException {
         // Check that secure='1' was sent in the response, indicating that the BOSH->XMPP connection
         // is also secure.
         String secure = packet.getAttribute(BodyQName.create("http://jabber.org/protocol/httpbind", "secure"));
         if(secure == null || (!secure.equalsIgnoreCase("true") && !secure.equals("1"))) {
             usingSecureConnection = false;
 
             if(config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required) {
                 throw new XMPPException("The BOSH->XMPP session is not secure, " +
                         "but the configuration requires a secure connection.",
                         XMPPError.Condition.forbidden);
             }
         }
 
         // Over BOSH, XMPP's <body id> is transmitted as authid.
         authID = packet.getAttribute(BodyQName.create("http://jabber.org/protocol/httpbind", "authid"));
 
         // Update the service name from <body from>, if provided.
         String from = packet.getAttribute(BodyQName.create("http://jabber.org/protocol/httpbind", "from"));
         if(from != null)
             config.setServiceName(from);
     }
 
     public void gracefulDisconnect(String packet)
     {
         assertNotLocked();
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: close()");
 
         try {
             // If any stanzas are waiting to be sent, send them in the disconnect message.
             bosh_client.disconnect(createBoshPacket(packet).build());
         }
         catch(BOSHException e)
         {
             // If the disconnect() fails for some reason, forcibly close the connection.
             // android.util.Log.w("SMACK", "XMPPStreamBOSH: sending disconnect(): error");
             e.printStackTrace();
             disconnect();
             return;
         }
 
         // We've sent the terminate packet.  Wait for the stream to close; we'll
         // receive a connectionEvent, which will call disconnect() and set connectionClosed.
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: waiting for disconnect");
         long waitUntil = System.currentTimeMillis() + SmackConfiguration.getPacketReplyTimeout();
         lock.lock();
         try {
             while(!connectionClosed) {
                 long ms = waitUntil - System.currentTimeMillis();
                 if(ms <= 0)
                     break;
                 try {
                     cond.await(ms, TimeUnit.MILLISECONDS);
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     break;
                 }
             }
         } finally {
             lock.unlock();
         }
 
         // android.util.Log.w("SMACK", "XMPPStreamBOSH: done waiting, " + (connectionClosed? "connection is closed":"connection is not closed"));
 
         // In case we timed out above and the stream isn't closed yet, forcibly close it.
         disconnect();
     }
 
     public void disconnect() {
         assertNotLocked();
 
         BOSHClient boshClientCopy;
         RecoveryTask recoveryTaskRef;
         lock.lock();
 
         try {
             if(initialLookup != null) {
                 // initializeConnection() is performing a DNS lookup.  Cancel it, but
                 // don't clear the reference.
                 initialLookup.cancel();
             }
 
             boshClientCopy = bosh_client;
             recoveryTaskRef = recoveryTask;
 
             // Set connectionClosed now, so if ResponseListener is waiting for callbacks, it'll
             // stop waiting.  Otherwise, boshClientCopy.close() won't be able to join its thread.
             // This also needs to be set for ConnectionListener.connectionEvent to know that the
             // disconnection event is expected.
             connectionClosed = true;
 
             // Signal the connectionClosed change.
             cond.signalAll();
         } finally {
             lock.unlock();
         }
 
         // Don't hold the lock while we close bosh_client, so the lock isn't held
         // when BOSHClientConnListener callbacks are called.
         if(boshClientCopy != null)
             boshClientCopy.close();
 
         if(recoveryTaskRef != null)
             recoveryTaskRef.cancel();
     }
 
     /**
      * A stream reset is required, usually due to SASL authentication completing.
      * Instruct the BOSH server to perform the restart.
      */
     public void streamReset() throws XMPPException {
         // We're supposed to check for <body restartlogic='true'>, which tells us that the server supports
         // stream restarts.  If it's not present, we can't restart the stream, which means we can't
         // authenticate via SASL.  However, the spec also notes correctly that older BOSH implementations
         // don't advertise restartlogic (despite supporting it).  If we refuse to authenticate
         // when restartlogic isn't present, then we'll break logins on older servers.  This really
         // isn't very useful: a BOSH server that we effectively can't authenticate through is useless,
         // so for now just pretend they all support stream restarts.
         assertNotLocked();
 
         /* Make sure any written data is flushed and sent to the server.  According
          * to XEP-0206, any stanzas we put in a <body restart='true'> packet will be
          * ignored. */
         writeData(ComposableBody.builder()
                 .setNamespaceDefinition("xmpp", "urn:xmpp:xbosh")
                 .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "version", "xmpp"), "1.0")
                 .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "restart", "xmpp"), "true")
                 .build());
     }
 
     /** Wait for callbacks to be available.  Returns {@link PacketCallback}, or
      *  null if {@link XMPPStreamTCP#disconnect} is called. */
     private PacketCallback getCallbacks() {
         assertNotLocked();
 
         lock.lock();
         try {
             while(callback == null && !connectionClosed)
                 ThreadUtil.uninterruptibleWait(cond);
             return callback;
         } finally {
             lock.unlock();
         }
     }
     
     private static ComposableBody.Builder createBoshPacket(String packet) {
         ComposableBody.Builder builder = ComposableBody.builder()
             .setNamespaceDefinition("xmpp", "urn:xmpp:xbosh")
             .setAttribute(BodyQName.createWithPrefix("urn:xmpp:xbosh", "version", "xmpp"), "1.0");
 
         if(packet != null) {
             builder.setPayloadXML(packet);
         }
 
         return builder;
     }
 
     private static class TemporaryRecoveryFailure extends Exception {
         XMPPException cause;
         TemporaryRecoveryFailure(XMPPException cause) { this.cause = cause; } 
     }
 
     /**
      * Asynchronously attempt to recover the connection.
      */
     static class RecoveryTask {
         private final BOSHClient client;
         private final ConnectionConfiguration config;
         private final URI wantedUri;
         private final PacketCallback callbacks;
         private final ReentrantLock lock = new ReentrantLock();
         private XMPPStreamBOSH lookupStream;
         private Thread thread;
         private boolean cancelled = false;
 
         RecoveryTask(BOSHClient client, ConnectionConfiguration config, URI wantedUri, PacketCallback callbacks) {
             this.client = client;
             this.config = config;
             this.wantedUri = wantedUri;
             this.callbacks = callbacks;
 
             if(callbacks == null)
                 throw new IllegalStateException("Callbacks must be set");
 
             thread = config.getThreadFactory().newThread(task);
             thread.setName("Reconnection");
         }
 
         private Runnable task = new Runnable() {
             public void run() {
                 assertNotLocked();
     
                 try {
                     reconnect();
                 } catch(TemporaryRecoveryFailure e) {
                     callbacks.onRecoverableError(e.cause);
                 } catch(XMPPException e) {
                     callbacks.onError(e);
                 }
             }
         };
 
         public void start() {
             thread.start();
         }
 
         public void cancel() {
             assertNotLocked();
 
             lock.lock();
             try {
                 // Stop reconnect() from creating lookupStream.
                 cancelled = true;
                 
                 // If reconnect() has already created lookupStream, cancel it.
                 if(lookupStream != null)
                     lookupStream.disconnect();
                 
                 // The remainder of reconnect() is nonblocking.
             } finally {
                 lock.unlock();
             }
             
             // If we're being called from within the thread, then we're underneith
             // the onError or onRecoverableError callback, in which case we're about
             // to exit anyway.
             if(Thread.currentThread() != thread)
                 ThreadUtil.uninterruptibleJoin(thread);
         }
 
         private void reconnect() throws XMPPException, TemporaryRecoveryFailure {
             assertNotLocked();
             lock.lock();
             try {
                 // If cancel() was already called, stop.
                 if(cancelled)
                     throw new XMPPException("Connection recovery cancelled");
 
                 // Create a slave stream to rerun discovery.
                 lookupStream = new XMPPStreamBOSH(config);
             } finally {
                 lock.unlock();
             }
 
             // Run the lookup unlocked, so it can be cancelled.
             ConnectDataBOSH data;
             try {
                 data = lookupStream.getConnectData();
             } catch(XMPPException e) {
                 // Autodiscovery itself failing is a recoverable error; it usually means that the
                 // network connection is offline.  Dispatch an onRecoverableError.
                 throw new TemporaryRecoveryFailure(e);
             } finally {
                 lookupStream.disconnect();
                 lookupStream = null;
             }
 
             lock.lock();
             try {
                 // If the URI we were connected to is no longer in the URI list, then it's been removed
                 // from TXT.  Raise a fatal error so reconnection starts over from scratch.
                 boolean hostFound = false;
                 for(URI uri: data.addresses) {
                     if(uri.equals(wantedUri))
                         hostFound = true;
                 }
 
                 if(!hostFound)
                     throw new XMPPException("BOSH URI " + wantedUri + " is no longer in discovered service list");
 
                 // Begin BOSH reconnection.  This call returns immediately.  Success is indicated when
                 // we receive another packet from the server; failure if onError/onRecoverableError is
                 // called again.
                 try {
                     if(!client.attemptReconnection()) {
                         // The connection is already established, and no reconnection attempt was
                         // needed.
                         return;
                     }
                 } catch(BOSHException e) {
                     // The reconnection failed unrecoverably.
                     throw new XMPPException(e);
                 }
             } finally {
                 lock.unlock();
             }
         }
         
         private void assertNotLocked() {
             if(lock.isHeldByCurrentThread())
                 throw new RuntimeException("Lock should not be held");
         }
     };
 
     public void recoverConnection() {
         assertNotLocked();
         lock.lock();
         try {
             if(bosh_client == null)
                 throw new IllegalStateException("Connection not initialized");
             if(connectionClosed)
                 return;
 
             // If the recovery task is already in progress, stop it and recreate it.
             RecoveryTask recoveryTaskRef = recoveryTask;
             recoveryTask = null;
 
             lock.unlock();
             try {
                 if(recoveryTaskRef != null)
                     recoveryTaskRef.cancel();
             } finally {
                 lock.lock();
             }
 
             // Check if disconnect() was called while we were unlocked.
             if(connectionClosed)
                 return;
 
             // Begin recovery.
             recoveryTask = new RecoveryTask(bosh_client, config, this.uri, callback);
             recoveryTask.start();
         } finally {
             lock.unlock();
         }
     }
 
     /** This is called from the ResponseListener.  A packet has been received.  If
      *  a reconnection attempt was taking place, reset the reconnection counter and
      *  inform the client of the reconnection. */
     private void checkForRecovery(PacketCallback cb) {
         assertNotLocked();
 
         lock.lock();
         try {
             // Be sure to only call onRecovered if a reconnection attempt was actually taking place.
             if(recoveryTask == null)
                 return;
         } finally {
             lock.unlock();
         }
 
         // 
         cb.onRecovered();
     }
 
     private class ResponseListener implements BOSHClientResponseListener
     {
         public void responseReceived(BOSHMessageEvent event)
         {
             assertNotLocked();
 
             PacketCallback currentCallback = getCallbacks();
             if(currentCallback == null)
                 return;
 
             AbstractBody body = event.getBody();
 
             String xml = body.toXML();
             Element bodyNode;
 
             try {
                 DocumentBuilder docBuilder = XmlUtil.getDocumentBuilder();
                 Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
 
                 // Retrieve <body>.
                 bodyNode = (Element) doc.getFirstChild();
             }
             catch(IOException e) {
                 dispatchErrorCallback(currentCallback, new XMPPException("Error reading packet", e), false);
                 return;
             }
             catch(SAXException e) {
                 dispatchErrorCallback(currentCallback, new XMPPException("Error reading packet", e), false);
                 return;
             }
 
             checkForRecovery(currentCallback);
 
             if(currentCallback instanceof SetupPacketCallback) {
                 SetupPacketCallback setupCallbacks = (SetupPacketCallback) currentCallback;
                 setupCallbacks.onBody(body);
                 
                 // SetupCallbacks receives the first packet to onBody, and clears itself.
                 // When that happens, wait for the new PacketCallbacks from setPacketCallbacks.
                 currentCallback = getCallbacks();
                 if(currentCallback == null)
                     return;
             }
 
             // The children of <body> are the XMPP payloads.
             NodeList nodes = bodyNode.getChildNodes();
             for(int i = 0; i < nodes.getLength(); ++i) {
                 Node node = nodes.item(i);
                 if(!(node instanceof Element))
                     continue;
                 currentCallback.onPacket((Element) node);
             }
         }
     }
 
     /**
      * Dispatch an onError or onRecoverableError callback.
      * <p>
      * This must never be called after shutdown(), or in any context where shutdown() might
      * finish before we do.  Calling from jbosh callbacks is safe, because shutdown() closes
      * down jbosh, ensuring that no more callbacks will be run.
      */
     private void dispatchErrorCallback(PacketCallback currentCallback, XMPPException e, boolean recoverable) {
         assertNotLocked();
 
         if(recoverable)
             currentCallback.onRecoverableError(e);
         else
             currentCallback.onError(e);
     }
     
     private class ConnectionListener implements BOSHClientConnListener
     {
         public void connectionEvent(BOSHClientConnEvent connEvent)
         {
             assertNotLocked();
 
             if(connEvent.isError()) {
                 PacketCallback currentCallback = getCallbacks();
 
                 // Never ignore errors during setup, or SetupPacketCallback.waitForCompletion
                 // will never return.  Don't report errors if the connection is already closed.
                 boolean reportError = !connectionClosed;
                 if(currentCallback instanceof SetupPacketCallback)
                     reportError = true;
                 
                 boolean recoverable = bosh_client.isRecoverableConnectionLoss();
                 if(currentCallback != null && reportError) {
                     XMPPException e = new XMPPException(connEvent.getCause());
                     dispatchErrorCallback(currentCallback, e, recoverable);
                     if(recoverable)
                         return;
                 }
             }
 
             if(!connEvent.isConnected() && !connectionClosed) {
                 disconnect();
             }
         }
     }
 
     private void assertNotLocked() {
         if(lock.isHeldByCurrentThread())
             throw new RuntimeException("Lock should not be held");
     }
 
     private void assertLocked() {
         if(!lock.isHeldByCurrentThread())
             throw new RuntimeException("Lock should be held");
     }
 };
