 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 /**
  * User: Ivan
  * Date: 06/03/13
  */
 public class PacketMulticaster {
     private static final Logger logger = LoggerFactory.getLogger(PacketMulticaster.class);
 
     private HashMap<String, ObjectOutputStream> connectedOuts = new HashMap<String, ObjectOutputStream>();
 
     public PacketMulticaster(HashMap<String, ObjectOutputStream> connectedOuts) {
         this.connectedOuts = connectedOuts;
     }
 
     public void multicastAction(MazewarPacket action) {
         // Set generic fields for action packet
         synchronized (Mazewar.lamportClk) {
             action.lamportClk = ++Mazewar.lamportClk;
         }
         action.seqNum = action.lamportClk;
         action.owner = Mazewar.myName;
 
         // ACK to myself
         trackAck(action, Mazewar.myName);
 
        // Add action to queue
        Mazewar.actionQueue.add(action);
 
         // Multicast action packet to all clients
         multicast(action);
         logger.info("Finished sending action packet all clients with seq num: " + action.seqNum + "\n");
     }
 
     public void multicastACK(MazewarPacket action) {
         // Create ack packet and set neccessary fields
         MazewarPacket ack = new MazewarPacket();
         ack.lamportClk = action.lamportClk;
         synchronized (Mazewar.lamportClk) {
             ack.seqNum = ++Mazewar.lamportClk;
         }
         ack.owner = action.owner;
         ack.ACKer = Mazewar.myName;
 
         multicast(ack);
         logger.info("Finished sending ACK to all clients with seq num: " + ack.seqNum + "\n");
     }
 
     private void multicast(MazewarPacket outgoing) {
         synchronized (Mazewar.connectedOuts) {
             for (Map.Entry<String, ObjectOutputStream> entry : connectedOuts.entrySet()) {
                 try {
                     entry.getValue().writeObject(outgoing);
                     logger.info("Multicast to " + entry.getKey().toUpperCase() + " with seq num: " + outgoing.seqNum);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public static void trackAck(MazewarPacketIdentifier action, String ACKer) {
         synchronized (Mazewar.ackTracker) {
             if (!Mazewar.ackTracker.containsKey(action))
                 Mazewar.ackTracker.put(action, new HashSet<String>());
             Mazewar.ackTracker.get(action).add(ACKer);
         }
     }
 }
