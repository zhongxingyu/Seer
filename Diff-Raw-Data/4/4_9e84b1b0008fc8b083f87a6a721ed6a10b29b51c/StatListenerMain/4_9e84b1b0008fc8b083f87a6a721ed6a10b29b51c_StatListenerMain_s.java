 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfstatsxtslite;
 
 import static com.github.etsai.kfstatsxtslite.StatMessage.Type.*;
 import com.github.etsai.kfstatsxtslite.message.*;
 import groovy.sql.Sql;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Main entry point for the UDP listener
  * @author etsai
  */
 public class StatListenerMain {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws SocketException, ClassNotFoundException, SQLException {
         ClomParser clom= new ClomParser();
         DatagramSocket socket;
         DatagramPacket packet;
         
         clom.parse(args);
        Class.forName("org.sqlite.JDBC");
         
         Map<String, PlayerContent> receivedContent= new HashMap<>();
        StatWriter writer= new StatWriter(Sql.newInstance(String.format("jdbc:sqlite:%s", clom.getDBName())));
         byte[] buffer= new byte[65536];
         socket= new DatagramSocket(clom.getPort());
         packet= new DatagramPacket(buffer, buffer.length);
         
         
         System.out.println("Listening on port: "+clom.getPort());
         while(true) {
             try {
                 socket.receive(packet);
                 StatMessage msg= StatMessage.parse(new String(packet.getData(), 0, packet.getLength()));
                 
                 switch (msg.getType()) {
                     case MATCH:
                         writer.writeMatchStat((MatchStat)msg);
                         break;
                     case PLAYER:
                         PlayerStat playerMsg= (PlayerStat)msg;
                         String steamID64= playerMsg.getSteamID64();
                         PlayerContent content;
                         
                         if (!receivedContent.containsKey(steamID64)) {
                             receivedContent.put(steamID64, new PlayerContent());
                         }
                         content= receivedContent.get(steamID64);
                         content.addPlayerStat(playerMsg);
                         if (content.isComplete()) {
                             System.out.println("Saving stats for: " + steamID64);
                             writer.writePlayerStat(content.getStats());
                             receivedContent.remove(steamID64);
                         }
                         break;
                 }
                 
             } catch (IOException ex) {
                 Logger.getLogger(StatListenerMain.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
