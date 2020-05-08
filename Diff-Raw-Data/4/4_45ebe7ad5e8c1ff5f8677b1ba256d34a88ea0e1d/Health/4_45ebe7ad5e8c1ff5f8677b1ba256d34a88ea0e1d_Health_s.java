 package com.fsscripts.bentzilla;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 public class Health extends JavaPlugin {
     public final static HashMap<Player, ArrayList<Block>> healthUsers = new HashMap();  
     private final HPlayerChatListener ThePlayerListener = new HPlayerChatListener(this);
     
    public Health(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super();
    }

 	public void onDisable() {
 		System.out.println("HealthPlugin Disabled");
 	}
 
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_CHAT, this.ThePlayerListener, Event.Priority.Normal, this);
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 	}
 
 	 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 			if (!(sender instanceof Player)) {
 				return false;
 			}
 			
 			Player player = (Player)sender;
 			String command = "/" + commandLabel + " " + join(args, 0);
 			
 			boolean action = false;
 			if (!action) {
 					action = toggleHealth(player, command);
 			}
 			return action;
 	    }
 	   
 	    public static boolean enabled(Player player) {
 			return healthUsers.containsKey(player);
 		}
 
 	    public boolean toggleHealth(Player player, String command){
 	    	if (command.toLowerCase().contains("/showhealth") || command.toLowerCase().contains("/health")) {
 		    	if(enabled(player)) {
 		    		this.healthUsers.remove(player);
 		    		player.sendMessage("HealthPlugin disabled");
 		    	} else {
 		    		this.healthUsers.put(player, null);
 		    		player.sendMessage("HealthPlugin enabled");
 		    	}		
 		    	return true;
 		    }
 	    	return false;
 	    }
     
     public static String join(String[] arr, int offset) {
 		return join(arr, offset, " ");
 	}
 
 	/**
 	 * Join an array command into a String
 	 * @author Hidendra
 	 * @param arr
 	 * @param offset
 	 * @param delim
 	 * @return
 	 */
 	public static String join(String[] arr, int offset, String delim) {
 		String str = "";
 
 		if (arr == null || arr.length == 0) {
 			return str;
 		}
 
 		for (int i = offset; i < arr.length; i++) {
 			str += arr[i] + delim;
 		}
 
 		return str.trim();
 	}
 }
