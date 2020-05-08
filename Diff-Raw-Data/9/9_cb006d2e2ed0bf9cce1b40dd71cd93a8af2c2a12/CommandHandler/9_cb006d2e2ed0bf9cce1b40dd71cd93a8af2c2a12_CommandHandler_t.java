 package com.cole2sworld.dragonlist;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Locale;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 @SuppressWarnings("static-method")
 /**
  * Handles commands
  *
  */
 public final class CommandHandler {
 	public void add(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.add")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		try {
 			InetAddress.getByName(args[0]);
 			if (GlobalConf.mode != WhitelistMode.IP) {
 				sender.sendMessage(ChatColor.RED+"You can't whitelist IPs outside of IP whitelist mode!");
 				return;
 			}
 			else {
 				WhitelistManager.addToIPWhitelist(InetAddress.getByName(args[0]));
 			}
 		} catch (UnknownHostException e) {
 			// exception means success
 		}
 		if (GlobalConf.mode == WhitelistMode.NAME) WhitelistManager.addToNameWhitelist(args[0]);
 		if (GlobalConf.mode == WhitelistMode.IP) WhitelistManager.addToIPWhitelist(IPLogManager.lookupByName(args[0]));
 		if (GlobalConf.mode == WhitelistMode.PASSWORD) WhitelistManager.addToPasswordedWhitelist(args[0]);
 		sender.sendMessage(ChatColor.AQUA+args[0]+" added to the "+GlobalConf.mode.toString()+" whitelist.");
 	}
 	public void export(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.export")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		for (String name : WhitelistManager.getWhitelistedNames()) {
 			Bukkit.getServer().getOfflinePlayer(name).setWhitelisted(true);
 			sender.sendMessage(ChatColor.GREEN+"Exported "+name);
 		}
 		sender.sendMessage(ChatColor.AQUA+"Finished!");
 	}
	public void importWhitelist(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.import")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		Server server = Bukkit.getServer();
 		for (OfflinePlayer player : server.getWhitelistedPlayers()) {
 			WhitelistManager.addToNameWhitelist(player.getName());
 			sender.sendMessage(ChatColor.GREEN+"Imported "+player.getName());
 		}
 		sender.sendMessage(ChatColor.AQUA+"Finished!");
 	}
 	public void remove(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.remove")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		try {
 			if (GlobalConf.mode != WhitelistMode.IP) {
 				InetAddress.getByName(args[0]);
 				sender.sendMessage(ChatColor.RED+"You can't whitelist IPs outside of IP whitelist mode!");
 				return;
 			}
 		} catch (UnknownHostException e) {
 			// exception means success
 		}
 		if (GlobalConf.mode == WhitelistMode.NAME) WhitelistManager.removeFromNameWhitelist(args[0]);
 		if (GlobalConf.mode == WhitelistMode.IP) WhitelistManager.removeFromIPWhitelist(IPLogManager.lookupByName(args[0]));
 		if (GlobalConf.mode == WhitelistMode.PASSWORD) WhitelistManager.removeFromPasswordWhitelist(args[0]);
 		sender.sendMessage(ChatColor.AQUA+args[0]+" removed from the "+GlobalConf.mode.toString()+" whitelist.");
 	}
 	public void setpass(CommandSender sender, String[] args, String label) {
 		if (args.length == 0) {
 			sender.sendMessage(ChatColor.RED+"Please specify a password!");
 			return;
 		}
 		if (AuthManager.badPasswords.contains(args[0].toLowerCase(Locale.ENGLISH))) {
 			sender.sendMessage(ChatColor.RED+"Invalid password!");
 			return;
 		}
 		StringBuilder builder = new StringBuilder();
 		for (String part : args) {
 			builder.append(part);
 			builder.append(" ");
 		}
 		builder.deleteCharAt(builder.length()-1);
 		AuthManager.changePassword(sender.getName(), builder.toString());
 		sender.sendMessage(ChatColor.AQUA+"Password set to '"+builder.toString()+"'");
 	}
 	public void on(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.toggle")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		GlobalConf.enabled = true;
 		GlobalConf.save();
 		sender.sendMessage(ChatColor.AQUA+"DragonList is now "+ChatColor.GREEN+"ON");
 	}
 	public void off(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.toggle")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		GlobalConf.enabled = false;
 		GlobalConf.save();
 		sender.sendMessage(ChatColor.AQUA+"DragonList is now "+ChatColor.RED+"OFF");
 	}
 	public void type(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.type")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		if (args.length == 0) {
 			sender.sendMessage(ChatColor.RED+"Please specify a type!");
 			return;
 		}
 		GlobalConf.mode = WhitelistMode.valueOf(args[0].toUpperCase(Locale.ENGLISH));
 		sender.sendMessage(ChatColor.AQUA+"Type set to "+GlobalConf.mode.toString().toLowerCase(Locale.ENGLISH));
 	}
 }
