 package nl.vu.cs.cn.tcp;
 
 
 import android.util.Log;
 
 import java.io.IOException;
 
 import nl.vu.cs.cn.IP;
 import nl.vu.cs.cn.IPUtil;
 import nl.vu.cs.cn.tcp.segment.RetransmissionSegment;
 
 public class TimeoutHandler implements OnTimeoutListener {
 
     private String TAG = "TimeoutHandler";
 
     private final TransmissionControlBlock tcb;
     private final IP ip;
 
     public TimeoutHandler(IP ip, TransmissionControlBlock tcb){
         this.ip = ip;
         this.tcb = tcb;
         TAG += (tcb.isServer()) ? " [server]" : " [client]";
     }
 
     public void onUserTimeout(){
         /*
          * TODO:
          * - flush all queus
          * - signal user: "error:  connection aborted due to user timeout" in general
          * - signal user: "error:  connection aborted due to user timeout" in any outstanding calls
          * - enter closed state
          */
 
         tcb.enterState(TransmissionControlBlock.State.CLOSED);
     }
 
     public void onRetransmissionTimeout(RetransmissionSegment retransmissionSegment){
         // remove segment from the retransmission queue
         if(!tcb.removeFromRetransmissionQueue(retransmissionSegment)){
             // the segment did not exist in the queue anymore
             return;
         }
 
         int retryNum = retransmissionSegment.getRetry();
         if(retryNum >= TransmissionControlBlock.MAX_RETRANSMITS){
             Log.v(TAG, "Segment " + retransmissionSegment.getSegment().getSeq() + " was not ACKed. Not retrying");
             // not retrying, so don't add it to the queue again
         } else {
             Log.v(TAG, "Segment " + retransmissionSegment.getSegment().getSeq() + " was not ACKed. Retry #" + (retryNum+1));
 
             retransmissionSegment.increaseRetry();
 
             IP.Packet packet = IPUtil.getPacket(retransmissionSegment.getSegment());
             try {
                 ip.ip_send(packet);
            } catch (IOException e) {
                // if an error occurs set a timer again and retry afterwards
                Log.w(TAG, "Error while resending packet", e);
            } finally {
                 // retrying, so add segment to the retransmission queue again
                 tcb.addToRetransmissionQueue(retransmissionSegment);
             }
         }
     }
 
     public void onTimeWaitTimeout(){
         tcb.enterState(TransmissionControlBlock.State.CLOSED);
     }
 }
