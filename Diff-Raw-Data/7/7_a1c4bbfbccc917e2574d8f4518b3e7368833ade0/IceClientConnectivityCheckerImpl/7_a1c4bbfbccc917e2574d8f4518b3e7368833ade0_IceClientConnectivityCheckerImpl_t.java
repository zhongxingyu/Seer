 package org.lastbamboo.common.ice;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import org.apache.mina.common.IoSession;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidatePair;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairState;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairVisitor;
 import org.lastbamboo.common.ice.candidate.IceCandidateVisitor;
 import org.lastbamboo.common.ice.candidate.IceCandidateVisitorAdapter;
 import org.lastbamboo.common.ice.candidate.IceTcpActiveCandidate;
 import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
 import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class that performs ICE connectivity checks for a single pair of ICE 
  * candidates. 
  */
 public class IceClientConnectivityCheckerImpl 
     implements IceClientConnectivityChecker
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
 
     private final IceCandidatePair m_pair;
 
     private final IceMediaStream m_mediaStream;
 
     private final IceAgent m_iceAgent;
 
     /**
      * Creates a new checker.
      * 
      * @param iceAgent The ICE agent controller for this session.
      * @param mediaStream The high level media stream. 
      * @param pair The pair to check
      */
     public IceClientConnectivityCheckerImpl(final IceAgent iceAgent,
         final IceMediaStream mediaStream, 
         final IceCandidatePair pair)
         {
         this.m_iceAgent = iceAgent;
         this.m_pair = pair;
         this.m_mediaStream = mediaStream;
         }
 
     public void check()
         {
         m_log.debug("Checking pair...");
         final IceCandidatePairVisitor<Object> visitor = 
             new ConnectPairVisitor();
         m_pair.accept(visitor);
         }
     
     private final class ConnectPairVisitor 
         implements IceCandidatePairVisitor<Object>
         {
 
         public Object visitTcpIceCandidatePair(final TcpIceCandidatePair pair)
             {
             final IceCandidate local = pair.getLocalCandidate();
             final TcpConnectCandidateVisitor visitor = 
                 new TcpConnectCandidateVisitor(pair);
             final Socket sock = local.accept(visitor);
            if (sock != null)
                {
                pair.setSocket(sock);
                pair.nominate();
                m_iceAgent.onNominatedPair(pair, m_mediaStream);
                }
             return sock;
             }
 
         public Object visitUdpIceCandidatePair(final UdpIceCandidatePair pair)
             {
             final IceCandidate local = pair.getLocalCandidate();
             final IceCandidateVisitor<IoSession> visitor = 
                 new IceUdpStunClientConnectivityChecker(m_iceAgent, m_mediaStream, pair);
             local.accept(visitor);
             return null;
             }
     
         }
     
     private final static class TcpConnectCandidateVisitor 
         extends IceCandidateVisitorAdapter<Socket>
         {
         
         private final Logger m_log = LoggerFactory.getLogger(getClass());
         
         private final IceCandidatePair m_pair;
 
         private TcpConnectCandidateVisitor(final IceCandidatePair pair)
             {
             m_pair = pair;
             }
         
         public Socket visitTcpActiveCandidate(
             final IceTcpActiveCandidate candidate)
             {
             m_log.debug("Checking active TCP candidate...");
             final IceCandidate remotePair = this.m_pair.getRemoteCandidate();
             final InetSocketAddress remote = remotePair.getSocketAddress();
             m_log.debug("Connecting to {}", remote);
             
             // TODO: We should really make sure this socket binds to the address
             // in the local candidate.
             final Socket client = new Socket();
             final InetAddress address = remote.getAddress();
             
             final int connectTimeout;
             final int icmpTimeout;
             if (address.isSiteLocalAddress())
                 {
                 // We should be able to connect to local, private addresses 
                 // really, really quickly.  So don't wait around too long.
                 connectTimeout = 3000;
                 icmpTimeout = 600;
                 }
             else
                 {
                 connectTimeout = 12000;
                 icmpTimeout = 3000;
                 }
             
             /*
             final StunClient stunClient = 
                 new TcpStunClient(remote, connectTimeout);
             
             if (stunClient.isConnected())
                 {
                 this.m_pair.setState(IceCandidatePairState.SUCCEEDED);
                 m_log.debug("Successfully connected!!");
                 return stunClient.getIoSession();
                 }
             else
                 {
                 m_log.debug("Could not connect to candidate at: {}", remote);
                 }
             this.m_pair.setState(IceCandidatePairState.FAILED);
             return null;
             */
 
             try
                 {
                 // We should be able to get an ICMP response very quickly.
                 m_log.debug("Checking if address is reachable: {}", address);
                 if (address.isReachable(icmpTimeout))
                     {
                     m_log.debug("Address is reachable. Connecting:{}", address);
                     client.connect(remote, connectTimeout);
                     this.m_pair.setState(IceCandidatePairState.SUCCEEDED);
                     m_log.debug("Successfully connected!!");
                     return client;
                     }
                 else
                     {
                     m_log.debug("The address was not reachable within " +  
                         icmpTimeout + " milliseconds...");
                     }
                 }
             catch (final IOException e)
                 {
                 m_log.debug("Could not access candidate at: {}", remote);
                 }
             this.m_pair.setState(IceCandidatePairState.FAILED);
             return null;
             }
         }
 
     }
