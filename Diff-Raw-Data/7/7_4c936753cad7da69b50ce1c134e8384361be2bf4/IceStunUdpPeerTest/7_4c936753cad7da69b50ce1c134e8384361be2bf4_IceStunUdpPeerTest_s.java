 package org.lastbamboo.common.ice;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.net.InetSocketAddress;
 import java.nio.channels.DatagramChannel;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.DatagramAcceptor;
 import org.apache.mina.transport.socket.nio.DatagramAcceptorConfig;
 import org.apache.mina.transport.socket.nio.DatagramConnector;
 import org.apache.mina.transport.socket.nio.DatagramConnectorConfig;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Test;
 import org.lastbamboo.common.ice.stubs.IceAgentStub;
 import org.lastbamboo.common.ice.stubs.IceStunCheckerFactoryStub;
 import org.lastbamboo.common.stun.client.UdpStunClient;
 import org.lastbamboo.common.stun.stack.StunIoHandler;
 import org.lastbamboo.common.stun.stack.StunProtocolCodecFactory;
 import org.lastbamboo.common.stun.stack.message.BindingErrorResponse;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.BindingSuccessResponse;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitor;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorAdapter;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
 import org.lastbamboo.common.util.NetworkUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Test sending of STUN messages between ICE STUN UDP peers.
  */
 public class IceStunUdpPeerTest 
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     private IceStunUdpPeer m_peer1;
     private IceStunUdpPeer m_peer2;
     
     
     @After public void close()
         {
         if (this.m_peer1 != null)
             {
             this.m_peer1.close();
             }
         if (this.m_peer2 != null)
             {
             this.m_peer2.close();
             }
         }
     
     /**
      * Tests BOTH the client and server side of STUN UDP peers.
      * 
      * @throws Exception If any unexpected error occurs.
      */
     @Test
     public void testIceStunUdpPeers() throws Exception
         {
         final IceAgent iceAgent = new IceAgentStub();
         final StunTransactionTracker<StunMessage> tracker = 
             new StunTransactionTrackerImpl();
         final StunMessageVisitorFactory messageVisitorFactory =
             new IceUdpStunConnectivityCheckerFactory(iceAgent, 
                 tracker, new IceStunCheckerFactoryStub());
             
         this.m_peer1 = new IceStunUdpPeer(messageVisitorFactory, true);
         this.m_peer2 = new IceStunUdpPeer(messageVisitorFactory, true);
         m_peer1.connect();
         m_peer2.connect();
         
         final InetSocketAddress address1 = m_peer1.getHostAddress();
         final InetSocketAddress address2 = m_peer2.getHostAddress();
         
         Assert.assertFalse(address1.equals(address2));
         m_log.debug("Sending STUN request to: "+address2);
         
         final StunMessageVisitor<InetSocketAddress> visitor = 
             new StunMessageVisitorAdapter<InetSocketAddress>()
             {
             
             @Override
             public InetSocketAddress visitBindingSuccessResponse(
                 final BindingSuccessResponse response)
                 {
                 return response.getMappedAddress();
                 }
 
             public InetSocketAddress visitBindingErrorResponse(
                 final BindingErrorResponse response)
                 {
                 return null;
                 }
             };
 
         // We use separate clients here because the ICE STUN peers aren't 
         // designed to receive incoming Binding Requests on their connected
         // ports -- the separate connectivity checkers take care of that.
         final UdpStunClient stunClient1 = new UdpStunClient();
         final UdpStunClient stunClient2 = new UdpStunClient();
 
         stunClient1.connect();
         stunClient2.connect();
         final InetSocketAddress localAddress1 = stunClient1.getHostAddress();
         final InetSocketAddress localAddress2 = stunClient2.getHostAddress();
         assertNotNull(localAddress1);
         assertNotNull(localAddress2);
         
         for (int i = 0; i < 4; i++)
             {
            final StunMessage msg1 = stunClient1.write(new BindingRequest(), address2);
             final InetSocketAddress mappedAddress1 = msg1.accept(visitor);
             Assert.assertEquals("Mapped address should equal the local address", 
                 localAddress1, mappedAddress1);
             
            final StunMessage msg2 = stunClient2.write(new BindingRequest(), address1);
             final InetSocketAddress mappedAddress2 = msg2.accept(visitor);
             Assert.assertEquals("Mapped address should equal the local address", 
                 localAddress2, mappedAddress2);
             }
         }
     
     /**
      * This test really just tests Java UDP handling and MINA UDP handling
      * with respect to SO_REUSEADDRESS.  Basically, we bind a server to a local
      * port and bind a bunch of UDP "clients" to the same port.  Those clients
      * are "connected" to remote hosts just using the connect() method of 
      * {@link DatagramChannel} under the covers of MINA.  We make sure that
      * any messages coming from those remote hosts are sent to the connected
      * clients and not to the server.  We then check to make sure messages
      * from random external clients go to the server -- the 
      * {@link DatagramChannel} that's just bound but hasn't had connect() 
      * called on it.
      *  
      * @throws Exception If any unexpected error occurs.
      */
     @Test
     public void testUdpConnectingAndBinding() throws Exception
         {
         final AtomicInteger serverRequestsReceived = new AtomicInteger(0);
         final int expectedServerMessages = 60;
         final StunMessageVisitorFactory serverVisitorFactory =
             new StunMessageVisitorFactory<StunMessage, IceMediaStream>()
             {
 
             public StunMessageVisitor<StunMessage> createVisitor(
                 final IoSession session)
                 {
                 final StunMessageVisitor<StunMessage> clientVisitor = 
                     new StunMessageVisitorAdapter<StunMessage>()
                     {
                     public StunMessage visitBindingRequest(
                         final BindingRequest request)
                         {
                         serverRequestsReceived.incrementAndGet();
                         if (serverRequestsReceived.get() == expectedServerMessages)
                             {
                             synchronized (serverRequestsReceived)
                                 {
                                 serverRequestsReceived.notify();
                                 }
                             }
                         return null;
                         }
                     };
                 return clientVisitor;
                 }
 
             public StunMessageVisitor<StunMessage> createVisitor(
                 final IoSession session, final IceMediaStream attachment)
                 {
                 return createVisitor(session);
                 }
             };
         
         final AtomicInteger clientRequestsReceived = new AtomicInteger(0);
         final int expectedClientMessages = 300;
         final StunMessageVisitorFactory clientVisitorFactory =
             new StunMessageVisitorFactory<StunMessage, IceMediaStream>()
             {
             public StunMessageVisitor<StunMessage> createVisitor(IoSession session)
                 {
                 final StunMessageVisitor<StunMessage> clientVisitor = 
                     new StunMessageVisitorAdapter<StunMessage>()
                     {
                     public StunMessage visitBindingRequest(final BindingRequest request)
                         {
                         clientRequestsReceived.incrementAndGet();
                         if (clientRequestsReceived.get() == expectedClientMessages)
                             {
                             synchronized (clientRequestsReceived)
                                 {
                                 clientRequestsReceived.notify();
                                 }
                             }
                         return null;
                         }
                     };
                 return clientVisitor;
                 }
 
             public StunMessageVisitor<StunMessage> createVisitor(
                 final IoSession session, final IceMediaStream attachment)
                 {
                 return createVisitor(session);
                 }
             };
 
         final InetSocketAddress boundAddress = 
             createServer(serverVisitorFactory);
         
         final Collection<InetSocketAddress> remoteAddresses = 
             createRemoteAddresses(42548);
         
         final IoHandler handler = new StunIoHandler(clientVisitorFactory);
         
         for (final InetSocketAddress remoteAddress : remoteAddresses)
             {
             // This binds the sessions and "connects" them to the remote host.
             createClientSession(boundAddress, remoteAddress, handler);
             }
         
         // Now create sessions from the remote host to the localhost and
         // check to see the "connected" UDP client receives the traffic, not
         // the acceptor/server.  Note the handler is irrelevant here.
         final Collection<IoSession> remoteSessions = 
             createRemoteSessions(remoteAddresses, boundAddress, handler);
 
         for (final IoSession remoteSession : remoteSessions)
             {
             for (int i = 0; i < 10; i++)
                 {
                 remoteSession.write(new BindingRequest());
                 }
             }
 
         // Make sure the clients got all the messages.
         synchronized (clientRequestsReceived)
             {
             if (clientRequestsReceived.get() < expectedClientMessages)
                 {
                 clientRequestsReceived.wait(3000);
                 }
             }
         
         Assert.assertEquals("Client did not receive expected messages", 
             clientRequestsReceived.get(), expectedClientMessages);
         
         // Now make sure that out of all the messages we just sent, none went
         // to the acceptor.
         Assert.assertTrue("Server received the message!!", 
             serverRequestsReceived.get() == 0);
         
 
         // Now make sure sending from a bunch of remote hosts from other ports
         // reaches the acceptor, not the clients.
         final Collection<InetSocketAddress> randomRemoteAddresses = 
             createRemoteAddresses(6321);
         final Collection<IoSession> randomRemoteSessions = 
             createRemoteSessions(randomRemoteAddresses, boundAddress, handler);
         
         for (final IoSession remoteSession : randomRemoteSessions)
             {
             for (int i = 0; i < 2; i++)
                 {
                 remoteSession.write(new BindingRequest());
                 }
             }
         
         synchronized (serverRequestsReceived)
             {
             if (serverRequestsReceived.get() < expectedServerMessages)
                 {
                 serverRequestsReceived.wait(8000);
                 }
             }
         // Now make sure the server **DID** receive the message.
         Assert.assertEquals("ERROR: server DID NOT receive the expected messages!!", 
             expectedServerMessages, serverRequestsReceived.get());
         
         // Make sure the number of client messages hasn't changed.
         Assert.assertEquals("Client did not receive expected messages", 
             clientRequestsReceived.get(), expectedClientMessages);
         }
     
     
     private Collection<IoSession> createRemoteSessions(
         final Collection<InetSocketAddress> remoteAddresses, 
         final InetSocketAddress boundAddress, 
         final IoHandler handler) throws Exception
         {
         final Collection<IoSession> remoteSessions = 
             new LinkedList<IoSession>();
         for (final InetSocketAddress remoteAddress : remoteAddresses)
             {
             final IoSession remoteSession = 
                 createClientSession(remoteAddress, boundAddress, handler);
             remoteSessions.add(remoteSession);
             }
         return remoteSessions;
         }
 
 
     private Collection<InetSocketAddress> createRemoteAddresses(
         final int startPort) throws Exception
         {
         final LinkedList<InetSocketAddress> addresses = 
             new LinkedList<InetSocketAddress>();
         for (int i = 0; i < 30; i++)
             {
             final InetSocketAddress remote = 
                 new InetSocketAddress(NetworkUtils.getLocalHost(), startPort+i);
             addresses.add(remote);
             }
 
         return addresses;
         }
 
 
     private InetSocketAddress createServer(
         final StunMessageVisitorFactory serverVisitorFactory) throws Exception
         {
         final int port = 47382;
         final InetSocketAddress boundAddress = 
             new InetSocketAddress(NetworkUtils.getLocalHost(), port);
         final DatagramAcceptor acceptor = new DatagramAcceptor();
         final DatagramAcceptorConfig config = new DatagramAcceptorConfig();
         config.getSessionConfig().setReuseAddress(true);
         
         final ProtocolCodecFactory codecFactory = 
             new StunProtocolCodecFactory();
         final ProtocolCodecFilter codecFilter = 
             new ProtocolCodecFilter(codecFactory);
         config.getFilterChain().addLast("stunFilter", codecFilter);
         final IoHandler handler = new StunIoHandler(serverVisitorFactory);
         
         acceptor.bind(boundAddress, handler, config);
         return boundAddress;
         }
     
     private IoSession createClientSession(final InetSocketAddress localAddress, 
         final InetSocketAddress remoteAddress, 
         final IoHandler ioHandler) throws Exception
         {
         final DatagramConnector connector = new DatagramConnector();
         final DatagramConnectorConfig cfg = connector.getDefaultConfig();
         cfg.getSessionConfig().setReuseAddress(true);
         final ProtocolCodecFactory codecFactory = 
             new StunProtocolCodecFactory();
         final ProtocolCodecFilter stunFilter = 
             new ProtocolCodecFilter(codecFactory);
         
         connector.getFilterChain().addLast("stunFilter", stunFilter);
         final ConnectFuture cf = 
             connector.connect(remoteAddress, localAddress, ioHandler);
         cf.join();
         final IoSession session = cf.getSession();
         return session;
         }
     }
