 /*
  * Copyright (C) 2013 Fabrizio Lungo
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
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.flungo.bukkit.randomwarp;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Fabrizio
  */
 class Commands implements CommandExecutor {
 
 	private RandomWarp plugin;
 
 	public Commands(RandomWarp plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
 		switch (cmnd.getName().toLowerCase()) {
 			case "rwarp":
 			case "randwarp":
 			case "randomwarp":
 				return handleUserCommand(cs, strings);
 		}
 		return false;
 	}
 
 	private boolean handleUserCommand(CommandSender cs, String[] args) {
 		if (cs instanceof Player && !(plugin.getPermissions().isUser((Player) cs))) {
 			cs.sendMessage(ChatColor.RED + "You don't have permisson to use that command");
 			return true;
 		}
 		try {
 			if (args.length <= 0) {
 				if (cs instanceof Player) {
 					try {
 						plugin.teleport((Player) cs, plugin.getConfig().getString("default"));
 					} catch (InvalidAreaException ex) {
 						cs.sendMessage("Could not teleport you. Contact server admin.");
 						plugin.getLogger().log(Level.WARNING, "Default area {0} doesn't exists", ex.getMessage());
 					}
 					return true;
 				} else {
 					cs.sendMessage(ChatColor.RED + "Only in game players can use that command.");
 					return false;
 				}
 			}
 			switch (args[0].toLowerCase()) {
 				case "reload":
 					if (cs instanceof Player && !plugin.getPermissions().isAdmin((Player) cs)) {
 						cs.sendMessage(ChatColor.RED + "You do not have permission to do that");
 						plugin.getLogger().log(Level.WARNING, "Player {0} tried to reload the plugin", cs.getName());
 						return true;
 					}
 					plugin.reload();
					cs.sendMessage(ChatColor.GREEN + "RandomWarp has been reloaded");
 					return true;
 				default:
 					if (args.length == 1) {
 						if (cs instanceof Player) {
 							if (!plugin.getPermissions().hasPermission((Player) cs, "warps." + args[0])) {
 								cs.sendMessage(ChatColor.RED + "You do not have permission for that area");
 								return true;
 							}
 							try {
 								plugin.teleport((Player) cs, args[0]);
 							} catch (InvalidAreaException ex) {
 								cs.sendMessage(ChatColor.RED + "Could not teleport you. Area \"" + args[0] + "\" is undefined");
 							}
 							return true;
 						} else {
 							cs.sendMessage(ChatColor.RED + "Only in game players can use that command.");
 							return false;
 						}
 					} else {
 						if (cs instanceof Player) {
 							if (!plugin.getPermissions().hasPermission((Player) cs, "warpother")) {
 								cs.sendMessage(ChatColor.RED + "You don't have permisson to use that command");
 								return true;
 							}
 							if (!plugin.getPermissions().hasPermission((Player) cs, "warps." + args[0])) {
 								cs.sendMessage(ChatColor.RED + "You do not have permission for that area");
 								return true;
 							}
 						}
 						Player p = plugin.getServer().getPlayer(args[1]);
 						if (p == null) {
 							cs.sendMessage(ChatColor.RED + "Player " + args[1] + " was not found or is not online");
 							return true;
 						}
 						try {
 							plugin.teleport(p, args[0]);
 						} catch (InvalidAreaException ex) {
 							cs.sendMessage("Could not teleport " + args[1] + ". Contact server admin.");
 								cs.sendMessage(ChatColor.RED + "Could not teleport " + args[1] + ". Area \"" + args[0] + "\" is undefined");
 						}
 						return true;
 					}
 			}
 		} catch (InvalidWorldException ex) {
 			cs.sendMessage("Could not teleport you. Contact server admin.");
 			plugin.getLogger().log(Level.WARNING, "World {0} does not exists", ex.getMessage());
 			return true;
 		} catch (InvalidCoordinatesException ex) {
 			cs.sendMessage("Could not teleport you. Contact server admin.");
 			plugin.getLogger().log(Level.WARNING, "Problem with coordinates in area {0}", ex.getMessage());
 			return true;
 		} catch (NoLocationFoundException ex) {
 			cs.sendMessage("A safe location could not be found. Please try again.");
 			plugin.getLogger().log(Level.WARNING, "Was unable to find a safe location in area {0}", ex.getMessage());
 			return true;
 		}
 	}
 }
