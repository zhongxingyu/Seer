 package com.cole2sworld.dragonlist;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Locale;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 @SuppressWarnings("static-method")
 /**
  * Handles commands
  *
  */
 public final class CommandHandler {
 	private boolean consoleConfirm = false;
 	public void add(CommandSender sender, String[] args, String label) {
 		Main.debug("Entering add");
 		if (!sender.hasPermission("dragonlist.add")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		try {
 			byte[] procIp = Util.processIp(args[0]);
 			if (GlobalConf.mode != WhitelistMode.IP) {
 				sender.sendMessage(ChatColor.RED+"You can't whitelist IPs outside of IP whitelist mode!");
 				return;
 			}
 			else {
 				if (Main.DEBUG) Main.debug("Adding to IP whitelist");
 				WhitelistManager.addToIPWhitelist(InetAddress.getByAddress(procIp));
 			}
 		} catch (UnknownHostException e) {
 			Main.debug("UHE thrown");
 		}
 		Main.debug("Whitelist mode is "+GlobalConf.mode);
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
 			Util.processIp(args[0]);
 			if (GlobalConf.mode != WhitelistMode.IP) {
 				sender.sendMessage(ChatColor.RED+"You can't whitelist IPs outside of IP whitelist mode!");
 				return;
 			}
 			else {
 				WhitelistManager.removeFromIPWhitelist(InetAddress.getByAddress(Util.processIp(args[0])));
 			}
 		} catch (UnknownHostException e) {
 			// exception means success
 		}
 		if (GlobalConf.mode == WhitelistMode.NAME) WhitelistManager.removeFromNameWhitelist(args[0]);
 		if (GlobalConf.mode == WhitelistMode.IP) WhitelistManager.removeFromIPWhitelist(IPLogManager.lookupByName(args[0]));
 		if (GlobalConf.mode == WhitelistMode.PASSWORD) WhitelistManager.removeFromPasswordWhitelist(args[0]);
 		sender.sendMessage(ChatColor.AQUA+args[0]+" removed from the "+GlobalConf.mode.toString().toLowerCase(Locale.ENGLISH)+" whitelist.");
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
 		GlobalConf.save();
 		sender.sendMessage(ChatColor.AQUA+"Type set to "+GlobalConf.mode.toString().toLowerCase(Locale.ENGLISH));
 	}
 	public void lookup(CommandSender sender, String[] args, String label) {
 		if (!sender.hasPermission("dragonlist.lookup")) {
 			sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 			return;
 		}
 		try {
 			String lookedup = IPLogManager.lookupByIP(InetAddress.getByAddress(Util.processIp(args[0])));
 			sender.sendMessage(args[0]+" = "+lookedup != null ? lookedup : "????");
 		} catch (UnknownHostException e) {
 			InetAddress lookedup = IPLogManager.lookupByName(args[0]);
 			sender.sendMessage(args[0]+" = "+lookedup != null ? lookedup.getHostAddress() : "????");
 		}
 	}
 	public void clear(CommandSender sender, String[] args, String label) {
 		if (sender instanceof Player) {
 			if (!sender.hasPermission("dragonlist.clear")) {
 				sender.sendMessage(ChatColor.RED+"You don't have permission to do that.");
 				return;
 			}
 			if (((Player)sender).hasMetadata("DLClearConfirming")) {
 				WhitelistManager.ips.clear();
 				WhitelistManager.names.clear();
 				for (String key : WhitelistManager.pass.getKeys(false)) {
 					WhitelistManager.pass.set(key, null);
 				}
 				WhitelistManager.save();
 				((Player) sender).removeMetadata("DLClearConfirming", Main.instance);
 				sender.sendMessage(ChatColor.AQUA+"Clearing complete.");
 				return;
 			}
 			sender.sendMessage(ChatColor.RED+"Are you sure you want to clear "+ChatColor.BOLD+"all "+ChatColor.RED+" whitelists? "+ChatColor.DARK_RED+ChatColor.ITALIC+ChatColor.BOLD+"This cannot be undone!");
 			sender.sendMessage(ChatColor.DARK_RED+"To confirm, use "+ChatColor.ITALIC+"/"+label+" clear"+ChatColor.DARK_RED+" again.");
 			((Player) sender).setMetadata("DLClearConfirming", new FixedMetadataValue(Main.instance, true));
 		} else {
 			if (consoleConfirm) {
 				WhitelistManager.ips.clear();
 				WhitelistManager.names.clear();
 				for (String key : WhitelistManager.pass.getKeys(false)) {
 					WhitelistManager.pass.set(key, null);
 				}
 				WhitelistManager.save();
 				consoleConfirm = false;
 				sender.sendMessage(ChatColor.AQUA+"Clearing complete.");
 				return;
 			}
 			sender.sendMessage(ChatColor.RED+"Are you sure you want to clear "+ChatColor.BOLD+"all "+ChatColor.RED+" whitelists? "+ChatColor.DARK_RED+ChatColor.ITALIC+ChatColor.BOLD+"This cannot be undone!");
 			sender.sendMessage(ChatColor.DARK_RED+"To confirm, use "+ChatColor.ITALIC+label+" clear"+ChatColor.DARK_RED+" again.");
 			consoleConfirm = true;
 		}
 	}
 }
