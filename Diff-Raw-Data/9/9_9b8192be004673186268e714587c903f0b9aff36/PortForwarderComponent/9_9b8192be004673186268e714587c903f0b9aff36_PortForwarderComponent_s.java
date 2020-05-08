 package org.kevoree.library.javase.webserver.portForward;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.InetAddress;
import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tboschat
  * Date: 7/25/12
  * Time: 10:28 AM
  * To change this template use File | Settings | File Templates.
  */
 @DictionaryType({
         @DictionaryAttribute(name = "source_host"),
         @DictionaryAttribute(name = "source_port"),
         @DictionaryAttribute(name = "destination_host"),
         @DictionaryAttribute(name = "destination_port")
 })
 @ComponentType
 public class PortForwarderComponent extends AbstractComponentType {
 
     private Logger logger = LoggerFactory.getLogger(PortForwarderComponent.class);
 
     @Start
     public void start() throws IOException {
 
         String source_host = this.getDictionary().get("source_host").toString();
         int source_port = Integer.parseInt(this.getDictionary().get("source_port").toString());
         String destination_host = this.getDictionary().get("destination_host").toString();
         int destination_port = Integer.parseInt(this.getDictionary().get("destination_port").toString());
 
         ServerSocket serverSocket=null;
         try{
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(destination_host, destination_port), 50);
         } catch (IOException e) {
             logger.debug(" Error with the socket server ", e);
             serverSocket.close();
         }
 
         while (true) {
             Socket clientSocket = serverSocket.accept();
             ClientThread clientThread = new ClientThread(clientSocket,source_host,source_port);
             clientThread.start();
         }
     }
 
     @Stop
     public void stop(){
 
     }


 }
