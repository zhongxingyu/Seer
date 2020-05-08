 package threads;
 
 import datatypes.FileDescriptor;
 import exceptions.InvalidMessageArguments;
 import gui.*;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.security.NoSuchAlgorithmException;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import messages.PutChunk;
 import messages.Stored;
 
 public class MDBThread extends Thread
 {
     // Multicast incoming definitions
     public static final String multicastAddress = "237.1.7.2";
     public static final int multicastPort = 4004;
     private final MulticastSocket inputSocket;
     // MC multicast
     private final MulticastSocket mcSocket;
     // Random number generator
     private Random rgen;
 
     public MDBThread() throws IOException
     {
         inputSocket = new MulticastSocket(MDBThread.multicastPort);
         inputSocket.setTimeToLive(1);
         inputSocket.joinGroup(InetAddress.getByName(multicastAddress));
 
         mcSocket = new MulticastSocket(MCThread.multicastPort);
         mcSocket.setTimeToLive(1);
         mcSocket.joinGroup(InetAddress.getByName(MCThread.multicastAddress));
 
         rgen = new Random();
     }
 
     @Override
     public void run()
     {
         for (;;)
         {
             try
             {
                byte[] receiveData = new byte[65535];
 
                 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                 inputSocket.receive(receivePacket);
 
                 String msgReceived = new String(receivePacket.getData());
 
                 FileChooserFrame.log.append("MDB - Received: " + msgReceived + "\n");
 
                 switch (msgReceived.split(" ")[0])
                 {
                     case "PUTCHUNK":
                         parseStored(msgReceived);
                         break;
                 }
             }
             catch (IOException | InterruptedException | InvalidMessageArguments | NoSuchAlgorithmException ex)
             {
                 Logger.getLogger(MDBThread.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private void sendStored(PutChunk msgReceived) throws IOException
     {
         Stored st = new Stored(msgReceived.getFileId(), msgReceived.getChunkNo());
         MulticastSocket outputSocket = new MulticastSocket(MCThread.multicastPort);
         outputSocket.setTimeToLive(1);
         InetAddress MCAddress = InetAddress.getByName(MCThread.multicastAddress);
         outputSocket.send(new DatagramPacket(st.toString().getBytes(), st.toString().length(), MCAddress, MCThread.multicastPort));
         outputSocket.close();
         
         FileChooserFrame.log.append("MDB - Sent: " + st + "\n");
     }
 
     private void parseStored(String msgReceived) throws IOException, InterruptedException, InvalidMessageArguments, NoSuchAlgorithmException
     {
         // Creating a PutChunk object from parsing the message...
         PutChunk msg = PutChunk.parseMsg(msgReceived);
         
         // Selecting a random time to sleep - give time for other threads to volunteer!
         int sleepTime = rgen.nextInt(400);
 
         FileChooserFrame.log.append("MDB - Going to sleep for " + sleepTime + " ms..." + "\n");
 
         // Before falling asleep, launch a thread to monitor how many STORED have been sent to the network
         final Queue<String> storedHosts = new PriorityQueue<>();
         CounterThread ct = new CounterThread(storedHosts);
         ct.start();
 
         // Falling asleep...
         Thread.sleep(sleepTime);
 
         ct.finish();
 
         // @TODO: Now, time to decide if storing or not - enhancement!
 
         // Store stuff...
         storeChunk(msg);
         sendStored(msg);
     }
 
     private void storeChunk(PutChunk msg) throws NoSuchAlgorithmException, IOException
     {
         // Create an appropriate folder, if it doesn't exist.
         FileDescriptor savedChunkDir = new FileDescriptor(FileDescriptor.receivedChunkDir + "/" + msg.getFileId());
         if (!savedChunkDir.exists())
         {
             savedChunkDir.mkdir();
         }
 
         // Saving the chunk...
         FileDescriptor savedChunk = new FileDescriptor((savedChunkDir.getPath() + "/" + msg.getFileId() + "_" + msg.getChunkNo()));
         savedChunk.createNewFile();
 
         FileOutputStream out = new FileOutputStream(savedChunk);
         out.write(msg.getChunkData());
         out.close();
     }
 
     public class CounterThread extends Thread
     {
         private Queue<String> storedHosts;
         private boolean stopFlag;
 
         public CounterThread(Queue<String> storedHosts)
         {
             this.storedHosts = storedHosts;
             stopFlag = false;
         }
 
         @Override
         public void run()
         {
             while (!stopFlag)
             {
                 try
                 {
                     // Receiving messages on the MC Channel
                     byte[] receiveData = new byte[1024];
                     DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
 
                     mcSocket.receive(receivePacket);
                     String receivedMsg = new String(receivePacket.getData());
                     
                     FileChooserFrame.log.append("MDB - Received (MC Channel): " + receivedMsg + "\n");
                     
                     // Checking if the message is a STORED and matches our file
                     String[] splitMsg = receivedMsg.split(" ");
                     if (splitMsg[0].equals("STORED"))
                     {
                         
                     }
                     
                     storedHosts.add(receivePacket.getAddress().toString());
                 }
                 catch (IOException ex)
                 {
                     Logger.getLogger(MDBThread.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         public void finish()
         {
             stopFlag = true;
         }
     }
 }
