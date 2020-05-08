 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 /**
 * This program will open up a ServerSocket listening for connections on port :8080.
  * When a client connects, the server will retrieve it's input and output streams, read
 * and print out the HTTP request status line before it sends back a HTTP response.
  * @author folkol
  */
 public class HttpServer {
     public static void main(String[] args) throws Exception {
         ServerSocket server = new ServerSocket(8080);
         while(true) {
             Socket accept = server.accept();
             BufferedReader in = new BufferedReader(new InputStreamReader(accept.getInputStream()));
             OutputStream out = accept.getOutputStream();
 
             System.out.println(in.readLine());
 
             out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
             out.write("Hello World".getBytes());
 
             accept.close();
         }
     }
 }
