 /**
  * $RCSfile$
  * $Revision$
  * $Date$
  *
  * Copyright 2003-2007 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
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
  */
 
 package org.jivesoftware.smack;
 
 import org.jivesoftware.smack.debugger.SmackDebugger;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.XMPPError;
 import org.jivesoftware.smack.util.StringUtils;
 import org.jivesoftware.smack.util.ObservableReader;
 import org.jivesoftware.smack.util.ObservableWriter;
 
 import org.apache.harmony.javax.security.auth.callback.CallbackHandler;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.util.Collection;
 import java.util.Vector;
 
 import org.w3c.dom.Element;
 
 /**
  * Creates a socket connection to a XMPP server. This is the default connection
  * to a Jabber server and is specified in the XMPP Core (RFC 3920).
  * 
  * @see Connection
  * @author Matt Tucker
  */
 public class XMPPConnection extends Connection {
     /**
      * The XMPPStream used for this connection.
      */
     private XMPPStream data_stream = null;
 
     private String user = null;
     private boolean connected = false;
     /**
      * Flag that indicates if the user is currently authenticated with the server.
      */
     private boolean authenticated = false;
 
     /**
      * This is false between connectUsingConfiguration calling packetReader.startup()
      * and connection events being fired, during which time no disconnection events
      * will be sent.
      */
     private boolean readyForDisconnection;
 
     /**
      * Flag that indicates if the user was authenticated with the server when the connection
      * to the server was closed (abruptly or not).
      */
     private boolean wasAuthenticated = false;
     private boolean anonymous = false;
 
     /** True if we've yet to connect to a server. */
     private boolean isFirstInitialization = true;
 
     private boolean suppressConnectionErrors;
 
     private PacketWriter packetWriter;
     private PacketReader packetReader;
 
     /** The SmackDebugger allows to log and debug XML traffic. */
     protected SmackDebugger debugger = null;
 
     final ObservableReader.ReadEvent readEvent;
     final ObservableWriter.WriteEvent writeEvent;
 
     Roster roster = null;
 
     /**
      * Creates a new connection to the specified XMPP server. A DNS SRV lookup will be
      * performed to determine the IP address and port corresponding to the
      * service name; if that lookup fails, it's assumed that server resides at
      * <tt>serviceName</tt> with the default port of 5222. Encrypted connections (TLS)
      * will be used if available, stream compression is disabled, and standard SASL
      * mechanisms will be used for authentication.<p>
      * <p/>
      * This is the simplest constructor for connecting to an XMPP server. Alternatively,
      * you can get fine-grained control over connection settings using the
      * {@link #XMPPConnection(ConnectionConfiguration)} constructor.<p>
      * <p/>
      * Note that XMPPConnection constructors do not establish a connection to the server
      * and you must call {@link #connect()}.<p>
      * <p/>
      * The CallbackHandler will only be used if the connection requires the client provide
      * an SSL certificate to the server. The CallbackHandler must handle the PasswordCallback
      * to prompt for a password to unlock the keystore containing the SSL certificate.
      *
      * @param serviceName the name of the XMPP server to connect to; e.g. <tt>example.com</tt>.
      * @param callbackHandler the CallbackHandler used to prompt for the password to the keystore.
      */
     public XMPPConnection(String serviceName, CallbackHandler callbackHandler) {
        this(null, callbackHandler, serviceName);
     }
 
     /**
      * Creates a new XMPP connection in the same way {@link #XMPPConnection(String,CallbackHandler)} does, but
      * with no callback handler for password prompting of the keystore.  This will work
      * in most cases, provided the client is not required to provide a certificate to 
      * the server.
      *
      * @param serviceName the name of the XMPP server to connect to; e.g. <tt>example.com</tt>.
      */
     public XMPPConnection(String serviceName) {
        this(null, null, serviceName);
     }
 
     /**
      * Creates a new XMPP connection in the same way {@link #XMPPConnection(ConnectionConfiguration,CallbackHandler)} does, but
      * with no callback handler for password prompting of the keystore.  This will work
      * in most cases, provided the client is not required to provide a certificate to 
      * the server.
      *
      *
      * @param config the connection configuration.
      */
     public XMPPConnection(ConnectionConfiguration config) {
         this(config, null, null);
     }
 
     /**
      * Creates a new XMPP connection using the specified connection configuration.<p>
      * <p/>
      * Manually specifying connection configuration information is suitable for
      * advanced users of the API. In many cases, using the
      * {@link #XMPPConnection(String)} constructor is a better approach.<p>
      * <p/>
      * Note that XMPPConnection constructors do not establish a connection to the server
      * and you must call {@link #connect()}.<p>
      * <p/>
      *
      * The CallbackHandler will only be used if the connection requires the client provide
      * an SSL certificate to the server. The CallbackHandler must handle the PasswordCallback
      * to prompt for a password to unlock the keystore containing the SSL certificate.
      *
      * @deprecated call {@link ConnectionConfiguration#setCallbackHandler} and use {@link XMPPConnection#XMPPConnection(ConnectionConfiguration)}.
      * @param config the connection configuration.
      * @param callbackHandler the CallbackHandler used to prompt for the password to the keystore.
      */
     public XMPPConnection(ConnectionConfiguration config, CallbackHandler callbackHandler) {
         this(config, callbackHandler, null);
     }
 
     /** The primary constructor. */
     private XMPPConnection(ConnectionConfiguration config, CallbackHandler callbackHandler, String serviceName) {
         super(config);
         if(serviceName != null)
             this.config.setServiceName(serviceName);
         if(callbackHandler != null)
             this.config.setCallbackHandler(callbackHandler);
 
         readEvent = new ObservableReader.ReadEvent();
         writeEvent = new ObservableWriter.WriteEvent();
 
         // These won't do anything until we call startup().
         packetReader = new PacketReader(this);
         packetWriter = new PacketWriter(this);
 
         // If debugging is enabled, we should start the thread that will listen for
         // all packets and then log them.
         if (debugger != null) {
             addPacketListener(debugger.getReaderListener(), null);
             if (debugger.getWriterListener() != null) {
                 addPacketSendingListener(debugger.getWriterListener(), null);
             }
         }
 
         // If debugging is enabled, we open a window and write out all network traffic.
         if (config.isDebuggerEnabled())
             initDebugger();
     }
 
     public String getConnectionID() {
         if (!isConnected()) {
             return null;
         }
         return data_stream.getConnectionID();
     }
 
     public String getUser() {
         if (!isAuthenticated()) {
             return null;
         }
         return user;
     }
 
     @Override
     public synchronized void login(String username, String password, String resource) throws XMPPException {
         if (!isConnected()) {
             throw new IllegalStateException("Not connected to server.");
         }
         if (authenticated) {
             throw new IllegalStateException("Already logged in to server.");
         }
         // Do partial version of nameprep on the username.
         username = username.toLowerCase().trim();
 
         String response;
         if (config.isSASLAuthenticationEnabled() &&
                 saslAuthentication.hasNonAnonymousAuthentication()) {
             // Authenticate using SASL
             if (password != null) {
                 response = saslAuthentication.authenticate(username, password, resource);
             }
             else {
                 response = saslAuthentication
                         .authenticate(username, resource, config.getCallbackHandler());
             }
         }
         else {
             // Authenticate using Non-SASL
             response = new NonSASLAuthentication(this).authenticate(username, password, resource);
         }
 
         // Set the user.
         if (response != null) {
             this.user = response;
             // Update the serviceName with the one returned by the server
             config.setServiceName(StringUtils.parseServer(response));
         }
         else {
             this.user = username + "@" + getServiceName();
             if (resource != null) {
                 this.user += "/" + resource;
             }
         }
 
         // Indicate that we're now authenticated.
         authenticated = true;
         anonymous = false;
 
         // Create the roster if it is not a reconnection or roster already created by getRoster()
         if (this.roster == null) {
             this.roster = new Roster(this);
         }
         if (config.isRosterLoadedAtLogin()) {
             this.roster.reload();
         }
 
         // Set presence to online.
         if (config.isSendPresence()) {
             packetWriter.sendPacket(new Presence(Presence.Type.available));
         }
 
         // Stores the authentication for future reconnection
         config.setLoginInfo(username, password, resource);
 
         // If debugging is enabled, change the the debug window title to include the
         // name we are now logged-in as.
         if (debugger != null) {
             debugger.userHasLogged(user);
         }
     }
 
     @Override
     public synchronized void loginAnonymously() throws XMPPException {
         if (!isConnected()) {
             throw new IllegalStateException("Not connected to server.");
         }
         if (authenticated) {
             throw new IllegalStateException("Already logged in to server.");
         }
 
         String response;
         if (config.isSASLAuthenticationEnabled() &&
                 saslAuthentication.hasAnonymousAuthentication()) {
             response = saslAuthentication.authenticateAnonymously();
         }
         else {
             // Authenticate using Non-SASL
             response = new NonSASLAuthentication(this).authenticateAnonymously();
         }
 
         // Set the user value.
         this.user = response;
         // Update the serviceName with the one returned by the server
         config.setServiceName(StringUtils.parseServer(response));
 
         // Set presence to online.
         packetWriter.sendPacket(new Presence(Presence.Type.available));
 
         // Indicate that we're now authenticated.
         authenticated = true;
         anonymous = true;
 
         // If debugging is enabled, change the the debug window title to include the
         // name we are now logged-in as.
         if (debugger != null) {
             debugger.userHasLogged(user);
         }
     }
 
     public Roster getRoster() {
         // synchronize against login()
         synchronized(this) {
             // if connection is authenticated the roster is already set by login() 
             // or a previous call to getRoster()
             if (!isAuthenticated() || isAnonymous()) {
                 if (roster == null) {
                     roster = new Roster(this);
                 }
                 return roster;
             }
         }
 
         if (!config.isRosterLoadedAtLogin()) {
             roster.reload();
         }
         // If this is the first time the user has asked for the roster after calling
         // login, we want to wait for the server to send back the user's roster. This
         // behavior shields API users from having to worry about the fact that roster
         // operations are asynchronous, although they'll still have to listen for
         // changes to the roster. Note: because of this waiting logic, internal
         // Smack code should be wary about calling the getRoster method, and may need to
         // access the roster object directly.
         if (!roster.rosterInitialized) {
             try {
                 synchronized (roster) {
                     long waitTime = SmackConfiguration.getPacketReplyTimeout();
                     long start = System.currentTimeMillis();
                     while (!roster.rosterInitialized) {
                         if (waitTime <= 0) {
                             break;
                         }
                         roster.wait(waitTime);
                         long now = System.currentTimeMillis();
                         waitTime -= now - start;
                         start = now;
                     }
                 }
             }
             catch (InterruptedException ie) {
                 // Ignore.
             }
         }
         return roster;
     }
 
     public boolean isConnected() {
         return connected;
     }
 
     public boolean isSecureConnection() {
         return this.data_stream != null && this.data_stream.isSecureConnection();
     }
 
     public boolean isAuthenticated() {
         return authenticated;
     }
 
     public boolean isAnonymous() {
         return anonymous;
     }
 
     /**
      * Closes the connection by setting presence to unavailable then closing the stream to
      * the XMPP server. The shutdown logic will be used during a planned disconnection or when
      * dealing with an unexpected disconnection. Unlike {@link #disconnect()} the connection's
      * packet reader, packet writer, and {@link Roster} will not be removed; thus
      * connection's state is kept.
      *
      * @param unavailablePresence the presence packet to send during shutdown.
      */
     protected void shutdown() {
         packetReader.assertNotInThread();
 
         if (data_stream != null)
             data_stream.disconnect();
 
         // These will block until the threads are completely shut down.  This should happen
         // immediately, due to calling data_stream.disconnect().
         packetReader.shutdown();
         packetWriter.shutdown();
 
         // packetReader and packetWriter are gone, so we can safely clear data_stream.
         data_stream = null;
 
         saslAuthentication.init();
 
         this.setWasAuthenticated(authenticated);
         authenticated = false;
         connected = false;
     }
 
     public void disconnect(Presence unavailablePresence) {
         packetReader.assertNotInThread();
 
         boolean wasConnected;
         synchronized(this) {
             wasConnected = connected;
             connected = false;
         }
 
         // Shutting down will cause I/O exceptions in the reader and writer threads;
         // suppress them.
         suppressConnectionErrors = true;
 
         // Cleanly close down the connection.
         if (wasConnected)
             data_stream.close(unavailablePresence != null? unavailablePresence.toXML():null);
 
         shutdown();
 
         if (roster != null) {
             roster.cleanup();
             roster = null;
         }
 
         // Clear packet listeners only on final disconnection.
         recvListeners.clear();
         sendListeners.clear();
         collectors.clear();
         interceptors.clear();
 
         wasAuthenticated = false;
         suppressConnectionErrors = false;
 
         // If we're the one that cleared connected, it's our job to notify about the
         // disconnection.
         if(wasConnected)
             notifyConnectionClosed();
     }
 
     public void sendPacket(Packet packet) {
         if (!isConnected()) {
             throw new IllegalStateException("Not connected to server.");
         }
         if (packet == null) {
             throw new NullPointerException("Packet is null.");
         }
         packetWriter.sendPacket(packet);
     }
 
     /**
      * Registers a packet interceptor with this connection. The interceptor will be
      * invoked every time a packet is about to be sent by this connection. Interceptors
      * may modify the packet to be sent. A packet filter determines which packets
      * will be delivered to the interceptor.
      *
      * @param packetInterceptor the packet interceptor to notify of packets about to be sent.
      * @param packetFilter      the packet filter to use.
      * @deprecated replaced by {@link Connection#addPacketInterceptor(PacketInterceptor, PacketFilter)}.
      */
     public void addPacketWriterInterceptor(PacketInterceptor packetInterceptor,
             PacketFilter packetFilter) {
         addPacketInterceptor(packetInterceptor, packetFilter);
     }
 
     /**
      * Removes a packet interceptor.
      *
      * @param packetInterceptor the packet interceptor to remove.
      * @deprecated replaced by {@link Connection#removePacketInterceptor(PacketInterceptor)}.
      */
     public void removePacketWriterInterceptor(PacketInterceptor packetInterceptor) {
         removePacketInterceptor(packetInterceptor);
     }
 
     /**
      * Registers a packet listener with this connection. The listener will be
      * notified of every packet that this connection sends. A packet filter determines
      * which packets will be delivered to the listener. Note that the thread
      * that writes packets will be used to invoke the listeners. Therefore, each
      * packet listener should complete all operations quickly or use a different
      * thread for processing.
      *
      * @param packetListener the packet listener to notify of sent packets.
      * @param packetFilter   the packet filter to use.
      * @deprecated replaced by {@link #addPacketSendingListener(PacketListener, PacketFilter)}.
      */
     public void addPacketWriterListener(PacketListener packetListener, PacketFilter packetFilter) {
         addPacketSendingListener(packetListener, packetFilter);
     }
 
     /**
      * Removes a packet listener for sending packets from this connection.
      *
      * @param packetListener the packet listener to remove.
      * @deprecated replaced by {@link #removePacketSendingListener(PacketListener)}.
      */
     public void removePacketWriterListener(PacketListener packetListener) {
         removePacketSendingListener(packetListener);
     }
 
     /** Create a new XMPPStream. */
     private static XMPPStream createDataStream(Class<? extends XMPPStream> transport, ConnectionConfiguration config) {
         // Create an instance of this transport.
         Constructor<? extends XMPPStream> constructor;
         try {
             constructor = transport.getConstructor(ConnectionConfiguration.class);
             return constructor.newInstance(config);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Initializes the connection, opening an XMPP stream to the server.
      *
      * @throws XMPPException if establishing a connection to the server fails.
      */
     private void connectUsingConfiguration() throws XMPPException {
         packetReader.assertNotInThread();
 
         // We may have several candidates to connect to: any number of XMPP
         // hosts via SRV discovery, and any number of BOSH hosts via TXT discovery.
         // Try transports in order of preference.
         Vector<Class<? extends XMPPStream>> transportsToAttempt =
             new Vector<Class<? extends XMPPStream>>();
         transportsToAttempt.add(XMPPStreamBOSH.class);
         transportsToAttempt.add(XMPPStreamTCP.class);
 
         XMPPException firstFailure = null;
 
         for(Class<? extends XMPPStream> transport: transportsToAttempt) {
             // Attempt to connect using this transport.  If the transport discovers more
             // than one server to connect to, try each in order.  Note that timeouts are
             // per-server.
             int attempt = 0;
             while(true) {
                 if(data_stream != null)
                     throw new AssertionError("data_stream should be null");
 
                 data_stream = createDataStream(transport, config);
 
                 // Tell the transport which discovered server to attempt.
                 data_stream.setDiscoveryIndex(attempt);
 
                 try {
                     connectUsingConfigurationAttempt();
                     return;
                 } catch(XMPPException e) {
                     // On failure, connectUsingConfigurationAttempt always clears data_stream.
                     if(data_stream != null)
                         throw new AssertionError("connectUsingConfigurationAttempt failed, but left data_stream set");
 
                     // If the error is remote_server_not_found, there are no more
                     // discovered resources to try with this transport.
                     XMPPError error = e.getXMPPError();
                     if(error != null && error.getCondition() == XMPPError.Condition.remote_server_not_found.toString())
                         break;
                     firstFailure = e;
                 }
 
                 attempt++;
             }
         }
 
         // We didn't connect.  Report the first failure other than remote_server_not_found
         // as the error.  XXX: not ideal
         if(firstFailure != null)
             throw firstFailure;
         else
             throw new XMPPException("Couldn't discover any servers to connect to");
     }
 
     private void connectUsingConfigurationAttempt() throws XMPPException {
         packetReader.assertNotInThread();
 
         data_stream.setReadWriteEvents(readEvent, writeEvent);
 
         // Start the packet writer.  This can't fail, and it won't do anything until
         // we receive packets.
         packetWriter.startup();
 
         readyForDisconnection = false;
 
         try {
             // Start the packet reader. The startup() method will block until we
             // get an opening stream packet back from server.
             packetReader.startup();
         }
         catch (XMPPException ex) {
             // An exception occurred in setting up the connection. Make sure we shut down the
             // readers and writers and close the socket.
             shutdown();
             throw ex;        // Everything stopped. Now throw the exception.
         }
 
         // Connection is successful.
         connected = true;
 
         if (isFirstInitialization) {
             isFirstInitialization = false;
 
             // Notify listeners that a new connection has been established
             for (ConnectionCreationListener listener: getConnectionCreationListeners()) {
                 listener.connectionCreated(XMPPConnection.this);
             }
         }
         else if (!wasAuthenticated) {
             notifyReconnection();
         }
 
         // Inform readerThreadException that disconnections are now allowed.
         synchronized(this) {
             readyForDisconnection = true;
             this.notifyAll();
         }
     }
 
     /**
      * Initialize the {@link #debugger}. You can specify a customized {@link SmackDebugger}
      * by setup the system property <code>smack.debuggerClass</code> to the implementation.
      *
      * @throws IllegalStateException if the reader or writer isn't yet initialized.
      * @throws IllegalArgumentException if the SmackDebugger can't be loaded.
      */
     private void initDebugger() {
         // Detect the debugger class to use.
         // Use try block since we may not have permission to get a system
         // property (for example, when an applet).
         Vector<String> debuggers = new Vector<String>();
         String requestedDebugger = null;
         try {
             requestedDebugger = System.getProperty("smack.debuggerClass");
             debuggers.add(requestedDebugger);
         }
         catch (Throwable t) {
             // Ignore.
         }
         debuggers.add("org.jivesoftware.smackx.debugger.EnhancedDebugger");
         debuggers.add("org.jivesoftware.smackx.debugger.AndroidDebugger");
         debuggers.add("org.jivesoftware.smack.debugger.LiteDebugger");
         for (String debuggerName: debuggers) {
             try {
                 Class<?> debuggerClass = Class.forName(debuggerName);
 
                 // Attempt to create an instance of this debugger.
                 Constructor<?> constructor = debuggerClass
                         .getConstructor(Connection.class, ObservableWriter.WriteEvent.class, ObservableReader.ReadEvent.class);
                 debugger = (SmackDebugger) constructor.newInstance(this, writeEvent, readEvent);
                 break;
             }
             catch (Exception e) {
                 if(requestedDebugger != null && requestedDebugger.equals(debuggerName))
                     e.printStackTrace();
                 continue;
             }
         }
     }
 
     void initializeConnection() throws XMPPException {
         data_stream.initializeConnection();
     }
 
     /*
      * Called when the XMPP stream is reset, usually due to successful
      * authentication.
      */
     void streamReset() throws XMPPException
     {
         this.data_stream.streamReset();
     }
 
     public boolean isUsingCompression() {
         return data_stream.isUsingCompression();
     }
 
     /**
      * Establishes a connection to the XMPP server and performs an automatic login
      * only if the previous connection state was logged (authenticated). It basically
      * creates and maintains a socket connection to the server.<p>
      * <p/>
      * Listeners will be preserved from a previous connection if the reconnection
      * occurs after an abrupt termination.
      *
      * @throws XMPPException if an error occurs while trying to establish the connection.
      *      Two possible errors can occur which will be wrapped by an XMPPException --
      *      UnknownHostException (XMPP error code 504), and IOException (XMPP error code
      *      502). The error codes and wrapped exceptions can be used to present more
      *      appropiate error messages to end-users.
      */
     public void connect() throws XMPPException {
         packetReader.assertNotInThread();
 
         // If we're already connected, or if we've disconnected but havn't yet cleaned
         // up, shut down.
         shutdown();
 
         // Establishes the connection, readers and writers
         connectUsingConfiguration();
         // Automatically makes the login if the user was previouslly connected successfully
         // to the server and the connection was terminated abruptly
         if (connected && wasAuthenticated) {
             // Make the login
             try {
                 if (isAnonymous()) {
                     // Make the anonymous login
                     loginAnonymously();
                 }
                 else {
                     login(config.getUsername(), config.getPassword(),
                             config.getResource());
                 }
                 notifyReconnection();
             }
             catch (XMPPException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Sets whether the connection has already logged in the server.
      *
      * @param wasAuthenticated true if the connection has already been authenticated.
      */
     private void setWasAuthenticated(boolean wasAuthenticated) {
         if (!this.wasAuthenticated) {
             this.wasAuthenticated = wasAuthenticated;
         }
     }
 
     /** Read a single packet from the stream.  Used by PacketReader. */
     protected Element readPacket() throws InterruptedException, XMPPException {
         return data_stream.readPacket();
     }
 
     /** Write a list of packets to the stream.  Used by PacketWriter. */
     protected void writePacket(Collection<Packet> packets) throws IOException {
         StringBuffer data = new StringBuffer();
         for(Packet packet: packets)
             data.append(packet.toXML());
 
         data_stream.writePacket(data.toString());
     }
 
     /**
      * Sends a notification indicating that the connection was reconnected successfully.
      */
     protected void notifyReconnection() {
         // Notify connection listeners of the reconnection.
         for (ConnectionListener listener: getConnectionListeners()) {
             try {
                 listener.reconnectionSuccessful();
             }
             catch (Exception e) {
                 // Catch and print any exception so we can recover
                 // from a faulty listener
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Sends a notification indicating that the connection was closed gracefully.
      */
     protected void notifyConnectionClosed() {
         for (ConnectionListener listener: getConnectionListeners()) {
             try {
                 listener.connectionClosed();
             }
             catch (Exception e) {
                 // Catch and print any exception so we can recover
                 // from a faulty listener and finish the shutdown process
                 e.printStackTrace();
             }
         }
     }
 
     protected void notifyConnectionClosedOnError(Exception e) {
         for (ConnectionListener listener: getConnectionListeners()) {
             try {
                 listener.connectionClosedOnError(e);
             }
             catch (Exception e2) {
                 // Catch and print any exception so we can recover
                 // from a faulty listener
                 e2.printStackTrace();
             }
         }
     }
 
     /**
      * Called by PacketReader when an error occurs after startup() returns successfully.
      *
      * @param error the exception that caused the connection close event.
      */
     protected void readerThreadException(Exception error) {
         // If errors are being suppressed, do nothing.  This happens during shutdown().
         synchronized(this) {
             if(suppressConnectionErrors)
                 return;
 
             // Only send one connection error.
             suppressConnectionErrors = true;
         }
 
         // Print the stack trace to help catch the problem.  Include the current
         // stack in the output.
         new Exception(error).printStackTrace();
 
         boolean wasConnected;
 
         // PacketReader.startup() has returned, so it's guaranteed that
         // connectUsingConfiguration will send out connection or reconnection
         // notifications and set connected = true.  If that hasn't happened
         // yet, wait for it, so we never send a disconnected event before its
         // corresponding connect event.
         synchronized(this) {
             while(!readyForDisconnection) {
                 try {
                     this.wait();
                 } catch(InterruptedException e) {
                     Thread.currentThread().interrupt();
                 }
             }
 
             wasConnected = connected;
             connected = false;
         }
 
         // Shut down the data stream.  shutdown() must be called to complete shutdown;
         // we're running under the reader thread, which shutdown() shuts down, so we
         // can't do that from here.  It's the responsibility of the user.
         this.data_stream.disconnect();
 
         // If we're the one that cleared connected, it's our job to notify about the
         // disconnection.
         if(wasConnected)
             notifyConnectionClosedOnError(error);
     }
 }
