 package org.lastbamboo.common.ice.candidate;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import junit.framework.TestCase;
 
import org.lastbamboo.common.ice.stubs.StunClientStub;
import org.lastbamboo.common.stun.client.StunClient;

 /**
  * Tests calculating foundations for ICE candidates. 
  */
 public class IceFoundationCalculatorTest extends TestCase
     {
     
     public void testCalculateFoundation() throws Exception
         {
         // Establish the bases.
         final InetSocketAddress hostAddress1 = 
             new InetSocketAddress("192.168.1.1", 2433);
         final InetSocketAddress hostAddress2 = 
             new InetSocketAddress("192.168.1.1", 33454);
         final InetSocketAddress hostAddress3 = 
             new InetSocketAddress("192.168.1.100", 33454);
         
        //final StunClient udpStunClient1 = new UdpStunClient();
        //final StunClient udpStunClient2 = new UdpStunClient();
        //final StunClient udpStunClient3 = new UdpStunClient();
        
         final IceCandidate tcpBaseCandidate1 = 
             new IceTcpHostPassiveCandidate(hostAddress1, false); 
         final IceCandidate tcpBaseCandidate2 = 
             new IceTcpHostPassiveCandidate(hostAddress2, false); 
         final IceCandidate tcpBaseCandidate3 = 
             new IceTcpHostPassiveCandidate(hostAddress3, false); 
         
         
         // Create the public addresses.
         final InetSocketAddress publicAddress1 = 
             new InetSocketAddress("92.68.1.1", 2433);
         final InetSocketAddress publicAddress2 = 
             new InetSocketAddress("92.68.1.1", 33454);
         final InetSocketAddress publicAddress3 = 
             new InetSocketAddress("92.68.1.100", 33454);
         
         // Create the STUN server addresses.
         final InetAddress stun1 = InetAddress.getByName("47.54.23.2");
         final InetAddress stun2 = InetAddress.getByName("4.4.23.2");
        final InetAddress stun3 = InetAddress.getByName("7.5.23.2");
        final StunClient stunClient1 = new StunClientStub(stun1, hostAddress1);
        final StunClient stunClient2 = new StunClientStub(stun2, hostAddress2);
        final StunClient stunClient3 = new StunClientStub(stun3, hostAddress3);
         
         final IceCandidate udpBaseCandidate1 =
             new IceUdpHostCandidate(hostAddress1, false);
         final IceCandidate udpBaseCandidate2 =
             new IceUdpHostCandidate(hostAddress2, false);
         final IceCandidate udpBaseCandidate3 =
             new IceUdpHostCandidate(hostAddress3, false);
         
         
         // Create the TCP host candidates.
         final Collection<IceCandidate> tcpHost = new LinkedList<IceCandidate>();
         tcpHost.add(new IceTcpHostPassiveCandidate(hostAddress1, false));
         tcpHost.add(new IceTcpHostPassiveCandidate(hostAddress2, false));
         tcpHost.add(new IceTcpHostPassiveCandidate(hostAddress3, false));
         runHostTests(tcpHost);
         
         final Collection<IceCandidate> udpHost = new LinkedList<IceCandidate>();
         udpHost.add(new IceUdpHostCandidate(hostAddress1, false));
         udpHost.add(new IceUdpHostCandidate(hostAddress2, false));
         udpHost.add(new IceUdpHostCandidate(hostAddress3, false));
         runHostTests(udpHost);
         
         final int relatedPort = 4729;
         
         // This is the related address for relayed candidates.
         final InetAddress mappedAddress = publicAddress1.getAddress();
         
         // Create the TCP server reflexive candidates.
         final Collection<IceCandidate> tcpSr = new LinkedList<IceCandidate>();
         tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, 
             tcpBaseCandidate1, stun1, false));
         // Base address with different port.
         tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, 
             tcpBaseCandidate2, stun1, false));
         // Base address with different IP.
         tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, 
             tcpBaseCandidate3, stun1, false));
         // Different STUN server address.
         tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress1, 
             tcpBaseCandidate1, stun2, false));
         // Different public address -- should have no effect on the foundation.
         tcpSr.add(new IceTcpServerReflexiveSoCandidate(publicAddress2, 
             tcpBaseCandidate1, stun1, false));
         runStunCandidateTests(tcpSr);
         
         // Create the UDP server reflexive candidates.
         final Collection<IceCandidate> udpSr = new LinkedList<IceCandidate>();
         udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate1, stun1, false));
         // Base address with different port.
         udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate2, stun1, false));
         // Base address with different IP.
         udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate3, stun1, false));
         // Different STUN server address.
         udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate1, stun2, false));
         // Different public address -- should have no effect on the foundation.
         udpSr.add(new IceUdpServerReflexiveCandidate(publicAddress2, 
             udpBaseCandidate1, stun1, false));
         runStunCandidateTests(udpSr);
         
         // Create the TCP relay candidates.
         final Collection<IceCandidate> tcpRelay = new LinkedList<IceCandidate>();
         tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress1, 
             stun1, mappedAddress, relatedPort, false));
         // Different STUN server address.
         tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress1, 
             stun2, mappedAddress, relatedPort, false));
         // Different public address, but same port.  Should be the same.
         tcpRelay.add(new IceTcpRelayPassiveCandidate(publicAddress2, 
             stun1, mappedAddress, relatedPort, false));
         runStunRelayCandidateTests(tcpRelay);
         
         // Create the UDP relay candidates.
         final Collection<IceCandidate> udpRelay = new LinkedList<IceCandidate>();
         udpRelay.add(new IceUdpRelayCandidate(publicAddress1, 
             stun1, mappedAddress, relatedPort, false));
         // Different STUN server address.
         udpRelay.add(new IceUdpRelayCandidate(publicAddress1, 
             stun2, mappedAddress, relatedPort, false));
         // Different public address -- should have no effect on the foundation.
         udpRelay.add(new IceUdpRelayCandidate(publicAddress2, 
             stun1, mappedAddress, relatedPort, false));
         runStunRelayCandidateTests(tcpRelay);
         
         // Create the UDP peer reflexive candidates.
         final Collection<IceCandidate> udpPeer = new LinkedList<IceCandidate>();
         udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate1, false, 4232L));
         // Base address with different port.
         udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate2, false, 4232L));
         // Base address with different IP.
         udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate3, false, 4232L));
         // Different STUN server address.
         udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress1, 
             udpBaseCandidate1, false, 4232L));
         // Different public address -- should have no effect on the foundation.
         udpPeer.add(new IceUdpPeerReflexiveCandidate(publicAddress2, 
             udpBaseCandidate1, false, 4232L));
         runStunCandidateTests(udpPeer);
         
         // Now test different candidate types to make sure they don't match.
         
         assertNoneEqual(tcpHost, udpHost);
         assertNoneEqual(tcpHost, tcpSr);
         assertNoneEqual(tcpHost, tcpRelay);
         assertNoneEqual(tcpHost, udpSr);
         assertNoneEqual(tcpHost, udpRelay);
         assertNoneEqual(tcpHost, udpPeer);
         }
 
     private void assertNoneEqual(final Collection<IceCandidate> candidates1, 
         final Collection<IceCandidate> candidates2)
         {
         int outerIndex = 0;
         for (final IceCandidate c1 : candidates1)
             {
             int innerIndex = 0;
             for (final IceCandidate c2: candidates2)
                 {
                 final String f1 = c1.getFoundation();
                 final String f2 = c2.getFoundation();
                 assertFalse("Foundations equal: "+f1+" "+f2+"\n"+
                     "outerIndex: "+outerIndex+" innerIndex: "+innerIndex, 
                     f1.equals(f2));
                 innerIndex++;
                 }
             outerIndex++;
             }
         }
     
 
     /**
      * Runs tests on calculating foundations with candidates that use a STUN
      * server.
      * 
      * @param stunCandidates The candidates to test.
      */
     private void runStunCandidateTests(
         final Collection<IceCandidate> stunCandidates)
         {
         final Iterator<IceCandidate> iter = stunCandidates.iterator();
         final IceCandidate c1 = iter.next();
         final IceCandidate c2 = iter.next();
         final IceCandidate c3 = iter.next();
         final IceCandidate c4 = iter.next();
         final IceCandidate c5 = iter.next();
         
         assertEquals(c1.getFoundation(), c2.getFoundation());
         assertFalse(c1.getFoundation() == c3.getFoundation());
         assertFalse(c1.getFoundation() == c4.getFoundation());
         assertEquals(c1.getFoundation(), c5.getFoundation());
         }
     
     private void runStunRelayCandidateTests(
         final Collection<IceCandidate> stunCandidates)
         {
         final Iterator<IceCandidate> iter = stunCandidates.iterator();
         final IceCandidate c1 = iter.next();
         final IceCandidate c2 = iter.next();
         final IceCandidate c3 = iter.next();
         //final IceCandidate c4 = iter.next();
         //final IceCandidate c5 = iter.next();
         
         assertEquals(c1.getFoundation(), c3.getFoundation());
         assertFalse(c1.getFoundation() == c2.getFoundation());
         //assertFalse(c1.getFoundation() == c3.getFoundation());
         assertFalse(c2.getFoundation() == c3.getFoundation());
         }
 
     private void runHostTests(final Collection<IceCandidate> hosts)
         {
         final Iterator<IceCandidate> iter = hosts.iterator();
         final IceCandidate c1 = iter.next();
         final IceCandidate c2 = iter.next();
         final IceCandidate c3 = iter.next();
         assertEquals(c1.getFoundation(), c2.getFoundation()); 
         assertFalse(c1.getFoundation() == c3.getFoundation());
         }
     }
