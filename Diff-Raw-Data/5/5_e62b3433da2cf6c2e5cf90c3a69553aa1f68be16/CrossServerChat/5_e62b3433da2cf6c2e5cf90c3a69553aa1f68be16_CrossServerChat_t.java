 package me.zippy120;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class CrossServerChat extends JavaPlugin{
 
     static final Logger log = Logger.getLogger("Minecraft");
     static CrossServerChat plugin;
     IRCBot bot = new IRCBot(this);
     Map<String, Boolean> recieve = new HashMap<String, Boolean>();
     
     
     
     //STOOOOOOOOOOOOFFFFFFFFFFFF
     public String 
     
     
     $username = "AraeosiaServers";
     
     
     public String
     
     
     $server = "irc.esper.net";
     
     
     public ArrayList<String>
     
     
     $channels = new ArrayList<String>();
     
     private void stuff(){
     	$channels.add("#araeosia-servers");
     }
     
     //END :D
 
     @Override
     public void onEnable(){
     	stuff();
         log.info("Your plugin has been enabled!");
         log.info("Enabling IRC bot...");
 
         
         IRCBot bot = new IRCBot(this);
         // Connect to the IRC server.
         try {
             bot.startBot();
         } catch (Exception e) {
             e.printStackTrace();
         }
 	
     }
 
     @Override
     public void onDisable(){
         log.info("Your plugin has been disabled.");
         log.info("Disabling IRC bot...");
         bot.disconnect();
     }
     public void onPlayerChatEvent(AsyncPlayerChatEvent event){
         String message = event.getFormat() + event.getMessage();
        for(String s : $channels)
        	bot.sendMessage(s, message);
     }
    
     public void sendToServer(String message) {
     	log.info("IRC: " + message);
         for (Player p : plugin.getServer().getOnlinePlayers()){
             if (isRecieveingMessages(p))
                 p.sendMessage(message);
         }	
     }
 
     private boolean isRecieveingMessages(Player p) {
         if(recieve.containsKey(p) && recieve.get(p) == true)
             return true;
         return false;
     }
 
     @Override
     public boolean onCommand(CommandSender sender,  Command cmd, String commandLabel, String[] args){
         if (cmd.getName().equalsIgnoreCase("CC")){
             if (recieve.containsKey(sender)){
                 if (recieve.get(sender) == true){
                     sender.sendMessage(ChatColor.RED + "Cross-Server chat disabled.");
                     recieve.put(sender.getName(), false);
                     return true;
                 } else {
                     sender.sendMessage(ChatColor.YELLOW + "Cross-Server chat enabled.");
                     recieve.put(sender.getName(), true);
                     return true;
                 }
             } else {
                 sender.sendMessage(ChatColor.YELLOW + "Cross-Server chat enabled.");
                 recieve.put(sender.getName(), true);
                 return true;
             }
         }
         return false;	
     }
 }
