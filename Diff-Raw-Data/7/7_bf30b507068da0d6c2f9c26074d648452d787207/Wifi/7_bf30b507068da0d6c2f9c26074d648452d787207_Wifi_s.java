 package edu.cmu.solarflare;
 
 import com.sun.spot.peripheral.TimeoutException;
 import com.sun.spot.util.Utils;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import org.json.me.JSONArray;
 import org.json.me.JSONException;
 import org.json.me.JSONObject;
 
 public class Wifi {
     private SolarFlare spot;
     public String ssid;
     public boolean isActive;
     public int tcpServerPort;
     private byte[] rxBuffer;
     private byte rxIndex;
     private byte rxByte;
     private int rxBytesAvailable;
     private int tcpServerCID;
     private MessageBuffer outgoing;         // a queue for outgoing messages, emptied by the sender thread
     private SerialBuffer incoming;          // a buffer of incoming bytes, emptied by the receiver thread
     private Vector pendingWifiClients;      // CID of wifi clients that have not yet sent their userid
     private Hashtable localClientCIDs;      // CID of clients at our spot's address (userID => clientCID)
     private Hashtable localClientUserIDs;   // inverse of localClientCIDs (clientCID => userID)
     
     public Wifi(SolarFlare spot, String ssid, int tcpServerPort) {
         this.spot = spot;
         this.ssid = ssid;
         this.isActive = false;
         this.tcpServerPort = tcpServerPort;
         this.outgoing = new MessageBuffer();
         this.incoming = new SerialBuffer(3);
         this.pendingWifiClients = new Vector(4);
         this.localClientCIDs = new Hashtable();
         this.localClientUserIDs = new Hashtable();
     }
     
     public void init() throws TimeoutException, IOException, Exception {    
         spot.board.initUART(9600, false);
         rxBuffer = new byte[256];
         tcpServerCID = -1;
         
         String[][] startupCommands = new String[][] {
             {"AT", "OK"},                                                       // check that WiFi module is responding
             {"ATE0", "OK"},                                                     // disable command echo
             {"ATV0", "0"},                                                      // disable verbose responses
             {"AT+WM=2", "0"},                                                   // enter limited access point mode
             {"AT+NSET=\"192.168.2.1\",\"255.255.255.0\",\"192.168.2.1\"", "0"}, // set static IP for DHCP server to <src_ip>,<net_mask>,<gateway>
             {"AT+DHCPSRVR=1", "0"},                                             // start DHCP server
             {"AT+WA=" + ssid, "0"},                                             // start access point, broadcassting specified SSID
             {"AT+NSTCP=" + tcpServerPort, "0"},                                 // start tcp server on the specified port
             {"AT+WRXACTIVE=1", "0"}                                             // keep 802.11 receiver always on
 //            {"AT+WP=4", "0"}                                                    // set wifi transmit power to medium (0 max, 7 min)
         };
         
         // run startup commands
         for (byte i = 0; i < (byte)startupCommands.length; i++) {
             sendAndCheckUART(startupCommands[i][0], startupCommands[i][1], 5);
         }
         
         this.isActive = true;
         outgoing.activate();
         System.out.println("Wifi on.");
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
                         String m = outgoing.get();  // will block until there's a message to send
                         System.out.println("Sending to WiFi module: " + m);
                         sendUART(m);
                     } catch (InterruptedException e) {
                         System.out.println("Error, WiFi threads: Could not get message from outgoing buffer. " + e);
                     }
                 }
             }
         }.start();
     }
     
     private void startReceiverThread() {
         // first thread monitors the serial connection and immediately populates the SerialBuffer
         new Thread() {
             public void run() {
                 byte[] maxBuffer, buffer;
                 int byteCount;
                 while (true) {
                     try {
                         maxBuffer = new byte[64];
                         byteCount = spot.board.readUART(maxBuffer, 0, 64);
                         buffer = new byte[byteCount];
                         System.arraycopy(maxBuffer, 0, buffer, 0, byteCount);
                         incoming.put(new String(buffer));
                     } catch (IOException e) {
                         System.out.println("Error, general: serial receiver thread threw - " + e);
                     }
                 }
             }
         }.start();
         
         // second thread empties the SerialBuffer and handles the strings
         new Thread() {
             public void run() {
                 while (true) {
                     String s;
                     try {
                         s = incoming.get().trim();
                         System.out.println("Got: " + s);
                         parseIncomingMessage(s);
                     } catch (InterruptedException e) {
                         System.out.println("Error, general: parsing incoming message - " + e);
                     }
                 }
             }
         }.start();
     }
     
     private void parseIncomingMessage(String inMsg) {        
         // skip insignificant/empty messages
         if (inMsg.length() < 3) { return; }
         
         // remove escape sequence at the start of the string, what remains after trim
         if ((byte) inMsg.charAt(1) == 27) {
             inMsg = inMsg.substring(2);
         }
         
         // check message type
         switch (inMsg.charAt(0)) {
             case 'S':
                 // S1hello
                 processWifiClientMessage(Integer.valueOf(Character.digit(inMsg.charAt(1), 10)), inMsg.substring(2));
                 break;
             case '7':
                 // 7 0 1 192.168.2.2 59010
                 String[] inMsgArray = Utils.split(inMsg, ' ');
                 if (Integer.parseInt(inMsgArray[1]) == tcpServerCID) {
                     System.out.println("Adding client CID: " + inMsgArray[2]);
                     pendingWifiClients.addElement(Integer.valueOf(inMsgArray[2]));  // add client CID and wait for user data
                 }
                 break;
             case '8':
                 // 8 1
                 Integer localCID = Integer.valueOf(Utils.split(inMsg, ' ')[1]);
                 spot.removeLocalClient((String) localClientUserIDs.get(localCID));
                 localClientCIDs.remove((String) localClientUserIDs.remove(localCID));
                 break;
         }
     }
     
     public void processWifiClientMessage(Integer clientCID, String msg) {
         if (msg.length() == 0) { return; }        
         System.out.println("Wifi message from " + clientCID + ": " + msg);
         
         try {
             JSONObject msgJSON = new JSONObject(msg);
             String msgAction = msgJSON.getString("action");
             
             // protocol-specific processing
             if (msgAction.equals("connect")) {
                 String userID = msgJSON.getString("userid");
                 if (pendingWifiClients.removeElement(clientCID)) {
                     // add client
                     localClientCIDs.put(userID, clientCID);
                     localClientUserIDs.put(clientCID, userID);
                     spot.addLocalClient(userID, msgJSON.getString("username"));
                 } else {
                     // re-send client list
                     sendClientList(userID);
                 }
             } else if (msgAction.equals("usermessage")) {
                 spot.relayUserMessage(msgJSON.getString("sender_userid"), msgJSON.getString("receiver_userid"), msgJSON.getString("message"));
             } else {
                 System.out.println("Message action '" + msgAction + "' not recognized!");
             }
             
             System.out.println("WiFi executed: " + msgAction);
         } catch (JSONException e) {
             System.out.println("Error, WiFi JSON: " + e);
         }
     }
     
     // send entire list of known clients to one local user
     public void sendClientList(String userID) {
         JSONArray clientArray = new JSONArray();
         for (Enumeration e = spot.clients.elements(); e.hasMoreElements();) {
             clientArray.put(((Client) e.nextElement()).toJSONObject());
         }
         JSONObject m = new JSONObject();
         try {
             m.put("action", "adduser");
             m.put("users", clientArray);
             sendToClient((Integer) localClientCIDs.get(userID), m.toString());
         } catch (JSONException e) {
             System.out.println("Error, WiFi JSON: " + e);
         }
     }
     
     public void relayUserMessage(String senderUserID, String receiverUserID, String msg) {
         JSONObject m = new JSONObject();
         try {
             m.put("action", "usermessage");
             m.put("sender_userid", senderUserID);
             m.put("receiver_userid", receiverUserID);
             m.put("message", msg);
             sendToClient((Integer) localClientCIDs.get(receiverUserID), m.toString());
         } catch (JSONException e) {
             System.out.println("Error, WiFi JSON: " + e);
         }
     }
     
     public void broadcastClientStatus(Client c, String status) {
         JSONArray clientArray = new JSONArray();
         clientArray.put(c.toJSONObject());
         JSONObject m = new JSONObject();
         String userID, msg;
         try {
             if (status.equals("online")) {
                 m.put("action", "adduser");
             } else if (status.equals("offline")) {
                 m.put("action", "removeuser");
             }
             
             m.put("users", clientArray);
             msg = m.toString();
             for (Enumeration e = localClientCIDs.keys(); e.hasMoreElements();) {
                 userID = (String) e.nextElement();
                 if (!userID.equals(c.userID)) {
                     sendToClient((Integer) localClientCIDs.get(userID), msg);
                 }
             }
         } catch (JSONException e) {
             System.out.println("Error, WiFi JSON: " + e);
         }
     }
     
     public void sendToClient(Integer clientCID, String msg) {
         outgoing.put((char) 27 + "S" + clientCID + msg);    // <Esc>S<CID><message>
         outgoing.put((char) 27 + "S" + clientCID);          // send dummy message to "force through" the previous one (hacky)
     }
     
     // Send message to WiFi module and read responses until receiving an expected string.
     // Outgoing message can be null, in which case the command has been send and it's 
     // just reading for an expected response.
     private void sendAndCheckUART(String message, String expectedResponse, int retryCount) throws Exception {
         purgeUART();
         
         do {
             sendUART(message);
 
             // special case: when starting the TCP server, we need to remember its CID
             if (message.startsWith("AT+NSTCP=")) {
                 checkTCPServerCID(readUART(false));     // process first response line for CID
             }
         } 
         while (!readUART(true).equals(expectedResponse) && retryCount-- > 0);
 
         if (retryCount == -1) {
             throw new TimeoutException("UART timed out sending '" + message + "' to WiFi module.");
         }
         
         System.out.println(message + " -- got " + expectedResponse);
     }
     
     private void checkTCPServerCID(String response) throws Exception {
         System.out.println("[start TCP server: " + response + "]");
         if (response.charAt(0) == '7' && response.length() == 3) {  // expect "7 0"
             tcpServerCID = Character.digit(response.charAt(2), 10);
             System.out.println("[TCP server CID: " + tcpServerCID + "]");
         }
         else {
             throw new Exception("Incorrect response to TCP server start command (wifi).");
         }
     }
     
     private void sendUART(String message) {
         spot.board.writeUART(message + "\n");
     }
     
     public String readUART(boolean onlyLastReply) {
         do {
             rxIndex = 0;
             do {
                 try {
                     rxByte = spot.board.readUART();
                     rxBytesAvailable = spot.board.availableUART();
                     rxBuffer[rxIndex] = rxByte;
                     rxIndex++;
                     //System.out.println((char)rxByte + " = " + rxByte + " (available: " + rxBytesAvailable + ") ");
                     Thread.sleep(5);    // a delay is needed for some bytes to show up in spot.board.availableUART()
                 } catch (IOException e) {
                     System.out.println("Error reading from UART: " + e);
                 } catch (InterruptedException e) {
                     System.out.println("Error, threads: " + e);
                 }
             } 
             while (rxByte != (byte)'\n');
         } 
         while (rxIndex < 3 || (onlyLastReply && rxBytesAvailable > 1));  // if it's an empty line (<CR><LF> pre-reply) or if onlyLastReply is set        
         
         return new String(rxBuffer, 0, rxIndex - 1).trim();
     }
     
     private void purgeUART() throws IOException {
         while (spot.board.availableUART() > 0) { 
             spot.board.readUART();
             try {
                 Thread.sleep(10);
             } catch (InterruptedException e) {
                 System.out.println("Error, threads: " + e);
             }
             
         }
     }
 }
