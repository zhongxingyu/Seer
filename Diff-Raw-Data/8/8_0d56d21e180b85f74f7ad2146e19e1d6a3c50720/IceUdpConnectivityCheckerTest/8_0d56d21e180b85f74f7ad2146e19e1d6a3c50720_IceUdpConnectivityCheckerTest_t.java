 package org.lastbamboo.common.ice;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidatePair;
 import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
 import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
 import org.lastbamboo.common.ice.stubs.IceAgentStub;
 import org.lastbamboo.common.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import junit.framework.TestCase;
 
 
 public class IceUdpConnectivityCheckerTest extends TestCase
     {
 
    private final Logger m_log = LoggerFactory.getLogger(getClass());
    
     public void testCheckAgainstPublicServer() throws Exception
         {
         final IceAgent iceAgent = new IceAgentStub();
         final IceMediaStreamDesc desc = 
             new IceMediaStreamDesc(false, true, "message", "http", 1);
         
         // This creates a local STUN server.
         final IceMediaStream iceMediaStream = 
             new IceMediaStreamImpl(iceAgent, desc, null);
         
         final InetSocketAddress hostAddress =
             new InetSocketAddress(NetworkUtils.getLocalHost(), 8459);
         final IceCandidate localCandidate = 
             new IceUdpHostCandidate(hostAddress, true);
         final InetAddress stunServer = InetAddress.getByName("stun.xten.net");
         final InetSocketAddress serverReflexiveAddress =
             new InetSocketAddress(stunServer, 3478);
         final IceCandidate remoteCandidate = 
             new IceUdpServerReflexiveCandidate(serverReflexiveAddress, 
                 localCandidate, stunServer, true);
         
        m_log.debug("About to create pair");
         final IceCandidatePair udpPair = 
             new UdpIceCandidatePair(localCandidate, remoteCandidate);
         final IceUdpConnectivityChecker checker = 
             new IceUdpConnectivityChecker(iceAgent, iceMediaStream, udpPair);
         
         
         localCandidate.accept(checker);
         
         Thread.sleep(6000);
         }
     }
