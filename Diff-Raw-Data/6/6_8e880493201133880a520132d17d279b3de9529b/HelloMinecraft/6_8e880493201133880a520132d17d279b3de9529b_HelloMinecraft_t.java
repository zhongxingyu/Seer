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
 				
 		log.info("Your plugin has been enabled.");
 	}
 	
 	public void onDisable(){ 
 		log.info("Your plugin has been enabled.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		
 		if(commandLabel.equalsIgnoreCase("hello")){ //If the player typed /hello then say hello
 			
 			log.info("hello");
			for (Player messageplayer : getServer().getOnlinePlayers()) //for each player online...
			{
				    messageplayer.sendMessage("Hello");                 //say hello
			}
 			return true;
 		}
 		return false;
 	}
 } 
