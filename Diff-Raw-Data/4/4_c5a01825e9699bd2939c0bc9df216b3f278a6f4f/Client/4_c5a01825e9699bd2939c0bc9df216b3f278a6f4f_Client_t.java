 // ______________________________________________________________________________
 //
 //           Project: jolokia-munin-plugin
 //            Module: jolokia-munin-plugin
 //             Class: Client
 //              File: Client.java
 //        changed by: christian
 //       change date: 23.04.13 19:06
 //       description:
 // ______________________________________________________________________________
 //
 //         Copyright: (c) Christian Fritz, all rights reserved
 // ______________________________________________________________________________
 package de.chrfritz.jolokiamunin.daemon;
 
 import de.chrfritz.jolokiamunin.config.Configuration;
 import de.chrfritz.jolokiamunin.controller.Dispatcher;
 import de.chrfritz.jolokiamunin.munin.MuninProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.nio.charset.Charset;
 
 public class Client implements Runnable {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);
 
     private static final int SOCKET_TIMEOUT = 1000 * 15;
 
     private final Socket clientSocket;
 
     private final Configuration configuration;
 
     private final MuninProvider muninProvider;
     private final Charset charset = Charset.forName("UTF-8");
 
     public Client(Socket clientSocket, MuninProvider muninProvider, Configuration configuration) {
         this.muninProvider = muninProvider;
         this.configuration = configuration;
         this.clientSocket = clientSocket;
         LOGGER.info("Connection accepted from {}", clientSocket.getRemoteSocketAddress());
     }
 
     /**
      * @see Thread#run()
      */
     @Override
     public void run() {
         try (
                 Socket socket = clientSocket;
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), charset))
         ) {
             socket.setSoTimeout(SOCKET_TIMEOUT);
             while (!Thread.interrupted()) {
                String command = reader.readLine();
                String response = handleCommands(command.trim());
                 writer.write(response);
                 writer.flush();
             }
         }
         catch (SocketTimeoutException e) {
             LOGGER.info("Closing connection because of read timeout");
         }
         catch (IOException e) {
             LOGGER.error("Can not open streams", e);
         }
     }
 
     protected String handleCommands(String commandLine) {
         String[] parts = commandLine.split("[\n ]+", 2);
         String command = parts[0];
         switch (command) {
             case "quit":
             case "exit":
                 Thread.currentThread().interrupt();
                 return "";
             default:
                 Dispatcher dispatcher = new Dispatcher(configuration, muninProvider);
                 return dispatcher.handleRequest(commandLine) + "\n";
         }
     }
 }
