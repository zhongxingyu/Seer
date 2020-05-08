 package epsilon.net;
 
 import java.net.DatagramPacket;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * PacketParser class creates a parsing thread that takes
  * incoming packets from the packet queue and parses its data.
  *
  * @author Magnus Mikalsen
  */
 public class PacketParser implements Runnable {
 
     // Incoming packet queue
     private BlockingQueue<DatagramPacket> incomingPacketQueue;
 
     // List og player states
     private HashMap<String, double[]> playerStateList;
 
     private NetworkHandler netHandler;
     private boolean isRunning = true;
 
     // The local players name
     private String name;
 
     /**
      * Constructor
      *
      * @param incomingPacketQueue Queue for incoming packets
      * @param playerStateList List og players and states
      * @param netHandler Reference to NetworkHandler
      * @param name Local player name
      */
     public PacketParser(BlockingQueue<DatagramPacket> incomingPacketQueue,
             HashMap<String, double[]> playerStateList, NetworkHandler netHandler, String name) {
         this.incomingPacketQueue = incomingPacketQueue;
         this.playerStateList = playerStateList;
         this.netHandler = netHandler;
         this.name = name;
     }
 
     /**
      * Parse incoming packets from the server
      */
     public void run() {
         while (isRunning) {
             try {
                 // Get packet from incoming packet queue
                 DatagramPacket packet = incomingPacketQueue.take();
 
                 // Get message from packet
                 String packetString = new String(packet.getData(), 0, packet.getLength());
 
                 // Split message into words
                 String[] strArray = packetString.split(" ");
 
                 // Get hash from packet message
                 String incomingHashToken = strArray[strArray.length-1];
 
                 // Remove hash from packet message
                 String modifiedPacketString = packetString.replace(incomingHashToken, "");
 
                 String calculatedHash = "";
 
                 try {
                     // Calculate a hash of incoming message
                     MessageDigest hash = MessageDigest.getInstance("SHA");
                     byte[] hashSum = hash.digest(modifiedPacketString.getBytes());
 
                     // Create a hexadecimal representation of the hash
                     StringBuilder hexString = new StringBuilder();
                     for (int i = 0; i < hashSum.length; i++) {
                         hexString.append(Integer.toHexString(0xFF & hashSum[i]));
                     }
 
                     calculatedHash = hexString.toString();
                 }
                 catch (NoSuchAlgorithmException e) {
                     System.out.println("Could not find hashing algorithm in parser");
                 }
 
                 // Check if hash is correct
                 if (calculatedHash.equals(incomingHashToken)) {
 
                     // Names of players not in packet message
                    HashSet<String> playersToRemove = new HashSet<String>(playerStateList.keySet());
                    playersToRemove = (HashSet<String>) playersToRemove.clone();
 
                     // Iterate through substrings in the packet message
                     for (int i = 0; i < strArray.length-1; i += 3) {
                         String pname = strArray[i];
                         String posX = strArray[i+1];
                         String posY = strArray[i+2];
 
                         // Check if name in packet message matches local player name
                         // We dont want info about the local player
                         // this is jusst a redundant check
                         if (!pname.equals(this.name)) {
 
                             double[] posArray = new double[2];
                             try {
                                 posArray[0] = Double.valueOf(posX);
                                 posArray[1] = Double.valueOf(posY);
                             }
                             catch (NumberFormatException e) {
                                 System.out.println("Cant convert x or y coordinates to double"); 
                             }
 
                             // Check if this is a new player
                             if (!playerStateList.containsKey(pname)) {
                                 netHandler.addNewPlayer(pname);
                             }
 
                             // Add player name and state to player state list
                             playerStateList.put(pname, posArray);
 
 
                             // Remove player name from the list of
                             // players to remove
                             playersToRemove.remove(pname);
 
                         }
 
                     }
 
                     System.out.println(playersToRemove.size());
 
                     // Remove players we dident receive information about
                     Iterator it = playersToRemove.iterator();
                     while (it.hasNext()) {
                         playerStateList.remove((String)it.next());
                     }
 
                 }
             }
             catch (InterruptedException ie) {
                 System.out.println("Interrupt from incoming packet queue");
             }
         }
     }
 
     /**
      * Stop parser thread
      */
     public void stopParser() {
         isRunning = false;
     }
 
 }
