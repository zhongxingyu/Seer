 package de.xghostkillerx.glowstonedrop;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 /**
  * GlowstoneDrop for CraftBukkit/Bukkit
  * Handles the commands!
  * 
  * Refer to the forum thread:
  * http://bit.ly/oW6iR1
  * Refer to the dev.bukkit.org page:
  * http://bit.ly/rcN2QB
  *
  * @author xGhOsTkiLLeRx
  * @thanks to XxFuNxX for the original GlowstoneDrop plugin!
  * 
  */
 
 public class GlowstoneDropCommands {
 	
 	GlowstoneDrop plugin;
 	public GlowstoneDropCommands(GlowstoneDrop instance) {
 	plugin = instance;
 	}
 
 	// Commands; always check for permissions!
 	public boolean GlowstoneDropCommand (CommandSender sender, Command command, String commandLabel, String[] args) {
 		if ((command.getName().equalsIgnoreCase("glowstonedrop")) || (command.getName().equalsIgnoreCase("glowdrop"))) {
 			// reload
 			if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
 				if (plugin.config.getBoolean("configuration.permissions") == true) {
 					if (sender.hasPermission("glowstonedrop.reload")) {
 						GlowstoneDropReload(sender, args);
 						return true;
 					}
 					else {
 						sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 						return true;
 					}
 				}
 				if (plugin.config.getBoolean("configuration.permissions") == false) {
 					GlowstoneDropReload(sender, args);
 					return true;
 				}
 			}
 			// help
 			if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
 				if (plugin.config.getBoolean("configuration.permissions") == true) {
 					if (sender.hasPermission("glowstonedrop.help")) {
 						GlowstoneDropHelp(sender, args);
 						return true;
 					}
 					else {
 	                    sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 	                    return true;
 					}
 				}
 				if (plugin.config.getBoolean("configuration.permissions") == false) {
 					GlowstoneDropHelp(sender, args);
 					return true;
 				}
 			}
 			// set
 			if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
 				// normal
 				if (args.length > 1 && args[1].equalsIgnoreCase("normal")) {
 					// block
 					if (args.length > 2 && args[2].equalsIgnoreCase("block")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.normal")) {
 								GlowstoneDropNormalBlock(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropNormalBlock(sender, args);
 							return true;
 						}
 					}
 					// dust
 					if (args.length > 2 && args[2].equalsIgnoreCase("dust")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.normal")) {
 								GlowstoneDropNormalDust(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropNormalDust(sender, args);
 							return true;
 						}
 					}
 				}
 				// nether
 				if (args.length > 1 && args[1].equalsIgnoreCase("nether")) {
 					// block
 					if (args.length > 2 && args[2].equalsIgnoreCase("block")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.nether")) {
 								GlowstoneDropNetherBlock(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropNetherBlock(sender, args);
 							return true;
 						}
 					}
 					// dust
 					if (args.length > 2 && args[2].equalsIgnoreCase("dust")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.nether")) {
 								GlowstoneDropNetherDust(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropNetherDust(sender, args);
 							return true;
 						}
 					}
 				}
 				// The End
 				if (args.length > 1 && args[1].equalsIgnoreCase("end")) {
 					// block
 					if (args.length > 2 && args[2].equalsIgnoreCase("block")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.end")) {
 								GlowstoneDropEndBlock(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropEndBlock(sender, args);
 							return true;
 						}
 					}
 					// dust
 					if (args.length > 2 && args[2].equalsIgnoreCase("dust")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.end")) {
 								GlowstoneDropEndDust(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropEndDust(sender, args);
 							return true;
 						}
 					}
 				}
 				// all
 				if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
 					// block
 					if (args.length > 2 && args[2].equalsIgnoreCase("block")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.all")) {
 								GlowstoneDropAllBlock(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropAllBlock(sender, args);
 							return true;
 						}
 					}
 					// dust
 					if (args.length > 2 && args[2].equalsIgnoreCase("dust")) {
 						if (plugin.config.getBoolean("configuration.permissions") == true) {
 							if (sender.hasPermission("glowstonedrop.set.all")) {
 								GlowstoneDropAllDust(sender, args);
 								return true;
 							} else {
 								sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 								return true;
 							}
 						}
 						if (plugin.config.getBoolean("configuration.permissions") == false) {
 							GlowstoneDropAllDust(sender, args);
 							return true;
 						}
 					}
 				}
 			}
 			// enable
 			if (args.length > 0 && args[0].equalsIgnoreCase("enable")) {
 				// permissions
 				if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.enable.permissions")) {
 							GlowstoneDropEnablePermissions(sender, args);
 							return true;
 						} else {
 							sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropEnablePermissions(sender, args);
 						return true;
 					}
 				}
 				// messages
 				if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.enable.messages")) {
 							GlowstoneDropEnableMessages(sender, args);
 							return true;
 						} else {
 							sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropEnableMessages(sender, args);
 						return true;
 					}
 				}
 			}
 			// disable
 			if (args.length > 0 && args[0].equalsIgnoreCase("disable")) {
 				// permissions
 				if (args.length > 1 && args[1].equalsIgnoreCase("permissions")) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.disable.permissions")) {
 							GlowstoneDropDisablePermissions(sender, args);
 							return true;
 						} else {
 							sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropDisablePermissions(sender, args);
 						return true;
 					}
 				}
 				// messages
 				if (args.length > 1 && args[1].equalsIgnoreCase("messages")) {
 					if (plugin.config.getBoolean("configuration.permissions") == true) {
 						if (sender.hasPermission("glowstonedrop.disable.messages")) {
 							GlowstoneDropDisableMessages(sender, args);
 							return true;
 						} else {
 							sender.sendMessage(ChatColor.DARK_RED + "You don't have the permission to do this!");
 							return true;
 						}
 					}
 					if (plugin.config.getBoolean("configuration.permissions") == false) {
 						GlowstoneDropDisableMessages(sender, args);
 						return true;
 					}
 				}
 			}
 			if (args.length > 0 && args[0].equalsIgnoreCase("add")) {
 				if (args[1] != null) {
 					String value = args[1];
 					plugin.items.add(value);
 					sender.sendMessage("DONE!");
 					plugin.loadConfigAgain();
 				}
 				else sender.sendMessage("Falsch gemacht!");
 			}
 		}
 		return false;
 	}
 	
 	// See the help with /glowstonedrop help or /glowdrop help
 	private boolean GlowstoneDropHelp(CommandSender sender, String[] args) {
 		PluginDescriptionFile pdfFile = plugin.getDescription();
 		sender.sendMessage(ChatColor.DARK_GREEN	+ "Welcome to the GlowstoneDrop version " + ChatColor.DARK_RED + pdfFile.getVersion() + ChatColor.DARK_GREEN + " help!");
 		sender.sendMessage("To see the help type " + ChatColor.DARK_RED	+ "/glowstonedrop help " + ChatColor.WHITE + "or " + ChatColor.DARK_RED	+ "/glowdrop help");
 		sender.sendMessage("To reload use " + ChatColor.DARK_RED + "/glowstonedrop reload " + ChatColor.WHITE + "or " + ChatColor.DARK_RED + "/glowdrop reload");
 		sender.sendMessage("To change the drops use " + ChatColor.DARK_RED + "/glowstonedrop set <world> <drop>");
 		sender.sendMessage("or " + ChatColor.DARK_RED + "/glowdrop set <world> <drop>");
 		sender.sendMessage("To enable something use " + ChatColor.DARK_RED + "/glowstonedrop enable " + ChatColor.YELLOW + "<value>");
 		sender.sendMessage("or " + ChatColor.DARK_RED + "/glowdrop enable " + ChatColor.YELLOW + "<value>");
 		sender.sendMessage("To disable something use " + ChatColor.DARK_RED	+ "/glowstonedrop disable " + ChatColor.YELLOW + "<value>");
 		sender.sendMessage("or " + ChatColor.DARK_RED + "/glowdrop disable " + ChatColor.YELLOW + "<value>");
 		sender.sendMessage(ChatColor.YELLOW + "Values " + ChatColor.WHITE + "can be: permissions, messages");
 		sender.sendMessage(ChatColor.YELLOW + "Worlds " + ChatColor.WHITE + "can be: normal, end, nether");
 		sender.sendMessage(ChatColor.YELLOW + "Drops " + ChatColor.WHITE + "can be: dust, block");
 		return true;
 	}
 	
 	// Reloads the config with /glowstonedrop reload or /glowdrop reload
 	private boolean GlowstoneDropReload(CommandSender sender, String[] args) {
 		PluginDescriptionFile pdfFile = plugin.getDescription();
 		plugin.loadConfigAgain();
 		sender.sendMessage(ChatColor.DARK_GREEN + "GlowstoneDrop version " + ChatColor.DARK_RED + pdfFile.getVersion() + ChatColor.DARK_GREEN + " reloaded!");
 		return true;
 	}
 	
 	// Enables permissions with /glowstonedrop enable permissions or /glowdrop enable permissions
 	private boolean GlowstoneDropEnablePermissions(CommandSender sender, String[] args) {
 		plugin.config.set("configuration.permissions", true);
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "GlowstoneDrop " + ChatColor.DARK_RED	+ "permissions " + ChatColor.DARK_GREEN	+ "enabled! Only OPs");
 		sender.sendMessage(ChatColor.DARK_GREEN + "or players with the permission can use the plugin!");
 		return true;
 	}
 	
 	// Disables permissions with /glowstonedrop disable permissions or /glowdrop disable permissions
 	private boolean GlowstoneDropDisablePermissions(CommandSender sender, String[] args) {
 		plugin.config.set("configuration.permissions", false);
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "GlowstoneDrop " + ChatColor.DARK_RED + "permissions " + ChatColor.DARK_GREEN	+ "disabled!"); 
 		sender.sendMessage(ChatColor.DARK_GREEN + "All players can use the plugin!");
 		return true;
 	}
 	
 	// Enables messages with /glowstonedrop enable messages or /glowdrop enable messages
 	private boolean GlowstoneDropEnableMessages(CommandSender sender, String[] args) {
 		plugin.config.set("configuration.messages", true);
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "GlowstoneDrop " + ChatColor.DARK_RED	+ "messages " + ChatColor.DARK_GREEN + "enabled!");
 		return true;
 	}
 	
 	// Disables messages with /glowstonedrop disable messages or /glowdrop disable messages
 	private boolean GlowstoneDropDisableMessages(CommandSender sender, String[] args) {
 		plugin.config.set("configuration.messages", false);
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "GlowstoneDrop " + ChatColor.DARK_RED + "messages " + ChatColor.DARK_GREEN + "disabled!");
 		return true;
 	}
 	
 	// Sets the normal drop to dust with /glowstonedrop set normal dust or /glowdrop set normal dust
 	private boolean GlowstoneDropNormalDust(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.normal", "dust");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "normal worlds " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "dust");
 		return true;
 	}
 	
	// Sets the nether drop to dust /glowstonedrop set nether dust or /glowdrop set nether dust
 	private boolean GlowstoneDropNetherDust(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.nether", "dust");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "nether worlds " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "dust");
 		return true;
 	}
 	
 	// Sets the the end drop to dust with /glowstonedrop set end dust or /glowdrop set end dust
 	private boolean GlowstoneDropEndDust(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.end", "dust");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "the end " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "dust");
 		return true;
 	}
 	
 	// Sets the all drops to dust with /glowstonedrop set all dust or /glowdrop set all dust
 	private boolean GlowstoneDropAllDust(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.normal", "dust");
 		plugin.config.set("worlds.nether", "dust");
 		plugin.config.set("worlds.end", "dust");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "all worlds " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "dust");
 		return true;
 	}
 	
 	// Sets the normal drop to block with /glowstone drop set normal block or /glowdrop set normal block
 	private boolean GlowstoneDropNormalBlock(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.normal", "block");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "normal worlds " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "blocks");
 		return true;
 	}
 	
 	// Sets the nether drop to block with /glowstone drop set nether block or /glowdrop set nether block
 	private boolean GlowstoneDropNetherBlock(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.nether", "block");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "nether worlds " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "blocks");
 		return true;
 	}
 	
 	// Sets the sklyand drop to block with /glowstone drop set end block or /glowdrop set end block
 	private boolean GlowstoneDropEndBlock(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.end", "block");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "the end " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "blocks");
 		return true;
 	}
 	
 	// Sets the all drops to block with /glowstone drop set all block or /glowdrop set all block
 	private boolean GlowstoneDropAllBlock(CommandSender sender, String[] args) {
 		plugin.config.set("worlds.normal", "block");
 		plugin.config.set("worlds.nether", "block");
 		plugin.config.set("worlds.end", "block");
 		plugin.saveConfig();
 		sender.sendMessage(ChatColor.DARK_GREEN + "Drop for " + ChatColor.DARK_RED	+ "all worlds " + ChatColor.DARK_GREEN + "set to " + ChatColor.DARK_RED	+ "blocks");
 		return true;
 	}
 }
