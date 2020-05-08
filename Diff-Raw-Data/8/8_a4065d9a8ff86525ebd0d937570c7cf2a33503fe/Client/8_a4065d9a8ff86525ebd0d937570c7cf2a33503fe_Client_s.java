 package client;
 
 /**
  * Title:        Client class
  * Description:  Client that connects to a server, receives and displays a
  *               message and then terminates
  * 
  * This class does not have any output streams set up yet, because
  * at the moment it only reads information from the server.
  *
  * @author Sam Beed B0632953
  */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.*;
 
 public class Client extends Thread
 {
     //Streams used for communicating with server
     private InputStream is;
     private BufferedReader fromServer;
     private Socket socket;    // Socket to server
     private static final int SERVER_PORT_NUMBER = 3000;
     private boolean connected;
    private boolean running;
 
     /**
      * Constructor
      */
     public Client() {
         connected = false;
        running = true;
     }
     
     /**
      * This is the client's main method - it performs a single
      * interaction with the server using the processHello method
      */
     @Override
     public void run()
     {     
        while (running) {
             
         }
         
         try {
             closeStreams();
             socket.close();   
         }
         catch (IOException e) {
             System.out.println("Exception in client run " + e);
         }
     }
 
     /*
      * This method creates a socket on the local host to
      * the specified port number, for communications with the server
      */
     public boolean connectToServer() {
         try {
             //this is a portable way of getting the local host address
             final InetAddress SERVER_ADDRESS = InetAddress.getLocalHost();
             System.out.println("Attempting to contact " + SERVER_ADDRESS);
 
             socket = new Socket(SERVER_ADDRESS, SERVER_PORT_NUMBER);
             openStreams();
             connected = true;
             return true;
         }
         catch (IOException e) {
             String ls = System.getProperty("line.separator");
             System.out.println(ls + "Trouble contacting the server: " + e);
             System.out.println("Perhaps you need to start the server?");
             System.out.println("Make sure they're talking on the same port?" + ls);
             return false;
         }
     }
     
 
     // open streams for communicating with the server
     private void openStreams() throws IOException
     {
         is = socket.getInputStream();
         fromServer = new BufferedReader(new InputStreamReader(is));
     }
 
     // close streams to server
     private void closeStreams() throws IOException
     {
         fromServer.close();
         is.close();
     }
 
     //An example method that completes a single interaction with the server
     //In this case, the client doesn't say anything to the server
     private void processHello() throws IOException
     {
         String messageFromServer = fromServer.readLine();
         System.out.println("Server said: " + messageFromServer); //display message
     }
     
     /**
      * Returns true if a connection is currently up.
      * @return Connection status
      */
     public boolean isConnected() {
         return connected;
     }
     
     /**
      * Ends the program.
      */
     public void quit() {
         System.exit(0);
     }
 }
