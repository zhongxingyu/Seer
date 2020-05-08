 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dht;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tcc10a
  */
 public class DHTNode extends Thread {
 
     private static String self;
     private static String nextNode;
     private static final int port = 1138;
     private static int keyVal;
     private static Properties keylist;
     public static final int PORT_NUMBER = 1138;
     protected Socket socket;
 
     public static void main(String[] args) {
         ServerSocket server = null;
         try {
             server = new ServerSocket(PORT_NUMBER);
             while (true) {
                 DHTNode forked = new DHTNode(server.accept());
             }
         } catch (IOException ex) {
             System.out.println("Unable to start server or accept connections");
             System.exit(1);
         } finally {
             try {
                 server.close();
             } catch (IOException ex) {
                 // not much can be done: log the error
                 // exits since this is the end of main
             }
         }
     }
 
     private DHTNode(Socket socket) throws IOException {
         this.socket = socket;
         Properties prop = new Properties();
         keylist = new Properties();
         prop.load(new FileInputStream("/home/dht/props/config.properties"));
         keylist.load(new FileInputStream("/home/dht/props/keylist.properties"));
         try {
             self = java.net.InetAddress.getLocalHost().getHostName();
             nextNode = prop.getProperty("NEXT");
             System.out.println(prop.getProperty("KEYVAL"));
             keyVal = Integer.parseInt(prop.getProperty("KEYVAL"));
         } catch (UnknownHostException ex) {
             Logger.getLogger(DHTNode.class.getName()).log(Level.SEVERE, null, ex);
             System.err.println("Problem getting hostname.");
             System.exit(1);
         } 
         start();
     }
 
     // the server services client requests in the run method
     @Override
     public void run() {
         try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
             while (true) {
                 String clientMessage = inFromClient.readLine();
                 System.out.println("Received: " + clientMessage);
                 if (clientMessage.toLowerCase().equals("shutdown")) {
                     outToClient.writeBytes("goodbye");
                     break;
                 } else if (clientMessage.toLowerCase().equals("keyval")) {
                     outToClient.writeBytes(Integer.toString(keyVal));
                     break;
                 } else if (clientMessage.contains("article")) {
                     String request = clientMessage.substring(8);
                     //System.out.println(request);
                     int req = Integer.parseInt(request);
                     //System.out.println(req);
                     int key = Integer.parseInt(keylist.getProperty("" + req));
                     System.out.println(key);
                     if (key > keyVal) {
                         System.out.println("Failed.");
                         outToClient.writeBytes("FAIL: " + nextNode);
                     } else {
                         System.out.println("It's ours!");
                         outToClient.writeBytes(Integer.toString(key));
                     }
                 }
             }
         } catch (IOException ex) {
             System.out.println("Unable to get streams from client");
         } finally {
             try {
                 socket.close();
             } catch (IOException ex) {
                 // not much can be done: log the error
                 ex.printStackTrace();
             }
         }
     }
 }
