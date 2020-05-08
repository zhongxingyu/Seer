 package com.wolvencraft.prison.util;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.wolvencraft.prison.CommandManager;
 import com.wolvencraft.prison.PrisonSuite;
 
 public class Message {
 	private static Logger logger = Logger.getLogger("PrisonSuite");
 	
 	public static void send(CommandSender sender, String message) {
 		if(message == null) message = "";
 		sender.sendMessage(Util.parseColors(message));
 	}
 	
 	public static void send(String message) {
 		if(message == null) message = "";
 		send(CommandManager.getSender(), message);
 	}
 	
 	public static void sendSuccess(CommandSender sender, String message) {
 		if(message == null) message = "";
 		send(sender, PrisonSuite.getLanguage().GENERAL_SUCCESS + " " + ChatColor.WHITE + message);
 	}
 	
 	public static void sendSuccess(String message) {
 		if(message == null) message = "";
 		sendSuccess(CommandManager.getSender(), message);
 	}
 	
 	public static void sendError(CommandSender sender, String message) {
 		if(message == null) message = "";
 		send(sender, PrisonSuite.getLanguage().GENERAL_ERROR + " " + ChatColor.WHITE + message);
 	}
 	
 	public static void sendError(String message) {
 		if(message == null) message = "";
 		sendError(CommandManager.getSender(), message);
 	}
 	
 	public static void sendCustom(String title, String message) {
 		if(message == null) message = "";
 		send(CommandManager.getSender(), ChatColor.GOLD + "[" + title + "] " + ChatColor.WHITE + message);
 	}
 
     /**
      * Broadcasts a message to all players on the server
      * @param message Message to be sent
      */
     public static void broadcast(String message) {
         if(message == null) message = "";
 		message = PrisonSuite.getLanguage().GENERAL_SUCCESS + " " + ChatColor.WHITE + message;
         for (Player p : Bukkit.getServer().getOnlinePlayers()) {
         	p.sendMessage(Util.parseColors(message));
         }
     }
 	
     /**
      * Sends a message into the server log if debug is enabled
      * @param message Message to be sent
      */
     public static void debug(String message) {
        if (PrisonSuite.getSettings().DEBUG) logger.log(Level.INFO, "[PrisonDebug] " + message);
     }
     
     /**
      * Sends a message into the server log if debug is enabled
 	 * @param level Severity level
      * @param message Message to be sent
      */
     public static void debug(Level level, String message) {
        if (PrisonSuite.getSettings().DEBUG) logger.log(level, "[PrisonDebug] " + message);
     }
 	
 	/**
 	 * Sends a message into the server log
 	 * @param message Message to be sent
 	 */
 	public static void log(String message) {
 		logger.info("[PrisonSuite] " + message);
 	}
 	
 	/**
 	 * Sends a message into the server log
 	 * @param level Severity level
 	 * @param message Message to be sent
 	 */
 	public static void log(Level level, String message) {
 		logger.log(level, "[PrisonSuite] " + message);
 	}
 	
 	public static void formatHelp(String command, String arguments, String description, String node) {
 		CommandSender sender = CommandManager.getSender();
 		if(!arguments.equalsIgnoreCase("")) arguments = " " + arguments;
 		if(sender.hasPermission(node) || node.equals(""))
 			sender.sendMessage(ChatColor.GOLD + "/prison " + command + ChatColor.GRAY + arguments + ChatColor.WHITE + " " + description);
 		return;
 	}
 	
 	public static void formatHelp(String command, String arguments, String description) {
 		formatHelp(command, arguments, description, "");
 		return;
 	}
 	
 	public static void formatHeader(int padding, String name) {
 		CommandSender sender = CommandManager.getSender();
 		String spaces = "";
 		for(int i = 0; i < padding; i++) { spaces = spaces + " "; }
 		sender.sendMessage(spaces + "-=[ " + ChatColor.BLUE + name + ChatColor.WHITE + " ]=-");
 	}
 }
