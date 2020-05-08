 package li.test.web.clent;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class HttpClient {
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private Socket socket;
 
     public static void main(String[] args) throws IOException,
             InterruptedException {
         HttpClient client = new HttpClient();
         client.initSocket("127.0.0.1", 8080);
         try {
             client.sendRequest();
             boolean loop = true;
             while (loop) {
                 loop = !client.receiveResponse();
                 Thread.sleep(100);
             }
 
         } finally {
             client.close();
         }
     }
 
     public void initSocket(String host, int port) {
         try {
             this.socket = new Socket(InetAddress.getByName(host), port);
         } catch (IOException e) {
             logger.info("can not create socket:", e);
         }
     }
 
     public void sendRequest() {
         OutputStream outputStream = null;
         try {
             outputStream = socket.getOutputStream();
         } catch (IOException e) {
             logger.info("fail to get output stream from the socket");
         }
         PrintWriter writer = new PrintWriter(outputStream, false);
 
         // send http request to the http server
         writer.println("GET /index.jsp HTTP/1.1");
         writer.println("HOST: localhost:8080");
         writer.println("Connection: close");
         writer.flush();
     }
 
     public boolean receiveResponse() {
         BufferedReader in = null;
         try {
             in = new BufferedReader(new InputStreamReader(
                     socket.getInputStream()));
         } catch (IOException e) {
             logger.info("fail to get input stream from the socket:", e);
             return false;
         }
         StringBuffer buffer = new StringBuffer(8096);
 
         boolean isReady = false;
         try {
             isReady = in.ready();
         } catch (IOException e) {
             logger.info(
                     "fail to get read the state of the input stream of socket:",
                     e);
             return false;
         }
 
         if (!isReady) {
             logger.info("no response");
             return false;
         }
 
         try {
             int temp = 0;
             while (temp != -1) {
                 temp = in.read();
                 buffer.append((char) temp);
             }
 
             System.out.println(buffer.toString());
         } catch (IOException e) {
             logger.info("fail to read");
             return false;
         }
         return true;
     }
 
     public void close() {
         if (!socket.isClosed()) {
             try {
                 socket.close();
             } catch (IOException e) {
                 logger.info("fail to close the socket:", e);
             }
         }
     }
 
     public void sendRequestAndReciveResponse() throws IOException,
             InterruptedException {
         Socket socket = null;
         OutputStream outputStream = null;
         socket = new Socket(InetAddress.getByName("127.0.0.1"), 8080);
         outputStream = socket.getOutputStream();
 
         PrintWriter writer = new PrintWriter(outputStream, true);
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 socket.getInputStream()));
 
         // send http request to the http server
         writer.println("GET /index.jsp HTTP/1.1");
         writer.println("HOST: localhost:8080");
         writer.println("Connection: close");
 
         // read the response
         boolean loop = true;
         StringBuffer sb = new StringBuffer(8096);
         while (loop) {
             if (in.ready()) {
                 int i = 0;
                 while (i != -1) {
                     i = in.read();
                     sb.append((char) i);
                 }
                 loop = false;
             }
             Thread.sleep(50);
         }
 
         // display the response
         System.out.println(sb.toString());
         socket.close();
     }
 }
