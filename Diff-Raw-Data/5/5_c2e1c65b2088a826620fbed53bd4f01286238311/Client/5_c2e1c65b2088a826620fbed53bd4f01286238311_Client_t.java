 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 /**
  * The core client class for the project.
  */
 public class Client
 {
     private static final int PORT = 4444;
 
     public static void main(String args[])
     {
         DatagramSocket socket = null;
         try {
             socket = new DatagramSocket();
             socket.setSoTimeout(5000);
         }
         catch (SocketException e)
         {
             System.out.println("Error: unable to open socket.");
             System.exit(1);
         }
 
         Scanner scan = new Scanner(System.in);
         String ip;
         String filename = null;
         InetAddress inetaddress = null;
 
         while (true)
         {
             // prompt user for server ip
             System.out.print("Server IP: ");
 
             // they didn't give us anything, try again
             if (!scan.hasNextLine())
                 break;
             ip = scan.nextLine();
 
             if (ip.equals("quit"))   // user wants to quit
                 System.exit(0);
 
             DatagramPacket connect = null;
             DatagramPacket received = null;
 
             // ask server if we can get a file
             String cmd = "GET_FILE";
             byte[] buffer = new byte[Chunk.DATA_BYTES];
             try {
                 inetaddress = InetAddress.getByName(ip);
                 connect = new DatagramPacket(cmd.getBytes(),
                     cmd.getBytes().length, inetaddress, PORT);
                 System.out.println("Sending request" +
                     connect.getAddress());
                 socket.send(connect);
             }
             catch (IOException e)
             {
                 System.out.println("Could not connect.");
                 continue;
             }
 
             // wait for response
             try {
                 received = new DatagramPacket(buffer, buffer.length);
                 socket.receive(received);
             }
             catch (SocketTimeoutException e)
             {
                 System.out.println("Server took too long to respond.");
                 continue;
             }
             catch (IOException i)
             {
                 System.out.println("Error: IO exception while reading " +
                     "from socket.");
                 continue;
             }
 
             // evaluate response
             String pack = new String(
                 received.getData(), 0, received.getLength());
             if (!pack.equals("OKGO"))
                 continue;
 
             // ask user for file name
             System.out.println("File to transfer: ");
             filename = scan.nextLine();
             if (filename.equals("quit"))  // user wants to quit
                 break;
 
             // ask server for file
             try {
                 DatagramPacket fileName = new DatagramPacket(
                     filename.getBytes(), filename.getBytes().length,
                     InetAddress.getByName(ip), PORT);
                 socket.send(fileName);
             }
             catch (IOException e)
             {
                 System.out.println("Error: IO exception while sending " +
                     "filename via socket.");
                 continue;
             }
 
             // wait for response
             try {
                 socket.receive(received);
             }
             catch (IOException e)
             {
                 System.out.println("Error: IO exception while receiving " +
                     "server's response.");
                 continue;
             }
 
             // check if there was an error
             pack = new String(received.getData(), 0, received.getLength());
             if (pack.startsWith("Error"))
            {
                System.out.println(pack);
                 continue;
            }
             // create local file
             String[] splitName = filename.split("/");
             File newFile = new File("local_" + 
                 splitName[splitName.length - 1]);
             if (newFile.exists())
             {
                 System.out.println("Error: can't write to disk");
                 continue;
             }
 
             // try to create output file and file stream
             FileOutputStream attemptStream = null;
             try {
                 newFile.createNewFile();
                 attemptStream = new FileOutputStream(newFile);
             }
             catch (IOException e)
             {
                 System.out.println("Error: IO exception while attempting to " +
                     "open file for writing.");
                 continue;
             }
 
             // set up variables
             final Long fileSize = Long.parseLong(pack);
             final FileOutputStream fos = attemptStream;
             final long receivedLength = received.getLength();
 
             System.out.println("Expected file size: " +
                 fileSize);
 
             // create an instance of the reliable data handler
             new TransferProtocolClient(socket, inetaddress, PORT,
                 (long) Math.ceil(fileSize * 1.0 / Chunk.DATA_BYTES),
                 createChunkHandler(fileSize, fos));
         }
     }
 
     /**
      * Creates a chunk handler which will write to a given output stream.
      * @param fileSize the expected file size in bytes
      * @param fos the file output stream to write to
      * @return a chunk handler
      */
     private static IChunkHandler createChunkHandler(final long fileSize,
         final FileOutputStream fos)
     {
         return new IChunkHandler(){
             private long have = 0;
             private long total = fileSize;
 
             public void receiveData(Chunk data)
             {
                 try
                 {
                     long need = total - have;
                     if (need < 0)
                     {
                         System.out.println("Shouldn't " +
                             "be getting more bytes.");
                         return;
                     }
                     fos.write(data.getData(), 0,
                         need < Chunk.DATA_BYTES ?
                         (int) need : data.getData().length);
                     have += data.getData().length;
                     System.out.print(
                         (long) (have * 100. / total) + 
                         "%" +"\r");
                 }
                 catch (IOException e)
                 {
                     System.out.println("IOException while " +
                         "writing to file.");
                     System.exit(1);
                 }
             }
             public void finish()
             {
                 try
                 {
                     fos.close();
                 }
                 catch (IOException e)
                 {
                     System.out.println("IOException while " +
                         "closing file.");
                     System.exit(1);
                 }
             }
         };
     }
 }
