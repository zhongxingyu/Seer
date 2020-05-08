 package org.lastbamboo.common.ice.sdp;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.lastbamboo.common.ice.IceTransportProtocol;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidateType;
 import org.lastbamboo.common.ice.candidate.IceTcpHostPassiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceTcpRelayPassiveCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpHostCandidate;
 import org.lastbamboo.common.ice.candidate.IceUdpServerReflexiveCandidate;
 import org.lastbamboo.common.sdp.api.Attribute;
 import org.lastbamboo.common.sdp.api.MediaDescription;
 import org.lastbamboo.common.sdp.api.SessionDescription;
 import org.lastbamboo.common.util.NetworkUtils;
 
 /**
  * Test for the class for generating SDP data.
  */
 public final class IceCandidateSdpEncoderTest extends TestCase
     {
 
     /**
      * Logger for this class.
      */
     private static final Log LOG = 
         LogFactory.getLog(IceCandidateSdpEncoderTest.class);
 
     /**
      * Tests the method for creating SDP for the local host.
      * @throws Exception If any unexpected error occurs.
      */
     public void testCreateSdp() throws Exception
         {
         
         final org.lastbamboo.common.sdp.api.SdpFactory sdpFactory = 
             org.lastbamboo.common.sdp.api.SdpFactory.getInstance();
         
         final IceCandidateSdpEncoder encoder = 
             new IceCandidateSdpEncoder("message", "http");
         
         final InetAddress stunServerAddress = 
             InetAddress.getByName("23.42.4.96");
         final InetSocketAddress sa1 = 
             new InetSocketAddress("46.2.62.1", 5466);
         final InetSocketAddress sa2 = 
             new InetSocketAddress("12.12.32.1", 4232);
         final InetSocketAddress sa3 = 
             new InetSocketAddress("192.168.1.3", 7652);
         
        //final InetAddress relatedAddress = InetAddress.getByName("42.12.32.1");
        //final int relatedPort = 4728;
         final InetAddress relayRelatedAddress = 
             InetAddress.getByName("97.12.82.13");
         final int relayRelatedPort = 8768;
         
         
         final InetSocketAddress hostSocketAddress = 
             new InetSocketAddress(NetworkUtils.getLocalHost(), 3124);
         final IceCandidate baseCandidate = 
             new IceUdpHostCandidate(hostSocketAddress, false);
         final IceUdpServerReflexiveCandidate udpServerReflexiveCandidate = 
             new IceUdpServerReflexiveCandidate(sa1, baseCandidate, 
                 stunServerAddress, false);
         
         final IceTcpRelayPassiveCandidate tcpRelayPassiveCandidate =
             new IceTcpRelayPassiveCandidate(sa2, 
                 stunServerAddress, relayRelatedAddress, relayRelatedPort, false);
 
         final IceTcpHostPassiveCandidate tcpHostPassiveCandidate =
             new IceTcpHostPassiveCandidate(sa3, false);
         
         final Collection<IceCandidate> candidates = 
             new LinkedList<IceCandidate>();
         
         candidates.add(udpServerReflexiveCandidate);
         candidates.add(tcpRelayPassiveCandidate);
         candidates.add(tcpHostPassiveCandidate);
         encoder.visitCandidates(candidates);
         final byte[] sdpBytes = encoder.getSdp();
         
         final SessionDescription sdp = 
             sdpFactory.createSessionDescription(new String(sdpBytes));
         
         final Collection mediaDescriptions = sdp.getMediaDescriptions(true);
         
         // There should be 3 media descriptions -- one for UDP, and two for
         // TCP (TURN and local).
         assertEquals(1, mediaDescriptions.size());
         
         final Iterator iter = mediaDescriptions.iterator();
         final MediaDescription md = (MediaDescription) iter.next();
         
         final Vector attributes = md.getAttributes(false);
         final Iterator attributesIter = attributes.iterator();
         
         // The order these are encoded in will likely change in the future!!
         final Attribute udpAttribute = (Attribute) attributesIter.next();
         final Attribute tcpRelayAttribute = (Attribute) attributesIter.next();
         final Attribute tcpHostAttribute = (Attribute) attributesIter.next();
         
         // Just create a collection with one element for the UDP test.
         final Collection<InetSocketAddress> udpBindings = 
             new HashSet<InetSocketAddress>();
         udpBindings.add(sa1);
         final Collection<InetSocketAddress> tcpBindings = 
             new HashSet<InetSocketAddress>();
         tcpBindings.add(sa2);
         final Collection<InetSocketAddress> localTcpBindings = 
             new HashSet<InetSocketAddress>();
         localTcpBindings.add(sa3);
 
         verifyCandidates(udpAttribute, udpBindings, 
             IceTransportProtocol.UDP.getName());
         verifyCandidates(tcpRelayAttribute, tcpBindings, 
             IceTransportProtocol.TCP_PASS.getName());
         verifyCandidates(tcpHostAttribute, localTcpBindings, 
             IceTransportProtocol.TCP_PASS.getName(), 8);
         }
 
     private void verifyCandidates(Attribute attribute, 
         final Collection<InetSocketAddress> bindings, 
         final String transport, final int numElements) throws Exception
         {
         int numCandidates = 0;
         LOG.trace("Testing attribute: "+attribute);
         if (!attribute.getName().startsWith("candidate"))
             {
             fail("Bad candidate: "+attribute);
             return;
             }
         numCandidates++;
         final StringTokenizer st = 
             new StringTokenizer(attribute.getValue(), " ");
         
         assertEquals(numElements, st.countTokens());
         
         // This will just be the foundation string.
         st.nextToken();
         
         // Just parse the component ID.
         assertTrue(NumberUtils.isNumber(st.nextToken()));
         
         assertEquals(transport, st.nextToken());
         assertTrue(NumberUtils.isNumber(st.nextToken()));
         
         final InetAddress address = InetAddress.getByName(st.nextToken());
         final int port = Integer.parseInt(st.nextToken());
         final InetSocketAddress socketAddress = 
             new InetSocketAddress(address, port);
         
         assertTrue("Address "+socketAddress+" not in: "+bindings, 
             bindings.contains(socketAddress));
         
         final String typeToken = st.nextToken();
         assertEquals("typ", typeToken);
         
         final String typeString = st.nextToken();
         
         // Just make sure it's there.
         final IceCandidateType type = IceCandidateType.toType(typeString);
         assertNotNull(type);
         
         if (st.hasMoreElements())
             {
             final String raddr = st.nextToken();
             assertEquals("raddr", raddr);
             // Just make sure this doesn't throw an exception.
             InetAddress.getByName(st.nextToken());
             
             final String rport = st.nextToken();
             assertEquals("rport", rport);
             assertTrue(NumberUtils.isNumber(st.nextToken()));
             }
         
         assertEquals(bindings.size(), numCandidates);
         }
 
     /**
      * Verifies that the candidates listed in the given media description
      * match the expected candidate addresses.
      * @param attribute The media description to check.
      * @param bindings The expected candidate bindings.
      * @param transport The transport for the candidate, such as TCP or UDP. 
      */
     private void verifyCandidates(final Attribute attribute, 
         final Collection<InetSocketAddress> bindings, final String transport) 
         throws Exception
         {
         verifyCandidates(attribute, bindings, transport, 12);
         }
     }
