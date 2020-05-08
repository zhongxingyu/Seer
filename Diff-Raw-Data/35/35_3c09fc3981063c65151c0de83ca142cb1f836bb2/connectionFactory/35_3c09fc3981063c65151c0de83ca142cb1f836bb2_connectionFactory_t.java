 package controllers;
 
 import channels.ConnectionListener;
 import channels.SocketChannel;
 
public class connectionFactory {
     public void connectToServer(String serverName, int serverPort, ConnectionListener listener) {
         SocketChannel.connectTo(serverName, serverPort, listener);
     }
 }
