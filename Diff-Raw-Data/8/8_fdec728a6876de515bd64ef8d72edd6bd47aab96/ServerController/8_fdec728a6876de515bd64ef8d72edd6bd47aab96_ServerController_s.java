 package server.test.controller;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Rik Schaaf
  */
 public class ServerController extends Thread {
 
     public static final int ERROR = 0; //Send when a corrupt packet was received
     public static final int OK = 1; //Send when a packet was succesfully received
     public static final int SERVER_READY = 2; //Ask whether or not the server is ready (respond with OK).
     public static final int CLOSE_SERVER = 3;
     /**/
     public static final int SEND_JOIN_REQUEST = 100;
     public static final int SEND_JOIN_ERROR = 1000;
     public static final int SEND_JOIN_ACCEPTED = 1001;
     public static final int SEND_SPECTATE_REQUEST = 101;
     public static final int SEND_SPECTATE_ERROR = 1010;
     public static final int SEND_SPECTATE_ACCEPTED = 1011;
     /**/
     public static final int SEND_GAME_OBJECT_DATA = 200;
     public static final int NOT_READY_FOR_GAME_OBJECT_DATA = 2000;
     public static final int READY_FOR_GAME_OBJECT_DATA = 2001;
     public static final int GAME_OBJECT_DATA_ERROR = 2010;
     public static final int GAME_OBJECT_DATA_ACCEPTED = 2011;
     public static final int GAME_OBJECT_DATA_INCOMPLETE = 2012;
     public static final int CANCEL = Integer.MAX_VALUE; //CANCEL can be used if a wrong input was given
     /**/
     public static final int SEND_GAME_OVER_WON = 300;
     public static final int SEND_GAME_OVER_LOST = 301;
     /**/
     public static final int SEND_TEXT_MESSAGE = 400;
     public static final int TEXT_MESSAGE_RECEIVED = 4000;
     public static final int TEXT_MESSAGE_ERROR = 4001;
     /**/
     DatagramSocket serverSocket;
     DatagramPacket receivePacket;
     DatagramPacket sendPacket;
 
     public ServerController(int port) throws SocketException {
         this.serverSocket = new DatagramSocket(port);
     }
 
     @Override
     public void run() {
         while (true) {
             try {
                 receivePacket = new DatagramPacket(new byte[4096], 4096);
                 serverSocket.receive(receivePacket);
                 DataInputStream in = new DataInputStream(new ByteArrayInputStream(receivePacket.getData()));
                 ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
                 DataOutputStream out = new DataOutputStream(bout);
                 switch (in.readInt()) {
                     case ServerController.SERVER_READY:
                         out.writeInt(ServerController.OK);
                         sendPacket = new DatagramPacket(bout.toByteArray(), bout.toByteArray().length, receivePacket.getAddress(), receivePacket.getPort());
                         serverSocket.send(sendPacket);
                         break;
                     case ServerController.CLOSE_SERVER:
                         return;
                     case ServerController.SEND_GAME_OBJECT_DATA:
                         out.writeInt(ServerController.READY_FOR_GAME_OBJECT_DATA);
                         sendPacket = new DatagramPacket(bout.toByteArray(), bout.toByteArray().length, receivePacket.getAddress(), receivePacket.getPort());
                         serverSocket.send(sendPacket);
                         receivePacket = new DatagramPacket(new byte[4096], 4096);
                         serverSocket.receive(receivePacket);
                         in = new DataInputStream(new ByteArrayInputStream(receivePacket.getData()));
 
                         int x = in.readInt();
                         int y = in.readInt();
                         int dx = in.readInt();
                         int dy = in.readInt();
                         System.out.println("SERVER: client send x=" + x + ", y=" + y + ", dx=" + dx + ", dy=" + dy + ".");
                         bout = new ByteArrayOutputStream(4096);
                         out = new DataOutputStream(bout);
                         out.writeInt(ServerController.GAME_OBJECT_DATA_ACCEPTED);
                         sendPacket = new DatagramPacket(bout.toByteArray(), bout.toByteArray().length, receivePacket.getAddress(), receivePacket.getPort());
                         serverSocket.send(sendPacket);
                         break;
                 }
             } catch (SocketException ex) {
                 Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(ServerController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
