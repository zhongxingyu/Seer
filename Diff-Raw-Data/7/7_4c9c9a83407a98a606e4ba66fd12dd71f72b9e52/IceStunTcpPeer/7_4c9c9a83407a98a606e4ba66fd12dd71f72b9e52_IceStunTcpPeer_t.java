 package org.lastbamboo.common.ice;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoService;
 import org.apache.mina.common.IoServiceConfig;
 import org.apache.mina.common.IoServiceListener;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.lastbamboo.common.stun.client.StunClient;
 import org.lastbamboo.common.stun.server.StunServer;
 import org.lastbamboo.common.stun.server.TcpStunServer;
 import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
 import org.lastbamboo.common.stun.stack.StunIoHandler;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.tcp.frame.TcpFrame;
 import org.lastbamboo.common.tcp.frame.TcpFrameCodecFactory;
 import org.lastbamboo.common.tcp.frame.TcpFrameIoHandler;
 import org.lastbamboo.common.upnp.UpnpManager;
 import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
 import org.lastbamboo.common.util.mina.DemuxingIoHandler;
 import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * ICE STUN peer for TCP.  This creates both the client and the server side
  * of STUN, reusing the same local port for both.
  *  
  * @param <T> The type returned by message visiting classes.
  */
 public class IceStunTcpPeer<T> implements StunClient, StunServer, 
     IoServiceListener
     {
     
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     private final StunClient m_stunClient;
     private final StunServer m_stunServer;
     private final IoHandler m_streamIoHandler = new TcpFrameIoHandler();
     private final boolean m_controlling;
     private final UpnpManager m_upnpManager;
     
     /**
      * Creates a new ICE STUN UDP peer.
      * 
      * @param tcpStunClient The TCP STUN client side handler.
      * @param messageVisitorFactory The factory for creating message visitors
      * on the server. 
      * @param controlling Whether or not this agent is controlling.
      * @param upnpManager The class for managing UPNP.
      */
     public IceStunTcpPeer(final StunClient tcpStunClient, 
         final StunMessageVisitorFactory messageVisitorFactory,
         final boolean controlling, final UpnpManager upnpManager)
         {
         m_stunClient = tcpStunClient;
         this.m_controlling = controlling;
         this.m_upnpManager = upnpManager;
         
         // We also add whether we're the controlling agent for thread
         // naming here just to make log reading easier.
         final String controllingString;
         if (controlling)
             {
             controllingString = "Controlling";
             }
         else
             {
             controllingString = "Not-Controlling";
             }
         
         final DemuxableProtocolCodecFactory stunCodecFactory =
             new StunDemuxableProtocolCodecFactory();
         final DemuxableProtocolCodecFactory tcpFramingCodecFactory =
             new TcpFrameCodecFactory();
         final ProtocolCodecFactory demuxingCodecFactory = 
             new DemuxingProtocolCodecFactory(stunCodecFactory, 
                 tcpFramingCodecFactory);
         
         final IoHandler stunIoHandler =
             new StunIoHandler<T>(messageVisitorFactory);
         final IoHandler ioHandler = 
             new DemuxingIoHandler<StunMessage, TcpFrame>(StunMessage.class, 
                 stunIoHandler, TcpFrame.class, m_streamIoHandler);
         this.m_stunServer =
             new TcpStunServer(demuxingCodecFactory, ioHandler, 
                 controllingString);
         this.m_stunServer.addIoServiceListener(this);
         }
 
     public void connect()
         {
         // We only use the TURN client for non-controlling agents to save
         // resources.
         if (!this.m_controlling)
             {
             this.m_stunClient.connect();
             this.m_stunServer.start(m_stunClient.getHostAddress());
             }
         else
             {
             // This tells the server to allocate an ephemeral port.
             m_log.debug("Starting TCP STUN server on ephemeral port");
             this.m_stunServer.start(null); 
             }
         
        this.m_upnpManager.mapAddress(m_stunServer.getBoundAddress());
         }
     
     public InetSocketAddress getHostAddress()
         {
         //return this.m_stunClient.getHostAddress();
         return this.m_stunServer.getBoundAddress();
         }
 
     public InetSocketAddress getRelayAddress()
         {
         return this.m_stunClient.getRelayAddress();
         }
 
     public InetSocketAddress getServerReflexiveAddress()
         {
         return this.m_stunClient.getServerReflexiveAddress();
         }
 
     public InetAddress getStunServerAddress()
         {
         return this.m_stunClient.getStunServerAddress();
         }
 
     public StunMessage write(final BindingRequest request, 
         final InetSocketAddress remoteAddress)
         {
         return this.m_stunClient.write(request, remoteAddress);
         }
     
     public StunMessage write(final BindingRequest request, 
         final InetSocketAddress remoteAddress, final long rto)
         {
         return this.m_stunClient.write(request, remoteAddress, rto);
         }
 
     public void start()
         {
         // We've already started the server for ICE.
         }
 
     public void start(final InetSocketAddress bindAddress)
         {
         // We've already started the server for ICE.
         }
 
     public InetSocketAddress getBoundAddress()
         {
         return this.m_stunServer.getBoundAddress();
         }
 
     public void addIoServiceListener(final IoServiceListener serviceListener)
         {
         this.m_stunServer.addIoServiceListener(serviceListener);
         this.m_stunClient.addIoServiceListener(serviceListener);
         }
 
     public void close()
         {
        this.m_upnpManager.unmapAddress(this.m_stunServer.getBoundAddress());
         this.m_stunClient.close();
         this.m_stunServer.close();
         }
 
     public void serviceActivated(final IoService service, 
         final SocketAddress serviceAddress, final IoHandler handler, 
         final IoServiceConfig config)
         {
         }
 
     public void serviceDeactivated(final IoService service, 
         final SocketAddress serviceAddress, final IoHandler handler, 
         final IoServiceConfig config)
         {
         }
 
     public void sessionCreated(final IoSession session)
         {
         //session.setAttribute(TcpFrameClientIoHandler.class.getSimpleName(), 
           //  this.m_streamIoHandler);
         }
 
     public void sessionDestroyed(final IoSession session)
         {
         // TODO Auto-generated method stub
         
         }
     }
