 package com.github.zhongl.nij.bio;
 
 import java.io.*;
 import java.net.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import com.github.zhongl.nij.util.Utils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Server {
   private static volatile boolean running = true;
   private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
   private static final ExecutorService SERVICE = Executors
       .newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
   private static final byte[] BUFFER = new byte[1024];
   private static final byte[] RESPONSE = "HTTP/1.0 200 OK\r\nContent-Length:1\r\n\r\na".getBytes();
 
   public static void main(String... args) throws Exception {
     final String host = args[0];
     final int port = Integer.parseInt(args[1]);
     final int backlog = Integer.parseInt(args[2]);
     final int size = Integer.parseInt(args[3]);
     final SocketAddress address = new InetSocketAddress(host, port);
 
     Runtime.getRuntime().addShutdownHook(new Thread("shutdow-hook"){
       @Override
       public void run() { running = false; }
     });
 
     ServerSocket socket = new ServerSocket();
     socket.setReceiveBufferSize(Utils.kb(size));
     socket.setReuseAddress(true);
     socket.setSoTimeout(500);
     socket.bind(address, backlog);
 
     System.out.println("Started at " + host + ":" + port +
         " with backlog: " + backlog +
         " receive buffer: " + size + "k.");
 
     while (running) {
       try { handle(socket.accept()); } catch (SocketTimeoutException e) { }
     }
     silentClose(socket);
     SERVICE.shutdownNow();
     System.out.println("Stopped.");
   }
 
   private static void handle(final Socket accept) {
     SERVICE.execute(new Runnable() {
       @Override
       public void run() {
         try {
           accept.setTcpNoDelay(true);
           accept.setSendBufferSize(1 * 1024);
           final InputStream inputStream = new BufferedInputStream(accept.getInputStream()) ;
           final OutputStream outputStream = new BufferedOutputStream(accept.getOutputStream());
          inputStream.read(BUFFER);
           outputStream.write(RESPONSE);
 //          outputStream.flush();
//          outputStream.close();
//          inputStream.close();
         } catch (IOException e) {
           LOGGER.error("Unexpected error", e);
         } finally {
           silentClose(accept);
         }
       }
     });
   }
 
   private static void silentClose(ServerSocket socket) { try {socket.close(); } catch (IOException e) { } }
 
   private static void silentClose(Socket accept) { try { accept.close(); } catch (IOException e1) { } }
 }
