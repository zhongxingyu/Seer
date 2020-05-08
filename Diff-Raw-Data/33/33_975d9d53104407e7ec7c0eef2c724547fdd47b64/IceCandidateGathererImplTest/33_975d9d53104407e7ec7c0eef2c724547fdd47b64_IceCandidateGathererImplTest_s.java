 package org.lastbamboo.common.ice.candidate;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.apache.mina.common.IoServiceListener;
 import org.lastbamboo.common.ice.IceMediaStreamDesc;
 import org.lastbamboo.common.stun.client.StunClient;
 import org.lastbamboo.common.stun.stack.message.BindingRequest;
 import org.lastbamboo.common.stun.stack.message.StunMessage;
 
 /**
  * Tests candidate gathering.
  */
 public class IceCandidateGathererImplTest extends TestCase
     {
 
     /**
      * Tests candidate gathering.
      * 
      * @throws Exception If any unexpected error occurs.
      */
     public void testGatherCandidates() throws Exception
         {
         final StunClient turnClient = new HostAndServerReflexiveSame();
         final StunClient udpStunClient = new HostAndServerReflexiveSame();
         final IceMediaStreamDesc desc = 
             new IceMediaStreamDesc(true, true, "message", "http", 1);
         
         final IceCandidateGatherer gatherer = 
             new IceCandidateGathererImpl(turnClient, udpStunClient, true, desc);
         
         final Collection<IceCandidate> candidates = gatherer.gatherCandidates();
         
         // The gatherer should prune out the server reflexive or host candidate
         // because they are equal -- this is simulating the no firewall case.
         //assertEquals(4, candidates.size());
         }
 
     private static final class HostAndServerReflexiveSame implements StunClient
         {
         private final InetSocketAddress m_sameAddressForHostAndSrflx =
             new InetSocketAddress("172.16.1.150", 4722);
 
         public InetSocketAddress getHostAddress()
             {
             return this.m_sameAddressForHostAndSrflx;
             }
 
         public InetSocketAddress getRelayAddress()
             {
             return new InetSocketAddress("28.16.1.150", 7821);
             }
 
         public InetSocketAddress getServerReflexiveAddress()
             {
             return this.m_sameAddressForHostAndSrflx;
             }
 
         public InetAddress getStunServerAddress()
             {
             try
                 {
                 return InetAddress.getByName("48.87.5.4");
                 }
             catch (UnknownHostException e)
                 {
                 return null;
                 }
             }
 
         public StunMessage write(BindingRequest request, InetSocketAddress remoteAddress)
             {
             // TODO Auto-generated method stub
             return null;
             }
 
         public StunMessage write(BindingRequest request, 
             final InetSocketAddress remoteAddress, long rto)
             {
             // TODO Auto-generated method stub
             return null;
             }
 
         public void addIoServiceListener(IoServiceListener serviceListener)
             {
             // TODO Auto-generated method stub
             
             }
 
         public void connect()
             {
             // TODO Auto-generated method stub
             
             }
 
         public void close()
             {
             // TODO Auto-generated method stub
             
             }
     
         }
     }
