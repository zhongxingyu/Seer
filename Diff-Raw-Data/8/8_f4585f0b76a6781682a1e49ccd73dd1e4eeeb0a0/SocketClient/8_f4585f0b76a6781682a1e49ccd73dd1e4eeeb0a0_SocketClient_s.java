 package test;
 
 import java.net.*;
 import java.io.*;
 
 public class SocketClient {
 
     private String address = "140.112.90.46";
     private int port = 8666;
 
     public SocketClient() {
         Socket client = new Socket();
         InetSocketAddress isa = new InetSocketAddress(this.address, this.port);
         try {
             client.connect(isa, 10000);
             BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
 
            String input;
             BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
            input = buf.readLine();
             while(!input.equals("quit")) {
                out.write(input.getBytes());
                 input = buf.readLine();
                 out.flush();
             }
             out.close();
             out = null;
             client.close();
             client = null;
         } catch (IOException e) {
             System.out.println("Socket error!");
             System.out.println(e.toString());
         }
     }
 
     public static void main(String args[]) {
         new SocketClient();
     }
 }
