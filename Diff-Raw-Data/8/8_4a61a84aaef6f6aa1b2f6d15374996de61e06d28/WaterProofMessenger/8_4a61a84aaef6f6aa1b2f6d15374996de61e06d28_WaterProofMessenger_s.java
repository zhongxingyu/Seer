 package isnork.g3;
 
 import isnork.sim.SeaLife;
 import isnork.sim.Observation;
 import isnork.sim.iSnorkMessage;
 
 import java.awt.geom.Point2D;
 import java.util.Map;
 import java.util.Set;
 import java.util.Queue;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 public class WaterProofMessenger implements Messenger {
     private Transcoder xcoder;
     private Queue<String> outboundMessages;
     private Map<Integer,iSnorkBuffer> receivedMessages;
     private Set<SeaLife> discovered;
 
     private class iSnorkBuffer {
         private Point2D location = null;
         private StringBuilder message = new StringBuilder();
         private int messageLength;
 
         public boolean hasLocation() {
             return location != null;
         }
 
         public void setLocation(Point2D location) {
             this.location = location;
         }
 
         public void add(String c) {
             message.append(c);
         }
 
         public boolean isComplete() {
             if(message.length() == xcoder.getMessageLength()) {
                 return true;
             }
             return false;
         }
 
         public String getMessage() {
             String retMsg = message.toString();
             message = new StringBuilder();
             return retMsg;
         }
 
         public String toString() {
             return message.toString();
         }
     }
 
     public WaterProofMessenger(Pokedex dex, int numDivers, int sideLength) {
         xcoder = new WaterProofTranscoder(dex.getSpeciesRanking(), sideLength);
         outboundMessages = new LinkedList<String>();
         receivedMessages = Maps.newHashMap();
         for(int i=0; i<numDivers; i++) {
             receivedMessages.put(-i, new iSnorkBuffer());
         }
         //System.out.println("received:\n"+receivedMessages.toString());
         discovered = Sets.newHashSet();
     }
 
     @Override
     public void addReceivedMessages(Set<iSnorkMessage> incomingMessages) {
         for(iSnorkMessage in : incomingMessages) {
             iSnorkBuffer msgBuffer = receivedMessages.get(in.getSender());
             if(msgBuffer == null) {
                System.out.println("msg from "+in.getSender()+" is null");
             }
            assert(msgBuffer != null);
             if(!msgBuffer.hasLocation()) {
                 msgBuffer.setLocation(in.getLocation());
             }
            //System.out.println("rcvd msg: "+in.getMsg());
             msgBuffer.add(in.getMsg());
             
             if(msgBuffer.isComplete()) {
                 discovered.add(xcoder.decode(msgBuffer.getMessage()));
             }
         }
        //System.out.println("\nreceived: "+receivedMessages.toString());
     }
 
     @Override
     public void addOutboundMessage(Observation seen) {
         List<String> messagesToSend = xcoder.encode(seen.getName(), seen.getId(), seen.getLocation());
         if(messagesToSend == null) {
             return;
         }
         //System.out.println("to send: "+messagesToSend.toString());
         outboundMessages.addAll(messagesToSend);
     }
 
     @Override
     public Set<SeaLife> getDiscovered() {
         Set<SeaLife> discoveredCreatures = Sets.newHashSet(discovered);
         discovered = Sets.newHashSet();
         //System.out.println("Discovered: "+discoveredCreatures.toString());
         return discoveredCreatures;
     }
 
     @Override
     public String sendNext() {
         // return next message-char in queue
         String msg = outboundMessages.poll();
         //System.out.println("next: "+msg);
         return msg;
     }
 }
