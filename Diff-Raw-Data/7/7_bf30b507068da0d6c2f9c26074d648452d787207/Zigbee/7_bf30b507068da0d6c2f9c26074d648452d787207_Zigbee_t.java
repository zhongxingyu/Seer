 package edu.cmu.solarflare;
 
 import java.io.IOException;
 import java.util.Vector;
 import javax.microedition.io.Connector;
 import javax.microedition.io.Datagram;
 import javax.microedition.io.DatagramConnection;
 
 import com.sun.spot.io.j2me.radiogram.RadiogramConnection;
 import com.sun.spot.peripheral.radio.RadioFactory;
 import com.sun.spot.util.IEEEAddress;
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 public class Zigbee {
     private SolarFlare spot;
     public String address;             // spot's 64-bit dotted MAC address
     private int seqNo;                  // sequence counter for outgoing messages 
     private DatagramConnection senderConnection;
     private Datagram senderDatagram;
     private RadiogramConnection receiverConnection;
     private Datagram receiverDatagram;
     private MessageBuffer outgoing;     // a queue for outgoing messages, emptied by the sender thread
     private Vector lastMessages;        // (for broadcast) don't interpret previously seen messages
     private JSONObject msgJSON;
     private String msgAction;
     
     public Zigbee(SolarFlare spot) {
         this.spot = spot;
         this.address = IEEEAddress.toDottedHex(RadioFactory.getRadioPolicyManager().getIEEEAddress());
         this.seqNo = 0;
         this.outgoing = new MessageBuffer();
         this.lastMessages = new Vector(20);     // keep a record of the last 20 messages seen
     }
     
     public void init() throws IOException {
         senderConnection = (DatagramConnection) Connector.open("radiogram://broadcast:37");
         senderDatagram = senderConnection.newDatagram(senderConnection.getMaximumLength());
         receiverConnection = (RadiogramConnection) Connector.open("radiogram://:37");
         receiverDatagram = receiverConnection.newDatagram(receiverConnection.getMaximumLength());
        outgoing.activate();
     }
     
     public void startComm() {
         startSenderThread();
         startReceiverThread();
     }
     
     private void startSenderThread() {
         new Thread() {
             public void run() {                
                 while (true) {
                     try {
                         String s = outgoing.get();      // will block until there's a message to send
                         senderDatagram.reset();
                         senderDatagram.writeUTF(s);
                         senderConnection.send(senderDatagram);      // broadcast
                         
                         System.out.println("ZigBee sent: " + s);
                     } catch (InterruptedException e) {
                         System.out.println("Error, ZigBee threads: Could not get message from outgoing buffer. " + e);
                     } catch (IOException e) {
                         System.out.println("Error, ZigBee I/O: Could not send. " + e);
                     }
                 }
             }
         }.start();
     }
     
     private void startReceiverThread() {
         new Thread() {
             public void run() {
                 while (true) {
                     try {
                         receiverDatagram.reset();
                         receiverConnection.receive(receiverDatagram);   // will block until there's a message to receive
                         parseIncomingMessage(receiverDatagram.readUTF());
                     } catch (IOException e) {
                         System.out.println("Error, ZigBee I/O: Nothing received. " + e);
                     }
                 }
             }
         }.start();
     }
     
     public void parseIncomingMessage(String msg) {
         System.out.println("Zigbee got: " + msg);
         try {
             msgJSON = new JSONObject(msg);
             
             // process message, only if we're not the sender and if it's a new messageID
             if (!msgJSON.getString("sender").equals(address) && !lastMessages.contains(msgJSON.getString("messageid"))) {
                 if (lastMessages.size() > 0) {
                     lastMessages.removeElementAt(0);
                 }
                 lastMessages.addElement(msgJSON.getString("messageid"));
                 
                 msgAction = msgJSON.getString("action");
                 if (msgAction.equals("adduser")) {
                     spot.addClient(msgJSON.getString("userid"), msgJSON.getString("username"), msgJSON.getString("sender"));
                 } else if (msgAction.equals("removeuser")) {
                     spot.removeClient(msgJSON.getString("userid"));
                 } else if (msgAction.equals("usermessage") && msgJSON.getString("receiver").equals(address)) {
                     spot.relayUserMessage(msgJSON.getString("sender_userid"), msgJSON.getString("receiver_userid"), msgJSON.getString("message"));
                 } else if (msgAction.equals("ack")) {
                     //TODO
                 }
                 
                 // re-broadcast
                 outgoing.put(msg);
                 
                 System.out.println("Zigbee executed: " + msgAction);
             }
         } catch (JSONException e) {
             System.out.println("Error, ZigBee JSON: " + e);
         }
     }
     
     public void broadcastClientStatus(Client c, String status) {
         try {
             JSONObject m = new JSONObject();
             
             if (status.equals("online")) {
                 m.put("action", "adduser");
             } else if (status.equals("offline")) {
                 m.put("action", "removeuser");
             }
             
             m.put("username", c.userName);
             m.put("userid", c.userID);
             broadcastJSON(m);
         } catch (JSONException e) {
             System.out.println("Error, ZigBee JSON: " + e);
         }
     }
     
     public void sendUserMessage(String senderUserID, String receiverUserID, String receiverSpotAddress, String msg) {
         try {
             JSONObject m = new JSONObject();
             m.put("action", "usermessage");
             m.put("sender_userid", senderUserID);
             m.put("receiver_userid", receiverUserID);
             m.put("message", msg);
             sendJSON(m, receiverSpotAddress);
         } catch (JSONException e) {
             System.out.println("Error, ZigBee JSON: " + e);
         }
     }
     
     public void broadcastJSON(JSONObject m) {
         sendJSON(m, "");
     }
     
     public void sendJSON(JSONObject m, String receiverSpotAddress) {
         try {
             // add sender, receiver, and messageID
             m.put("sender", address);
             m.put("receiver", receiverSpotAddress);
             m.put("messageid", address + (seqNo++));
             
             // serialize JSON and send off
             broadcast(m.toString());
         } catch (JSONException e) {
             System.out.println("Error, ZigBee JSON: " + e);
         }
     }
     
     public void broadcast(String m) {
         // add message to outgoing buffer
         outgoing.put(m);
     }
 }
