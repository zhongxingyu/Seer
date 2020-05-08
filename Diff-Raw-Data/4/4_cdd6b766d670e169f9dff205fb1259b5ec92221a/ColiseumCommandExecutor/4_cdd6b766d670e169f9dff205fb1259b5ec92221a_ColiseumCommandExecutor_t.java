 package org.SkyCraft.Coliseum;
 
 import java.util.logging.Logger;
 
 import org.SkyCraft.Coliseum.Arena.Arena;
 import org.SkyCraft.Coliseum.Arena.PVPArena;
 import org.SkyCraft.Coliseum.Arena.Combatant.Combatant;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.command.RemoteConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 public class ColiseumCommandExecutor implements CommandExecutor {
 
 	private ColiseumPlugin plugin;
 	private Logger log;
 	private ConfigHandler config;
 
 	ColiseumCommandExecutor(ColiseumPlugin plugin, Logger log, ConfigHandler config) {
 		this.plugin = plugin;
 		this.log = log;
 		this.config = config;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String aliasUsed, String[] args) {
 		if(sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender) {
 			sender.sendMessage("[Coliseum] Coliseum can only be used in-game.");
 			return true;
 		}
 		else {
 			if(args.length == 0) {
 				sender.sendMessage(ChatColor.GRAY + "[Coliseum] You need to add arguments to do anything useful.");
 				return true;
 			}
 
 			String argument = args[0];
 			//TODO add you are not editing messages.
 			if(argument.equalsIgnoreCase("create") && args.length >= 3) {//TODO editor permissions
 				if(args[1].equalsIgnoreCase("pvp")) {
 					StringBuilder sb = new StringBuilder();
 					for(int i = 2; i <= (args.length - 1); i++) {
 						if(i + 1 == args.length) {
 							sb.append(args[i]);
 							break;
 						}
 						sb.append(args[i] + " ");
 					}
 					PVPArena a = new PVPArena(sb.toString(), plugin);
 					plugin.getArenaSet().add(a);
 					config.createArena(sb.toString(), "pvp");
 					sender.sendMessage(ChatColor.GRAY + "[Coliseum] Created a new PVP arena called " + sb.toString() + ".");
 					for(Arena a2 : plugin.getArenaSet()) {
 						if(a2.isPlayerEditing((Player) sender)) {
 							a2.removeEditor((Player) sender);
 							sender.sendMessage(ChatColor.GRAY + "You are no longer editing " + a.getName() + ".");
 							break;
 						}
 					}
 					a.setPlayerEditing((Player) sender);
 					sender.sendMessage(ChatColor.GRAY + "[Coliseum] Now editing " + sb.toString() + ".");
 					return true;
 				}
 				else {
 					sender.sendMessage(ChatColor.GRAY + "[Coliseum] Not a recognized arena type.");
 					return true;
 				}
 			}
 			else if(argument.equalsIgnoreCase("edit")) {//TODO editor permissions
 				if(args.length >=2) {
 					StringBuilder sb = new StringBuilder();
 					for(int i = 1; i <= (args.length - 1); i++) {
 						if(i + 1 == args.length) {
 							sb.append(args[i]);
 							break;
 						}
 						sb.append(args[i] + " ");
 					}
 					for(Arena a : plugin.getArenaSet()) {
 						if(a.isThisArena(sb.toString())) {
 							for(Arena a2 : plugin.getArenaSet()) {
 								if(a2.isPlayerEditing((Player) sender)) {
 									a2.removeEditor((Player) sender);
 									sender.sendMessage(ChatColor.GRAY + "[Coliseum] You are no longer editing " + sb.toString() + ".");
 									break;
 								}
 							}
 							if(!a.isEnabled()) {
 								a.setPlayerEditing((Player) sender);
 								sender.sendMessage(ChatColor.GRAY + "[Coliseum] Now editing " + sb.toString() + ".");
 								return true;
 							}
 							else {
 								sender.sendMessage(ChatColor.GRAY + "[Coliseum] The arena you chose was enabled; you could not be placed in editing mode.");
 								return true;
 							}
 						}
 					}
 					sender.sendMessage(ChatColor.GRAY + "[Coliseum] No arena was found by that name.");
 					return true;
 				}
 				else {
 					for(Arena a : plugin.getArenaSet()) {
 						if(a.isPlayerEditing((Player) sender)) {
 							a.removeEditor((Player) sender);
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] You are no longer editing " + a.getName() + ".");
 							return true;
 						}
 					}
 					sender.sendMessage(ChatColor.GRAY + "[Coliseum] You need to supply an arena to begin editing or you were not editing anything to be removed from.");
 					return true;
 				}
 			}
 			else if(argument.equalsIgnoreCase("posone") || argument.equalsIgnoreCase("po") || argument.equalsIgnoreCase("p1")) {//TODO editor permissions
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						a.getRegion().setPos1(((Player) sender).getTargetBlock(null, 10));
 						config.setArenaPos(1, a.getName(), a.getRegion().getPos1());
 						sender.sendMessage(ChatColor.GRAY + "[Coliseum] Position one set.");
 						return true;
 					}//TODO Check if block is null (looking too far away), do later in interest of getting this done.
 				}
 			}
 			else if(argument.equalsIgnoreCase("wposone") || argument.equalsIgnoreCase("wpo") || argument.equalsIgnoreCase("wp1")) {//TODO editor permissions
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						a.getWaitingRegion().setPos1(((Player) sender).getTargetBlock(null, 10));
 						config.setArenaPos(3, a.getName(), a.getWaitingRegion().getPos1());
 						sender.sendMessage(ChatColor.GRAY + "[Coliseum] Waiting region position one set.");
 						return true;
 					}//TODO Check if block is null (looking too far away), do later in interest of getting this done.
 				}
 			}
 			else if(argument.equalsIgnoreCase("postwo") || argument.equalsIgnoreCase("pt") || argument.equalsIgnoreCase("p2")) {//TODO editor permissions
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						a.getRegion().setPos2(((Player) sender).getTargetBlock(null, 10));
 						config.setArenaPos(2, a.getName(), a.getRegion().getPos2());
 						sender.sendMessage(ChatColor.GRAY + "[Coliseum] Position two set.");
 						return true;
 					}//TODO Check if block is null (looking too far away), do later in interest of getting this done.
 				}
 			}
 			else if(argument.equalsIgnoreCase("wpostwo") || argument.equalsIgnoreCase("wpt") || argument.equalsIgnoreCase("wp2")) {//TODO editor permissions
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						a.getWaitingRegion().setPos2(((Player) sender).getTargetBlock(null, 10));
 						config.setArenaPos(4, a.getName(), a.getWaitingRegion().getPos2());
 						sender.sendMessage(ChatColor.GRAY + "[Coliseum] Waiting region position two set.");
 						return true;
 					}//TODO Check if block is null (looking too far away), do later in interest of getting this done.
 				}
 			}
 			else if(argument.equalsIgnoreCase("spawn") && args.length >= 2) {//TODO editor permissions
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						StringBuilder sb = new StringBuilder();
 						for(int i = 1; i <= (args.length - 1); i++) {
 							if(i + 1 == args.length) {
 								sb.append(args[i]);
 								break;
 							}
 							sb.append(args[i] + " ");
 						}
 						if(sb.toString().equalsIgnoreCase("waiting") || sb.toString().equalsIgnoreCase("wait") || sb.toString().equalsIgnoreCase("w")) {
 							if(a.getWaitingRegion().setSpawn(((Player) sender).getTargetBlock(null, 10).getLocation())) {
 								config.setSpawn(a.getName(), "WaitingAreaSpawn", a.getWaitingRegion().getSpawn());
 								sender.sendMessage(ChatColor.GRAY + "[Coliseum] Waiting area spawn set.");
 							}
 							else {
 								sender.sendMessage(ChatColor.GRAY + "[Coliseum] Waiting spawn was not created, place spawn inside region.");
 							}
 							return true;
 						}
 						else {
 							if(a.getTeams().containsKey(sb.toString().toLowerCase())) {//TODO if containsKey is case sensitive need new way to match arena names
 								if(a.getRegion().addTeamSpawn(sb.toString().toLowerCase(), ((Player) sender).getTargetBlock(null, 10).getLocation())) {
									config.setSpawn(a.getName(), sb.toString().toLowerCase(), a.getRegion().getTeamSpawn(sb.toString().toLowerCase()));
									sender.sendMessage(ChatColor.GRAY + "[Coliseum] Team " + sb.toString().toLowerCase() + " spawn was created.");
 									return true;
 								}
 								else {
 									sender.sendMessage(ChatColor.GRAY + "[Coliseum] Spawn was not created, place spawn inside region.");
 									return true;
 								}
 							}
 							else {
 								sender.sendMessage(ChatColor.GRAY + "[Coliseum] No team with that name was found, no spawn was set.");
 								return true;
 							}
 						}
 					}
 				}
 			}
 			else if(argument.equalsIgnoreCase("addteam") && args.length >= 2) {
 				StringBuilder sb = new StringBuilder();
 				for(int i = 1; i <= (args.length - 1); i++) {
 					if(i + 1 == args.length) {
 						sb.append(args[i]);
 						break;
 					}
 					sb.append(args[i] + " ");
 				}
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						a.addTeamName(sb.toString().toLowerCase());
 						sender.sendMessage(ChatColor.GRAY + "[Coliseum] Team " + sb.toString().toLowerCase() + " added.");
 						return true;
 					}
 				}
 			}
 			else if(argument.equalsIgnoreCase("remteam") && args.length >= 2) {
 				StringBuilder sb = new StringBuilder();
 				for(int i = 1; i <= (args.length - 1); i++) {
 					if(i + 1 == args.length) {
 						sb.append(args[i]);
 						break;
 					}
 					sb.append(args[i] + " ");
 				}
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						if(a.removeTeamName(sb.toString().toLowerCase())) {
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] Team " + sb.toString().toLowerCase() + " removed");
 							config.removeTeam(a.getName(), sb.toString());
 							return true;
 						}
 						else {
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] No team " + sb.toString().toLowerCase() + " was found");
 							return true;
 						}
 					}
 				}
 			}
 			else if(argument.equalsIgnoreCase("enable") && args.length >= 2) {//TODO Admin (and/or editor) [multiple permissions? admin can boot, editor can't?]
 				StringBuilder sb = new StringBuilder();
 				for(int i = 1; i <= (args.length - 1); i++) {
 					if(i + 1 == args.length) {
 						sb.append(args[i]);
 						break;
 					}
 					sb.append(args[i] + " ");
 				}
 
 				for(Arena a: plugin.getArenaSet()) {
 					if(a.isThisArena(sb.toString())) {
 						if(a.enable()) {
 							config.setArenaEnabledState(a.getName(), true);
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] Arena was successfully enabled!");
 							return true;
 						}
 						sender.sendMessage(ChatColor.GRAY + "[Coliseum] This arena was not ready to be enabled for some reason.");
 						return true;
 					}
 				}
 				sender.sendMessage(ChatColor.GRAY + "[Coliseum] No arena was found by that name.");
 				return true;
 			}
 			else if(argument.equalsIgnoreCase("join") && !plugin.isPlayerJoined(((Player) sender).getName()) && args.length >= 2) {//TODO player permissions
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isPlayerEditing((Player) sender)) {
 						sender.sendMessage(ChatColor.GRAY + "[Coliseum] You're still editing something. Quit editing and try to join again.");
 						return true;
 					}
 				}
 
 				StringBuilder sb = new StringBuilder();
 				for(int i = 1; i <= (args.length - 1); i++) {
 					if(i + 1 == args.length) {
 						sb.append(args[i]);
 						break;
 					}
 					sb.append(args[i] + " ");
 				}
 
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.isThisArena(sb.toString())) {
 						if(a.isEnabled()) {
 							a.addPlayer(((Player) sender));
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] Welcome to " + a.getName() + "!");
 							return true;
 						}
 						else {
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] Arena was not enabled.");
 							return true;
 						}
 					}
 					sender.sendMessage(ChatColor.GRAY + "[Coliseum] No arena was found by that name.");
 					return true;
 				}
 			}
 			else if(argument.equalsIgnoreCase("leave") && plugin.isPlayerJoined(((Player) sender).getName())) {//TODO player permissions
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.hasThisPlayer(((Player) sender))) {
 						a.removePlayer(((Player) sender));
 						sender.sendMessage(ChatColor.GRAY + "Arena left.");
 						return true;
 					}
 					sender.sendMessage(ChatColor.GRAY + "[Coliseum] You're not in an arena.");
 					return true;
 				}
 			}
 			else if(argument.equalsIgnoreCase("team") && args.length >= 2) {
 				StringBuilder sb = new StringBuilder();
 				for(int i = 1; i <= (args.length - 1); i++) {
 					if(i + 1 == args.length) {
 						sb.append(args[i]);
 						break;
 					}
 					sb.append(args[i] + " ");
 				}
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.hasThisPlayer((Player) sender)) {
 						if(a.getTeams().containsKey(sb.toString().toLowerCase())) {
 							a.getCombatant((Player) sender).joinTeam(sb.toString());
 							sender.sendMessage(ChatColor.GRAY + "[Colisemm] Your team is now set to " + sb.toString() + ".");
 							return true;
 						}
 						else {
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] No team was found by that name.");
 							return true;
 						}
 					}
 				}
 			}
 			else if(argument.equalsIgnoreCase("ready")) {//TODO fix me
 				for(Arena a : plugin.getArenaSet()) {
 					if(a.hasThisPlayer((Player) sender)) {
 						Combatant c = a.getCombatant((Player) sender);
 						if(c.setReadiness(!c.isReady())) {
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] You are now " + (c.isReady() ? "ready." : "not ready."));
 							return true;
 						}
 						else {
 							sender.sendMessage(ChatColor.GRAY + "[Coliseum] You could not be readied for some reason. Ensure you are entirely set up.");
 							return true;
 						}
 					}
 				}
 				sender.sendMessage(ChatColor.GRAY + "[Coliseum] You are not in an arena.");
 			}
 			else if(argument.equalsIgnoreCase("start")) {//TODO FIX ME
 				for(Arena a : plugin.getArenaSet()) {
 					a.start();
 				}
 			}
 			//TODO implement other commands (disable, kick, forcestart, forcend, createteam, removeteam)
 			//TODO implement a "help" message.
 			return true;
 		}
 
 	}
 }
