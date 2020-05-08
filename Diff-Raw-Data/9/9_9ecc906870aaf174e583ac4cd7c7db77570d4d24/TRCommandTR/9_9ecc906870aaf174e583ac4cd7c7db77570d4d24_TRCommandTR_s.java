 package com.github.dreadslicer.tekkitrestrict.commands;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.github.dreadslicer.tekkitrestrict.TRCacheItem;
 import com.github.dreadslicer.tekkitrestrict.TRLimitBlock;
 import com.github.dreadslicer.tekkitrestrict.TRLogger;
 import com.github.dreadslicer.tekkitrestrict.TRNoItem;
 import com.github.dreadslicer.tekkitrestrict.TRPerformance;
 import com.github.dreadslicer.tekkitrestrict.TRPermHandler;
 import com.github.dreadslicer.tekkitrestrict.TRSafeZone;
 import com.github.dreadslicer.tekkitrestrict.tekkitrestrict;
 import com.github.dreadslicer.tekkitrestrict.lib.TRLimit;
 
 public class TRCommandTR implements CommandExecutor {
 
 	private tekkitrestrict plugin; // pointer to your main class, unrequired if
 									// you don't need methods from the main
 									// class
 
 	public TRCommandTR(tekkitrestrict plugin) {
 		this.plugin = plugin;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 		Player player = null;
 		boolean admin = false;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 			// TRNoItem.isItemBanned(player, 100);
 			try {
 
 				// ru.tehkode.permissions.PermissionUser cc =
 				// perm.getUser(player.getName());
 
 				if (TRPermHandler.hasPermission(player, "admin", "", "")) {
 					admin = true;
 				}
 			} catch (Exception e) {
 				if (player.isOp()) {
 					admin = true;
 				}
 			}
 		} else {
 			admin = true;
 		}
 		LinkedList<String> message = new LinkedList<String>();
 		boolean usemsg = true;
 		if (cmd.getName().equalsIgnoreCase("tekkitrestrict")
 				|| cmd.getName().equalsIgnoreCase("tr")) { // If the player
 															// typed /basic then
 															// do the
 
 			try {
 				// following...
 				if (args.length == 0) {
 					message.add("[TekkitRestrict "
 							+ tekkitrestrict.getInstance().getDescription()
 									.getVersion() + " Commands]");
 					message.add("Aliases: /tr, /tekkitrestrict");
 
 					if (admin) {
 						message.add("/tr admin - list admin commands");
 					}
 				} else {
 					if (args[0].equalsIgnoreCase("reload") && admin) {
 						plugin.reload();
 						if (player != null) {
 							message.add("Tekkit Restrict Reloaded!");
 						}
 					} else if (args[0].equalsIgnoreCase("threadlag") && admin) {
 						TRPerformance.getThreadLag(player);
 					} else if (args[0].equalsIgnoreCase("reinit") && admin) {
 						tekkitrestrict.getInstance().getServer().reload();
 					} else if (args[0].equalsIgnoreCase("admin") && admin) {
 						if (args.length == 1) {
 							message.addAll(help(1));
 						} else if (args[1].equalsIgnoreCase("help")) {
 							try {
 								message.addAll(help(Integer.parseInt(args[2])));
 							} catch (Exception e) {
 								message.addAll(help(2));
 							}
 						} else if (args[1].equalsIgnoreCase("safezone")) {
 							if (args.length == 2) {
 								// list the safezones...
 								int pg = TRSafeZone.zones.size() / 5;
 								pg = pg > 0 ? pg : 1;
 								message.add("SafeZones - Page 1 of " + pg);
 								for (int i = 0; i < 5; i++) {
 									if (TRSafeZone.zones.size() > i) {
 										TRSafeZone z = TRSafeZone.zones.get(i);
 										if (z != null) {
 											message.add("[" + z.world + "] "
 													+ z.name);
 										}
 									}
 								}
 							} else {
 								if (args[2].equalsIgnoreCase("list")) {
 									// list the safezones...
 									int sp = 1;
 									try {
 										sp = Integer.parseInt(args[3]);
 									} catch (Exception e) {
 									}
 									int pg = TRSafeZone.zones.size() / 5;
 
 									pg = pg > 0 ? pg : 1;
 									if (sp > pg) {
 										sp = pg;
 									}
 									message.add("SafeZones - Page " + sp
 											+ " of " + pg);
 									for (int i = (sp * 5); i < (sp * 5 + 5); i++) {
 										if (TRSafeZone.zones.size() > i) {
 											TRSafeZone z = TRSafeZone.zones
 													.get(i);
 											if (z != null) {
 												message.add("[" + z.world
 														+ "] " + z.name);
 											}
 										}
 									}
 								} else if (args[2].equalsIgnoreCase("addwg")) {
 									String name = args[3];
 									// loop through the safezones
 									boolean inList = false;
 									for (int i = 0; i < TRSafeZone.zones.size(); i++) {
 										if (TRSafeZone.zones.get(i).world
 												.equals(player.getWorld()
 														.getName())) {
 											if (TRSafeZone.zones.get(i).name
 													.equals(name)) {
 												inList = true;
 											}
 										}
 									}
 
 									if (inList) {
 										message.add("Error! Cannot add duplicate SafeZones for this region.");
 									} else if (tekkitrestrict.pm
 											.isPluginEnabled("WorldGuard")) {
 										try {
 											com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) tekkitrestrict.pm
 													.getPlugin("WorldGuard");
 											Map<String, com.sk89q.worldguard.protection.regions.ProtectedRegion> rm = wg
 													.getRegionManager(
 															player.getWorld())
 													.getRegions();
 											com.sk89q.worldguard.protection.regions.ProtectedRegion pr = rm
 													.get(name);
 											if (pr != null) {
 												TRSafeZone zone = new TRSafeZone();
 												zone.mode = 1;
 												zone.name = name;
 												zone.world = player.getWorld()
 														.getName();
 												TRSafeZone.zones.add(zone);
 												TRSafeZone.save();
 												message.add("Attached to region `"
 														+ name + "`!");
 											} else {
 												message.add("Region does not exist!");
 											}
 										} catch (Exception E) {
 											message.add("Error! (does the region exist?)");
 										}
 
 									} else {
 										message.add("WorldGuard not installed!");
 									}
 								} else if (args[2].equalsIgnoreCase("rem")
 										|| args[2].equalsIgnoreCase("del")
 										|| args[2].equalsIgnoreCase("remove")
 										|| args[2].equalsIgnoreCase("delete")) {
 									// remove a safezone based on the name.
 									// name MUST be exact.
 									for (int i = 0; i < TRSafeZone.zones.size(); i++) {
 										TRSafeZone zone = TRSafeZone.zones
 												.get(i);
 										if (zone.name.equalsIgnoreCase(args[3])) {
 											TRSafeZone.zones.remove(i);
 											i--;
 											message.add(args[3] + " removed.");
 										}
 									}
 									try {
 										tekkitrestrict.db
 												.query("DELETE FROM `tr_saferegion` WHERE `name` = '"
 														+ tekkitrestrict
 																.antisqlinject(args[3])
 														+ "'");
 									} catch (Exception e) {
 									}
 									// delete the in-game safezone...
 									
 								} else if (args[2].equalsIgnoreCase("check")) {
 									message.add(TRSafeZone.inSafeZone(player)
 											+ " in a safe zone!");
 								}
 							}
 						} else if (args[1].equalsIgnoreCase("limit")) {
							if (args[2] == null) {
 								message.add("[TekkitRestrict "
 										+ tekkitrestrict.getInstance()
 												.getDescription().getVersion()
 										+ " Limit Commands]");
 								message.add("/tr admin limit clear [player]");
 								message.add("/tr admin limit clear [player] [INDEX[id:data]]");
 								message.add("/tr admin limit list [player]");
 							} else if (args[2].equalsIgnoreCase("clear")) {
 								// clears a player's limits...
 								try {
 									TRLimitBlock cc = TRLimitBlock
 											.getLimiter(args[3]); //len 4
 									if (args.length == 5) {
 										List<TRCacheItem> iss = TRCacheItem
 												.processItemString("", args[4],
 														-1);
 										for (TRCacheItem isr : iss) {
 											for (TRLimit trl : cc.itemlimits) {
 												//tekkitrestrict.log.info(isr.id+":"+isr.getData()+" ?= "+trl.blockID+":"+trl.blockData);
 												if (TRNoItem.equalSet(isr.id,
 														isr.getData(),
 														trl.blockID,
 														trl.blockData)) {
 													if(trl.placedBlock != null)
 														trl.placedBlock.clear();
 													else trl.placedBlock = new LinkedList<Location>();
 												}
 												int ci = cc.itemlimits.indexOf(trl);
 												if(ci != -1) cc.itemlimits.set(ci, trl);
 											}
 										}
 										message.add("Cleared " + args[3]
 												+ "'s block limits for "+args[4]);
 									} else {
 										cc.clearLimits();
 										message.add("Cleared " + args[3]
 												+ "'s block limits!");
 									}
 								} catch (Exception E) {
 									message.add("[Error!] Please use the formats:");
 									message.add("/tr admin limit clear [player]");
 									message.add("/tr admin limit clear [player] [INDEX[id:data]]");
 									message.add("/tr admin limit list [player]");
 								}
 							} else if (args[2].equalsIgnoreCase("list")) {
 								// clears a player's limits...
 								try {
 									TRLimitBlock cc = TRLimitBlock
 											.getLimiter(args[3]);
 									boolean c = false;
 									for (TRLimit l : cc.itemlimits) {
 										c=true;
 										int cccl = cc.getMax(cc.player, l.blockID, l.blockData);
 										cccl = cccl == -1 ? 0 : cccl;
 										message.add("[" + l.blockID + ":"
 												+ l.blockData + "] - "
 												+ l.placedBlock.size()+"/"+cccl+" blocks");
 									}
 									if(!c) message.add(args[3]+" does not have any limits!");
 								} catch (Exception E) {
 									message.add("[Error...] Please use the format:");
 									message.add("  /tr admin limit list [player]");
 								}
 							}
 						} else if (args[1].equalsIgnoreCase("emc")) {
 							// tempset, lookup
							if (args[2] == null) {
 								message.add("/tr admin emc tempset [id:data] [EMC]");
 								message.add("/tr admin emc lookup [id:data]");
 							} else if (args[2].equalsIgnoreCase("tempset")) {
 								try {
 									if (args[3] != null) {
 										List<TRCacheItem> iss = TRCacheItem
 												.processItemString("", args[3],
 														-1);
 										for (TRCacheItem isr : iss) {
 											int data = isr.getData();
 											HashMap<Integer, Integer> hm = (HashMap<Integer, Integer>)ee.EEMaps.alchemicalValues
 													.get(isr.id);
 											if (hm != null) {
 												hm.put(data, Integer
 														.parseInt(args[4]));
 												message.add("Temporary EMC set successful!");
 												ee.EEMaps.alchemicalValues.put(
 														isr.id, hm);
 											} else {
 												hm = new HashMap<Integer, Integer>();
 												hm.put(data, Integer
 														.parseInt(args[4]));
 												message.add("Temporary EMC set successful!");
 												ee.EEMaps.alchemicalValues.put(
 														isr.id, hm);
 											}
 										}
 									} else {
 										message.add("Use format:");
 										message.add("/tr admin emc tempset [id] [EMC]   (data = 0)");
 										message.add("/tr admin emc tempset [id:data] [EMC]");
 										message.add("/tr admin emc tempset [id-id2] [EMC]");
 									}
 								} catch (Exception e) {
 									TRLogger.Log("debug",
 											"[COM] EMCSet " + e.getMessage());
 									message.add("Sorry, EMC set unsuccessful...");
 									message.add("/tr admin emc tempset [id] [EMC]   (data = 0)");
 									message.add("/tr admin emc tempset [id:data] [EMC]");
 									message.add("/tr admin emc tempset [id-id2] [EMC]");
 								}
 							} else if (args[2].equalsIgnoreCase("lookup")) {
 								try {
 									if (args[3] != null) {
 										List<TRCacheItem> iss = TRCacheItem
 												.processItemString("", args[3],
 														-1);
 										for (TRCacheItem isr : iss) {
 											HashMap<Integer, Integer> hm = (HashMap<Integer, Integer>)ee.EEMaps.alchemicalValues
 													.get(isr.id);
 											int data = isr.getData();
 											// message.add("[Results]");
 											if (hm != null) {
 												if (data == 0) {
 													Iterator<?> ks = hm.keySet()
 															.iterator();
 													while (ks.hasNext()) {
 														try {
 															Integer dat = Integer
 																	.parseInt(ks
 																			.next()
 																			.toString());
 															Object jj = hm
 																	.get(dat);
 															if (jj != null) {
 																int emc = Integer
 																		.parseInt(jj
 																				.toString());
 																message.add("["
 																		+ isr.id
 																		+ ":"
 																		+ dat
 																		+ "] EMC: "
 																		+ emc);
 															}
 														} catch (Exception e) {
 														}
 													}
 												} else {
 													Object adfk = hm
 															.get(isr.data);
 													if (adfk != null) {
 														int emc = Integer
 																.parseInt(adfk
 																		.toString());
 														int datax = isr.data == -10 ? 0
 																: isr.data;
 														message.add("["
 																+ isr.id + ":"
 																+ datax
 																+ "] EMC: "
 																+ emc);
 													}
 												}
 											}
 										}
 									} else {
 										message.add("Use format:");
 										message.add("/tr admin emc lookup [id]   (data = 0)");
 										message.add("/tr admin emc lookup [id:data]");
 									}
 								} catch (Exception e) {
 									//e.printStackTrace();
 									TRLogger.Log("debug", "[COM] EMCLookup "
 											+ e.getMessage());
 									message.add("Sorry, EMC lookup unsuccessful...");
 									message.add("/tr admin emc lookup [id]   (all registered data values... spam?)");
 									message.add("/tr admin emc lookup [id:data]");
 									message.add("/tr admin emc lookup [id-id2] (Spam?)");
 								}
 							}
 						}
 					} else {
 						if (player != null) {
 							usemsg = false;
 							player.sendMessage("You do not have access to that command!");
 						}
 					}
 				}
 			} catch (Exception e) {
 				message.add("An error has occured processing your command.");
 				TRLogger.Log("debug", "TRCommandTR Error: " + e.getMessage());
 				for (StackTraceElement ee : e.getStackTrace()) {
 					TRLogger.Log("debug", "     " + ee.toString());
 				}
 			}
 			if (usemsg) {
 				sendMessage(player, message.toArray(new String[0]));
 				message.clear();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public static void sendMessage(Player player, String[] message) {
 		if (player != null) {
 			for (int k = 0; k < message.length; k++) {
 				player.sendRawMessage(message[k]);
 			}
 		} else {
 			for (int k = 0; k < message.length; k++) {
 				tekkitrestrict.log.log(Level.OFF, message[k]);
 			}
 		}
 	}
 
 	public static List<String> help(int page) {
 		List<String> message = new LinkedList<String>();
 		if (page == 1) {
 			message.add("[TekkitRestrict "
 					+ tekkitrestrict.getInstance().getDescription()
 							.getVersion() + " Admin Commands]");
 			message.add("[Help page 1] - /tr help [page]");
 			message.add("/tr admin safezone list [page] - list safezones");
 			message.add("/tr admin safezone addwg [region] - add safezone using WorldGuard");
 			message.add("/tr admin safezone rem [name] - remove safezone");
 			message.add("/tr admin limit clear [player]");
 			message.add("/tr admin limit clear [player] [INDEX[id:data]]");
 			message.add("/tr admin limit list [player]");
 			message.add("[1/2 pages]");
 		} else if (page == 2) {
 			message.add("[TekkitRestrict "
 					+ tekkitrestrict.getInstance().getDescription()
 							.getVersion() + " Admin Commands]");
 			message.add("[Help page 2]");
 			message.add("/tr admin emc tempset [id:data] [EMC]");
 			message.add("/tr admin emc lookup [id:data]");
 			message.add("[2/2 pages]");
 		}
 		return message;
 	}
}
