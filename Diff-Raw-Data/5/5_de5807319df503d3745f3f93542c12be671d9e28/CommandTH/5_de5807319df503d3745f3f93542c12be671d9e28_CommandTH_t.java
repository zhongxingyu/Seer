 package au.com.addstar.truehardcore;
 /*
 * TrueHardcore
 * Copyright (C) 2013 add5tar <copyright at addstar dot com dot au>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import au.com.addstar.truehardcore.HardcorePlayers.HardcorePlayer;
 import au.com.addstar.truehardcore.HardcorePlayers.PlayerState;
 import au.com.addstar.truehardcore.HardcoreWorlds.HardcoreWorld;
 
 public class CommandTH implements CommandExecutor {
 	private TrueHardcore plugin;
 	
 	public CommandTH(TrueHardcore instance) {
 		plugin = instance;
 	}
 	
 	/*
 	 * Handle the /truehardcore command
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		String action = "";
 		if (args.length > 0) {
 			action = args[0].toUpperCase();
 		}
 
 		if (action.equals("PLAY")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.use")) { return true; }
 			}
 
 			if (args.length > 1) {
 				World world = plugin.getServer().getWorld(args[1]);
 				if (world == null) {
 					sender.sendMessage(ChatColor.RED + "Error: Unknown world!");
 					return true;
 				}
 				
 				if (plugin.IsHardcoreWorld(world)) {
 					Player player = (Player) sender;
 					if (!plugin.IsHardcoreWorld(player.getWorld())) {
 						plugin.PlayGame(world.getName(), player);
 					} else {
 						sender.sendMessage(ChatColor.RED + "Error: You are already in a hardcore world!");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Error: That is not a hardcore world!");
 				}
 			} else {
 				sender.sendMessage(ChatColor.YELLOW + "Usage: /th play <world>");
 			}
 		}
 		else if (action.equals("LEAVE")) {
 			final Player player = (Player) sender;
 			final Location oldloc = player.getLocation();
 
 			if (!plugin.IsPlayerSafe(player, 5, 5, 5)) {
 				player.sendMessage(ChatColor.RED + "It's not safe to leave.. there are monsters around..");
 				return true;
 			}
 			
 			player.sendMessage(ChatColor.GOLD + "Teleportation will commence in " + ChatColor.RED + "5 seconds" + ChatColor.GOLD + ". Don't move.");
 			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
 				@Override
 				public void run() {
 					Location newloc = player.getLocation();
 					if (newloc.distance(oldloc) <= 1) {
 						plugin.LeaveGame(player);
					} else {
						player.sendMessage(ChatColor.DARK_RED + "Pending teleportation request cancelled.");
 					}
 				}
 			}, 5 * 20L);
 		}
 		else if (action.equals("INFO")) {
 			HardcorePlayer hcp = null;
 			if (args.length == 1) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.info")) { return true; }
 
 					Player player = (Player) sender;
 					hcp = plugin.HCPlayers.Get(player);
 					if (hcp != null) {
 						hcp.updatePlayer(player);
 					} else {
 						sender.sendMessage(ChatColor.RED + "You must be in the hardcore world to use this command");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Usage: /th info <player> [world]");
 				}
 			}
 			else if (args.length == 2) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.info.other")) { return true; }
 				}
 				Player player = (Player) plugin.getServer().getPlayer(args[1]);
 				if (player != null) {
 					hcp = plugin.HCPlayers.Get(player);
 					if (plugin.IsHardcoreWorld(player.getWorld())) {
 						hcp.updatePlayer(player);
 					} else {
 						sender.sendMessage(ChatColor.RED + "Error: Unknown player!");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Unknown player");
 				}
 			}
 			else if (args.length == 3) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.info.other")) { return true; }
 				}
 				hcp = plugin.HCPlayers.Get(args[2], args[1]);
 				if (hcp != null) {
 					Player player = (Player) plugin.getServer().getPlayer(args[2]);
 					if (player != null) {
 						if (plugin.IsHardcoreWorld(player.getWorld())) {
 							if (args[1] == player.getWorld().getName()) {
 								hcp.updatePlayer(player);
 							}
 						}
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Error: Unknown player!");
 				}
 			}
 			
 			if (hcp != null) {
 				Integer gt;
 				if (hcp.getState() == PlayerState.IN_GAME) {
 					gt = (hcp.getGameTime() + hcp.TimeDiff(hcp.getLastJoin(), new Date()));
 				} else {
 					gt = hcp.getGameTime();
 				}
 				String gametime = Util.Long2Time(gt);
 
 				sender.sendMessage(ChatColor.GREEN + "Hardcore player information:");
 				sender.sendMessage(ChatColor.YELLOW + "Player: "        + ChatColor.AQUA + hcp.getPlayerName());
 				sender.sendMessage(ChatColor.YELLOW + "World: "         + ChatColor.AQUA + hcp.getWorld());
 				sender.sendMessage(ChatColor.YELLOW + "State: "         + ChatColor.AQUA + hcp.getState());
 				sender.sendMessage(ChatColor.YELLOW + "Game Time: "     + ChatColor.AQUA + gametime);
 				sender.sendMessage(ChatColor.YELLOW + "Current Level: " + ChatColor.AQUA + hcp.getLevel());
 				sender.sendMessage(ChatColor.YELLOW + "Total Score: "   + ChatColor.AQUA + hcp.getScore());
 				sender.sendMessage(ChatColor.YELLOW + "Total Deaths: "  + ChatColor.AQUA + hcp.getDeaths());
 				sender.sendMessage(ChatColor.YELLOW + "Top Score: "     + ChatColor.AQUA + hcp.getTopScore());
 			}
 		}
 		else if (action.equals("DUMP")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.admin")) { return true; }
 			}
 			if (args.length == 1) {
 				for (String key : plugin.HCPlayers.AllRecords().keySet()) {
 					HardcorePlayer hcp = plugin.HCPlayers.Get(key);
 					sender.sendMessage(Util.padRight(key, 30) + " " + hcp.getState());
 				}
 			}
 			else if (args.length == 3) {
 				HardcorePlayer hcp = plugin.HCPlayers.Get(args[2], args[1]);
 				if (hcp != null) {
 					sender.sendMessage(ChatColor.YELLOW + "Player Name    : " + ChatColor.AQUA + hcp.getPlayerName());
 					sender.sendMessage(ChatColor.YELLOW + "World          : " + ChatColor.AQUA + hcp.getWorld());
 					sender.sendMessage(ChatColor.YELLOW + "SpawnPos       : " + ChatColor.AQUA + hcp.getSpawnPos());
 					sender.sendMessage(ChatColor.YELLOW + "LastPos        : " + ChatColor.AQUA + hcp.getLastPos());
 					sender.sendMessage(ChatColor.YELLOW + "LastJoin       : " + ChatColor.AQUA + hcp.getLastJoin());
 					sender.sendMessage(ChatColor.YELLOW + "LastQuit       : " + ChatColor.AQUA + hcp.getLastQuit());
 					sender.sendMessage(ChatColor.YELLOW + "GameStart      : " + ChatColor.AQUA + hcp.getGameStart());
 					sender.sendMessage(ChatColor.YELLOW + "GameEnd        : " + ChatColor.AQUA + hcp.getGameEnd());
 					sender.sendMessage(ChatColor.YELLOW + "GameTime       : " + ChatColor.AQUA + hcp.getGameTime());
 					sender.sendMessage(ChatColor.YELLOW + "Level          : " + ChatColor.AQUA + hcp.getLevel());
 					sender.sendMessage(ChatColor.YELLOW + "Exp            : " + ChatColor.AQUA + hcp.getExp());
 					sender.sendMessage(ChatColor.YELLOW + "Score          : " + ChatColor.AQUA + hcp.getScore());
 					sender.sendMessage(ChatColor.YELLOW + "TopScore       : " + ChatColor.AQUA + hcp.getTopScore());
 					sender.sendMessage(ChatColor.YELLOW + "State          : " + ChatColor.AQUA + hcp.getState());
 					sender.sendMessage(ChatColor.YELLOW + "GodMode        : " + ChatColor.AQUA + hcp.isGodMode());
 					sender.sendMessage(ChatColor.YELLOW + "DeathMsg       : " + ChatColor.AQUA + hcp.getDeathMsg());
 					sender.sendMessage(ChatColor.YELLOW + "DeathPos       : " + ChatColor.AQUA + hcp.getDeathPos());
 					sender.sendMessage(ChatColor.YELLOW + "Deaths         : " + ChatColor.AQUA + hcp.getDeaths());
 					sender.sendMessage(ChatColor.YELLOW + "Modified       : " + ChatColor.AQUA + hcp.isModified());
 					sender.sendMessage(ChatColor.YELLOW + "Cow Kills      : " + ChatColor.AQUA + hcp.getCowKills());
 					sender.sendMessage(ChatColor.YELLOW + "Pig Kills      : " + ChatColor.AQUA + hcp.getPigKills());
 					sender.sendMessage(ChatColor.YELLOW + "Sheep Kills    : " + ChatColor.AQUA + hcp.getSheepKills());
 					sender.sendMessage(ChatColor.YELLOW + "Chicken Kills  : " + ChatColor.AQUA + hcp.getChickenKills());
 					sender.sendMessage(ChatColor.YELLOW + "Creeper Kills  : " + ChatColor.AQUA + hcp.getCreeperKills());
 					sender.sendMessage(ChatColor.YELLOW + "Zombie Kills   : " + ChatColor.AQUA + hcp.getZombieKills());
 					sender.sendMessage(ChatColor.YELLOW + "Skeleton Kills : " + ChatColor.AQUA + hcp.getSkeletonKills());
 					sender.sendMessage(ChatColor.YELLOW + "Spider Kills   : " + ChatColor.AQUA + hcp.getSpiderKills());
 					sender.sendMessage(ChatColor.YELLOW + "Ender Kills    : " + ChatColor.AQUA + hcp.getEnderKills());
 					sender.sendMessage(ChatColor.YELLOW + "Slime Kills    : " + ChatColor.AQUA + hcp.getSlimeKills());
 					sender.sendMessage(ChatColor.YELLOW + "Moosh Kills    : " + ChatColor.AQUA + hcp.getMooshKills());
 					sender.sendMessage(ChatColor.YELLOW + "Other Kills    : " + ChatColor.AQUA + hcp.getOtherKills());
 					sender.sendMessage(ChatColor.YELLOW + "Player Kills   : " + ChatColor.AQUA + hcp.getPlayerKills());
 				}
 			}
 		}
 		else if (action.equals("DUMPWORLDS")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.admin")) { return true; }
 			}
 			if (args.length == 1) {
 				for (String key : plugin.HardcoreWorlds.AllRecords().keySet()) {
 					HardcoreWorld hcw = plugin.HardcoreWorlds.Get(key);
 					sender.sendMessage(ChatColor.YELLOW + "World Name : " + ChatColor.AQUA + hcw.getWorld().getName());
 					sender.sendMessage(ChatColor.YELLOW + "Greeting   : " + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', hcw.getGreeting()));
 					sender.sendMessage(ChatColor.YELLOW + "Ban Time   : " + ChatColor.AQUA + hcw.getBantime());
 					sender.sendMessage(ChatColor.YELLOW + "Distance   : " + ChatColor.AQUA + hcw.getSpawnDistance());
 					sender.sendMessage(ChatColor.YELLOW + "Protection : " + ChatColor.AQUA + hcw.getSpawnProtection());
 					sender.sendMessage(ChatColor.YELLOW + "ExitPos    : " + ChatColor.AQUA + hcw.getExitPos());
 				}
 			}
 		}
 		else if (action.equals("SET")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.admin")) { return true; }
 			}
 			if (args.length == 3) {
 				if (args[1].toUpperCase().equals("EXIT")) {
 					World world = plugin.getServer().getWorld(args[2]);
 					if ((world != null) && (plugin.IsHardcoreWorld(world))) {
 						HardcoreWorld hcw = plugin.HardcoreWorlds.Get(world.getName());
 						plugin.Debug("Setting ExitPos for " + hcw.getWorld().getName());
 						Player player = (Player) sender;
 						hcw.setExitPos(player.getLocation());
 						plugin.Config().set("worlds." + world.getName() + ".exitpos", Util.Loc2Str(player.getLocation()));
 						plugin.saveConfig();
 					} else {
 						sender.sendMessage(ChatColor.RED + "Not a valid hardcore world");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Invalid option \"" + args[1] + "\"");
 				}
 			}
 		}
 		else if (action.equals("LIST") || action.equals("WHO")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.list")) { return true; }
 			}
 
 			sender.sendMessage(ChatColor.GREEN + "Players currently in hardcore worlds:");
 
 			boolean Playing = false;
 			for (String w : plugin.HardcoreWorlds.AllRecords().keySet()) {
 				World world = plugin.getServer().getWorld(w);
 				if ((world != null) && (world.getPlayers().size() > 0)) {
 					ArrayList<String> players = new ArrayList<String>();
 					for (Player p : world.getPlayers()) {
 						if (plugin.IsPlayerVanished(p)) continue;
 						Playing = true;
 						players.add(p.getName());
 					}
 					sender.sendMessage(ChatColor.YELLOW + world.getName() + ": " + ChatColor.AQUA + StringUtils.join(players, ", "));
 				}
 			}
 			if (!Playing) {
 				sender.sendMessage(ChatColor.RED + "None");
 			}
 		}
 		else if (action.equals("STATS") || action.equals("KILLS")) {
 			HardcorePlayer hcp = null;
 			if (args.length == 1) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.stats")) { return true; }
 
 					Player player = (Player) sender;
 					hcp = plugin.HCPlayers.Get(player);
 					if (hcp == null) {
 						sender.sendMessage(ChatColor.RED + "You must be in the hardcore world to use this command");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Usage: /th info <player> [world]");
 				}
 			}
 			else if (args.length == 2) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.stats.other")) { return true; }
 				}
 				Player player = (Player) plugin.getServer().getPlayer(args[1]);
 				if (player != null) {
 					hcp = plugin.HCPlayers.Get(player);
 					if (!plugin.IsHardcoreWorld(player.getWorld())) {
 						sender.sendMessage(ChatColor.RED + "Error: Unknown player!");
 					}
 				} else {
 					sender.sendMessage(ChatColor.RED + "Unknown player");
 				}
 			}
 			else if (args.length == 3) {
 				if (sender instanceof Player) {
 					if (!Util.RequirePermission((Player) sender, "truehardcore.stats.other")) { return true; }
 				}
 				hcp = plugin.HCPlayers.Get(args[2], args[1]);
 				if (hcp == null) {
 					sender.sendMessage(ChatColor.RED + "Error: Unknown player!");
 				}
 			}
 			
 			if (hcp != null) {
 				sender.sendMessage(ChatColor.GREEN + "Hardcore player statistics:");
 				sender.sendMessage(ChatColor.YELLOW + "Cow Kills      : " + ChatColor.AQUA + hcp.getCowKills());
 				sender.sendMessage(ChatColor.YELLOW + "Pig Kills      : " + ChatColor.AQUA + hcp.getPigKills());
 				sender.sendMessage(ChatColor.YELLOW + "Sheep Kills    : " + ChatColor.AQUA + hcp.getSheepKills());
 				sender.sendMessage(ChatColor.YELLOW + "Chicken Kills  : " + ChatColor.AQUA + hcp.getChickenKills());
 				sender.sendMessage(ChatColor.YELLOW + "Creeper Kills  : " + ChatColor.AQUA + hcp.getCreeperKills());
 				sender.sendMessage(ChatColor.YELLOW + "Zombie Kills   : " + ChatColor.AQUA + hcp.getZombieKills());
 				sender.sendMessage(ChatColor.YELLOW + "Skeleton Kills : " + ChatColor.AQUA + hcp.getSkeletonKills());
 				sender.sendMessage(ChatColor.YELLOW + "Spider Kills   : " + ChatColor.AQUA + hcp.getSpiderKills());
 				sender.sendMessage(ChatColor.YELLOW + "Ender Kills    : " + ChatColor.AQUA + hcp.getEnderKills());
 				sender.sendMessage(ChatColor.YELLOW + "Slime Kills    : " + ChatColor.AQUA + hcp.getSlimeKills());
 				sender.sendMessage(ChatColor.YELLOW + "Moosh Kills    : " + ChatColor.AQUA + hcp.getMooshKills());
 				sender.sendMessage(ChatColor.YELLOW + "Other Kills    : " + ChatColor.AQUA + hcp.getOtherKills());
 				sender.sendMessage(ChatColor.YELLOW + "Player Kills   : " + ChatColor.AQUA + hcp.getPlayerKills());
 			}
 		}
 		else if (action.equals("SAVE")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.admin")) { return true; }
 			}
 			plugin.Debug("Saving buffered data...");
 			plugin.SaveAllPlayers();
 		}
 		else if (action.equals("RELOAD")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.admin")) { return true; }
 			}
 			plugin.LoadWhiteList();
 		}
 		else if (action.equals("DISABLE")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.admin")) { return true; }
 			}
 			plugin.GameEnabled = false;
 			sender.sendMessage(ChatColor.RED + "TrueHardcore has been disabled.");
 		}
 		else if (action.equals("ENABLE")) {
 			if (sender instanceof Player) {
 				if (!Util.RequirePermission((Player) sender, "truehardcore.admin")) { return true; }
 			}
 			plugin.GameEnabled = true;
 			sender.sendMessage(ChatColor.GREEN + "TrueHardcore has been enabled.");
 		}
 		else {
 			sender.sendMessage(ChatColor.LIGHT_PURPLE + "TrueHardcore Commands:");
 			sender.sendMessage(ChatColor.AQUA + "/th play   " + ChatColor.YELLOW + ": Start or resume your hardcore game");
 			sender.sendMessage(ChatColor.AQUA + "/th leave  " + ChatColor.YELLOW + ": Leave the hardcore game (progress is saved)");
 			sender.sendMessage(ChatColor.AQUA + "/th list   " + ChatColor.YELLOW + ": List the current hardcore players");
 			sender.sendMessage(ChatColor.AQUA + "/th info   " + ChatColor.YELLOW + ": Display your current game information");
 			sender.sendMessage(ChatColor.AQUA + "/th stats  " + ChatColor.YELLOW + ": Display kill statistics");
 		}
 		
 		return true;
 	}
 }
