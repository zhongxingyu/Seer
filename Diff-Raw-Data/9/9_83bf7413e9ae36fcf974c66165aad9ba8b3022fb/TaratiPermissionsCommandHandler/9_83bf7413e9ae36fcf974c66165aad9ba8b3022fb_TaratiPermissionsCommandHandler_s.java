 package com.schneenet.tarati;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class TaratiPermissionsCommandHandler implements CommandExecutor {
 
 	private static final String COMMAND_TARATIPERMS = "taratiperms";
 	private static final String COMMAND_TARATIPERMS_RELOAD = "reload";
 	private static final String COMMAND_TARATIPERMS_RELOAD_ALL = "all";
 	private static final String COMMAND_TARATIPERMS_RELOAD_USER = "user";
 
 	private TaratiPermissionsPlugin plugin;
 
 	public TaratiPermissionsCommandHandler(TaratiPermissionsPlugin instance) {
 		this.plugin = instance;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (COMMAND_TARATIPERMS.equals(command.getName())) {
 			if (sender instanceof Player) {
 				Player p = (Player) sender;
 				if (this.plugin.getPermissionManager().has(p, COMMAND_TARATIPERMS, p.getWorld().getName())) {
 					if (args.length > 0) {
 						if (COMMAND_TARATIPERMS_RELOAD.equals(args[0])) {
 							if (this.plugin.getPermissionManager().has(p, COMMAND_TARATIPERMS + "." + COMMAND_TARATIPERMS_RELOAD, p.getWorld().getName())) {
 								if (args.length > 1) {
 									if (COMMAND_TARATIPERMS_RELOAD_ALL.equals(args[1])) {
 										if (this.plugin.getPermissionManager().has(p, COMMAND_TARATIPERMS + "." + COMMAND_TARATIPERMS_RELOAD + "." + COMMAND_TARATIPERMS_RELOAD_ALL, p.getWorld().getName())) {
 											Player[] players = this.plugin.getServer().getOnlinePlayers();
 											for (int i = 0; i < players.length; i++) {
 												this.plugin.processPlayer(players[i]);
 											}
 											p.sendMessage(ChatColor.GREEN + "Reloaded groups for " + players.length + " players.");
 										} else {
 											sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
 										}
 									} else if (COMMAND_TARATIPERMS_RELOAD_USER.equals(args[1])) {
 										if (args.length > 2) {
 											Player target = this.plugin.getServer().getPlayer(args[2]);
 											if (target != null) {
 												this.plugin.processPlayer(target);
 												p.sendMessage(ChatColor.GREEN + "Reloaded group for " + ChatColor.WHITE + args[2]);
 											} else {
 												p.sendMessage(ChatColor.RED + "There is no player by that name.");
 											}
 										} else {
 											p.sendMessage(ChatColor.RED + "Not enough arguments.");
 											p.sendMessage(ChatColor.RED + "Usage:");
 											p.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 										}
 									} else {
 										p.sendMessage(ChatColor.RED + "Invalid argument.");
 										p.sendMessage(ChatColor.RED + "Usage:");
 										p.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 										p.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_ALL);
 									}
 								} else {
 									this.plugin.processPlayer(p);
 									p.sendMessage(ChatColor.GREEN + "Reloaded your group.");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
 							}
 						} else { // Other commands go at this level
 							sender.sendMessage(ChatColor.RED + "Usage:");
 							sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD);
 							sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_ALL);
 							sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Usage:");
 						sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD);
 						sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_ALL);
 						sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
 				}
 			} else { // CommandSender is console
 				if (args.length > 0 && COMMAND_TARATIPERMS_RELOAD.equals(args[0])) {
 					if (args.length > 1) {
 						if (COMMAND_TARATIPERMS_RELOAD_ALL.equals(args[1])) {
 							Player[] players = this.plugin.getServer().getOnlinePlayers();
 							for (int i = 0; i < players.length; i++) {
 								this.plugin.processPlayer(players[i]);
 							}
 							sender.sendMessage("Reloaded groups for " + players.length + " players.");
 						} else if (COMMAND_TARATIPERMS_RELOAD_USER.equals(args[1])) {
 							if (args.length > 2) {
 								Player target = this.plugin.getServer().getPlayer(args[2]);
 								if (target != null) {
 									this.plugin.processPlayer(target);
 									sender.sendMessage("Reloaded group for " + args[2]);
 								} else {
 									sender.sendMessage(ChatColor.RED + "There is no user by that name.");
 								}
 							} else {
 								sender.sendMessage(ChatColor.RED + "Not enough arguments.");
 								sender.sendMessage(ChatColor.RED + "Usage:");
 								sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "Invalid argument.");
 							sender.sendMessage(ChatColor.RED + "Usage:");
 							sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 							sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_ALL);
 						}
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not enough arguments.");
 						sender.sendMessage(ChatColor.RED + "Usage:");
 						sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_ALL);
 						sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Usage:");
 					sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_ALL);
 					sender.sendMessage("    /" + COMMAND_TARATIPERMS + " " + COMMAND_TARATIPERMS_RELOAD + " " + COMMAND_TARATIPERMS_RELOAD_USER + " <user>");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 }
