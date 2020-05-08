 import AndrewCassidy.PluggableBot.PluggableBot;
 import AndrewCassidy.PluggableBot.Plugin;
 import Markov2.MarkovString;
 import java.io.*;
 /*
  * Markov.java
  *
  * Created on 10 October 2007, 19:34
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 /**
  *
  * @author Administrator
  */
 public class Markov implements Plugin {
     
     private MarkovString m = new MarkovString();
 
     public void onAction(String sender, String login, String hostname, String target, String action) {
     }
 
     public void onJoin(String channel, String sender, String login, String hostname) {
     }
 
     public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
     }
 
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         if (message.startsWith("!bobstats"))
         {
            File f = new File("Markov2.db4o");
             double size = (double)f.length() / 1048576f;
             PluggableBot.Message(channel, "My dictionary currently holds " + m.getWordCount() + " words and " + m.getConnectionCount() + " word associations. My dictionary file is " + String.format("%1$2f", size) + " MB");
         }
         else
         {        
             m.Learn(message);
             if (message.toLowerCase().indexOf(PluggableBot.Nick().toLowerCase()) > -1)
             {
                 PluggableBot.Message(channel, m.Generate());
             }
         }
     }
 
     public void onPart(String channel, String sender, String login, String hostname) {
     }
 
     public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
     }
 
     public String getHelp() {
         return "The Markov plugin is a simple implementation of Markov chains. This plugin allows me to 'Learn' from what is said in the channel and be able to peice together sentences.";
     }
     public void onPrivateMessage(String sender, String login, String hostname, String message) {
        
     }
     
     public void unload()
     {
         m.cleanup();
     }
 }
