 /**
  *  Name: Config.java
  *  Date: 14:27:53 - 8 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Description:
  *  
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse;
 
 import java.util.ArrayList;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Config {
 
 	@SuppressWarnings("serial")
 	public static void load(JavaPlugin plugin) {
 		
 		FileConfiguration config = plugin.getConfig();
 		boolean save = false;
 		
 		// General
 		
 		if(!config.contains("debug")) {
 			config.set("debug", false);
 			save = true;
 		}
 		
		if(!config.contains("halloween.enabled")) {
 			config.set("halloween.enabled", false);
			save = true;
		}
		
		if(!config.contains("halloween.forcepumpkin")) {
 			config.set("halloween.forcepumpkin", false);
 			save = true;
 		}
 		
 		if(!config.contains("worldnames")) {
 			config.set("worldnames.survivalgames1", "survivalgames1_template");
 			save = true;
 		}
 		
 		if(!config.contains("timeoutTillStart")) {
 			config.set("timeoutTillStart", 120);
 			save = true;
 		}
 		
 		if(!config.contains("spawnProtectionName")) {
 			config.set("spawnProtectionName", "sgspawn");
 			save = true;
 		}
 		
 		if(!config.contains("timeoutTillArenaInSeconds")) {
 			config.set("timeoutTillArenaInSeconds", 300);
 			save = true;
 		}
 		
 		if(!config.contains("allowedCommandsInGame")) {
 			
 			ArrayList<String> allowedcommands = new ArrayList<String>() {{ 
 				add("/sgplayers");
 				add("/sgleave");
 			}};
 			
 			config.set("allowedCommandsInGame", allowedcommands);
 			save = true;
 		}
 		
 		// Database
 		
 		if(!config.contains("database.auth.username")) {
 			config.set("database.auth.username", "username");
 			save = true;
 		}
 		
 		if(!config.contains("database.auth.password")) {
 			config.set("database.auth.password", "password");
 			save = true;
 		}
 		
 		if(!config.contains("database.settings.host")) {
 			config.set("database.settings.host", "localhost");
 			save = true;
 		}
 		
 		if(!config.contains("database.settings.port")) {
 			config.set("database.settings.port", 3306);
 			save = true;
 		}
 				
 		if(!config.contains("database.settings.database")) {
 			config.set("database.settings.database", "survivalgames");
 			save = true;
 		}
 		
 		if(!config.contains("database.settings.tablename")) {
 			config.set("database.settings.tablename", "sg_stats");
 			save = true;
 		}
 		
 		if(save) {
 			plugin.saveConfig();
 		}
 	}
 }
