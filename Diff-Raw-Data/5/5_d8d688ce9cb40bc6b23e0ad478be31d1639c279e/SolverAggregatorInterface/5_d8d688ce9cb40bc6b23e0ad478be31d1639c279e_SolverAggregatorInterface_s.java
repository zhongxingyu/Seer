 /*
  * Owl Platform Solver-Aggregator Library for Java
  * Copyright (C) 2012 Robert Moore and the Owl Platform
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation; either version 2.1 of the License, or
  * (at your option) any later version.
  *  
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *  
  * You should have received a copy of the GNU Lesser General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package com.owlplatform.solver;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.apache.mina.core.RuntimeIoException;
 import org.apache.mina.core.future.ConnectFuture;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.executor.ExecutorFilter;
 import org.apache.mina.transport.socket.SocketConnector;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.owlplatform.common.SampleMessage;
 
 import com.owlplatform.solver.listeners.ConnectionListener;
 import com.owlplatform.solver.listeners.SampleListener;
 import com.owlplatform.solver.protocol.codec.AggregatorSolverProtocolCodecFactory;
 import com.owlplatform.solver.protocol.messages.HandshakeMessage;
 import com.owlplatform.solver.protocol.messages.SubscriptionMessage;
 import com.owlplatform.solver.rules.SubscriptionRequestRule;
 
 /**
  * A simple interface to the aggregator to be used by Java-based solvers.
  * Handles connection set-up and tear-down, as well as automatic handshaking.
  * 
  * @author Robert Moore II
  * 
  */
 public class SolverAggregatorInterface {
   /**
    * A wrapper to take hide the IOAdapter events from classes using the
    * SolverAggregatorInterface.
    * 
    * @author Robert Moore
    * 
    */
   private static final class AdapterHandler implements SolverIoAdapter {
     /**
      * The actual object that will respond to events.
      */
     private final SolverAggregatorInterface parent;
 
     /**
      * Creates a new AdapterHandler with the specified
      * {@code SolverAggregatorInterface} to actually handle events.
      * 
      * @param parent
      *          the real event handler
      */
     public AdapterHandler(SolverAggregatorInterface parent) {
       super();
       this.parent = parent;
     }
 
     @Override
     public void connectionOpened(IoSession session) {
       this.parent.connectionOpened(session);
     }
 
     @Override
     public void connectionClosed(IoSession session) {
       this.parent.connectionClosed(session);
     }
 
     @Override
     public void exceptionCaught(IoSession session, Throwable exception) {
       this.parent.exceptionCaught(session, exception);
     }
 
     @Override
     public void handshakeReceived(IoSession session,
         HandshakeMessage handshakeMessage) {
       this.parent.handshakeReceived(session, handshakeMessage);
     }
 
     @Override
     public void handshakeSent(IoSession session,
         HandshakeMessage handshakeMessage) {
       this.parent.handshakeSent(session, handshakeMessage);
     }
 
     @Override
     public void subscriptionRequestReceived(IoSession session,
         SubscriptionMessage subscriptionMessage) {
       this.parent.subscriptionResponseReceived(session, subscriptionMessage);
     }
 
     @Override
     public void subscriptionRequestSent(IoSession session,
         SubscriptionMessage subscriptionMessage) {
       this.parent.subscriptionRequestSent(session, subscriptionMessage);
     }
 
     @Override
     public void subscriptionResponseSent(IoSession session,
         SubscriptionMessage subscriptionMessage) {
       this.parent.subscriptionResponseSent(session, subscriptionMessage);
     }
 
     @Override
     public void subscriptionResponseReceived(IoSession session,
         SubscriptionMessage subscriptionMessage) {
       this.parent.subscriptionResponseReceived(session, subscriptionMessage);
     }
 
     @Override
     public void solverSampleSent(IoSession session, SampleMessage sampleMessage) {
       this.parent.solverSampleSent(session, sampleMessage);
     }
 
     @Override
     public void solverSampleReceived(IoSession session,
         SampleMessage sampleMessage) {
       this.parent.solverSampleReceived(session, sampleMessage);
     }
 
     @Override
     public void sessionIdle(IoSession session, IdleStatus idleStatus) {
       this.parent.sessionIdle(session, idleStatus);
     }
   }
 
   /**
    * The {@link HandshakeMessage} received from the aggregator.
    */
   private HandshakeMessage receivedHandshake = null;
 
   /**
    * The HandshakeMessage sent to the aggregator.
    */
   private HandshakeMessage sentHandshake = null;
 
   /**
    * The {@link SubscriptionMessage} request sent to the aggregator.
    */
   private SubscriptionMessage sentSubscription = null;
 
   /**
    * The {@code SubscriptionMessage} response received from the aggregator.
    */
   private SubscriptionMessage receivedSubscription = null;
 
   /**
    * Logging facility for this class.
    */
   private static final Logger log = LoggerFactory
       .getLogger(SolverAggregatorInterface.class);
 
   /**
    * The object that will pass events to this SolverAggregatorInterface. Used to
    * hide interface methods.
    */
   private final AdapterHandler handler = new AdapterHandler(this);
 
   /**
    * How long to wait when connecting and disconnecting from the aggregator, in
    * milliseconds.
    */
   private long connectionTimeout = 10000;
 
   /**
    * How long to wait between connection attempts to the aggregator, in
    * milliseconds.
    */
   private long connectionRetryDelay = 10000;
 
   /**
    * Whether or not to try and stay connected to the aggregator.
    */
   private boolean stayConnected = false;
 
   /**
    * Whether or not to disconnect from the aggregator if an exception is thrown.
    */
   private boolean disconnectOnException = true;
 
   /**
    * The hostname or IP address of the aggregator.
    */
   private String host;
 
   /**
    * The port number the aggregator is listening on for solvers.
    */
   private int port = 7009;
 
   /**
    * The session of the connected aggregator, or {@code null} if no connection
    * is established.
    */
   private IoSession session;
 
   /**
    * Used to connect to the aggregator.
    */
   private SocketConnector connector;
 
   /**
    * Handler for IO events.
    */
   private SolverIoHandler ioHandler;
 
   /**
    * Flag to indicate connected status.
    */
   private volatile boolean connected = false;
 
   /**
    * Returns {@code true} if the connection to the aggregator has been
    * established.
    * 
    * @return {@code true} if the connection(socket) to the aggregator is
    *         established, else {@code false}.
    */
   public boolean isConnected() {
     return this.connected;
   }
 
   /**
    * Set of subscription rules to be sent to the aggregator.
    */
   SubscriptionRequestRule[] rules = new SubscriptionRequestRule[] { SubscriptionRequestRule
       .generateGenericRule() };
 
   /**
    * Returns the set of rules that have been or will be sent to the aggregator.
    * 
    * @return the rules
    */
   public SubscriptionRequestRule[] getRules() {
     return this.rules;
   }
 
   /**
    * Sets the new rules to send to the aggregator. Changes to the rules will not
    * take effect until the next time a connection is made. If the new rule set
    * is intended to replace the current set completely, then the caller should
    * disconnect and connect to the aggregator.
    * 
    * @param rules
    *          the rules to set
    */
   public void setRules(SubscriptionRequestRule[] rules) {
     this.rules = rules;
   }
 
   /**
    * Listeners for sample received events.
    */
   protected ConcurrentLinkedQueue<SampleListener> sampleListeners = new ConcurrentLinkedQueue<SampleListener>();
 
   /**
    * Listeners for connection events.
    */
   protected ConcurrentLinkedQueue<ConnectionListener> connectionListeners = new ConcurrentLinkedQueue<ConnectionListener>();
 
   /**
    * Creates a new SolverAggregatorConnection with a SolverIoHandler as the IO
    * handler.
    */
   public SolverAggregatorInterface() {
     this.ioHandler = new SolverIoHandler(this.handler);
   }
 
   /**
    * Thread pool for handling IO events outside of the MINA IO thread.
    */
   private ExecutorFilter executors = null;
 
   /**
    * Creates a new SolverAggregatorConnection with the provided SolverIoHandler
    * as the IO handler.
    * 
    * @param ioHandler
    *          the IO handler for the connection
    */
   public SolverAggregatorInterface(SolverIoHandler ioHandler) {
     if (ioHandler == null) {
       throw new IllegalArgumentException(
           "Cannot use a null IO Handler for aggregator interface.");
     }
     this.ioHandler = ioHandler;
     this.ioHandler.setSolverIoAdapter(this.handler);
   }
 
   /**
    * Configures the socket connector and gets it ready to connect.
    * 
    * @return {@code true} if the connector was configured successfully, else
    *         {@code false}.
    */
   protected boolean setConnector() {
     if (this.host == null) {
       log.error("No host value set, cannot set up socket connector.");
       return false;
     }
     if (this.port < 0 || this.port > 65535) {
       log.error("Port value is invalid {}.", Integer.valueOf(this.port));
       return false;
     }
 
     this.executors = new ExecutorFilter(1);
 
     this.connector = new NioSocketConnector();
     this.connector.getSessionConfig().setTcpNoDelay(true);
     if (!this.connector.getFilterChain().contains(
         AggregatorSolverProtocolCodecFactory.CODEC_NAME)) {
       this.connector.getFilterChain().addLast(
           AggregatorSolverProtocolCodecFactory.CODEC_NAME,
           new ProtocolCodecFilter(new AggregatorSolverProtocolCodecFactory(
               false)));
     }
     this.connector.getFilterChain().addLast("ExecutorPool", this.executors);
     this.connector.setHandler(this.ioHandler);
     log.debug("Connector set up successful.");
     return true;
   }
 
   /**
    * Initiates a connection to the Aggregator (if it is not yet connected). If
    * {@code #stayConnected} is {@code true}, then this method will NOT return
    * until a connection is established. If callers wish to remain connected to
    * the aggregator, it is best to call {@code setStayConnected(true)} only
    * after calling this method.
    * 
    * This method has been replaced by {@link #connect(long)}, and is equivalent
    * to calling {@code #connect(0)}.
    * 
    * @return {@code true} if the connection is established, else {@code false}.
    */
   @Deprecated
   public boolean doConnectionSetup() {
     return this.connect(0);
   }
 
   /**
    * Initiates a connection to the Aggregator (if it is not yet connected). If
    * {@code #stayConnected} is {@code true}, then this method will NOT return
    * until a connection is established or the timeout is exceeded. A timeout of
    * 0 indicates an infinite timeout value.
    * 
    * @param timeout
    *          how long to wait (in milliseconds) for the connection to
    * @return {@code true} if the connection is established within the timeout
    *         period, else {@code false}.
    */
   public boolean connect(long timeout) {
     this.connectionTimeout = timeout;
     if (this.connector == null) {
       if (!this.setConnector()) {
         log.error("Unable to set up connection to the aggregator.");
         return false;
       }
     }
 
     if (this.session != null) {
       log.error("Already connected!");
       return false;
     }
 
     long waitTime = timeout;
     do {
       long startAttempt = System.currentTimeMillis();
       if (this._connect(waitTime)) {
         log.info("Connected to {}",this);
         return true;
       }
 
       if (this.stayConnected) {
         long retryDelay = this.connectionRetryDelay;
         if (timeout > 0 && timeout < this.connectionRetryDelay * 2) {
           retryDelay = timeout / 2;
           if (retryDelay < 100) {
             retryDelay = 100;
           }
         }
         try {
           log.warn(String.format(
               "Connection to %s:%d failed, waiting %dms before retrying.",
               this.host, Integer.valueOf(this.port), Long.valueOf(retryDelay)));
           Thread.sleep(retryDelay);
         } catch (InterruptedException ie) {
           // Ignored
         }
         waitTime = waitTime - (System.currentTimeMillis() - startAttempt);
       }
     } while (this.stayConnected && waitTime > 0);
     this._disconnect();
     this.finishConnection();
 
     return false;
   }
 
   /**
    * Clean-up any remaining connection-related state. Notify listeners that the
    * connection has ended.
    */
   void finishConnection() {
     this.connector.dispose();
     this.connector = null;
     for (ConnectionListener listener : this.connectionListeners) {
       listener.connectionEnded(this);
     }
     if (this.executors != null) {
       this.executors.destroy();
     }
   }
 
   /**
    * Shuts down this connection to the aggregator. This method has been replaced
    * by {@link #disconnect()}.
    */
   @Deprecated
   public void doConnectionTearDown() {
     this.disconnect();
   }
 
   /**
    * Disconnects from the aggregator if already connected.
    */
   public void disconnect() {
     // Make sure we don't automatically reconnect
     this.stayConnected = false;
     this._disconnect();
   }
 
   /**
    * Establishes a connection to the remote aggregator specified by
    * {@code #host} and {@code #port}, waiting for the {@code timeout} period
    * before giving-up, or an infinite amount of time if {@code timeout} is &le;
    * 0.
    * 
    * @param timeout
    *          the timeout period in milliseconds to wait for the connection
    * @return {@code true} if the connection is established successfully, else
    *         {@code false}.
    */
   protected boolean _connect(long timeout) {
 
     ConnectFuture connFuture = this.connector.connect(new InetSocketAddress(
         this.host, this.port));
     if (timeout > 0) {
       if (!connFuture.awaitUninterruptibly(timeout)) {
         return false;
       }
     } else {
       connFuture.awaitUninterruptibly();
 
     }
     if (!connFuture.isConnected()) {
       return false;
     }
 
     try {
       log.info("Connecting to {}:{}.", this.host, Integer.valueOf(this.port));
       this.session = connFuture.getSession();
     } catch (RuntimeIoException ioe) {
       log.error(String.format("Could not create session to aggregator %s:%d.",
           this.host, Integer.valueOf(this.port)), ioe);
       return false;
     }
     return true;
   }
 
   /**
    * Disconnects from the aggregator, destroying any sessions and executor
    * filters that are already created. Resets the connection state.
    */
   protected void _disconnect() {
 
     if (this.session != null) {
       log.debug("Closing connection to aggregator at {} (waiting {}ms).",
           this.session.getRemoteAddress(), Long.valueOf(this.connectionTimeout));
       this.session.close(false).awaitUninterruptibly(this.connectionTimeout);
       this.session = null;
       this.sentHandshake = null;
       this.receivedHandshake = null;
       this.sentSubscription = null;
       this.receivedSubscription = null;
       for (ConnectionListener listener : this.connectionListeners) {
         listener.connectionInterrupted(this);
       }
     }
   }
 
   /**
    * Called when the connection to the aggregator closes.
    * 
    * @param session
    *          the connection that closed.
    */
   protected void connectionClosed(IoSession session) {
     this.connected = false;
     this._disconnect();
     while (this.stayConnected) {
       
       try {
         Thread.sleep(this.connectionRetryDelay);
       } catch (InterruptedException ie) {
         // Ignored
       }
       log.debug("Reconnecting to aggregator at {}:{}", this.host,
           Integer.valueOf(this.port));
       if (this.connect(this.connectionTimeout)) {
         return;
       }
 
     }
     this.finishConnection();
 
   }
 
   /**
    * Called when a connection to the aggregator is opened (socket connected).
    * 
    * @param session
    *          the connection that opened.
    */
   protected void connectionOpened(IoSession session) {
 
     if (this.session == null) {
       this.session = session;
     }
 
     log.info("Connected to {}.", session.getRemoteAddress());
 
     for (ConnectionListener listener : this.connectionListeners) {
       listener.connectionEstablished(this);
     }
 
     log.debug("Attempting to write handshake.");
     this.session.write(HandshakeMessage.getDefaultMessage());
   }
 
   /**
    * Called when a handshake message is received from the aggregator.
    * 
    * @param session
    *          the session on which the message arrived.
    * @param handshakeMessage
    *          the received message.
    */
   protected void handshakeReceived(IoSession session,
       HandshakeMessage handshakeMessage) {
     log.debug("Received {}", handshakeMessage);
     this.receivedHandshake = handshakeMessage;
     Boolean handshakeCheck = this.checkHandshake();
     if (handshakeCheck == null) {
       return;
     }
 
     if (Boolean.FALSE.equals(handshakeCheck)) {
       log.warn("Handshakes did not match.");
       this._disconnect();
     }
     if (Boolean.TRUE.equals(handshakeCheck)) {
       SubscriptionMessage msg = this.generateGenericSubscriptionMessage();
       log.debug("Attempting to write {}.", msg);
       this.session.write(msg);
       this.connected = true;
     }
   }
 
   /**
    * Called when a handshake message is actually sent to the aggregator.
    * 
    * @param session
    *          the session on which the message was sent.
    * @param handshakeMessage
    *          the sent message.
    */
   protected void handshakeSent(IoSession session,
       HandshakeMessage handshakeMessage) {
     log.debug("Sent {}", handshakeMessage);
     this.sentHandshake = handshakeMessage;
     Boolean handshakeCheck = this.checkHandshake();
     if (handshakeCheck == null) {
       return;
     }
 
     if (Boolean.FALSE.equals(handshakeCheck)) {
       log.warn("Handshakes did not match.");
       this._disconnect();
     }
     if (Boolean.TRUE.equals(handshakeCheck)) {
       SubscriptionMessage msg = this.generateGenericSubscriptionMessage();
       log.debug("Attempting to write {}.", msg);
      this.session.write(msg);
       this.connected = false;
     }
   }
 
   /**
    * Creates a generic subscription message with the rules defined within this
    * interface.
    * 
    * @return the new message.
    */
   protected SubscriptionMessage generateGenericSubscriptionMessage() {
 
     SubscriptionMessage subMessage = new SubscriptionMessage();
     subMessage.setRules(this.rules);
     subMessage.setMessageType(SubscriptionMessage.SUBSCRIPTION_MESSAGE_ID);
 
     return subMessage;
   }
 
   /**
    * Called when a sample is received from the aggregator.
    * 
    * @param session
    *          the session the sample was received on.
    * @param sampleMessage
    *          the received sample.
    */
   protected void solverSampleReceived(IoSession session,
       SampleMessage sampleMessage) {
     for (SampleListener listener : this.sampleListeners) {
       listener.sampleReceived(this, sampleMessage);
     }
   }
 
   /**
    * Called when a sample was sent to the aggregator. Shouldn't happen, as it
    * would be a protocol error.
    * 
    * @param session
    *          the session on which the sample was sent.
    * @param sampleMessage
    *          the sent sample.
    */
   protected void solverSampleSent(IoSession session, SampleMessage sampleMessage) {
     log.error("Protocol error: Sent Sample message to the aggregator:\n{}",
         sampleMessage);
     this._disconnect();
   }
 
   /**
    * Called when the session becomes idle. Idle checking is not currently
    * implemented.
    * 
    * @param session
    *          the session that is idle.
    * @param idleStatus
    *          indicates which side of the session is idle.
    */
   protected void sessionIdle(IoSession session, IdleStatus idleStatus) {
     // TODO: Need to implement idle checking
   }
 
   /**
    * Called when a subscription request is received from the aggregator.
    * Shouldn't happen, since that would be a protocol error.
    * 
    * @param session
    *          the session on which the message arrived.
    * @param subscriptionMessage
    *          the received message.
    */
   protected void subscriptionRequestReceived(IoSession session,
       SubscriptionMessage subscriptionMessage) {
     log.error(
         "Protocol error: Received subscription message from the aggregator:\n{}.",
         subscriptionMessage);
     this._disconnect();
   }
 
   /**
    * Called after a subscription request message is sent to the aggregator.
    * 
    * @param session
    *          the session on which the message was sent.
    * @param subscriptionMessage
    *          the sent message.
    */
   protected void subscriptionRequestSent(IoSession session,
       SubscriptionMessage subscriptionMessage) {
     log.info("Sent {}", subscriptionMessage);
     this.sentSubscription = subscriptionMessage;
   }
 
   /**
    * Called when a subscription response is received from the aggregator.
    * 
    * @param session
    *          the session on which message was received.
    * @param subscriptionMessage
    *          the received message.
    */
   protected void subscriptionResponseReceived(IoSession session,
       SubscriptionMessage subscriptionMessage) {
     log.info("Received {}", subscriptionMessage);
     this.receivedSubscription = subscriptionMessage;
 
     if (this.sentSubscription == null) {
       log.error(
           "Protocol error: Received a subscription response without sending a request.\n{}",
           subscriptionMessage);
       this._disconnect();
       return;
     }
 
     if (!this.sentSubscription.equals(this.receivedSubscription)) {
       log.info(
           "Server did not fully accept subscription request.\nOriginal:\n{}\nAmended\n{}",
           this.sentSubscription, this.receivedSubscription);
     }
 
     for (ConnectionListener listener : this.connectionListeners) {
       listener.subscriptionReceived(this, subscriptionMessage);
     }
   }
 
   /**
    * Called when a subscription response is sent to the aggregator. Shouldn't
    * happen, since that would be a protocol violation.
    * 
    * @param session
    *          the session on which the message was sent.
    * @param subscriptionMessage
    *          the sent message.
    */
   protected void subscriptionResponseSent(IoSession session,
       SubscriptionMessage subscriptionMessage) {
     log.error(
         "Protocol error: Sent a subscription response to the aggregator.\n{}",
         subscriptionMessage);
     this._disconnect();
     return;
   }
 
   /**
    * Registers a listener to be notified of received samples.
    * 
    * @param listener
    *          the listener to register.
    */
   public void addSampleListener(SampleListener listener) {
     this.sampleListeners.add(listener);
   }
 
   /**
    * Unregisters a listener to be notified of received samples.
    * 
    * @param listener
    *          the listener to unregister.
    */
   public void removeSampleListener(SampleListener listener) {
     this.sampleListeners.remove(listener);
   }
 
   /**
    * Registers a listener to be notified of connection events.
    * 
    * @param listener
    *          the listener to register.
    */
   public void addConnectionListener(ConnectionListener listener) {
     this.connectionListeners.add(listener);
   }
 
   /**
    * Unregisters a listener to be notified of connection events.
    * 
    * @param listener
    *          the listener to unregister.
    */
   public void removeConnectionListener(ConnectionListener listener) {
     this.connectionListeners.remove(listener);
   }
 
   /**
    * Called when an exception occurs on the session
    * 
    * @param session
    *          the session on which the exception occurred
    * @param cause
    *          the exception
    */
   protected void exceptionCaught(IoSession session, Throwable cause) {
    log.error("Unhandled exception for: {}", cause);
     if (this.disconnectOnException) {
       this._disconnect();
     }
   }
 
   /**
    * Returns the current host setting.
    * 
    * @return the current host.
    */
   public String getHost() {
     return this.host;
   }
 
   /**
    * Sets the host for the aggregator to connect to. Changes to this value don't
    * have any effect until a connection attempt is made to the aggregator.
    * 
    * @param host
    *          the new host value for the aggregator.
    */
   public void setHost(String host) {
     if (host == null) {
       throw new IllegalArgumentException("Aggregator host cannot be null.");
     }
     this.host = host;
   }
 
   /**
    * Returns the configured port for the aggregator connection.
    * 
    * @return the port number.
    */
   public int getPort() {
     return this.port;
   }
 
   /**
    * Sets the port for the aggregator connection. Changes to this value don't
    * have any effect until a connection attempt is made to the aggregator.
    * 
    * @param port
    *          the new port value for the aggregator
    */
   public void setPort(int port) {
     if (port <= 0 || port > 65535) {
       throw new IllegalArgumentException(
           "Aggregator port must be a valid TCP port number.");
     }
     this.port = port;
   }
 
   /**
    * Validates the sent and received handshakes. If either is missing,
    * {@code null} is returned. If either is invalid, {@code Boolean.FALSE} is
    * returned. If both are valid, {@code Boolean.TRUE} is returned.
    * 
    * @return {@code Boolean.TRUE} if both handshakes (sent/received) are valid,
    *         {@code Boolean.FALSE} if either is invalid, and {@code null} if
    *         either is missing.
    */
   protected Boolean checkHandshake() {
     if (this.sentHandshake == null) {
       log.debug("Sent handshake is null, not checking.");
       return null;
     }
     if (this.receivedHandshake == null) {
       log.debug("Received handshake is null, not checking.");
       return null;
     }
 
     if (!this.sentHandshake.equals(this.receivedHandshake)) {
       log.error(
           "Handshakes do not match.  Closing connection to aggregator at {}.",
           this.session.getRemoteAddress());
       boolean prevValue = this.stayConnected;
       this.stayConnected = false;
       this._disconnect();
       this.stayConnected = prevValue;
       return Boolean.FALSE;
     }
     return Boolean.TRUE;
   }
 
   /**
    * Returns the current value of the connection timeout.
    * 
    * @return the current connection timeout value.
    */
   public long getConnectionTimeout() {
     return this.connectionTimeout;
   }
 
   /**
    * Sets the new value for the connection timeout. Changes to this value don't
    * have any effect until a connection attempt is made.
    * 
    * @param connectionTimeout
    *          the new connection timeout value.
    */
   public void setConnectionTimeout(long connectionTimeout) {
     this.connectionTimeout = connectionTimeout;
   }
 
   /**
    * Indicates whether a reconnection should be attempted after a disconnect
    * occurs. This setting has no effect on calls to {@link #disconnect()}.
    * 
    * @return {@code true} if the interface will automatically reconnect, else
    *         {@code false}.
    */
   public boolean isStayConnected() {
     return this.stayConnected;
   }
 
   /**
    * Sets whether or not the interface should reconnect to the aggregator after
    * a connection failure.
    * 
    * @param stayConnected
    *          {@code true} to reconnect automatically, or {@code false} to
    *          remain disconnected.
    */
   public void setStayConnected(boolean stayConnected) {
     this.stayConnected = stayConnected;
   }
 
   /**
    * Indicates whether an exception will cause a disconnect or not.
    * 
    * @return {@code true} if the interface will disconnect on exception, or
    *         {@code false} if it will not.
    */
   public boolean isDisconnectOnException() {
     return this.disconnectOnException;
   }
 
   /**
    * Set whether or not this interface should disconnect when an exception is
    * thrown.
    * 
    * @param disconnectOnException
    *          {@code true} if this interface should disconnect when an exception
    *          is thrown, or {@code false} if it should maintain the connection.
    */
   public void setDisconnectOnException(boolean disconnectOnException) {
     this.disconnectOnException = disconnectOnException;
   }
 
   /**
    * Returns the current setting for the connection retry delay (the interval between connection attempts).
    * @return the current connection retry delay, in milliseconds.
    */
   public long getConnectionRetryDelay() {
     return this.connectionRetryDelay;
   }
 
   /**
    * Sets the connection retry delay for this interface.
    * @param connectionRetryDelay the new connection retry delay, in milliseconds.
    */
   public void setConnectionRetryDelay(long connectionRetryDelay) {
     this.connectionRetryDelay = connectionRetryDelay;
   }
 
   /**
    * Returns the session in use by this interface.
    * @return the underlying session in use.
    */
   public IoSession getSession() {
     return this.session;
   }
 
   @Override
   public String toString() {
     return "Solver-Aggregator connection @" + this.host + ":" + this.port;
   }
 }
