 package com.etriacraft.etriabending.suites;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Player;
 
 import com.etriacraft.etriabending.EtriaBending;
 import com.etriacraft.etriabending.util.PlayerUtils;
 
 public class TeleportSuite {
 
 	public static HashMap<String, LinkedList<String>> tpRequestDb = new HashMap<String, LinkedList<String>>();
 	EtriaBending plugin;
 
 	public TeleportSuite(EtriaBending instance) {
 		this.plugin = instance;
 		init();
 	}
 
 	private void init() {
 		PluginCommand spawn = plugin.getCommand("spawn");
 		PluginCommand tpa = plugin.getCommand("tpa");
 		PluginCommand tp = plugin.getCommand("tp");
 		PluginCommand tphere = plugin.getCommand("tphere");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.spawn")) {
 					s.sendMessage("cYou don't have permission for that!");
 				} else {
 					if (!(s instanceof Player)) return false;
 
 					PlayerUtils.teleport((Player) s, ((Player) s).getWorld().getSpawnLocation());
 					s.sendMessage("aSent to the spawn ofe " + ((Player) s).getWorld().getName());
 					return true;
 				} return true;
 			}
 		}; spawn.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				String player = args[0];
 				final Player p;
 				if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("y")) {
 					if (!s.hasPermission("eb.tpa.answer")) {
 						s.sendMessage("cYou don't have permission to do that!");
 						return true;
 					}
 					if (!tpRequestDb.containsKey(s.getName()) || tpRequestDb.get(s.getName()).isEmpty()) {
 						s.sendMessage("cYou have no teleport requests pending.");
 						return true;
 					}
 					if (args.length < 2) {
 						player = tpRequestDb.get(s.getName()).getLast();
 					} else {
 						player = args[1];
 					}
 
 					p = Bukkit.getPlayer(player);
 					if (p == null) {
 						s.sendMessage("cThat player is no longer online.");
 						return true;
 					}
 					if (tpRequestDb.get(s.getName()).contains(p.getName())) {
 						PlayerUtils.teleport(p, ((Player) s).getLocation());
 						p.sendMessage("e" + s.getName() + "aaccepted your teleport request");
 						s.sendMessage("aYou accepted the teleport request frome " + p.getName());
 						tpRequestDb.get(s.getName()).remove(p.getName());
 					} else s.sendMessage("cThis player hasn't request teleport permission from you.");
 				} else if (args[0].equalsIgnoreCase("decline") || args[0].equalsIgnoreCase("n")) {
 					if (!s.hasPermission("eb.tpa.answer")) {
 						s.sendMessage("cYou don't have permission to do that!");
 						return true;
 					}
 					if (args.length < 2) return false;
 					player = args[1];
 					p = Bukkit.getPlayer(player);
 					if (p == null) {
 						s.sendMessage("cThat player is no longer online.");
 						return true;
 					}
 					if (!tpRequestDb.containsKey(s.getName()) || tpRequestDb.get(s.getName()).contains(p.getName())) {
 						p.sendMessage("cYour teleport request to 7" + s.getName() + " cwas declined.");
 						tpRequestDb.get(s.getName()).remove(p.getName());
 						s.sendMessage("aDeniede " + player + "'s ateleport request.");
 					} else s.sendMessage("cThis player hasn't requested teleport permission from you.");
 				} else {
 					if (!s.hasPermission("eb.tpa")) {
 						s.sendMessage("cYou don't have permission to do that!");
 					} else {
 						p = Bukkit.getPlayer(player);
 						if (p == null) {
 							s.sendMessage("cThat player is not online.");
 							return true;
 						}
 						if (!tpRequestDb.containsKey(p.getName())) {
 							tpRequestDb.put(p.getName(), new LinkedList<String>());
 						}
 
 						tpRequestDb.get(p.getName()).add(s.getName());
 						s.sendMessage("aYou have sent a teleport request toe " + p.getName());
 						p.sendMessage("aYou have received a teleport request frome " + s.getName());
 						p.sendMessage("aDo e/tpa accept " + s.getName() + " ato accept, or e/tpa decline " + s.getName() + " ato deny");
 					}
 					return true;
 				}
 				return true;
 			}
 		}; tpa.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.tp")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					Location loc;
 					if (args.length >= 3) { // Teleporting to coords
 						try {
 							double x = Double.parseDouble(args[0]);
 							double y = Double.parseDouble(args[1]);
 							double z = Double.parseDouble(args[2]);
 							World w = (args.length >= 4)? Bukkit.getWorld(args[3]) : ((Player) s).getWorld();
 							if (w == null) {
 								s.sendMessage("cThat world doesn't exist.");
 								return true;
 							}
 							loc = new Location(w, x, y, z);
 							s.sendMessage(String.format("aTeleporting to: X:e %1$s aY:e %2$s aZ:e %3$s$a ine $4$s", x, y, z, w.getName()));
 						} catch (NumberFormatException e) {
 							s.sendMessage("Invalid coordinates");
 							return true;
 						}
 					} else { // Teleporting to a player
 						Player p = Bukkit.getPlayer(args[0]);
 						if (p == null) {
 							s.sendMessage("cThat player is not online.");
 							return true;
 						}
 						loc = p.getLocation();
 						s.sendMessage("aTeleport toe " + p.getName());
 					}
 
 					PlayerUtils.teleport((Player) s, loc);
 					return true;
 				}
 				return true;
 			}
 		}; tp.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.tphere")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					Player p = Bukkit.getPlayer(args[0]);
 					if (p == null) s.sendMessage("cThat player is not online.");
 					else {
 						PlayerUtils.teleport(p, ((Player) s).getLocation());
 						p.sendMessage("aSummoned bye " + s.getName());
 						s.sendMessage("aYou broughte " + p.getName());
 					}
 					return true;
 				} return true;
 			}
 		}; tphere.setExecutor(exe);
 	}
 
 
 
 }
