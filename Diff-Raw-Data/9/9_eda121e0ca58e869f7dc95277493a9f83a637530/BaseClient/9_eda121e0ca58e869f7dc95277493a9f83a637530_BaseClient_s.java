 package edu.berkeley.cs.cs162.Client;
 
 import edu.berkeley.cs.cs162.Writable.ClientInfo;
 import edu.berkeley.cs.cs162.Writable.Message;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 import edu.berkeley.cs.cs162.Writable.MessageProtocol;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.Random;
 
 abstract public class BaseClient implements Client {
 
     String name;
     byte type;
     ClientInfo clientInfo;
     ServerConnection connection;
 
     public BaseClient(String name, byte type) {
         this.name = name;
         this.type = type;
         clientInfo = MessageFactory.createObserverClientInfo(name);
     }
 
     public BaseClient(String name) {
         this(name, (byte) -1);
     }
 
     public BaseClient() {
         this("");
     }
 
     public String getName() {
         return this.name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public byte getType() {
         return this.type;
     }
 
     public void setType(byte type) {
         this.type = type;
     }
 
     public ClientInfo getClientInfo() {
         return clientInfo;
     }
 
    private void connectTo(String address, Integer port){
         try
         {
             // Create the C2S and S2C sockets
             Socket c1 = new Socket(address, port);
             Socket c2 = new Socket(address, port);
 
             // Attempt to connect to the GameServer via 3-way Handshake
             connection = new ServerConnection(c1, c2);
             System.out.println(connection.initiate3WayHandshake(new Random()));
             Message connectMessage = MessageFactory.createConnectMessage(clientInfo);
             Message serverResponse = connection.sendSyncToServer(connectMessage);
        } catch(IOException e) {
            return;
         }
     }
 }
