 package org.runetekk;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.math.BigInteger;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.Properties;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 
 /**
  * Main.java
  * @version 1.0.0
  * @author RuneTekk Development (SiniSoul)
  */
 public final class Main implements Runnable {
     
     /**
      * The {@link Logger} for this class.
      */
     private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
     
     /**
      * The size of all the incoming packets from the client.
      */
     private static final int[] INCOMING_SIZES;
     
     /**
      * The private key for deciphering the enciphered login block.
      */
     private static final BigInteger PRIVATE_KEY;
     
     /**
      * The modulus for deciphering the enciphered login block.
      */
     private static final BigInteger MODULUS;
     
     /**
      * The amount of time in milliseconds before the garbage should be taken out.
      */
     private static final long GC_TIME = 30000;
     
     /**
      * The main handler.
      */
     private static Main main;
     
     /**
      * The regions for this handler.
      */
     static Region[][] regions;
     
     /**
      * The {@link Client} array.
      */
     static Client[] clientArray;
     
     /**
      * The music names array.
      */
     static String[] musicNames;
     
     /**
      * The farming type configuration ids.
      */
     static int[][] farmingTypeConfigs;
     
     /**
      * The farming patch states.
      */
     static int[][][] farmingPatchStates;
     
     /**
      * The maximum amount of players allowed to connect to this server.
      */
     static final int MAXIMUM_CLIENTS = 2048;
     
     /**
      * The local thread.
      */
     private Thread thread;
     
     /**
      * The local thread is currently paused.
      */
     private boolean isPaused;
     
     /**
      * The {@link ServerSocket} to accept connections from.
      */
     private ServerSocket serverSocket;
     
     /**
      * The list of clients currently connected to the server.
      */
     ListNode activeList;
     
     /**
      * The list of client ids removed from the server.
      */
     ListNode removedList;
     
     /**
      * The offset in the client list.
      */
     int listOffset;
     
     /**
      * The {@link IoWriter} for this handler.
      */
     IoWriter writer;
     
     /**
      * The next time when the garbage will be taken out.
      */
     long nextGc;
     
     /**
      * Prints the application tag.
      */
     private static void printTag() {
         System.out.println(""
         + "                     _____               _______   _    _                           "
         + "\n                    |  __ \\             |__   __| | |  | |                       "
         + "\n                    | |__) |   _ _ __   ___| | ___| | _| | __                     "
         + "\n                    |  _  / | | | '_ \\ / _ \\ |/ _ \\ |/ / |/ /                  "
         + "\n                    | | \\ \\ |_| | | | |  __/ |  __/   <|   <                    "
         + "\n                    |_|  \\_\\__,_|_| |_|\\___|_|\\___|_|\\_\\_|\\_\\             "
         + "\n----------------------------------------------------------------------------------"
         + "\n                                Game Server 1.1.3                                 "
         + "\n                                 See RuneTekk.com                                 "
         + "\n                               Created by SiniSoul                                "
         + "\n----------------------------------------------------------------------------------");
     }
     
     /**
      * Loads the regions for the server.
      */
     private static void loadRegions(DataInputStream is) throws IOException {
         regions = new Region[256][];
         int opcode = 0;
         while((opcode = is.read()) != 0) {
             switch(opcode) {
                 case 1:
                     {
                         int regionX = is.read();
                         int amountRegions = is.read();
                         regions[regionX] = new Region[255];                  
                         for(int i = 0; i < amountRegions; i++) {
                             int regionY = is.read();
                             regions[regionX][regionY] = new Region();
                             if(is.read() != 0) {
                                 int amountChunks = is.read();
                                 for(int j = 0; j < amountChunks; j++) {
                                     int hash = is.read();
                                     if(regions[regionX][regionY].chunks == null)
                                         regions[regionX][regionY].chunks = new Chunk[8][];
                                     if(regions[regionX][regionY].chunks[hash >> 3] == null)
                                         regions[regionX][regionY].chunks[hash >> 3] = new Chunk[8];
                                     regions[regionX][regionY].chunks[hash >> 3][hash & 0x7] = new Chunk();
                                 }
                             }
                         }
                         
                     }
                     break;
                     
                 case 2:
                     {
                         int x = is.read();
                         int y = is.read();
                         int id = is.read() << 8 | is.read();
                         Region region = null;
                         if(regions == null || regions[x] == null || (region = regions[x][y]) == null)
                             continue;
                         region.songId = id;
                     }
                     break;
             }
         }
     }
     
     /**
      * Loads the music configurations for the server.
      * @param is The data input stream to load the musics for.
      */
     private static void loadMusics(DataInputStream is) throws IOException {
         int maximumId = (is.read() << 8 | is.read()) + 1;
         int opcode = 0;
         while((opcode = is.read()) != 0) {
             switch(opcode) {
                 
                 case 1:
                     if(musicNames == null)
                         musicNames = new String[maximumId];
                     int id = is.read() << 8 | is.read();
                     String str = "";
                     int len = is.read();
                     while(len-- > 0) {
                         str += (char) is.read();
                     }
                     musicNames[id] = str;
                     break;
             }
         }
     }
     
     /**
      * Loads the farming configurations for the server.
      * @param is The data input stream to load the farming data for.
      */
     private static void loadFarming(DataInputStream is) throws IOException {
         int opcode = 0;
         while((opcode = is.read()) != 0) {
             switch(opcode) {
                 
                 case 1:
                     {
                         int type = is.read();
                         int amount = is.read();
                         int crops = is.read();
                         if(farmingTypeConfigs == null)
                             farmingTypeConfigs = new int[256][];
                         farmingTypeConfigs[type] = new int[amount];      
                         if(farmingPatchStates == null)
                             farmingPatchStates = new int[256][crops][];
                     }
                     break;
                     
                 case 2:
                     {
                         int type = is.read();
                         int patch = is.read();
                         farmingTypeConfigs[type][patch] = is.read() << 8 | is.read();
                     }
                     break;
                     
                 case 3:
                 case 4:
                 case 5:
                 case 6:
                     {
                         int type = is.read();
                         int crop = is.read();
                         if(farmingPatchStates[type][crop] == null)
                             farmingPatchStates[type][crop] = new int[4];
                         farmingPatchStates[type][crop][opcode - 3] = (is.read() << 8 | is.read()) | 1 << 31;
                     }
                     break;
             }
         }
     }
     
     /**
      * Initializes the local thread.
      */
     private void initialize() {
         writer = new IoWriter(this);
         thread = new Thread(this);
         thread.start();
     }
     
      @Override
     public void run() {
         for(;;) {
             synchronized(this) {
                 if(isPaused)
                     break;
                 Client acceptedClient = null;
                 try {
                      Socket socket = serverSocket.accept();
                      acceptedClient = new Client(socket);
                 } catch(IOException ex) {
                     if(!(ex instanceof SocketTimeoutException))
                         destroy();
                 }     
                 if(acceptedClient != null) {
                     int position = listOffset;
                     if(position >= MAXIMUM_CLIENTS) {
                         position = -1;
                         synchronized(removedList) {
                             ListNode node = removedList.childNode;
                             if(node != null) {
                                 position = ((IntegerNode) node).value;
                                 node.removeFromList();
                             }   
                         }
                     } else
                         ++listOffset;
                     try {
                         byte[] response = new byte[9];
                         if(position < 0) {
                             response[8] = 7;
                             acceptedClient.outputStream.write(response);
                             acceptedClient.destroy();
                         } else {
                             IntegerNode positionNode = new IntegerNode(position);
                             acceptedClient.localId = positionNode;  
                             if(acceptedClient.inputStream.read() != 14) {
                                 response[8] = 10;
                                 acceptedClient.outputStream.write(response);
                                 LOGGER.log(Level.WARNING, "Client disconnected : Invalid handshake op!");
                                 throw new IOException();
                             }
                             if((acceptedClient.nameHash = acceptedClient.inputStream.read()) < 0 || acceptedClient.nameHash > 31) {
                                 response[8] = 10;
                                 acceptedClient.outputStream.write(response);
                                 LOGGER.log(Level.WARNING, "Client disconnected : Invalid name hash - {0}!", acceptedClient.nameHash);
                                 throw new IOException();
                             } 
                             long sessionKey = UUID.randomUUID().getMostSignificantBits();
                             byte[] oldResponse = response;
                             response = new byte[17];
                             System.arraycopy(oldResponse, 0, response, 0, oldResponse.length);
                             response[9] =  (byte) (sessionKey >>> 56L);
                             response[10] = (byte) (sessionKey >>> 48L);
                             response[11] = (byte) (sessionKey >>> 40L);
                             response[12] = (byte) (sessionKey >>> 32L);
                             response[13] = (byte) (sessionKey >>> 24L);
                             response[14] = (byte) (sessionKey >>> 16L);
                             response[15] = (byte) (sessionKey >>> 8L);
                             response[16] = (byte) (sessionKey & 0xFFL);
                             positionNode.parentNode = activeList.parentNode;
                             positionNode.childNode = activeList;
                             positionNode.parentNode.childNode = positionNode;
                             positionNode.childNode.parentNode = positionNode;
                             acceptedClient.sessionKey = sessionKey;
                             acceptedClient.incomingBuffer = new byte[Client.BUFFER_SIZE];
                             acceptedClient.outputStream.write(response);
                             clientArray[position] = acceptedClient;
                             acceptedClient.timeoutStamp = System.currentTimeMillis() + 5000L;
                         }
                     } catch(Exception ex) {
                         acceptedClient.destroy();
                     }
                 }
                 ListNode node = activeList;
                 while((node = node.childNode) != null) { 
                     if(!(node instanceof IntegerNode))
                         break;
                     IntegerNode position = (IntegerNode) node;
                     Client client = clientArray[position.value];
                     if(client == null) {
                         LOGGER.log(Level.WARNING, "Null client id, removed from active list!");
                         removeClient(position);
                         continue;
                     }
                     if(client.timeoutStamp > -1L && client.timeoutStamp < System.currentTimeMillis()) {
                         LOGGER.log(Level.WARNING, "Client disconnected : timeout reached!");
                         removeClient(position);
                         client.destroy();
                         continue;
                     }
                     try {
                         int avail = client.inputStream.available();
                         if(avail > 0) {
                             if((client.iWritePosition < client.iReadPosition ? 
                                 client.iReadPosition - client.iWritePosition : 
                                 Client.BUFFER_SIZE - client.iWritePosition - client.iReadPosition) < avail) {
                                 LOGGER.log(Level.WARNING, "Client disconnected : ib overflow!");
                                 removeClient(position);
                                 client.destroy();
                                 continue;
                             }
                             client.inputStream.read(client.incomingBuffer, client.iWritePosition, avail);
                             client.iWritePosition = (client.iWritePosition + avail) % Client.BUFFER_SIZE;
                         }
                     } catch(IOException ex) {
                         LOGGER.log(Level.WARNING, "Client disconnected : exception caught while reading data!");
                         removeClient(position);
                         client.destroy();
                         continue;
                     }
                     try {
                         switch(client.state) {
                             case 0:
                                 if(client.iWritePosition != client.iReadPosition) {
                                     int opcode = client.incomingBuffer[client.iReadPosition];
                                     if(opcode != 18 && opcode != 16) {
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid login op!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }
                                     client.isReconnecting = opcode == 18;
                                     int size = client.incomingBuffer[client.iReadPosition + 1] & 0xFF;
                                     if((client.iWritePosition < client.iReadPosition ? 
                                         client.iReadPosition - client.iWritePosition : 
                                         Client.BUFFER_SIZE - client.iWritePosition - client.iReadPosition) < size)
                                         continue;
                                     client.iReadPosition += 2;
                                     ByteBuffer buffer = new ByteBuffer(size);
                                     if(client.iReadPosition < client.iWritePosition) {
                                         System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, size);
                                     } else {
                                         System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, Client.BUFFER_SIZE - client.iReadPosition);
                                         System.arraycopy(client.incomingBuffer, 0, buffer.payload, 0, client.iWritePosition);
                                     }
                                     client.iReadPosition += size;
                                     if(buffer.getUbyte() != 255) {
                                         client.outputStream.write(10);
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid initop!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }   
                                     if(buffer.getUword() != 317) {
                                         client.outputStream.write(6);
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid rev!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }
                                     client.isLowMemory = buffer.getUbyte() == 1;
                                     /* CRC CHECKS OF THE DOWNLOADED ARCHIVES */
                                     for(int i = 0; i < 9; i++)
                                         buffer.getDword();
                                     int encipheredBlockSize = buffer.getUbyte();
                                     byte[] encipheredData = new byte[encipheredBlockSize];
                                     System.arraycopy(buffer.payload, buffer.offset, encipheredData, 0, encipheredBlockSize);
                                     BigInteger encipheredBlock = new BigInteger(encipheredData);
                                     buffer.payload = encipheredBlock.modPow(PRIVATE_KEY, MODULUS).toByteArray();
                                     buffer.offset = 0;
                                     if(buffer.getUbyte() != 10) {
                                         client.outputStream.write(10);
                                         LOGGER.log(Level.WARNING, "Client disconnected : invalid rsachk!");
                                         removeClient(position);
                                         client.destroy();
                                         continue;
                                     }
                                     int[] seeds = new int[4];
                                     for(int i = 0; i < 4; i++)
                                         seeds[i] = buffer.getDword();
                                     client.uid = buffer.getDword();
                                     client.username = buffer.getString();
                                     client.password = buffer.getString();
                                     client.timeoutStamp = -1L;
                                     /* REDESIGN BIT */
                                     byte[] response = new byte[3];
                                     response[0] = (byte) 2;
                                     response[1] = (byte) 2; //client.rights;
                                     client.outputStream.write(response);     
                                     client.outgoingBuffer = new byte[Client.BUFFER_SIZE];
                                     /* UPDATE STUFF */
                                     client.flagBuffer = new ByteBuffer(122);
                                     client.appearanceUpdates = new boolean[MAXIMUM_CLIENTS];
                                     client.activePlayers = new ListNode();
                                     client.activePlayers.childNode = client.activePlayers;
                                     client.activePlayers.parentNode = client.activePlayers;
                                     client.addedPlayers = new ListNode();
                                     client.addedPlayers.childNode = client.addedPlayers;
                                     client.addedPlayers.parentNode = client.addedPlayers;
                                     client.playerIndex = new byte[(MAXIMUM_CLIENTS + 7) >> 3];
                                     /* MUSIC STUFF */
                                     client.activeMusic = new byte[(musicNames.length + 7) >> 3];
                                     /* APPEARANCE STUFF */
                                     client.appearanceStates = new int[12];
                                     client.appearanceStates[0] = 31 | 256;
                                     client.colorIds = new int[5];
                                     client.animationIds = new int[7];
                                     client.incomingCipher = new IsaacCipher(seeds);
                                     for(int i = 0; i < seeds.length; i++)
                                         seeds[i] += 50;
                                     client.outgoingCipher = new IsaacCipher(seeds);
                                     client.state = 1;
                                 }
                                 break;
                             
                             
                             case 6:
                                 if(client.lastRecievedPing + 15000L < System.currentTimeMillis())
                                     client.timeoutStamp = System.currentTimeMillis() + 60000L;
                                 while(client.iReadPosition != client.iWritePosition) {
                                     int opcode = client.incomingBuffer[client.iReadPosition] - client.incomingCipher.getNextValue() & 0xFF;
                                     int size = INCOMING_SIZES[opcode];
                                     if(size < -2) {
                                         LOGGER.log(Level.WARNING, "Client disconnected : unknown packet - {0}!", opcode);
                                         removeClient(position);
                                         client.destroy();
                                         break;
                                     }
                                     int avail = client.iWritePosition < client.iReadPosition ? 
                                                  client.iReadPosition - client.iWritePosition : 
                                                  Client.BUFFER_SIZE - client.iWritePosition - client.iReadPosition;
                                     int offAmt = 1;
                                     if(size == -2)
                                         if(avail < 2)
                                             break;
                                         else {
                                             size = ((client.incomingBuffer[client.iReadPosition + 1] & 0xFF) << 8) | 
                                                     (client.incomingBuffer[client.iReadPosition + 2] & 0xFF);
                                             avail -= 2;
                                             offAmt += 2;
                                         }
                                     if(size == -1)
                                         if(avail < 1)
                                             break;
                                         else {
                                             size = client.incomingBuffer[client.iReadPosition + 1] & 0xFF;
                                             avail -= 1;
                                             offAmt += 1;
                                         }
                                     if(avail < size)
                                         break;
                                     client.iReadPosition = (client.iReadPosition + offAmt) % Client.BUFFER_SIZE;
                                     ByteBuffer buffer = null;
                                     if(size > 0) {
                                         buffer = new ByteBuffer(size);
                                         if(client.iReadPosition < client.iWritePosition) {
                                             System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, size);
                                         } else {
                                             System.arraycopy(client.incomingBuffer, client.iReadPosition, buffer.payload, 0, Client.BUFFER_SIZE - client.iReadPosition);
                                             System.arraycopy(client.incomingBuffer, 0, buffer.payload, 0, client.iWritePosition);
                                         }
                                         client.iReadPosition = (client.iReadPosition + size) % Client.BUFFER_SIZE;
                                     }
                                     switch(opcode) {
                                         
                                         /* Ping */
                                         case 0:
                                             client.lastRecievedPing = System.currentTimeMillis();
                                             client.timeoutStamp = -1L;
                                             break;
                                             
                                         /* Chat */
                                         case 4:
                                             client.chatEffects = buffer.getUbyteB() | buffer.getUbyteB() << 8;
                                             client.chatData = new byte[size - 2];
                                             System.arraycopy(buffer.payload, 2, client.chatData, 0, size - 2);
                                             for(int i = 0; i < client.chatData.length; i++) {
                                                 client.chatData[i] += 128;
                                             }
                                             client.activeFlags |= 1 << 5;
                                             break;
                                         
                                         /* Command */
                                         case 103:        
                                             client.commandStr = new String(buffer.payload, 0, size);                                      
                                             break;
                                             
                                         /* Walking */
                                         case 98:
                                         case 164:
                                         case 248:
                                             int firstX = buffer.getUwordLe128();
                                             buffer.offset = buffer.payload.length - 3 - (opcode == 248 ? 14 : 0);
                                             int firstY = buffer.getUwordLe();
                                             int amountSteps = (size - 3 - (opcode == 248 ? 14 : 0))/2 - 1;
                                             if(amountSteps > Client.MAXIMUM_STEPS)
                                                 throw new RuntimeException();
                                             int writePosition = 0;
                                             client.stepQueue[client.walkingQueue.length - 2] = amountSteps + 1;
                                             client.stepQueue[client.walkingQueue.length - 1] = 0;
                                             client.stepQueue[writePosition++] = firstX << 15 | firstY;
                                             buffer.offset = 2;
                                             while(amountSteps-- > 0) {
                                                 client.stepQueue[writePosition++] = ((buffer.getByte() + firstX) << 15) | (buffer.getByte() + firstY);
                                             }                                    
                                             break;
                                         
                                         /* Idle logout */
                                         case 202:
                                             removeClient(position);
                                             client.destroy();
                                             break;
                                     }
                                 }
                                 break;
                         }
                     } catch(Exception ex) {
                         LOGGER.log(Level.WARNING, "Client disconnected : ", ex);
                         removeClient(position);
                         client.destroy();
                         continue;
                     }
                 }           
                 node = activeList;
                 while((node = node.childNode) != null) { 
                    if(!(node instanceof IntegerNode))
                        break;
                    IntegerNode position = (IntegerNode) node;
                    Client client = clientArray[position.value];
                    if(client == null) {
                        LOGGER.log(Level.WARNING, "Null client id, removed from active list!");
                        removeClient(position);
                        continue;
                    }
                    try {
                        switch(client.state) {
                            
                            /**
                             * Initial connection state.
                             */
                            case 1:
                                Client.sendMessage(client, "Welcome to RuneTekk.");
                                client.activeFlags |= 1 << 7;
                                client.state = 2;
                                break;
                               
                            /**
                             * Initial loop state.
                             */
                            case 2:
                                if(client.activeFlags != 0)
                                   Client.writeFlaggedUpdates(client, client.flagBuffer, client.activeFlags);
                                client.updateSteps();
                                client.updateMovement();
                                client.updateRegion();
                                client.state = 3;
                                break;
                             
                            /**
                             * Update state.
                             */
                            case 3:
                                Client.populatePlayers(client);
                                client.state = 4;
                                break;
                              
                            /**
                             * Write update state.
                             */
                            case 4:
                                Client.sendPlayerUpdate(client);
                                client.state = 5;
                                break;
                                
                            /** 
                             * Reset state.
                             */
                            case 5:
                                if((client.lastUpdates[client.lastUpdates.length - 1] + 1) % Client.MAXIMUM_STEPS <= client.lastUpdates[client.lastUpdates.length - 2])
                                    client.lastUpdates[client.lastUpdates.length - 1] = (client.lastUpdates[client.lastUpdates.length - 1] + 1) % Client.MAXIMUM_STEPS;
                                client.activeFlags = 0;
                                client.state = 6;
                                break;
                             
                            /**
                             * Idle state.
                             */
                            case 6:    
                                    if(client.hasWritten) {  
                                       int type = 0;
                                       int crop = 0;
                                       int patch = 1;
                                       int hash = farmingPatchStates[type][crop][0] & ~(1 << 31);
                                       int config = farmingTypeConfigs[type][patch];
                                       Client.sendLargeConfig(client, config, ((hash >> 8) + (int) (Math.random() * (hash & 0xFF))) << patch * 8);
                                        client.hasWritten = false;
                                        client.state = 2;
                                    }
                                    break;
                        }
                    } catch(Exception ex) {
                        LOGGER.log(Level.WARNING, "Client disconnected : ", ex);
                        removeClient(position);
                        client.destroy();
                        continue;
                    }           
                 }
             }
             long curTime = -1L;
             if(nextGc < (curTime = System.currentTimeMillis())) {
                 nextGc = curTime + GC_TIME;
                 System.gc();
             }
         }
      }
      
      /**
       * Removes a client from this handler.
       * @param client The client to remove.
       */
      void removeClient(IntegerNode client) {
          synchronized(removedList) {             
              client.removeFromList();
              client.parentNode = removedList.parentNode;
              client.childNode = removedList;
              client.parentNode.childNode = client;
              client.childNode.parentNode = client;
              clientArray[client.value] = null;
          }
      }
        
     /**
      * Destroys this local application.
      */
     private void destroy() {
         if(!isPaused)  {
             if(thread != null) {
                 synchronized(this) {
                     isPaused = true;
                     notifyAll();
                 }
                 try {
                     thread.join();
                 } catch(InterruptedException ex) {
                 }
             }
             activeList = null;
             removedList = null;
             clientArray = null;
             thread = null;
         }
     }
     
     /**
      * Report an error to the local logger for this class. All errors
      * are reported as severe.
      * @param message The message to report.
      * @param ex The exceptions to report.
      */
     static void reportError(String message, Exception ex) {
         LOGGER.log(Level.SEVERE, "{0} - {1}", new Object[]{ message, ex.getMessage()});
     }
     
     /**
      * The starting point for this application.
      * @param args The command line arguments.
      */
     public static void main(String[] args) {
         printTag();
         Properties serverProperties = new Properties();
         try {
             serverProperties.load(new FileReader(args[1]));
         } catch(Exception ex) {
             reportError("Exception thrown while loading the server properties", ex);
             throw new RuntimeException();
         }
         if(args[0].equals("odump")) {
             ArchivePackage configPack = null;
             try {
                 FileIndex index = new FileIndex(-1, new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("MAINFILE"), "r"), new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("C-INDEX"), "r"));
                 configPack = new ArchivePackage(index.get(Integer.parseInt(serverProperties.getProperty("C-ID"))));
                 index.destroy();              
             } catch(Exception ex) {
                 reportError("Exception thrown while initializing the config pack", ex);
                 throw new RuntimeException();
             }
             try {
                 ByteBuffer buffer = new ByteBuffer(configPack.getArchive("loc.dat"));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(serverProperties.getProperty("OUTDIR") + "objs.txt"));
                 int objectId = 0;
                 while(buffer.offset < buffer.payload.length) {
                     while(true) {
                         int opcode = buffer.getUbyte();
                         if(opcode == 0)
                             break;
                         if(opcode == 1)
                             buffer.offset += buffer.getUbyte() * 3;
                         if(opcode == 2)
                             writer.append("Object Id: " + objectId + ", " + buffer.getString() + "\n");
                         if(opcode == 3)
                             writer.append("\tExamine: " + buffer.getString() + "\n");
                         if(opcode == 5)
                             buffer.offset += buffer.getUbyte() * 2;
                         if(opcode == 14)
                             buffer.getUbyte();
                         if(opcode == 15)
                             buffer.getUbyte();
                         if(opcode == 19)
                             buffer.getUbyte();
                         if(opcode == 24)
                             buffer.getUword();
                         if(opcode == 28)
                             buffer.getUbyte();
                         if(opcode == 29)
                             buffer.getByte();
                         if(opcode == 39)
                             buffer.getByte();
                         if(opcode >= 30 && opcode < 39)
                             buffer.getString();
                         if(opcode == 40)
                             buffer.offset += buffer.getUbyte() * 4;
                         if(opcode == 60)
                             buffer.getUword();
                         if(opcode == 65)
                             buffer.getUword();
                         if(opcode == 66)
                             buffer.getUword();
                         if(opcode == 67)
                             buffer.getUword();
                         if(opcode == 68)
                             buffer.getUword();
                         if(opcode == 69)
                             buffer.getUbyte();
                         if(opcode == 70)
                             buffer.getUword();
                         if(opcode == 71)
                             buffer.getUword();
                         if(opcode == 72)
                             buffer.getUword();
                         if(opcode == 75)
                             buffer.getUbyte();
                         if(opcode == 77)
                             buffer.offset += 4 + (buffer.getUbyte() * 2);
                     }
                     objectId++;
                 }           
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the object files", ex);
                 throw new RuntimeException();
             }
         } else if(args[0].equals("vdump")) {
             ArchivePackage configPack = null;
             try {
                 FileIndex index = new FileIndex(-1, new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("MAINFILE"), "r"), new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("C-INDEX"), "r"));
                 configPack = new ArchivePackage(index.get(Integer.parseInt(serverProperties.getProperty("C-ID"))));
                 index.destroy();              
             } catch(Exception ex) {
                 reportError("Exception thrown while initializing the config pack", ex);
                 throw new RuntimeException();
             }
             try {
                 BufferedWriter writer = new BufferedWriter(new FileWriter(serverProperties.getProperty("OUTDIR") + "varbit.txt"));
                 ByteBuffer buffer = new ByteBuffer(configPack.getArchive("varbit.dat"));
                 int amountVarbitArchives = buffer.getUword();
                 for(int i = 0; i < amountVarbitArchives; i++) {
                     writer.append("Varbit Id: " + i + "\n");
                     while(true) {
                         int opcode = buffer.getUbyte();
                         if(opcode == 0)
                             break;
                         if(opcode == 1) {
                             int configId = buffer.getUword();
                             int shift = buffer.getUbyte();
                             int bits = buffer.getUbyte();
                             writer.append("\tConfig Id: " + configId + "\n\tShift: " + shift + "\n\tMsb: " + bits + "\n\tmsbs: ");
                             for(int j = 32; j > 0; j--) {
                                 if(j > shift && j <= shift + bits)
                                     writer.append("1");
                                 else
                                     writer.append("0");
                                 if(j == 17)
                                     writer.append("\n\tlsbs: ");
                             }
                             writer.append("\n\tRange: 0..." + ((1 << bits) - 1));
                         }
                     }
                     writer.append("\n\n");
                 }
                 writer.close();
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the varbit files", ex);
                 throw new RuntimeException();
             }
             try {
                 ByteBuffer buffer = new ByteBuffer(configPack.getArchive("loc.dat"));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(serverProperties.getProperty("OUTDIR") + "objvarbit.txt"));
                 int objectId = 0;
                 while(buffer.offset < buffer.payload.length) {
                     String name = null;
                     while(true) {
                         int opcode = buffer.getUbyte();
                         if(opcode == 0)
                             break;
                         if(opcode == 1)
                             buffer.offset += buffer.getUbyte() * 3;
                         if(opcode == 2)
                             name = buffer.getString();
                         if(opcode == 3)
                             buffer.getString();
                         if(opcode == 5)
                             buffer.offset += buffer.getUbyte() * 2;
                         if(opcode == 14)
                             buffer.getUbyte();
                         if(opcode == 15)
                             buffer.getUbyte();
                         if(opcode == 19)
                             buffer.getUbyte();
                         if(opcode == 24)
                             buffer.getUword();
                         if(opcode == 28)
                             buffer.getUbyte();
                         if(opcode == 29)
                             buffer.getByte();
                         if(opcode == 39)
                             buffer.getByte();
                         if(opcode >= 30 && opcode < 39)
                             buffer.getString();
                         if(opcode == 40)
                             buffer.offset += buffer.getUbyte() * 4;
                         if(opcode == 60)
                             buffer.getUword();
                         if(opcode == 65)
                             buffer.getUword();
                         if(opcode == 66)
                             buffer.getUword();
                         if(opcode == 67)
                             buffer.getUword();
                         if(opcode == 68)
                             buffer.getUword();
                         if(opcode == 69)
                             buffer.getUbyte();
                         if(opcode == 70)
                             buffer.getUword();
                         if(opcode == 71)
                             buffer.getUword();
                         if(opcode == 72)
                             buffer.getUword();
                         if(opcode == 75)
                             buffer.getUbyte();
                         if(opcode == 77) {
                             writer.append("Object Id: " + objectId + (name != null ? ", " + name : ""));
                             writer.append("\n\tVarbit Id: " + buffer.getUword());
                             writer.append("\n\tConfig Id: " + buffer.getUword());
                             int amountObjects = buffer.getUbyte();
                             writer.append("\n\tAmount Objects: " + amountObjects);
                             for(int i = 0; i <= amountObjects; i++) {
                                 writer.append("\n\t\t" + i + " => " + buffer.getUword());
                             }
                             writer.append("\n\n");
                         }
                     }
                     objectId++;
                 }           
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the object files", ex);
                 throw new RuntimeException();
             }
         } else if(args[0].equals("wdump")) {
             ArchivePackage widgetPack = null;
             try {
                 FileIndex index = new FileIndex(-1, new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("MAINFILE"), "r"), new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("C-INDEX"), "r"));
                 widgetPack = new ArchivePackage(index.get(Integer.parseInt(serverProperties.getProperty("W-ID"))));
                 index.destroy();              
             } catch(Exception ex) {
                 reportError("Exception thrown while loading the files for the widget dump", ex);
                 throw new RuntimeException();
             }
             ByteBuffer buffer = new ByteBuffer(widgetPack.getArchive("data"));                 
             int amountWidgets = buffer.getUword();
             int[][] scriptInstructions = new int[amountWidgets][];
             int[][] scriptConditions = new int[amountWidgets][];
             int[][][] scriptOpcodes = new int[amountWidgets][][];
             while(buffer.offset < buffer.payload.length) {
                 int widgetId = buffer.getUword();
                 if(widgetId == 65535) {
                     buffer.getUword();
                     widgetId = buffer.getUword();
                 }
                 int type = buffer.getUbyte();
                 int fieldType = buffer.getUbyte();
                 buffer.getUword();
                 buffer.getUword();
                 buffer.getUword();
                 buffer.getUbyte();
                 if(buffer.getUbyte() != 0)
                     buffer.getUbyte();
                 int amountScriptInstructions = buffer.getUbyte();
                 if(amountScriptInstructions > 0) {
                     scriptInstructions[widgetId] = new int[amountScriptInstructions];
                     scriptConditions[widgetId] = new int[amountScriptInstructions];
                     for(int i = 0; i < amountScriptInstructions; i++) {
                         scriptInstructions[widgetId][i] = buffer.getUbyte();
                         scriptConditions[widgetId][i] = buffer.getUword();
                     }
                 }
                 int amountScripts = buffer.getUbyte();
                 if(amountScripts > 0) {
                     scriptOpcodes[widgetId] = new int[amountScripts][];
                     for(int i = 0; i < amountScripts; i++) {
                         int size = buffer.getUword();
                         scriptOpcodes[widgetId][i] = new int[size];
                         for(int j = 0; j < size; j++) {
                             scriptOpcodes[widgetId][i][j] = buffer.getUword();
                         }
                     }
                 }
                 if(type == 0) {
                     buffer.getUword();
                     buffer.getUbyte();
                     int amountchildren = buffer.getUword();
                     for(int i = 0; i < amountchildren; i++) {
                         buffer.getUword();
                         buffer.getUword();
                         buffer.getUword();
                     }
                 }
                 if(type == 1) {
                     buffer.getUword();
                     buffer.getUbyte();
                 }
                 if(type == 2) {
                     buffer.getUbyte();
                     buffer.getUbyte();
                     buffer.getUbyte();
                     buffer.getUbyte();
                     buffer.getUbyte();
                     buffer.getUbyte();
                     for(int i = 0; i < 20; i++) {
                         int op = buffer.getUbyte();
                         if(op == 1) {
                             buffer.getUword();
                             buffer.getUword();
                             buffer.getString();
                         }
                     }
                     for(int i = 0; i < 5; i++) {
                         buffer.getString();
                     }
 
                 }
                 if(type == 3)
                     buffer.getUbyte();
                 if(type == 4 || type == 1) {
                     buffer.getUbyte();
                     buffer.getUbyte();
                     buffer.getUbyte();
                 }
                 if(type == 4) {
                     buffer.getString();
                     buffer.getString();
                 }
                 if(type == 1 || type == 3 || type == 4)
                     buffer.getDword();
                 if(type == 3 || type == 4) {
                     buffer.getDword();
                     buffer.getDword();
                     buffer.getDword();
                 }
                 if(type == 5) {
                     buffer.getString();
                     buffer.getString();
                 }
                 if(type == 6) {
                     int modelId = buffer.getUbyte();
                     if(modelId != 0) {
                         buffer.getUbyte();
                     }
                     modelId = buffer.getUbyte();
                     if(modelId != 0) {
                         buffer.getUbyte();
                     }
                     modelId = buffer.getUbyte();
                     if(modelId != 0)
                         buffer.getUbyte();
                     modelId = buffer.getUbyte();
                     if(modelId != 0)
                         buffer.getUbyte();
                     buffer.getUword();
                     buffer.getUword();
                     buffer.getUword();
                 }
                 if(type == 7){
                     buffer.getUbyte();
                     buffer.getUbyte();
                     buffer.getUbyte();
                     buffer.getDword();
                     buffer.getUword();
                     buffer.getUword();
                     buffer.getUbyte();
                     for(int k4 = 0; k4 < 5; k4++) {
                         buffer.getString();
                     }
                 }
                 if(fieldType == 2 || type == 2) {
                     buffer.getString();
                     buffer.getString();
                     buffer.getUword();
                 }
                 if(type == 8)
                     buffer.getString();
                 if(fieldType == 1 || fieldType == 4 || fieldType == 5 || fieldType == 6) {
                     buffer.getString();
                 }
             }
             try {               
                 BufferedWriter writer = new BufferedWriter(new FileWriter(serverProperties.getProperty("OUTDIR") + "wscripts.txt"));
                 for(int widgetId = 0; widgetId < scriptInstructions.length; widgetId++) {
                     if(scriptInstructions[widgetId] != null) {
                         writer.append("Widget " + widgetId + ", Scripts: " + scriptInstructions[widgetId].length + "\n\n");
                         for(int scriptId = 0; scriptId < scriptInstructions[widgetId].length; scriptId++) {
                             if(scriptOpcodes[widgetId] != null &&  scriptOpcodes[widgetId].length > scriptId && scriptOpcodes[widgetId][scriptId] != null) {                               
                                 int[] opcodes = scriptOpcodes[widgetId][scriptId];
                                 String script = "";
                                 String op = "";
                                 char logicOp = '+';
                                 char lastLogicOp = '+';
                                 boolean bool = false;
                                 int offset = 0;
                                 logic:
                                 for(; offset < opcodes.length;) {
                                     int opcode = opcodes[offset++];
                                     bool = false;
                                     switch(opcode) {
 
                                         case 0:
                                             break logic;
 
                                         case 1:
                                             op = "getDynamicLevel(Id => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 2:
                                             op = "getStaticLevel(Id => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 3:
                                             op = "getExperience(Id => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 4:
                                             op = "getItemAmount(WidgetId => " + opcodes[offset++] + ", ItemId => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 5:
                                             op = "getConfigValue(Id => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 6:
                                             op = "getXpForNextLevel(Id => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 7:
                                             op = "unknown(Id => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 8:
                                             op = "getCombatLevel()";
                                             break;
 
                                         case 9:
                                             op = "getSkillTotal()";
                                             break;
 
                                         case 10:
                                             op = "hasAmountItem(Id => " + opcodes[offset++] + ", Amount => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 11:
                                             op = "getUnusedValue0()";
                                             break;
 
                                         case 12:
                                             op = "getUnusedValue1()";
                                             break;
 
                                         case 13:
                                             op = "isConfigBitToggled(Id => " + opcodes[offset++] + ", Bit => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 14:
                                             op = "getConfigValue(VarbitId => " + opcodes[offset++] + ")";
                                             break;
 
                                         case 15:
                                             logicOp = '-';
                                             bool = true;
                                             break;
 
                                         case 16:
                                             logicOp = '\\';
                                             bool = true;
                                             break;
 
                                         case 17:
                                             logicOp = '*';
                                             bool = true;
                                             break;
 
                                         case 18:
                                             op = "getX()";
                                             break;
 
                                         case 19:
                                             op = "getY()";
                                             break;
 
                                         case 20:
                                             op = "" + opcodes[offset++];
                                             break;
                                     }
                                     if(!bool) {
                                         script = (!script.equals("") && lastLogicOp != logicOp ? "(" : "") + script;
                                         script += (!script.equals("") ? " " + logicOp : "") + (script.length() > 40 ? "\n\t\t   " : "") + op + (offset != opcodes.length - 1 && lastLogicOp != logicOp ? ") " : "");
                                         if(logicOp != '\\')
                                             lastLogicOp = logicOp;
                                         logicOp = '+';
                                     }
                                 }
                                 String scriptInstruction = "!=";
                                 if(scriptInstructions[widgetId] != null) {
                                     switch(scriptInstructions[widgetId][scriptId]) {
 
                                         case 2:
                                             scriptInstruction = ">=";
                                             break;
 
                                         case 3:
                                             scriptInstruction = "<=";
                                             break;
 
                                         case 4:
                                             scriptInstruction = "==";
                                             break;
                                     }
                                     int scriptCondition = scriptConditions[widgetId][scriptId];                           
                                     writer.append("\t\tif(" + script + " " + scriptInstruction + " " + scriptCondition + ")\n\t\t\treturn false\n");
                                 }
                             }
                             writer.append("\n");
                         }
                     }
                 }
                 writer.close(); 
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the widget script file", ex);
                 throw new RuntimeException();
             }
         } else if(args[0].equals("setup")) {
             FileIndex landscapeIndex = null;
             ArchivePackage versionPack = null;
             try {
                 landscapeIndex = new FileIndex(-1, new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("MAINFILE"), "r"), new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("L-INDEX"), "r"));
                 FileIndex index = new FileIndex(-1, new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("MAINFILE"), "r"), new RandomAccessFile(serverProperties.getProperty("CACHEDIR") + serverProperties.getProperty("C-INDEX"), "r"));
                 versionPack = new ArchivePackage(index.get(Integer.parseInt(serverProperties.getProperty("V-ID"))));
                 index.destroy();              
             } catch(Exception ex) {
                 reportError("Exception thrown while loading the files for setup", ex);
                 throw new RuntimeException();
             }
             try {
                 DataOutputStream os = new DataOutputStream(new FileOutputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("RFILE")));
                 byte[] mapIndex = versionPack.getArchive("map_index");
                 int amountRegions = mapIndex.length/7;
                 int[][] rCount = new int[256][];
                 int[][] cCount = new int[amountRegions][];
                 int cCountOffset = 1;
                 for(int i = 0; i < mapIndex.length; i += 7) {
                     int oldHash = ((mapIndex[i] & 0xFF) << 8) | (mapIndex[i + 1] & 0xFF);
                     int floorId = ((mapIndex[i + 2] & 0xFF) << 8) | (mapIndex[i + 3] & 0xFF);
                     int hash = oldHash >> 8;
                     if(rCount[hash] == null)
                         rCount[hash] = new int[257];
                     int countOffset = rCount[hash][256]++;
                     rCount[hash][countOffset] = (oldHash & 0xFF);
                     DataInputStream is = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(landscapeIndex.get(floorId))));
                     long updated = 0L;
                     for(int z = 0; z < 4; z++) {
                         for(int x = 0; x < 64; x++) {
                             for(int y = 0; y < 64; y++) {
                                 for(;;) {
                                     int opcode = is.read();
                                     if(opcode == 0)
                                         break;
                                     if(opcode == 1) {
                                         is.read();
                                         break;
                                     }
                                     if(opcode <= 49 || opcode > 81) {
                                         long bitValue = 1L << (long) ((x >> 3) + (8 * (y >> 3)));
                                         if((bitValue & updated) == 0) {
                                             if(cCount[cCountOffset - 1] == null) {
                                                 cCount[cCountOffset - 1] = new int[65];
                                                 rCount[hash][countOffset] |= cCountOffset << 8;
                                             }
                                             int chunkHash = ((x >> 3) << 3) | (y >> 3);
                                             cCount[cCountOffset - 1][cCount[cCountOffset - 1][64]++] = chunkHash;
                                             updated |= bitValue;
                                         }
                                         if(opcode <= 49)
                                             is.read();
                                     }                                
                                 }
                             }
                         }
                     }
                     is.close();
                     if(updated != 0L)
                         cCountOffset++;
                 }
                 for(int x = 0; x < rCount.length; x++) {
                     if(rCount[x] != null) {
                         os.write(1);
                         os.write(x);
                         os.write(rCount[x][256] & 0xFF);                     
                         for(int i = 0; i < rCount[x][256]; i++) {
                             os.write(rCount[x][i] & 0xFF);
                             if((rCount[x][i] & ~0xFF) != 0) {
                                 os.write(1);
                                 int[] chunks = cCount[(rCount[x][i] >> 8) - 1];
                                 os.write(chunks[64]);                               
                                 for(int k = 0; k < chunks[64]; k++) {
                                     os.write(chunks[k]);
                                 }
                             } else
                                 os.write(0);                            
                         }
                     }
                 }
                 DataInputStream is = new DataInputStream(new FileInputStream(serverProperties.getProperty("MC-FILE")));
                 byte[] data = new byte[is.available()];
                 is.readFully(data);
                 String[] lines = new String(data).split("[\n]");
                 for(String line : lines) {
                     if(line.length() <= 1 || line.charAt(0) == '#')
                         continue;
                     String[] tokens = line.split("[ ]");
                     int minX = Integer.parseInt(tokens[0]);
                     int minY = Integer.parseInt(tokens[1]);
                     int maxX = Integer.parseInt(tokens[2]);
                     int maxY = Integer.parseInt(tokens[3]);
                     int songId = Integer.parseInt(tokens[4].replaceAll("[\r]", ""));
                     if(minX > maxX) {
                         int temp = maxX;         
                         maxX = minX;
                         minX = temp;
                     }
                     if(minY > maxY) {
                         int temp = maxY;         
                         maxY = minY;
                         minY = temp;
                     }                       
                     for(int x = minX >> 6; x <= maxX >> 6; x++) {
                         for(int y = minY >> 6; y <= maxY >> 6; y++) {
                             os.write(2);
                             os.write(x);
                             os.write(y);
                             os.write(songId >> 8);
                             os.write(songId);
                         }
                     }
                 }
                 is.close();
                 os.writeByte(0);
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the region files", ex);
                 throw new RuntimeException();
             }
             try {
                 ListNode nameList = new ListNode();
                 nameList.childNode = nameList;
                 nameList.parentNode = nameList;
                 ListNode idList = new ListNode();
                 idList.childNode = idList;
                 idList.parentNode = idList;
                 int maximumId = 0;
                 DataInputStream is = new DataInputStream(new FileInputStream(serverProperties.getProperty("MN-FILE")));
                 byte[] data = new byte[is.available()];
                 is.readFully(data);
                 String[] lines = new String(data).split("[\n]");
                 for(String line : lines) {
                     if(line.length() <= 1 || line.charAt(0) == '#')
                         continue;
                     String[] tokens = line.split("[ ]");
                     StringNode strNode = new StringNode(tokens[0].replace('_', ' '));
                     strNode.parentNode = nameList.parentNode;
                     strNode.childNode = nameList;
                     strNode.parentNode.childNode = strNode;
                     strNode.childNode.parentNode = strNode;
                     IntegerNode intNode = new IntegerNode(Integer.parseInt(tokens[1].replaceAll("[\r]", "")));
                     intNode.parentNode = idList.parentNode;
                     intNode.childNode = idList;
                     intNode.parentNode.childNode = intNode;
                     intNode.childNode.parentNode = intNode;
                     if(intNode.value > maximumId)
                         maximumId = intNode.value;
                 }
                 is.close();
                 DataOutputStream os = new DataOutputStream(new FileOutputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("MFILE")));
                 os.write(maximumId >> 8);
                 os.write(maximumId);                
                 ListNode strNode = nameList, intNode = idList;
                 while((strNode = strNode.childNode) != null && (intNode = intNode.childNode) != null) {
                     if(!(strNode instanceof StringNode) && !(intNode instanceof IntegerNode))
                         break;
                     os.write(1);
                     String value = ((StringNode) strNode).value;
                     int id = ((IntegerNode) intNode).value;    
                     os.write(id >> 8);
                     os.write(id);
                     os.write(value.length());
                     os.write(value.getBytes());
                 }
                 os.write(0);
                 os.close();
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the music names", ex);
                 throw new RuntimeException();
             }
             try {
                 DataOutputStream os = new DataOutputStream(new FileOutputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("FFILE")));
                 DataInputStream is = new DataInputStream(new FileInputStream(serverProperties.getProperty("F-CONFIG")));
                 byte[] data = new byte[is.available()];
                 is.readFully(data);
                 String[] lines = new String(data).split("[\n]");
                 int pos = 0;
                 for(; pos < lines.length;) {
                     String line = lines[pos++].replaceAll("[\r]", "");
                     if(line.length() <= 1 || line.charAt(0) == '#')
                         continue;
                     if(line.equals("end"))
                         break;
                     String[] tokens = line.split("[ ]");
                     os.write(1);
                     os.write(Integer.parseInt(tokens[0]));
                     os.write(Integer.parseInt(tokens[1]));
                     os.write(Integer.parseInt(tokens[2]));
                 }
                 for(; pos < lines.length;) {
                     String line = lines[pos++].replaceAll("[\r]", "");
                     if(line.length() <= 1 || line.charAt(0) == '#')
                         continue;
                     if(line.equals("end"))
                         break;
                     String[] tokens = line.split("[ ]");
                     os.write(2);
                     int config = Integer.parseInt(tokens[2]);
                     os.write(Integer.parseInt(tokens[0]));
                     os.write(Integer.parseInt(tokens[1]));
                     os.write(config >> 8);
                     os.write(config);
                 }
                 for(; pos < lines.length;) {
                     String line = lines[pos++].replaceAll("[\r]", "");
                     if(line.length() <= 1 || line.charAt(0) == '#')
                         continue;
                     if(line.equals("end"))
                         break;
                     String[] tokens = line.split("[ ]");
                     os.write(3);
                     os.write(Integer.parseInt(tokens[0]));
                     os.write(Integer.parseInt(tokens[1]));
                     os.write(Integer.parseInt(tokens[2]));
                     os.write(Integer.parseInt(tokens[3]));
                 }
                 for(; pos < lines.length; pos++) {
                     String line = lines[pos].replaceAll("[\r]", "");
                     if(line.length() <= 1 || line.charAt(0) == '#')
                         continue;
                     if(line.equals("end"))
                         break;
                     String[] tokens = line.split("[ ]");
                     os.write(4);
                     os.write(Integer.parseInt(tokens[0]));
                     os.write(Integer.parseInt(tokens[1]));
                     os.write(Integer.parseInt(tokens[2]));
                     os.write(Integer.parseInt(tokens[3]));
                 }
                 os.write(0);
                 os.flush();
                 os.close();
                 is.close();
             } catch(Exception ex) {
                 reportError("Exception thrown while dumping the farming file", ex);
                 throw new RuntimeException();
             }
         } else if(args[0].equals("server")) {
             int portOff = -1;
             try {
                 portOff = Integer.parseInt(serverProperties.getProperty("PORTOFF"));
             } catch(Exception ex) {
                 reportError("Exception thrown while parsing the port offset", ex);
                 throw new RuntimeException();
             }
             try {
                 DataInputStream is = new DataInputStream(new FileInputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("RFILE")));
                 loadRegions(is);
                 is.close();
             } catch(Exception ex) {
                 reportError("Exception thrown while reading the regions file", ex);
                 throw new RuntimeException();
             }
             try {
                 DataInputStream is = new DataInputStream(new FileInputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("MFILE")));
                 loadMusics(is);
                 is.close();
             } catch(Exception ex) {
                 reportError("Exception thrown while reading the musics file", ex);
                 throw new RuntimeException();
             }
             try {
                 DataInputStream is = new DataInputStream(new FileInputStream(serverProperties.getProperty("OUTDIR") + serverProperties.getProperty("FFILE")));
                 loadFarming(is);
                 is.close();
             } catch(Exception ex) {
                 reportError("Exception thrown while reading the farming file", ex);
                 throw new RuntimeException();
             }
             serverProperties = null;
             main = new Main(portOff);
         }
     }  
     
     /**
      * Prevent external construction;
      * @param portOff The port offset to initialize this server on.
      */
     private Main(int portOff) {
         try {
             activeList = new ListNode();
             activeList.parentNode = activeList;
             activeList.childNode = activeList;
             removedList = new ListNode();
             removedList.parentNode = removedList;
             removedList.childNode = removedList;
             clientArray = new Client[MAXIMUM_CLIENTS];
             nextGc = System.currentTimeMillis() + GC_TIME;
             serverSocket = new ServerSocket();
             serverSocket.setSoTimeout(5);
             serverSocket.setReceiveBufferSize(Client.BUFFER_SIZE);
             serverSocket.bind(new InetSocketAddress(43594 + portOff));
             initialize();
         } catch(Exception ex) {
             LOGGER.log(Level.SEVERE, "Exception thrown while initializing : {0}", ex);
             throw new RuntimeException();
         }
     }
     
     static {
         INCOMING_SIZES = new int[] {
              0, -3, -3,  1, -1, -3, -3, -3, -3, -3,
             -3, -3, -3, -3,  8, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,          
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,        
             -3, -3, -3, -3, -3, -3, -3, -1,  0, -3,
             -3, -3, -3, -3, -3, -3,  4, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -1, -3,           
             -3, -3, -3, -1, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3,  0, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3,  6, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,           
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             
             -3, -3, -3, -3, -1, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3,  4, -3, -3, -3, -3, -3, -3, -3, -3,        
             -3, -3,  0, -3, -3, -3, -3, -3, -3, -3,
             
              4, -3,  0, -3, -3, -3, -3, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -1, -3, -3, -3,
             -3, -3, -3, -3, -3, -3, -3, -3, -3, -3,
             -3,  4, -3, -3, -3, -3, -3, -3, -1, -3,
             -3, -3,  6, -3, -3,
         };
         
         PRIVATE_KEY = new BigInteger("834770591012857827640080639045432158672036"
                                    + "332921897115808929595140145146005194253762"
                                    + "779032390472037351677263434664687344175834"
                                    + "243081727930336566362994680698738590975776"
                                    + "850294050839976416113702926128859739698260"
                                    + "975713377976273616378038375714231420479829"
                                    + "941906054307584484688808996961472506323292"
                                    + "27683313213345");
         MODULUS = new BigInteger("9411057631461099471899808167811272170730276809"
                                + "7953573382594472711586892647324048387895220267"
                                + "2785486684044464558549845428941919231900626219"
                                + "1300411087109062176691151141707764775553043533"
                                + "4378014024591274020906279816717181801750245525"
                                + "6847336335570778443716610104475663472453168488"
                                + "50605330431469711797488557035631");
     }
 }
