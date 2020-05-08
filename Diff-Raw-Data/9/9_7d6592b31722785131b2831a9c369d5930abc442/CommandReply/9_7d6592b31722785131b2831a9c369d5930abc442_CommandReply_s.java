 package com.gmail.snipsrevival.commands;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.gmail.snipsrevival.AdminAid;
 import com.gmail.snipsrevival.CommonUtilities;
 import com.gmail.snipsrevival.ConfigValues;
 
 public class CommandReply implements CommandExecutor {
 	
 	AdminAid plugin;
 	CommonUtilities common;
 	
 	public CommandReply(AdminAid plugin) {
 		this.plugin = plugin;
 		plugin.getCommand("reply").setExecutor(this);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		
 		this.common = new CommonUtilities(plugin);
 		
 		if(!sender.hasPermission("adminaid.msg")) {
 			sender.sendMessage(ChatColor.RED + "You don't have permission to use that command");
 			return true;
 		}
 		if(args.length < 1) {
 			sender.sendMessage(ChatColor.RED + "Too few arguments!");
 			sender.sendMessage(ChatColor.RED + "Use " + ChatColor.WHITE + "/reply <message> " + ChatColor.RED + "to reply to private message");
 			return true;
 		}
 				
 		if(!plugin.lastSender.containsKey(sender.getName())) {
 			sender.sendMessage(ChatColor.RED + "There isn't a player you can reply to");
 			return true;
 		}
 		
 		StringBuilder strBuilder = new StringBuilder();			
 		String prefix = new ConfigValues(plugin).getPrefix(sender);
 		
 		for(int i = 0; i < args.length; i++) {
 			strBuilder.append(args[i] + " ");
 		}
 		String message = strBuilder.toString().trim();
 		
 		String name = plugin.lastSender.get(sender.getName());
 		if(name.equalsIgnoreCase("CONSOLE")) {
 			plugin.getServer().getConsoleSender().sendMessage(prefix + message);
 			plugin.lastSender.put("CONSOLE", sender.getName());
 		}
 		else {
 			Player targetPlayer = Bukkit.getServer().getPlayer(name);
 			targetPlayer.sendMessage(prefix + message);
 			plugin.lastSender.put(targetPlayer.getName(), sender.getName());
 		}
 		sender.sendMessage(ChatColor.GREEN + "Private message sent successfully");
 		return true;
 	}
 }
