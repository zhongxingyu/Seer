 /**
 * clientUDP.java
 * Author: John Huang
 * Course: ECE428
 * Written in 2010
 */
 
 import java.io.*;
 import java.net.*;
 
 /**
 * The client class that uses the UDP protocol to implement the World Cup players program. The program
 * makes use of a file that reads what port the serverUDP is listening to. At the end of the execution,
 * the client deletes that port file.
 */
 public class clientUDP {
     // Constants
 	private static final String IP = "127.0.0.1";
 	private static final String PORT_FILENAME = "portUDP.ini";
 	private static final String OUTPUT_FILENAME = "outUDP.dat";
     private static final String TERMINATION_STRING = "DONE";
 
     // Variables
     private String filename;
     private String country;
     private DatagramSocket socket = null;
     private int port;
     private InetAddress address  = null;
 
 	/**
 	 * Constructor for clientUDP.
 	 *
 	 * Args:
 	 *   filename			The filename that consists the number of players.
 	 *   country			The country used to filter the list of players.
 	 *   socket				The socket connection used to the UDP server.
 	 *   port				The port used to connect to the UDP server.
 	 */
     public clientUDP(String filename, String country, DatagramSocket socket, int port) {
         this.filename = filename;
         this.country = country;
         this.socket = socket;
         try {
             this.address = InetAddress.getByName(IP);
         }
         catch (UnknownHostException e) {
 			System.err.println("[ERROR]: Unable to retrieve IP.");
         }
         this.port = port;
     }
 
 	/**
 	 * Sends the list of players to the UDP server.
 	 */
     public void sendPlayers() {
         // Read the input file.
         BufferedReader inputStream = null;
 
         try {
             inputStream = new BufferedReader(new FileReader(filename));
             // Send the country data to the server first.
             this.sendPacket(this.country);
 
             // Loop through the lines in the file and send each one to the
             // server after it has been read.
             String line;
             while ((line = inputStream.readLine()) != null) {
                 this.sendPacket(line);
             }
 
             // Tell the server that we are done sending data.
             this.sendPacket(TERMINATION_STRING);
 
             inputStream.close();
         }
         catch (Exception e) {
             System.err.println("[ERROR] Error sending players to server.");
         }
     }
 
 	/**
 	 * Sends a packet to the UDP server.
 	 *
 	 * Args:
 	 *   data				The string of data to send to the UDP server.
 	 */
     private void sendPacket(String data) {
         byte[] buf;
         buf = data.getBytes();
         DatagramPacket packet = new DatagramPacket(buf, buf.length,
                                                    this.address, this.port);
         try {
             this.socket.send(packet);
         }
         catch (Exception e) {
             System.err.println("[ERROR] Error sending packet to server: " + e.getMessage());
         }
     }
 
 	/**
 	 * Method to recieve the processed players from the server.	 
 	 */
     public void receivePlayers() {
         PrintWriter outputStream = null;
 
         try {
             outputStream = new PrintWriter(new FileWriter(OUTPUT_FILENAME));
             String received = this.receivePacket();
             int numPlayers = Integer.parseInt(received);
 
             if (numPlayers == 0) {
                outputStream.println(this.country + " did not qualify for the world cup.");
             }
             else {
                 for (int i = 0; i < numPlayers; i++) {
                     String player = this.receivePacket();
                     outputStream.println(player);
                 }
             }
 
             outputStream.close();
         }
         catch (Exception e) {
             System.err.println("[ERROR] Error receiving players from server.");
         }
     }
 
 	/**
 	 * Method to receive packet from the server.
 	 */
     private String receivePacket() {
         byte[] buf = new byte[1024];
         DatagramPacket packet = new DatagramPacket(buf, buf.length);
         try {
             this.socket.receive(packet);
         }
         catch (Exception e) {
             System.err.println("[ERROR] Error receiving packet from server.");
         }
         return new String(packet.getData(), 0, packet.getLength());
     }
 
 	/**
 	 * Busy loop to wait for the port filename to exist. If it does, the method retrieves the port number.
 	 */
     private static int getPortNumber() {
         int port = -1;
 
 		try
 		{
 			File f = null;
 			while(true)
 			{
 				f = new File(PORT_FILENAME);
 				if(f.exists())
 				{
 					String line = new BufferedReader(new FileReader(PORT_FILENAME)).readLine();
 					port = Integer.parseInt(line);
 					break;
 				}
 			}
 		}
 		catch (IOException e)
 		{
 			System.err.println("[ERROR] Error getting port number.");
 		}
         return port;
     }
 	
 	/**
 	 * Main Method.
 	 */
     public static void main(String[] args) throws IOException {
         if (args.length != 2) {
              System.out.println("USAGE: java clientUDP [filename] [country]");
              return;
         }
         
         // Pick up port from server-generated file.
         int port = getPortNumber();
 
         // Create a datagram socket.
         DatagramSocket socket = new DatagramSocket();
 
         clientUDP client = new clientUDP(args[0], args[1], socket, port);
 		
         // Send country and player data to server.
         client.sendPlayers();
 
         // Receive team player list from server.
         client.receivePlayers();
 		
         // Close the socket.
         socket.close();
     }
 }
