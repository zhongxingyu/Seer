 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.TreeMap;
 import java.util.SortedMap;
 
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.Utility;
 
 /**
  * Layer above the basic messaging layer that provides reliable, in-order
  * delivery in the absence of faults. This layer does not provide much more than
  * the above.
  *
  * At a minimum, the student should extend/modify this layer to provide
  * reliable, in-order message delivery, even in the presence of node failures.
  */
 public class ReliableInOrderMsgLayer {
   public static int TIMEOUT = 3;
   public static int MAX_RESENDS = 3;
 
   private HashMap<Integer, InChannel> inConnections;
   private HashMap<Integer, OutChannel> outConnections;
   private RIONode n;
 
   /**
    * Constructor.
    *
    * @param destAddr
    *            The address of the destination host
    * @param msg
    *            The message that was sent
    * @param timeSent
    *            The time that the ping was sent
    */
   public ReliableInOrderMsgLayer(RIONode n) {
     inConnections = new HashMap<Integer, InChannel>();
     outConnections = new HashMap<Integer, OutChannel>();
     this.n = n;
   }
 
   /**
    * Receive a data packet.
    *
    * @param from
    *            The address from which the data packet came
    * @param pkt
    *            The Packet of data
    */
   public void RIODataReceive(int from, byte[] msg) {
     RIOPacket riopkt = RIOPacket.unpack(msg);
 
     // at-most-once semantics
     byte[] seqNumByteArray = Utility.stringToByteArray("" + riopkt.getSeqNum());
     n.send(from, Protocol.ACK, seqNumByteArray);
 
     InChannel in = inConnections.get(from);
     if(in == null) {
       in = new InChannel();
       inConnections.put(from, in);
     }
 
     LinkedList<RIOPacket> toBeDelivered = in.gotPacket(riopkt);
     for(RIOPacket p: toBeDelivered) {
       // deliver in-order the next sequence of packets
       n.onRIOReceive(from, p.getProtocol(), p.getPayload());
     }
   }
 
   /**
    * Receive an acknowledgment packet.
    *
    * @param from
    *            The address from which the data packet came
    * @param pkt
    *            The Packet of data
    */
   public void RIOAckReceive(int from, byte[] msg) {
     int seqNum = Integer.parseInt( Utility.byteArrayToString(msg) );
     outConnections.get(from).gotACK(seqNum);
   }
 
   /**
    * Send a packet using this reliable, in-order messaging layer. Note that
    * this method does not include a reliable, in-order broadcast mechanism.
    *
    * @param destAddr
    *            The address of the destination for this packet
    * @param protocol
    *            The protocol identifier for the packet
    * @param payload
    *            The payload to be sent
    * @param callback
    *            Callback called when the network layer has finished with the packet.
    * @return
    *            The sequence number of the packet, or -1 if a sending error occurred.
    */
   public int RIOSend(int destAddr, int protocol, byte[] payload, Callback callback) {
     OutChannel out = outConnections.get(destAddr);
     if(out == null) {
       out = new OutChannel(this, destAddr);
       outConnections.put(destAddr, out);
     }
 
     return out.sendRIOPacket(n, protocol, payload, callback);
   }
 
   /**
    * Callback for timeouts while waiting for an ACK.
    *
    * This method is here and not in OutChannel because OutChannel is not a
    * public class.
    *
    * @param destAddr
    *            The receiving node of the unACKed packet
    * @param seqNum
    *            The sequence number of the unACKed packet
    */
   public void onTimeout(Integer destAddr, Integer seqNum) {
     // Target OutChannel could have been removed, so we must test its nullity.
     OutChannel out = outConnections.get(destAddr);
     if (out != null) {
       out.onTimeout(n, seqNum);
     }
   }
 
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer();
     for(Integer i: inConnections.keySet()) {
       sb.append(inConnections.get(i).toString() + "\n");
     }
 
     return sb.toString();
   }
 
   /**
    * Remove the element of outConnections corresponding to the argument address
    *
    * @param destAddr
    *            The node corresponding to the outChannel to be removed from
    *            outConnections
    */
   public void removeOutChannel(Integer destAddr) {
     outConnections.remove(destAddr);
   }
 }
 
 /**
  * Representation of an incoming channel to this node
  */
 class InChannel {
   private int lastSeqNumDelivered;
   private HashMap<Integer, RIOPacket> outOfOrderMsgs;
 
   InChannel(){
     lastSeqNumDelivered = -1;
     outOfOrderMsgs = new HashMap<Integer, RIOPacket>();
   }
 
   /**
    * Method called whenever we receive a data packet.
    *
    * @param pkt
    *            The packet
    * @return A list of the packets that we can now deliver due to the receipt
    *         of this packet
    */
   public LinkedList<RIOPacket> gotPacket(RIOPacket pkt) {
     LinkedList<RIOPacket> pktsToBeDelivered = new LinkedList<RIOPacket>();
     int seqNum = pkt.getSeqNum();
 
     if(seqNum == lastSeqNumDelivered + 1) {
       // We were waiting for this packet
       pktsToBeDelivered.add(pkt);
       ++lastSeqNumDelivered;
       deliverSequence(pktsToBeDelivered);
     }else if(seqNum > lastSeqNumDelivered + 1){
       // We received a subsequent packet and should store it
       outOfOrderMsgs.put(seqNum, pkt);
     }
     // Duplicate packets are ignored
 
     return pktsToBeDelivered;
   }
 
   /**
    * Helper method to grab all the packets we can now deliver.
    *
    * @param pktsToBeDelivered
    *            List to append to
    */
   private void deliverSequence(LinkedList<RIOPacket> pktsToBeDelivered) {
     while(outOfOrderMsgs.containsKey(lastSeqNumDelivered + 1)) {
       ++lastSeqNumDelivered;
       pktsToBeDelivered.add(outOfOrderMsgs.remove(lastSeqNumDelivered));
     }
   }
 
   @Override
   public String toString() {
     return "last delivered: " + lastSeqNumDelivered + ", outstanding: " + outOfOrderMsgs.size();
   }
 }
 
 /**
  * Representation of an outgoing channel to this node
  */
 class OutChannel {
   private static class SentPacket implements Comparable<SentPacket> {
     RIOPacket pkt;
     Callback callback;
     boolean acked;
 
     public SentPacket(RIOPacket pkt, Callback successCallback) {
       this.pkt = pkt;
       this.callback = callback;
       this.acked = false;
     }
 
     public int compareTo(SentPacket other) {
       return pkt.getSeqNum() - other.pkt.getSeqNum();
     }
 
     public void ack(boolean callCallback) {
       acked = true;
       if (callCallback && callback != null) {
         try {
           callback.setParams(new Object[]{pkt.getSeqNum(), null});
           callback.invoke();
         } catch (Exception e) {
           System.err.println("An exception occurred while trying to call the" +
                              "callback for packet " + pkt.getSeqNum() + ":");
           e.printStackTrace();
         }
       }
     }
 
     public boolean callIfAcked() {
       if (acked && callback != null) {
         try {
           callback.setParams(new Object[]{pkt.getSeqNum(), null});
           callback.invoke();
         } catch (Exception e) {
           System.err.println("An exception occurred while trying to call the" +
                              "callback for packet " + pkt.getSeqNum() + ":");
           e.printStackTrace();
         }
       }
       return acked;
     }
 
     public void timeout() {
       error(new NetworkExceptions.Timeout(pkt));
     }
 
     public void error(Exception exc) {
         if (callback != null) {
           try {
             callback.setParams(new Object[]{pkt.getSeqNum(), exc});
             callback.invoke();
           } catch (Exception e) {
             System.err.println("An exception occurred while trying to call the" +
                                "callback for packet " + pkt.getSeqNum() + ":");
             e.printStackTrace();
           }
         }
     }        
   }
 
   private TreeMap<Integer, SentPacket> activePackets;
   private int lastSeqNumSent;
   private ReliableInOrderMsgLayer parent;
   private int destAddr;
 
   OutChannel(ReliableInOrderMsgLayer parent, int destAddr){
     lastSeqNumSent = -1;
     activePackets = new TreeMap<Integer, SentPacket>();
     this.parent = parent;
     this.destAddr = destAddr;
   }
 
   /**
    * Send a new RIOPacket out on this channel.
    *
    * @param n
    *            The sender and parent of this channel
    * @param protocol
    *            The protocol identifier of this packet
    * @param payload
    *            The payload to be sent
    * @param callback
    *            Callback called when the network has finished with the packet,
    *            successfully or in failure. callback is of the format
    *            void(int, Exception), and the second arg will be null if the
    *            packet was delivered successfully.
    *
    *            NOTE: Callback will _NOT_ be called if an error occurs within
    *            this function. Instead, a sequence number of -1 will be returned.
    *
    * @return sequence number of the packet, or -1 if an error occurred
    */
   protected int sendRIOPacket(RIONode n, int protocol, byte[] payload, Callback callback) {
     try{
       Method onTimeoutMethod = Callback.getMethod("onTimeout", parent, new String[]{ "java.lang.Integer", "java.lang.Integer" });
       RIOPacket newPkt = new RIOPacket(protocol, ++lastSeqNumSent, payload);
       activePackets.put(lastSeqNumSent, new SentPacket(newPkt, callback));
 
       n.addTimeout(new Callback(onTimeoutMethod, parent, new Object[]{ destAddr, lastSeqNumSent }), ReliableInOrderMsgLayer.TIMEOUT);
 
       n.send(destAddr, Protocol.DATA, newPkt.pack());
       return newPkt.getSeqNum();
     } catch(Exception e) {
       System.err.println("An exception occurred when sending packet " + lastSeqNumSent + " to node " + destAddr);
       e.printStackTrace();
       activePackets.remove(lastSeqNumSent);
       lastSeqNumSent--;
       return -1;
     }
   }
 
   /**
    * Called when a timeout for this channel triggers.
    *
    * @param n
    *            The sender and parent of this channel
    * @param seqNum
    *            The sequence number of the unACKed packet
    */
   public void onTimeout(RIONode n, Integer seqNum) {
     if(activePackets.containsKey(seqNum)) {
       resendRIOPacket(n, seqNum);
     }
   }
 
   /**
    * Called when we get an ACK back. Removes the outstanding packet if it is
    * still in activePackets.
    *
    * @param seqNum
    *            The sequence number that was just ACKed
    */
   protected void gotACK(int seqNum) {
     if (activePackets.size() == 0)
       return;
 
     boolean callCallback = activePackets.firstKey() == seqNum;
     SentPacket pkt = activePackets.get(seqNum);
     if (pkt != null) {
       pkt.ack(callCallback);
       if (callCallback)
         activePackets.remove(pkt.pkt.getSeqNum());
     }
     
     flushACKedPackets();
   }
 
   /**
    * Flush the earliest contiguous gropu of ACK'd packets from our queue, if 
    * they exist. Calls the callback for each ACK'd packet, and deletes it until
    * it encounters an unACK'd packet.
    */
   protected void flushACKedPackets() {
     while (activePackets.size() > 0) {
       SentPacket sentPkt = activePackets.firstEntry().getValue();
       if (!sentPkt.callIfAcked())
         break;
       else
         activePackets.remove(sentPkt.pkt.getSeqNum());
     }
   }
 
   /**
    * Resend an unACKed packet, or if it has been resent MAX_RESENDS times,
    * drop the packet and call failureCallback (if non-null) with a
    * TimeoutException.
    *
    * @param n
    *            The sender and parent of this channel
    * @param seqNum
    *            The sequence number of the unACKed packet
    */
   private void resendRIOPacket(RIONode n, int seqNum) {
     SentPacket pkt = activePackets.get(seqNum);
     if (pkt == null)
       return;
 
     try{
       RIOPacket riopkt = pkt.pkt;
       if (riopkt.getNumResends() < ReliableInOrderMsgLayer.MAX_RESENDS) {
         // Resend the packet.
        Method onTimeoutMethod = Callback.getMethod("onTimeout", parent, new String[]{ "java.lang.Integer", "java.lang.Integer" });
 
         riopkt.incNumResends();
         n.send(destAddr, Protocol.DATA, riopkt.pack());
         n.addTimeout(new Callback(onTimeoutMethod, parent, new Object[]{ destAddr, seqNum }), ReliableInOrderMsgLayer.TIMEOUT);
       } else {
         // Resend limit has been reached, so stop resending.
         activePackets.remove(seqNum);
         parent.removeOutChannel(destAddr);
         pkt.timeout();
         flushACKedPackets();
       }
     }catch(Exception e) {
      System.err.println("Exception occurred while resending the packet: ");
      e.printStackTrace();
       activePackets.remove(pkt.pkt.getSeqNum());
       pkt.error(e);
     }
   }
 }
