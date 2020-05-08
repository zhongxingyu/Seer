 package jnet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Scanner;
 
 
 public class Client {
     private String ipAddress;
     private int portNumber;
     
     private Socket socket;
     private PrintWriter out;
     private BufferedReader in;
     
     /**
      * Creates a new Client networking object without opening a connection.
      * The IP address and port number are set to default values.
      */
     public Client() {
         this.ipAddress = "127.0.0.1";
         this.portNumber = 8000;
     }
     
     /**
      * Creates a new Client networking object without opening a connection.
      * 
      * @param ipAddress the IP address of the server to connect to
      * @param portNumber the port number on the server to connect on
      */
     public Client(String ipAddress, int portNumber) {
         this.ipAddress = ipAddress;
         this.portNumber = portNumber;
     }
     
     /**
      * Creates a new Client networking object. 
      * If connectNow is true, this will also open a connection to the server.
      * 
      * @param ipAddress the IP address of the server to connect to
      * @param portNumber the port number on the server to connect on
      * @param connectNow true to connect to the server now; false otherwise
      * @throws IOException if the client fails to connect to the server
      */
     public Client(String ipAddress, int portNumber, boolean connectNow) throws IOException {
         this.ipAddress = ipAddress;
         this.portNumber = portNumber;
         
         if (connectNow)
             reconnect();
     }
 
     /**
      * Connects to the server at the specified IP address on the specified port number.
      * 
      * @param ipAddress the IP address of the server to connect to
      * @param portNumber the port number on the server to connect on
      * @throws IOException if the client fails to connect to the server
      */
     public void connect(String ipAddress, int portNumber) throws IOException {
         this.ipAddress = ipAddress;
         this.portNumber = portNumber;
         
         reconnect();
     }
     
     /**
      * Connects to the last server connected to. 
      * If a connection is currently open, it will close it first.
      * 
      * @throws IOException if the client fails to connect to the server
      */
     public void reconnect() throws IOException {
         if (socket != null)
             close();
         
         try {
             socket = new Socket();
             socket.connect(new InetSocketAddress(ipAddress, portNumber), 4000);
             out = new PrintWriter(socket.getOutputStream(), true);        
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         } catch (IOException e) {
             socket = null;
             out = null;
             in = null;
             throw e;
         }
     }
     
     /**
      * Sends a message to the server.
      * The message should not contain any newline characters.
      * 
      * @param message the message to send to the server
      * @throws IOException
      */
     public void send(String message) throws IOException {
         if (out == null)
             reconnect();
         
         out.println(message);
     }
     
     /**
      * Receives a message from the server. This method will block until data arrives.
      * If an error occurs when receiving the response, the connection will be closed and the exception thrown.
      * 
      * @return the message from the server
      * @throws IOException if an error occurs when receiving the server's response
      */
     public String receive() throws IOException {
         if (in == null)
             reconnect();
         
         try {
             return in.readLine();
         } catch (IOException e) {
             close();
             throw e;
         }
     }
     
     /**
      * Closes down the client's connection to the server.
      * 
      * @throws IOException if an error occurs when closing the connection
      */
     public void close() throws IOException {
         if (socket == null)
             return;
         
         try {
             out.close();
             in.close();
             socket.close();
         } catch (IOException e) {
             socket = null;
             out = null;
             in = null;
             throw e;
         }
     }
     
     public static void main(String[] args) {
         Client c = new Client();
         Scanner scan = new Scanner(System.in);
         
         try {
             c.reconnect();
             while (true) {
                 String str = scan.nextLine();
                 if (str.equalsIgnoreCase("exit"))
                     break;
                 
                 c.send(str);
                 System.out.println(c.receive());
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 c.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
