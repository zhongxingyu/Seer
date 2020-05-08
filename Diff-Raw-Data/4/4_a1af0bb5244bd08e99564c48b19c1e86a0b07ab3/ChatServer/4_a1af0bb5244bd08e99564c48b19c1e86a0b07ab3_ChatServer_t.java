 /*
  * Copyright (C) 2013 MineStar.de 
  * 
  * This file is part of ConAir.
  * 
  * ConAir is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * ConAir is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.conair.network;
 
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import de.minestar.conair.core.Core;
 
 public class ChatServer implements Runnable {
 
     private final ByteBuffer networkBuffer;
 
     private Selector selector;
 
     private ServerSocketChannel serverSocket;
 
     private boolean isRunning;
 
     private PacketHandler packetHandler;
 
     public ChatServer(int port) throws Exception {
 
         this.networkBuffer = ByteBuffer.allocateDirect(4096);
 
         this.packetHandler = new PacketHandler(networkBuffer);
 
         this.selector = Selector.open();
 
         // Listening on the port
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port));

         // Non-Blocking for Selector activity
         serverSocket.configureBlocking(false);
         serverSocket.register(selector, SelectionKey.OP_ACCEPT);
 
         isRunning = true;
     }
 
     @Override
     public void run() {
         try {
             while (isRunning) {
                 int rdyChannels = selector.select();
                 // No channel want something
                 if (rdyChannels == 0) {
                     continue;
                 }
 
                 // Iterate over all channel which want something
                 Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                 while (it.hasNext()) {
                     SelectionKey key = it.next();
                     // New client wants to connect
                     if (key.isAcceptable()) {
                         // accept new client
                         onClientAccept();
                     }
                     // client want to send something
                     if (key.isReadable()) {
                         onClientRead(key);
                     }
                     // client can receive something
                     if (key.isWritable()) {
                         onClientWrite(key);
                     }
                     it.remove();
                 }
 
             }
         } catch (Exception e) {
             Logger.getLogger(Core.NAME).throwing("de.minestar.conair.core.network.ChatServer", "run", e);
             isRunning = false;
         }
     }
 
     public void stop() {
         this.isRunning = false;
     }
 
     /*
      * ACCEPTING
      */
 
     public void onClientAccept() throws Exception {
         // accept new client
         SocketChannel clientSocket = serverSocket.accept();
         clientSocket.configureBlocking(false);
 
         clientSocket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE).attach(new ConnectedClient(clientSocket.getRemoteAddress().toString()));
     }
 
     /*
      * READING
      */
     public void onClientRead(SelectionKey key) throws Exception {
         if (!(key.channel() instanceof SocketChannel)) {
             return;
         }
         // Read into the clients specific buffer
         SocketChannel channel = (SocketChannel) key.channel();
         ConnectedClient client = (ConnectedClient) key.attachment();
         // When readfrom fails the client has disconnected
         if (!client.readFrom(channel)) {
             key.cancel();
         }
 
         if (packetHandler.isPacketComplete(client.getClientBuffer())) {
             NetworkPacket packet = packetHandler.extractPacket(client.getClientBuffer());
             handlePacket(client, packet);
         }
 
     }
 
     // Handle a single packet
     private void handlePacket(ConnectedClient src, NetworkPacket packet) {
         // We have a broadcast server - broadcast all packages
         broadcastPacket(src, packet);
     }
 
     // Deliver the packet the all other clients
     private void broadcastPacket(ConnectedClient src, NetworkPacket packet) {
         Set<SelectionKey> keys = selector.keys();
         for (SelectionKey key : keys) {
             if (!(key.channel() instanceof SocketChannel))
                 continue;
             ConnectedClient client = (ConnectedClient) key.attachment();
             if (client.equals(src))
                 continue;
 
             client.addPacket(networkBuffer);
         }
         networkBuffer.clear();
     }
 
     /*
      * WRITINGG
      */
     private void onClientWrite(SelectionKey key) throws Exception {
         if (!(key.channel() instanceof SocketChannel)) {
             return;
         }
 
         ConnectedClient client = (ConnectedClient) key.attachment();
         if (client.hasDataToSend()) {
             // If write fails the client has disconnected
             if (!client.write((SocketChannel) key.channel())) {
                 key.cancel();
             }
         }
     }
 }
