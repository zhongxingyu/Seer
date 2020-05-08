 package com.CMDbox;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import com.CMDbox.Config.MessagesTextFile;
 
 public class BroadCaster {
 	public static int line = MessagesTextFile.MessagesConfig.getInt("Messages.How many lines");
 	public static int x = 0;
 	public static int interval = MessagesTextFile.MessagesConfig.getInt("BroadCaster.interval");
 	public static int inter = interval * 20;
 	public static String prefix = MessagesTextFile.MessagesConfig.getString("BroadCaster.prefix");
 	
 	public static void messages(){
 		if (MessagesTextFile.MessagesConfig.getBoolean("BroadCaster.Enable") == true){
 			CMDbox.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(CMDbox.plugin, new Runnable() {
 				   public void run() {
 					   if (x == line){
 						   x = 0;
 					   }
 					   x++;
 				       String message = MessagesTextFile.MessagesConfig.getString("Messages." + x);
 				        
 				       //Colors
 				        message = message.replaceAll("&0", ChatColor.BLACK + "");
 				        message = message.replaceAll("&1", ChatColor.DARK_BLUE + "");
 				        message = message.replaceAll("&2", ChatColor.DARK_GREEN + "");
 				        message = message.replaceAll("&3", ChatColor.DARK_AQUA + "");
 				        message = message.replaceAll("&4", ChatColor.DARK_RED + "");
 				        message = message.replaceAll("&5", ChatColor.DARK_PURPLE + "");
 				        message = message.replaceAll("&6", ChatColor.GOLD + "");
 				        message = message.replaceAll("&7", ChatColor.GRAY + "");
 				        message = message.replaceAll("&8", ChatColor.DARK_GRAY+ "");
 				        message = message.replaceAll("&9", ChatColor.BLUE + "");
 				        message = message.replaceAll("&a", ChatColor.GREEN + "");
 				        message = message.replaceAll("&b", ChatColor.AQUA + "");
 				        message = message.replaceAll("&c", ChatColor.RED + "");
 				        message = message.replaceAll("&d", ChatColor.LIGHT_PURPLE + "");
 				        message = message.replaceAll("&e", ChatColor.YELLOW + "");
 				        message = message.replaceAll("&f", ChatColor.WHITE + "");
 				        message = message.replaceAll("&g", ChatColor.MAGIC + "");
				        
				        prefix = prefix.replaceAll("&0", ChatColor.BLACK + "");
				        prefix = prefix.replaceAll("&1", ChatColor.DARK_BLUE + "");
				        prefix = prefix.replaceAll("&2", ChatColor.DARK_GREEN + "");
				        prefix = prefix.replaceAll("&3", ChatColor.DARK_AQUA + "");
				        prefix = prefix.replaceAll("&4", ChatColor.DARK_RED + "");
				        prefix = prefix.replaceAll("&5", ChatColor.DARK_PURPLE + "");
				        prefix = prefix.replaceAll("&6", ChatColor.GOLD + "");
				        prefix = prefix.replaceAll("&7", ChatColor.GRAY + "");
				        prefix = prefix.replaceAll("&8", ChatColor.DARK_GRAY+ "");
				        prefix = prefix.replaceAll("&9", ChatColor.BLUE + "");
				        prefix = prefix.replaceAll("&a", ChatColor.GREEN + "");
				        prefix = prefix.replaceAll("&b", ChatColor.AQUA + "");
				        prefix = prefix.replaceAll("&c", ChatColor.RED + "");
				        prefix = prefix.replaceAll("&d", ChatColor.LIGHT_PURPLE + "");
				        prefix = prefix.replaceAll("&e", ChatColor.YELLOW + "");
				        prefix = prefix.replaceAll("&f", ChatColor.WHITE + "");
				        prefix = prefix.replaceAll("&g", ChatColor.MAGIC + "");
 				       
 				       Bukkit.broadcastMessage(prefix + " " + message);
 				   }
 			}, 60L, inter);
 		}
 	}
 }
