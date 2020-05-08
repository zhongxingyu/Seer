 package me.zippy120;
 
 import java.io.IOException;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 
 public class IRCBot extends PircBot{
 
     protected CrossServerChat plugin;
     
     public IRCBot(CrossServerChat plugin){
     	this.plugin = plugin;
         this.setName(plugin.$username);
     }
     
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message){
         plugin.sendToServer(message);
     }
     public void startBot(){
    	
     // Connect to the IRC server.
         try {
            this.connect(plugin.$server);
         } catch (NickAlreadyInUseException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (IrcException e) {
             e.printStackTrace();
         }
 
        this.setVerbose(false);
        
 
     }
     
     @Override
     public void onConnect(){
     	// Join the channel.
        for(String s : plugin.$channels)
     	   this.joinChannel(s);
     }
 }
