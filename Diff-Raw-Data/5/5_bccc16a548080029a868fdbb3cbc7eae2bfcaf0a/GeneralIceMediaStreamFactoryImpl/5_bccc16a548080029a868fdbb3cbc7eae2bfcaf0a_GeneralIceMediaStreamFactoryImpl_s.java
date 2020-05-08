 package org.lastbamboo.common.ice;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import org.apache.mina.common.IoHandler;
 import org.apache.mina.common.IoServiceListener;
 import org.apache.mina.filter.codec.ProtocolCodecFactory;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidateGatherer;
 import org.lastbamboo.common.ice.candidate.IceCandidateGathererImpl;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairFactory;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairFactoryImpl;
 import org.lastbamboo.common.ice.transport.IceTcpConnector;
 import org.lastbamboo.common.ice.transport.IceUdpConnector;
 import org.lastbamboo.common.stun.client.StunClient;
 import org.lastbamboo.common.stun.stack.StunDemuxableProtocolCodecFactory;
 import org.lastbamboo.common.stun.stack.StunIoHandler;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 import org.lastbamboo.common.stun.stack.message.StunMessageVisitorFactory;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTracker;
 import org.lastbamboo.common.stun.stack.transaction.StunTransactionTrackerImpl;
 import org.lastbamboo.common.tcp.frame.TcpFrameCodecFactory;
 import org.lastbamboo.common.turn.client.StunTcpFrameTurnClientListener;
 import org.lastbamboo.common.turn.client.TcpTurnClient;
 import org.lastbamboo.common.turn.client.TurnClientListener;
 import org.lastbamboo.common.turn.client.TurnStunDemuxableProtocolCodecFactory;
 import org.lastbamboo.common.upnp.UpnpManager;
 import org.lastbamboo.common.util.mina.DemuxableProtocolCodecFactory;
 import org.lastbamboo.common.util.mina.DemuxingIoHandler;
 import org.lastbamboo.common.util.mina.DemuxingProtocolCodecFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Factory for creating media streams.  This factory offers a more complex
  * API intended for specialized ICE implementations of the simpler
  * {@link IceMediaStreamFactory} interface to use behind the scenes.
  */
 public class GeneralIceMediaStreamFactoryImpl 
     implements GeneralIceMediaStreamFactory
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     
     public <T> IceMediaStream newIceMediaStream(
         final IceMediaStreamDesc streamDesc, final IceAgent iceAgent, 
         final DemuxableProtocolCodecFactory protocolCodecFactory, 
         final Class<T> protocolMessageClass, 
         final IoHandler udpProtocolIoHandler,
         final TurnClientListener delegateTurnClientListener,
         final UpnpManager upnpManager, 
         final IoServiceListener udpServiceListener) 
         throws IceTcpConnectException, IceUdpConnectException
         {
         final DemuxableProtocolCodecFactory stunCodecFactory =
             new StunDemuxableProtocolCodecFactory();
         final ProtocolCodecFactory demuxingCodecFactory = 
             new DemuxingProtocolCodecFactory(
                 stunCodecFactory, protocolCodecFactory);
         final StunTransactionTracker<StunMessage> transactionTracker =
             new StunTransactionTrackerImpl();
 
         final IceStunCheckerFactory checkerFactory =
             new IceStunCheckerFactoryImpl(transactionTracker);
         
         final StunMessageVisitorFactory messageVisitorFactory =
             new IceStunConnectivityCheckerFactoryImpl<StunMessage>(iceAgent, 
                 transactionTracker, checkerFactory);
         final IoHandler stunIoHandler = 
             new StunIoHandler<StunMessage>(messageVisitorFactory);
         final IoHandler udpIoHandler = 
             new DemuxingIoHandler<StunMessage, T>(
                 StunMessage.class, stunIoHandler, protocolMessageClass, 
                 udpProtocolIoHandler);
 
         final StunClient udpStunPeer;
         if (streamDesc.isUdp())
             {
             try
                 {
                 udpStunPeer = 
                     new IceStunUdpPeer(demuxingCodecFactory, udpIoHandler,
                         iceAgent.isControlling(), transactionTracker);
                 }
             catch (final IOException e)
                 {
                 // Note the constructor of the peer attempts a connection, so
                 // this is named correctly.
                 m_log.warn("Error connecting UDP peer", e);
                 throw new IceUdpConnectException("Could not create UDP peer", e);
                 }
             udpStunPeer.addIoServiceListener(udpServiceListener);
             }
         else
             {
             udpStunPeer = null;
             }
         
         // This class just decodes the TCP frames.
 
         final IceStunTcpPeer tcpStunPeer;
         if (streamDesc.isTcp())
             {
             final TurnClientListener turnClientListener =
                 new StunTcpFrameTurnClientListener(messageVisitorFactory, 
                     delegateTurnClientListener);
             
             final DemuxableProtocolCodecFactory tcpFramingCodecFactory =
                 new TcpFrameCodecFactory();
             
             final TurnStunDemuxableProtocolCodecFactory mapper = 
                 new TurnStunDemuxableProtocolCodecFactory();
             final ProtocolCodecFactory codecFactory = 
                 new DemuxingProtocolCodecFactory(mapper, 
                     tcpFramingCodecFactory);
             
             // We only start a TURN client on the answerer to save resources --
             // handled internally -- see IceStunTcpPeer connect.
             final StunClient tcpTurnClient = 
                 new TcpTurnClient(turnClientListener, codecFactory);
                 
             tcpStunPeer = 
                 new IceStunTcpPeer(tcpTurnClient, messageVisitorFactory, 
                     iceAgent.isControlling(), upnpManager);
             }
         else
             {
             tcpStunPeer = null;
             }
         
         final IceCandidateGatherer gatherer =
             new IceCandidateGathererImpl(tcpStunPeer, udpStunPeer, 
                 iceAgent.isControlling(), streamDesc);
         
         final IceMediaStreamImpl stream = new IceMediaStreamImpl(iceAgent, 
             streamDesc, gatherer);
         
         if (tcpStunPeer != null)
             {
             tcpStunPeer.addIoServiceListener(stream);
             }
         if (udpStunPeer != null)
             {
             udpStunPeer.addIoServiceListener(stream);
             }
         
         m_log.debug("Added media stream as listener...connecting...");
         if (tcpStunPeer != null)
             {
             try
                 {
                 tcpStunPeer.connect();
                 }
             catch (final IOException e)
                 {
                 m_log.warn("Error connecting TCP peer", e);
                 throw new IceTcpConnectException("Could not create TCP peer", e);
                 }
             }
         if (udpStunPeer != null)
             {
             try
                 {
                 udpStunPeer.connect();
                 }
             catch (final IOException e)
                 {
                 // Note this will not occur because the connect at this 
                 // point is effectively a no-op -- the connection takes place
                 // immediately in the constructor.
                 m_log.warn("Error connecting UDP peer", e);
                 throw new IceUdpConnectException("Could not create UDP peer", e);
                 }
             }
         
         final Collection<IceCandidate> localCandidates = 
             gatherer.gatherCandidates();
         
         final IceUdpConnector udpConnector = 
             new IceUdpConnector(demuxingCodecFactory,
                 udpIoHandler, iceAgent.isControlling());
         udpConnector.addIoServiceListener(stream);
         udpConnector.addIoServiceListener(udpServiceListener);
         final IceTcpConnector tcpConnector =
             new IceTcpConnector(messageVisitorFactory, 
                 iceAgent.isControlling());
         tcpConnector.addIoServiceListener(stream);
         final IceCandidatePairFactory candidatePairFactory = 
             new IceCandidatePairFactoryImpl(
                 checkerFactory, udpConnector, tcpConnector);
         
         final IceCheckList checkList = 
             new IceCheckListImpl(candidatePairFactory, 
                 localCandidates);
         final ExistingSessionIceCandidatePairFactory existingSessionPairFactory =
             new ExistingSessionIceCandidatePairFactoryImpl(checkerFactory);
         final IceCheckScheduler scheduler = 
             new IceCheckSchedulerImpl(iceAgent, stream, checkList,
                 existingSessionPairFactory);
         stream.start(checkList, localCandidates, scheduler);
         return stream;
         }
     }
