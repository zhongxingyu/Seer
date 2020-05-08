 package me.cmastudios.plugins.WarhubModChat;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import me.cmastudios.plugins.WarhubModChat.util.*;
 import me.cmastudios.plugins.WarhubModChat.SLAPI;
 
 public class WarhubModChat extends JavaPlugin {
 
 	Permission permissions = new Permission();
 	Message messageUtil = new Message();
 	String version;
 	Logger log = Logger.getLogger("Minecraft");
 	private final plrLstnr playerListener = new plrLstnr(this);
 	public HashMap<Player, String> channels = new HashMap<Player, String>();
 	public HashMap<Player, String> ignores = new HashMap<Player, String>();
 	public HashMap<String, Integer> mutedplrs = new HashMap<String, Integer>();
 	public HashMap<String, Integer> warnings = new HashMap<String, Integer>();
 
 	@Override
 	public void onDisable() {
 		channels.clear();
 		try {
 			SLAPI.save(warnings, "warnings.bin");
 			warnings.clear();
 			SLAPI.save(mutedplrs, "mutedplrs.bin");
 			mutedplrs.clear();
 		} catch (Exception e) {
 			log.severe("[WarhubModChat] Failed to save data!");
 			e.printStackTrace();
 		}
 		log.info("[WarhubModChat] Disabled!");
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onEnable() {
 		permissions.setupPermissions();
 		Config.setup();
 		try {
 			warnings = (HashMap<String, Integer>) SLAPI.load("warnings.bin");
 			mutedplrs = (HashMap<String, Integer>) SLAPI.load("mutedplrs.bin");
 		} catch (Exception e) {
 			log.severe("[WarhubModChat] Failed to load data!");
 			e.printStackTrace();
 		}
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(playerListener, this);
 		PluginDescriptionFile pdffile = this.getDescription();
 		version = pdffile.getVersion();
 		log.info("[WarhubModChat] Version " + version
 				+ " by cmastudios enabled!");
 	}
 
 	@SuppressWarnings("static-access")
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		}
 		if (cmd.getName().equalsIgnoreCase("modchat")) {
 			if (!permissions.has(player, "warhub.moderator")) {
 				player.sendMessage(ChatColor.RED
 						+ "You don't have the permissions to do that!");
 				return true;
 			}
 			if (args.length < 1) {
 				if (player == null) {
 					log.info("You can't use channels from the console, use '/modchat <message>' to chat.");
 					return true;
 				}
 				channels.put(player, "mod");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to mod.");
 			} else {
 				String message = "";
 				for (String arg : args) {
 					message = message + arg + " ";
 				}
 				if (message.equals(""))
 					return false;
 				if (player == null) {
 					List<Player> sendto = new ArrayList<Player>();
 					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 						if (permissions.has(p, "warhub.moderator")) {
 							sendto.add(p);
 						}
 					}
 					for (Player p : sendto) {
 						p.sendMessage(messageUtil.colorizeText(Config
 								.read("modchat-format")
 								.replace("%player", "tommytony")
 								.replace("%message", message)));
 					}
 					log.info("[MODCHAT] tommytony: " + message);
 					sendto.clear();
 					return true;
 				}
 				if (channels.containsKey(player)) {
 					String channel = channels.remove(player);
 					player.chat(message);
 					channels.put(player, channel);
 					channel = null;
 				} else {
 					channels.put(player, "mod");
 					player.chat(message);
 					channels.remove(player);
 				}
 
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("alert")) {
 			if (player == null) {
 				log.info("You can't use alert from the console, use '/say <message>' to chat.");
 				return true;
 			}
 			if (!permissions.has(player, "warhub.moderator")) {
 				player.sendMessage(ChatColor.RED
 						+ "You don't have the permissions to do that!");
 				return true;
 			}
 			if (args.length < 1) {
 				channels.put(player, "alert");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to alert.");
 			} else {
 				String message = "";
 				for (String arg : args) {
 					message = message + arg + " ";
 				}
 				if (message.equals(""))
 					return false;
 				if (channels.containsKey(player)) {
 					String channel = channels.remove(player);
 					player.chat(message);
 					channels.put(player, channel);
 					channel = null;
 				} else {
 					channels.put(player, "alert");
 					player.chat(message);
 					channels.remove(player);
 				}
 
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("global")) {
 			if (player == null) {
 				log.info("You can't use global from the console, use '/say <message>' to chat.");
 				return true;
 			}
 			if (args.length < 1) {
 				channels.remove(player);
 				player.sendMessage(ChatColor.YELLOW
 						+ "Chat switched to global.");
 			} else {
 				String message = "";
 				for (String arg : args) {
 					message = message + arg + " ";
 				}
 				if (message.equals(""))
 					return false;
 				if (channels.containsKey(player)) {
 					String channel = channels.remove(player);
 					player.chat(message);
 					channels.put(player, channel);
 					channel = null;
 				} else {
 					player.chat(message);
 				}
 
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("channel")) {
 			if (player == null) {
 				log.info("You can't use channels from the console, use '/<say/modchat> <message>' to chat.");
 				return true;
 			}
 			if (args.length < 1) {
 				if (!permissions.has(player, "warhub.moderator")) {
 					player.sendMessage(ChatColor.RED
 							+ "You're not a mod, and cannot change channels.");
 					return true;
 				} else {
 					player.sendMessage(ChatColor.RED
 							+ "Use '/ch <mod/alert/global>' to change your channel.");
 				}
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("mod")
 					|| args[0].equalsIgnoreCase("modchat")
 					|| args[0].equalsIgnoreCase("m")) {
 				if (!permissions.has(player, "warhub.moderator")) {
 					player.sendMessage(ChatColor.RED
 							+ "You don't have the permissions to do that!");
 					return true;
 				}
 				channels.put(player, "mod");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to mod.");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("a")
 					|| args[0].equalsIgnoreCase("alert")) {
 				if (!permissions.has(player, "warhub.moderator")) {
 					player.sendMessage(ChatColor.RED
 							+ "You don't have the permissions to do that!");
 					return true;
 				}
 				channels.put(player, "alert");
 				player.sendMessage(ChatColor.YELLOW + "Chat switched to alert.");
 				return true;
 			}
 			if (args[0].equalsIgnoreCase("g")
 					|| args[0].equalsIgnoreCase("global")) {
 				channels.remove(player);
 				player.sendMessage(ChatColor.YELLOW
 						+ "Chat switched to global.");
 				return true;
 			}
 			if (!permissions.has(player, "warhub.moderator")) {
 				player.sendMessage(ChatColor.RED
 						+ "You're not a mod, and cannot change channels.");
 				return true;
 			} else {
 				player.sendMessage(ChatColor.RED
 						+ "Use '/ch <mod/alert/global>' to change your channel.");
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("say")) {
 			if (player != null)
 				if (!player.isOp()) {
 					player.sendMessage(ChatColor.RED
 							+ "You don't have the permissions to do that!");
 					return true;
 				}
 			if (args.length == 0)
 				return false;
 			String message = "";
 			for (String arg : args) {
 				message = message + arg + " ";
 			}
 			if (message.equals(""))
 				return false;
 			this.getServer().broadcastMessage(
 					messageUtil.colorizeText(Config.read("say-format"))
 							.replace("%message", message));
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("deaf")) {
 			if (args.length < 1) {
 				player.sendMessage(ChatColor.YELLOW + "Deafened players:");
 				String plrs = "";
 				for (Player plr : ignores.keySet()) {
 					plrs += plr.getDisplayName() + ", ";
 				}
 				player.sendMessage(ChatColor.YELLOW + plrs);
 				player.sendMessage(ChatColor.YELLOW
 						+ "Use /deaf <player> to deafen someone.");
 				return true;
 			} else if (args.length == 1) {
 				Player todeafen = PlayerInfo.toPlayer(args[0]);
 				if (todeafen == player) {
 					if (ignores.containsKey(player)) {
 						ignores.remove(player);
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been undeafened.");
 					} else {
 						ignores.put(player, "");
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been deafened.");
 					}
 				} else if (player.hasPermission("warhub.moderator")) {
 					if (ignores.containsKey(todeafen)) {
 						ignores.remove(todeafen);
 						player.sendMessage(ChatColor.YELLOW
 								+ todeafen.getName() + " has been undeafened.");
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been undeafened.");
 					} else {
 						ignores.put(todeafen, "");
 						player.sendMessage(ChatColor.YELLOW
 								+ todeafen.getName() + " has been deafened.");
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been deafened.");
 					}
 
 				} else {
 					player.sendMessage(ChatColor.RED
 							+ "You do not have permissions to deafen others.");
 				}
				return true;
 			}
 			return false;
 		}
 		if (cmd.getName().equalsIgnoreCase("me")) {
 	    	if (mutedplrs.containsKey(player.getName())) {
 	    		player.sendMessage(ChatColor.RED + "You are muted.");
 	    		return true;
 	    	}
 			String message = "";
 			for (String arg : args) {
 				message = message + arg + " ";
 			}
 			if (message == "")
 				return false;
 			if (message == " ")
 				return false;
 			if (message.contains("\u00A7")
 					&& !player.hasPermission("warhub.moderator")) {
 				message = message.replaceAll("\u00A7[0-9a-fA-FkK]", "");
 			}
 
 			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
 				if (!ignores.containsKey(p))
 					p.sendMessage("* " + ChatColor.WHITE
 							+ player.getDisplayName() + ChatColor.WHITE + " "
 							+ message);
 			}
 			return true;
 		}
 		if (cmd.getName().equalsIgnoreCase("mute")) {
 			if (args.length < 1) {
 				player.sendMessage(ChatColor.YELLOW + "Muted players:");
 				String plrs = "";
 				for (String plr : mutedplrs.keySet()) {
 					plrs += plr + ", ";
 				}
 				player.sendMessage(ChatColor.YELLOW + plrs);
 				player.sendMessage(ChatColor.YELLOW
 						+ "Use /mute <player> to mute someone.");
 				return true;
 			} else if (args.length == 1) {
 				Player todeafen = PlayerInfo.toPlayer(args[0]);
 				if (todeafen == player && player.hasPermission("warhub.moderator")) {
 					if (mutedplrs.containsKey(player.getName())) {
 						mutedplrs.remove(player.getName());
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been unmuted.");
 					} else {
 						mutedplrs.put(player.getName(), 1);
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been muted.");
 					}
 				} else if (player.hasPermission("warhub.moderator")) {
 					if (mutedplrs.containsKey(todeafen.getName())) {
 						mutedplrs.remove(todeafen.getName());
 						player.sendMessage(ChatColor.YELLOW
 								+ todeafen.getName() + " has been unmuted.");
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been unmuted.");
 					} else {
 						mutedplrs.put(todeafen.getName(), 1);
 						player.sendMessage(ChatColor.YELLOW
 								+ todeafen.getName() + " has been muted.");
 						todeafen.sendMessage(ChatColor.YELLOW
 								+ "You have been muted.");
 					}
 
 				} else {
 					player.sendMessage(ChatColor.RED
 							+ "You do not have permissions to mute players.");
 				}
				return true;
 			}
 			return false;
 		}
 		return false;
 	}
 
 	public int parseTimeString(String time) {
 		if (!time.matches("[0-9]*h?[0-9]*m?"))
 			return -1;
 		if (time.matches("[0-9]+"))
 			return Integer.parseInt(time);
 		if (time.matches("[0-9]+m"))
 			return Integer.parseInt(time.split("m")[0]);
 		if (time.matches("[0-9]+h"))
 			return Integer.parseInt(time.split("h")[0]) * 60;
 		if (time.matches("[0-9]+h[0-9]+m")) {
 			String[] split = time.split("[mh]");
 			return (Integer.parseInt(split[0]) * 60)
 					+ Integer.parseInt(split[1]);
 		}
 		return -1;
 	}
 
 	public String parseMinutes(int minutes) {
 		if (minutes == 1)
 			return "one minute";
 		if (minutes < 60)
 			return minutes + " minutes";
 		if (minutes % 60 == 0) {
 			if (minutes / 60 == 1)
 				return "one hour";
 			else
 				return (minutes / 60) + " hours";
 		}
 		if (minutes == -1)
 			return "indefinitely";
 		int m = minutes % 60;
 		int h = (minutes - m) / 60;
 		return h + "h" + m + "m";
 	}
 
 }
