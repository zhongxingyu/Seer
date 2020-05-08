 package com.LRFLEW.PvP;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Commands implements CommandExecutor {
 	private final PvP plugin;
 	
 	protected Commands (PvP instance) {
 		plugin = instance;
 	}
 	
 	public boolean onCommand (CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (args.length >= 1 && args[0].equalsIgnoreCase("on")) {
 			if (sender instanceof Player && args.length == 1) {
 				Player player = (Player)sender;
 				if (plugin.cooldown.get(player.getName()) != null && plugin.cooldown.get(player.getName()) > System.currentTimeMillis()) {
 						player.sendMessage(Settings.preFx + "You need to wait " + plugin.sets.cooldownOnToOff + " seconds before you can turn PVP on");
 				} else {
 					if (!plugin.PvP.contains(player.getName())) {
 						plugin.PvP.add(player.getName());
 						plugin.cooldown.put(player.getName(), System.currentTimeMillis() + (plugin.sets.cooldownOnToOff*1000));
 						if (plugin.sets.announce) Misc.announceExclude(Settings.preFx + player.getDisplayName() + " is raring to fight", player);
 					} else {
 						if (plugin.cooldown.containsKey(player.getName())) plugin.cooldown.remove(player.getName());
 					}
 					player.sendMessage(Settings.preFx + "PvP is " + ChatColor.WHITE + "On" + Settings.preFx + " for you. Beware! " +
 							"Turn it off by typing " + ChatColor.WHITE + "/pvp off");
 					}
 				return true;
 			} else if (args.length >= 2){
 				Player player = Bukkit.getPlayer(args[1]);
 				if (plugin.cooldown.get(player.getName()) != null && plugin.cooldown.get(player.getName()) > System.currentTimeMillis()) {
 					sender.sendMessage(Settings.preFx + "You need to wait " + plugin.sets.cooldownOnToOff + 
 							" seconds before you can turn PVP on for" + player.getDisplayName());
 				} else {
 					if (!plugin.PvP.contains(player.getName())) {
 						plugin.PvP.add(player.getName());
 						plugin.cooldown.put(player.getName(), System.currentTimeMillis() + (plugin.sets.cooldownOnToOff*1000));
 						if (plugin.sets.announce) Misc.announceExclude(Settings.preFx + player.getDisplayName() + " is raring to fight", player);
 					} else {
 						if (plugin.cooldown.containsKey(player.getName())) plugin.cooldown.remove(player.getName());
 					}
 				}
 				sender.sendMessage(Settings.preFx + "PvP is " + ChatColor.WHITE + "On" + Settings.preFx + " for " + player.getDisplayName());
 				return true;
 			}
 		}
 		if (args.length >= 1 && args[0].equalsIgnoreCase("off")) {
 			if (sender instanceof Player && args.length == 1) {
 				Player player = (Player)sender;
 				if (plugin.cooldown.get(player.getName()) != null && plugin.cooldown.get(player.getName()) > System.currentTimeMillis()) {
 					player.sendMessage(Settings.preFx + "You need to wait " + plugin.sets.cooldownOnToOff + " seconds before you can turn PVP off");
 					return true;
 				} else {
 					if (plugin.PvP.contains(player.getName())) {
 						plugin.PvP.remove(player.getName());
 						plugin.cooldown.put(player.getName(), System.currentTimeMillis() + (plugin.sets.cooldownOffToOn*1000));
 						if (plugin.sets.announce) Misc.announceExclude(Settings.preFx + player.getDisplayName() + " is done fighting", player);
 					}
 					player.sendMessage(Settings.preFx + "PvP is " + ChatColor.WHITE + "Off" + Settings.preFx + " for you. " +
 							"Just look out for spiders :)");
 					return true;
 				}
 			} else if (args.length >= 2) {
 				Player player = Bukkit.getPlayer(args[1]);
 				if (plugin.cooldown.get(player.getName()) != null && plugin.cooldown.get(player.getName()) > System.currentTimeMillis()) {
 					sender.sendMessage(Settings.preFx + "You need to wait " + plugin.sets.cooldownOnToOff + 
 							" seconds before you can turn PVP on for" + player.getDisplayName());
 					return true;
 				} else {
 					if (plugin.PvP.contains(player.getName())) {
 						plugin.PvP.remove(player.getName());
 						plugin.cooldown.put(player.getName(), System.currentTimeMillis() + (plugin.sets.cooldownOffToOn*1000));
 						if (plugin.sets.announce) Misc.announceExclude(Settings.preFx + player.getDisplayName() + " is done fighting", player);
 					}
 					sender.sendMessage(Settings.preFx + "PvP is " + ChatColor.WHITE + "On" + Settings.preFx + " for " + player.getDisplayName());
 					return true;
 				}
 			}
 		}
 		if (args.length >= 1 && args[0].equalsIgnoreCase("spar")) {
 			if (sender instanceof Player) {
 				Player sparrer = (Player)sender;
 				
 				if (args.length > 1) {
 					Player sparri = plugin.getServer().getPlayer(args[1]);
 					if (sparri == null) {
 						sparrer.sendMessage("Player not Found");
 						return true;
 					}
 					plugin.sparRequest.put(sparri.getName(), sparrer.getName());
 					sparri.sendMessage(sparrer.getDisplayName() + Settings.preFx + " wants to spar you.  ");
 					sparri.sendMessage(Settings.preFx + "Accept by typing " + ChatColor.WHITE + "/pvp yes" + 
 							Settings.preFx + " or deny with " + ChatColor.WHITE + "/pvp no");
 					sparrer.sendMessage("Request sent to " + sparri.getDisplayName());
 					return true;
 				}
 			}
 		}
 		if (args.length >= 1 && args[0].equalsIgnoreCase("yes")) {
 			if (sender instanceof Player) {
 				Player sparri = (Player)sender;
 				if (plugin.sparRequest.containsKey(sparri.getName())) {
 					if (plugin.sparRequest.get(sparri.getName()) == null) {
 						plugin.sparRequest.remove(sparri.getName());
 						new Misc.MapMissmatchException(sparri.getName() + " -> null");
 					} else {
 						Player sparrer = Bukkit.getPlayerExact(plugin.sparRequest.get(sparri.getName()));
 						plugin.sparRequest.remove(sparri.getName());
 						plugin.spar.put(sparri.getName(), sparrer.getName());
 						plugin.spar.put(sparrer.getName(), sparri.getName());
 						sparri.sendMessage(Settings.preFx + "You are now sparring " + ChatColor.WHITE + sparrer.getDisplayName() + 
 								Settings.preFx + ".  Good luck");
 						sparrer.sendMessage(Settings.preFx + "You are now sparring " + ChatColor.WHITE + sparrer.getDisplayName() + 
 								Settings.preFx + ".  Good luck");
 						if (plugin.sets.announce) Misc.announceExclude(Settings.preFx + sparri.getDisplayName() + " and " + sparrer.getDisplayName() + 
 								" are noew dueling eachother.  Epic", sparrer, sparri);
 						return true;
 					}
 				}
 			}
 		}
 		if (args.length >= 1 && args[0].equalsIgnoreCase("no")) {
 			if (sender instanceof Player) {
 				Player sparri = (Player)sender;
 				if (plugin.sparRequest.containsKey(sparri.getName())) {
 					if (plugin.sparRequest.get(sparri.getName()) == null) {
 						plugin.sparRequest.remove(sparri.getName());
 						new Misc.MapMissmatchException(sparri.getName() + " -> null");
 					} else {
 						Player sparrer = Bukkit.getPlayerExact(plugin.sparRequest.get(sparri.getName()));
 						plugin.sparRequest.remove(sparri.getName());
 						sparri.sendMessage("You have denied the spar request from " + sparrer.getDisplayName());
 						sparrer.sendMessage(sparrer.getDisplayName() + " Denied you sparring request.  Sorry");
 						return true;
 					}
 				}
 			}
 		}
 		if (args.length >= 1 && args[0].equalsIgnoreCase("killswitch") || args.length >= 1 && args[0].equalsIgnoreCase("ks")) {
 			if (sender instanceof Player) 
 				if (!((Player)sender).isOp()) {
 					sender.sendMessage((Settings.preFx + "You don't have permissions to run this command"));
 					return true;
 				}
 			if (args[1].equalsIgnoreCase("dissable") || args[1].equalsIgnoreCase("dis")) {
 				plugin.killSwitch = 0;
 				Bukkit.broadcastMessage(Settings.preFx + "Users can now set their own PvP status");
 			} else {
 				if (args[1].equalsIgnoreCase("on")) {
 					plugin.killSwitch = 1;
 					Bukkit.broadcastMessage(Settings.preFx + "PvP is now forced on for everybody");
 				} if (args[1].equalsIgnoreCase("off")) {
 					plugin.killSwitch = -1;
 					Bukkit.broadcastMessage(Settings.preFx + "PvP is now forced off for everybody");
 					return true;
 				}
 			}
 		}
 		if (args.length >= 1 && args[0].equalsIgnoreCase("list")) {
 			if (plugin.PvP.isEmpty()) {
 				sender.sendMessage(Settings.preFx + "Nobody has PvP on");
 				return true;
 			}
 			int page;
 			if (args.length >= 2) try {
 				page = Integer.parseInt(args[1]) - 1;
 				if (page < 0) {
 					if (page == -1) sender.sendMessage(Settings.preFx + "no page 0");
 					else sender.sendMessage(Settings.preFx + "no negative pages");
 					page = 0;
 				}
 				if (page * 16 > plugin.PvP.size()) {
					sender.sendMessage(Settings.preFx + "there is no page " + (page + 1));
 					return true;
 				}
 			} catch (NumberFormatException e) {
 				sender.sendMessage(Settings.preFx + "\"" + args[1] + "\"" + " is not a number");
 				page = 0;
 			}
 			else page = 0;
 			boolean display = sender instanceof Player;
			sender.sendMessage(Settings.preFx + "page " + (page + 1) + " out of " + Math.ceil((double)plugin.PvP.size()/16));
 			String[] names = new String[16];
 			int i = 0, j = 0;
 			for (String p : plugin.PvP) {
 				if (i++ < page * 16) continue;
 				if (display) names[j++] = Bukkit.getPlayer(p).getDisplayName();
 				else names[j++] = p;
 				if (j >= 16) break;
 			}
 			for (i = 0; i < 4 && (i*4) < j; i++) {
 				String temp = "";
 				for (int k = 0; k < 4 && (i*4) + k < j; k++) {
 					temp += Settings.preFx + names[(i*4) + k];
 				}
 				sender.sendMessage(temp);
 			}
 			return true;
 		}
 		if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
 			if (sender instanceof Player) {
 				Player player = (Player)sender;
 				player.sendMessage(Settings.preFx + "Usage:");
 				player.sendMessage(ChatColor.WHITE + "   /PvP on" + Settings.preFx + " - you can be attacked by players");
 				player.sendMessage(ChatColor.WHITE + "   /PvP off" + Settings.preFx + " - players can't attack you");
 				return true;
 			} else {
 				System.out.println("Usage:");
 				System.out.println("\t/PvP on" + Settings.preFx + " - you can be attacked by players");
 				System.out.println("\t/PvP off" + Settings.preFx + " - players can't attack you");
 			}
 		}
 		if (sender instanceof Player) {
 			Player player = (Player)sender;
 			if ( plugin.PvP.contains(player.getName())) {
 				player.sendMessage(Settings.preFx + "PvP is " + ChatColor.WHITE + "On" + Settings.preFx + " for you. Beware!");
 			} else {
 				player.sendMessage(Settings.preFx + "PvP is " + ChatColor.WHITE + "Off" + Settings.preFx + " for you. " +
 						"Just look out for spiders :)");
 			}
 			player.sendMessage(Settings.preFx + "Usage:");
 			player.sendMessage(ChatColor.WHITE + "   /PvP on" + Settings.preFx + " - you can be attacked by players");
 			player.sendMessage(ChatColor.WHITE + "   /PvP off" + Settings.preFx + " - players can't attack you");
 			Long l = plugin.cooldown.get(player.getName());
 			if (l != null && l <= System.currentTimeMillis()) plugin.cooldown.remove(player.getName());
 			return true;
 		} else {
 			System.out.println("Type \"pvp help\" for a list of commands");
 		}
 		return true;
 	}
 	
 }
