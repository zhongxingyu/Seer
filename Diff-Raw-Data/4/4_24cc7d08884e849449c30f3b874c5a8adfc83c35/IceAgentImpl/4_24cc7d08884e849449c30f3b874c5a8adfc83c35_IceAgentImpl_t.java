 package org.lastbamboo.common.ice;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.net.Socket;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Random;
 
 import org.apache.mina.common.ByteBuffer;
 import org.lastbamboo.common.ice.candidate.IceCandidate;
 import org.lastbamboo.common.ice.candidate.IceCandidatePair;
 import org.lastbamboo.common.ice.candidate.IceCandidatePairVisitor;
 import org.lastbamboo.common.ice.candidate.TcpIceCandidatePair;
 import org.lastbamboo.common.ice.candidate.UdpIceCandidatePair;
 import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoder;
 import org.lastbamboo.common.ice.sdp.IceCandidateSdpDecoderImpl;
 import org.lastbamboo.common.stun.client.StunClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Implementation of an ICE agent.  An agent can contain multiple media 
  * streams and manages the top level of an ICE exchange. 
  */
 public class IceAgentImpl implements IceAgent, IceCandidatePairVisitor<Socket>
     {
 
     private final Logger m_log = LoggerFactory.getLogger(getClass());
     
     private boolean m_controlling;
 
     /**
      * TODO: This is just a placeholder for now for the most part, as we only
      * currently support a single media stream.
      */
     private final Collection<IceMediaStream> m_mediaStreams =
         new LinkedList<IceMediaStream>();
 
     /**
      * The tie breaker to use when both agents think they're controlling.
      */
     private final byte[] m_tieBreaker;
 
     private final IceCandidateSdpDecoder m_iceCandidateDecoder;
 
     private final IceMediaStream m_mediaStream;
 
     /**
      * Creates a new ICE agent.
      * 
      * @param tcpTurnClient The TCP TURN client.
      * @param controlling Whether or not this agent will start out as 
      * controlling.  This can change with role conflicts, although that
      * should rarely happen.
     * @param mediaStreamFactory Factory for creating media streams.
      */
     public IceAgentImpl(final StunClient tcpTurnClient, 
         final boolean controlling, 
         final IceMediaStreamFactory mediaStreamFactory)
         {
         this.m_iceCandidateDecoder = new IceCandidateSdpDecoderImpl();
         this.m_controlling = controlling;
         this.m_tieBreaker = 
             new BigInteger(64, new Random()).toByteArray();
 
         // TODO: We only currently support a single media stream!!
         this.m_mediaStream = 
             mediaStreamFactory.createStream(this, tcpTurnClient);
         this.m_mediaStreams.add(this.m_mediaStream);
         }
 
     public void onValidPairsForAllComponents(final IceMediaStream mediaStream)
         {
         // See ICE section 7.1.2.2.3.  This indicates the media stream has a
         // valid pair for all it's components.  That event can potentially 
         // unfreeze checks for other media streams.  
         
         // TODO: We only currently handle a single media stream, so we don't
         // perform these checks for now!!!
         }
 
     public void onUnfreezeCheckLists(final IceMediaStream mediaStream)
         {
         // Specified in ICE section 7.1.2.3.
         // TODO: We only currently handle a single media stream, so we don't
         // unfreeze any other streams for now!!
         }
 
     public long calculateDelay(final int Ta_i)
         {
         return IceTransactionDelayCalculator.calculateDelay(Ta_i, 
             this.m_mediaStreams.size());
         }
 
     public boolean isControlling()
         {
         return this.m_controlling;
         }
 
     public void setControlling(final boolean controlling)
         {
         this.m_controlling = controlling;
         }
     
     public void recomputePairPriorities()
         {
         this.m_mediaStream.recomputePairPriorities(this.m_controlling);
         }
     
     public byte[] getTieBreaker()
         {
         return m_tieBreaker;
         }
     
     public byte[] generateAnswer()
         {
         return m_mediaStream.encodeCandidates();
         }
     
     public byte[] generateOffer()
         {
         return m_mediaStream.encodeCandidates();
         }
 
     public Socket createSocket(final ByteBuffer answer) throws IOException
         {
         // TODO: We should process all possible media streams.
         
         // Note we set the controlling status of remote candidates to 
         // whatever we are not!!
         final Collection<IceCandidate> remoteCandidates = 
             this.m_iceCandidateDecoder.decode(answer, !this.m_controlling);
 
         this.m_mediaStream.establishStream(remoteCandidates);
         
         final Collection<IceCandidatePair> validPairs = 
             this.m_mediaStream.getValidPairs();
         
         synchronized (validPairs)
             {
             for (final IceCandidatePair pair : validPairs)
                 {
                 final Socket socket = pair.accept(this);
                 if (socket != null)
                     {
                     return socket;
                     }
                 }
             }
         
         m_log.debug("Could not create socket");
         throw new IOException("Could not create socket");
         }
     
     public Socket visitTcpIceCandidatePair(final TcpIceCandidatePair pair)
         {
         return pair.getSocket();
         }
 
     public Socket visitUdpIceCandidatePair(final UdpIceCandidatePair pair)
         {
         // TODO: Add processing for UDP pairs.
         return null;
         }
 
     }
