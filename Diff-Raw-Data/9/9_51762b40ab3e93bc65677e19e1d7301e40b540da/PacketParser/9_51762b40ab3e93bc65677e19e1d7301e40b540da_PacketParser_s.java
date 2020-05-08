 package epsilon.net;
 
 import java.net.DatagramPacket;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * PacketParser class creates a parsing thread that takes
  * incoming packets from the packet queue and parses its data.
  * @author mm
  */
 public class PacketParser implements Runnable {
 
     private BlockingQueue<DatagramPacket> packetQueue;
     private HashMap<String, double[]> playerPosList;
     private NetworkHandler netHandler;
 
     private boolean isRunning = true;
 
     private String name;
 
     /**
      * Constructor
      * @param packetQueue
      */
     public PacketParser(BlockingQueue<DatagramPacket> packetQueue,
             HashMap<String, double[]> playerPosList, NetworkHandler netHandler, String name) {
         this.packetQueue = packetQueue;
         this.playerPosList = playerPosList;
         this.netHandler = netHandler;
         this.name = name;
     }
 
     /**
      * Parse incoming packets from the server
      */
     public void run() {
         while (isRunning) {
             try {
                 DatagramPacket packet = packetQueue.take();
 
                 String packetString = new String(packet.getData(), 0, packet.getLength());
                 String calculatedHash = "";
 
                 String[] strArray = packetString.split(" ");
                 String incomingHashToken = strArray[strArray.length-1];
                 String modifiedPacketString = packetString.replace(" " + incomingHashToken, "");
 
                 try {
                     MessageDigest hash = MessageDigest.getInstance("SHA");
                     byte[] hashSum = hash.digest(modifiedPacketString.getBytes());
 
                     StringBuilder hexString = new StringBuilder();
 
                     for (int i = 0; i < hashSum.length; i++) {
                        hexString.append(Integer.toHexString(hashSum[i]));
                     }
 
                     calculatedHash = hexString.toString();
                 }
                 catch (NoSuchAlgorithmException e) {
                     System.out.println("could not hash incoming message");
                 }
 
                 if (calculatedHash.equals(incomingHashToken)) {
 
                     for (int i = 0; i < strArray.length-1; i += 3) {
                         String pname = strArray[i];
                         String posX = strArray[i+1];
                         String posY = strArray[i+2];
                         
                         if (!pname.equals(this.name)) {
                             double[] posArray = new double[2];
 
                             try {
                                 posArray[0] = Double.valueOf(posX);
                                 posArray[1] = Double.valueOf(posY);
                             }
                             catch (NumberFormatException e) {
                                 System.out.println("Cant convert x or y coordinates to double"); 
                             }
 
                             if (!playerPosList.containsKey(pname)) {
                                 netHandler.addNewPlayer(pname);
                             }
 
                             playerPosList.put(pname, posArray);
                         }
 
                     }
                 }
             }
             catch (InterruptedException ie) {
                 System.out.println("Interrupt from queue");
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
