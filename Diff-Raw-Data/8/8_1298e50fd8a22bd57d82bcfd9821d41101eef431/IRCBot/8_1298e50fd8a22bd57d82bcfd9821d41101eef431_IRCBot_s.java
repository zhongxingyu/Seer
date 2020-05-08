 package me.zippy120;
 
 import java.io.IOException;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 
 public class IRCBot extends PircBot{
     protected CrossServerChat plugin;
     
     public IRCBot(){
         this.setName("DarkHelmetMainBot");
     }
     
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message){
         plugin.sendToServer(message);
     }
     public static void startBot(){
         IRCBot bot = new IRCBot();
     // Connect to the IRC server.
         try {
             bot.connect("irc.esper.net");
         } catch (NickAlreadyInUseException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (IrcException e) {
             e.printStackTrace();
         }
 
         // Join the channel.
 
        bot.setVerbose(false);
        
        // Join the channel.
       bot.joinChannel("#araeosia-servers");
     }
 }
