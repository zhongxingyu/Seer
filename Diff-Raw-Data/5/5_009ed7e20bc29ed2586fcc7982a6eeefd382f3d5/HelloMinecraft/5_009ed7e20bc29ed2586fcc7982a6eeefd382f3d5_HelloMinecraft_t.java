 package me.smickles.HelloMinecraft;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * HelloMinecraft plugin for Bukkit
  *
  * @author smickles
  */
 
 public class HelloMinecraft extends JavaPlugin {
 	
 	Logger log = Logger.getLogger("Minecraft");
 
 	public void onEnable(){ 
 		
 		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() { // on this server's scheduler, run the repeating task of...
 
 		    public void run() {
 		    	log.info("hello (automatic)"); // log the instance of Hello
 		    	for (Player messageplayer : getServer().getOnlinePlayers()) //for each player online...
 				{
 					    messageplayer.sendMessage("Hello");                 //say hello
 				} 
 		    }
 		}, 60L, 18000L); // 60 server ticks / 20 = 3 second delay
 		                 // 18000 server ticks = 15 minute repetition
 				
		log.info("HelloMinecraft successfully enabled.");
 	}
 	
 	public void onDisable(){ 
		log.info("HelloMinecraft successfully disabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		if(commandLabel.equalsIgnoreCase("hello")){ //If the player typed /hello then say hello
 			
 			log.info("hello (/hello)"); // log the instance of hello
 			for (Player messageplayer : getServer().getOnlinePlayers()) //for each player online...
 			{
 				    messageplayer.sendMessage("Hello");                 //say hello
 			}
 			return true;
 		}
 		return false;
 	}
 } 
