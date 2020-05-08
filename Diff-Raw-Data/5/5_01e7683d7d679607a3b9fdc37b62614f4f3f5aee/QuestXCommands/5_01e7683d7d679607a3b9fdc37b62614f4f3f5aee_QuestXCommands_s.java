 package com.adamki11s.commands;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.adamki11s.ai.RandomMovement;
 import com.adamki11s.display.FixedSpawnsDisplay;
 import com.adamki11s.display.Pages;
 import com.adamki11s.display.QuestDisplay;
 import com.adamki11s.display.StaticStrings;
 import com.adamki11s.display.TaskDisplay;
 import com.adamki11s.guidance.LocationGuider;
 import com.adamki11s.io.FileLocator;
 import com.adamki11s.npcs.NPCHandler;
 import com.adamki11s.npcs.SimpleNPC;
 import com.adamki11s.npcs.loading.FixedLoadingTable;
 import com.adamki11s.npcs.population.Hotspot;
 import com.adamki11s.npcs.population.HotspotManager;
 import com.adamki11s.npcs.tasks.TaskRegister;
 import com.adamki11s.quests.QuestManager;
 import com.adamki11s.quests.setup.QuestSetup;
 import com.adamki11s.quests.setup.QuestUnpacker;
 import com.adamki11s.questx.QuestX;
 import com.adamki11s.updates.Updater;
 import com.adamki11s.updates.Updater.UpdateResult;
 import com.adamki11s.updates.Updater.UpdateType;
 import com.adamki11s.utils.FileUtils;
 import com.topcat.npclib.entity.HumanNPC;
 
 public class QuestXCommands implements CommandExecutor {
 
 	QuestX plugin;
 	NPCHandler handle;
 
 	public QuestXCommands(QuestX main) {
 		this.plugin = main;
 		this.handle = main.getNPCHandler();
 	}
 
 	HumanNPC test;
 	RandomMovement rm;
 
 	HashMap<String, QuestSetup> setups = new HashMap<String, QuestSetup>();
 
 	Pages npcList;
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("questx") || label.equalsIgnoreCase("q")) {
 			if (!(sender instanceof Player)) {
 				QuestX.logError("QuestX Commands must be issued in-game.");
 				return true;
 			} else {
 				Player p = (Player) sender;
 
 				if (args.length >= 1 && (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?"))) {
 					HelpDispatcher.helpDispatcher(p, args);
 					return true;
 				}
 
 				if (args.length == 1 && args[0].equalsIgnoreCase("force-update") && QPerms.hasPermission(p, "questx.update.update")) {
 					Updater u = new Updater(QuestX.p, "questx", QuestX.f, UpdateType.DEFAULT, true);
 					if (u.getResult() == UpdateResult.SUCCESS) {
 						QuestX.logChat(p, "QuestX version " + u.getLatestVersionString() + " was updated successfully!");
 					} else {
 						QuestX.logChat(p, ChatColor.RED + "Something went wrong downloading the update! Result = " + u.getResult().toString());
 					}
 					return true;
 				}
 
 				/*
 				 * Quest Commands (START)
 				 */
 
				if (args.length == 2 && args[0].equalsIgnoreCase("quest")) {
					if (args[1].equalsIgnoreCase("info")) {
						QuestDisplay.displayCurrentQuestInfo(p);
					}
 					return true;
 				}
 				
 				
 				if (args.length == 2 && args[0].equalsIgnoreCase("quest") && args[1].equalsIgnoreCase("next") && QPerms.hasPermission(p, "questx.quests.setup")) {
 					QuestX.logChat(p, "registered next command");
 					if (setups.containsKey(p.getName())) {
 						QuestX.logChat(p, "Setting up quest");
 						QuestSetup qs = this.setups.get(p.getName());
 						if (!qs.isSetupComplete()) {
 							qs.setupSpawn(p);
 							if (qs.isSetupComplete()) {
 								qs.removeFromList();
 								this.setups.remove(p.getName());
 								QuestX.logChat(p, "Quest setup successfully!");
 							}
 						} else {
 							QuestX.logChat(p, "Setup is completed already.");
 						}
 					} else {
 						QuestX.logChat(p, "You aren't setting up a quest!");
 					}
 					return true;
 				}
 
 				if (args.length >= 3 && args[0].equalsIgnoreCase("quest") && args[1].equalsIgnoreCase("unpack") && QPerms.hasPermission(p, "questx.quests.setup")) {
 					StringBuilder build = new StringBuilder();
 					for(int i = 2; i < args.length; i++){
 						//if final arg
 						if((i + 1) == args.length){
 							build.append(args[i]);
 						} else {
 							build.append(args[i]).append(" ");
 						}
 					}
 					String qName = build.toString();
 					
 					QuestUnpacker upack = new QuestUnpacker(qName);
 					boolean suc = upack.unpackQuest();
 					if (suc) {
 						QuestX.logChat(p, "Unpack successfull");
 						QuestX.logChat(p, "/QuestX quest setup <questname> " + ChatColor.GREEN + " to setup this quest");
 					} else {
 						QuestX.logChat(p, "Error while unpacking");
 					}
 					return true;
 				}
 
 				if (args.length >= 3 && args[0].equalsIgnoreCase("quest") && args[1].equalsIgnoreCase("setup") && QPerms.hasPermission(p, "questx.quests.setup")) {
 					StringBuilder build = new StringBuilder();
 					for(int i = 2; i < args.length; i++){
 						//if final arg
 						if((i + 1) == args.length){
 							build.append(args[i]);
 						} else {
 							build.append(args[i]).append(" ");
 						}
 					}
 					String qName = build.toString();
 					
 					if (setups.containsKey(p.getName())) {
 						QuestX.logChat(p, "You are already setting this quest up!");
 						return true;
 					}
 					if (!setups.containsKey(p.getName())) {
 						if (QuestManager.doesQuestExist(qName)) {
 							if (!QuestManager.hasQuestBeenSetup(qName)) {
 								QuestSetup qs = new QuestSetup(qName, handle);
 								if (!qs.canSetup()) {
 									QuestX.logChat(p, "Failed to start setup, reason : " + qs.getFailSetupReason());
 								} else {
 									QuestX.logChat(p, "Setup successful! /questx quest next " + ChatColor.GREEN + "To select the next spawn location");
 									qs.sendInitialMessage(p);
 									this.setups.put(p.getName(), qs);
 								}
 							} else {
 								QuestX.logChat(p, "This quest has already been setup");
 							}
 						} else {
 							QuestX.logChat(p, "A quest by that name does not exist");
 						}
 					}
 					return true;
 				}
 
 				
 
 				/*
 				 * Quest Commands (END)
 				 */
 
 				/*
 				 * NPC Commands (START)
 				 */
 
 				if (args.length >= 2 && args[0].equalsIgnoreCase("npc") && args[1].equalsIgnoreCase("list") && QPerms.hasPermission(p, "questx.npcs.list")) {
 					String[] list = new String[handle.getNPCs().size()];
 					int count = 0;
 					for (SimpleNPC npc : handle.getNPCs()) {
 						list[count] = npc.getName();
 						count++;
 					}
 					Arrays.sort(list, String.CASE_INSENSITIVE_ORDER);
 					this.npcList = new Pages(list, 10);
 					if (args.length == 2) {
 						// page 1
 						String[] send = this.npcList.getStringsToSend(1);
 						QuestX.logChat(p, ChatColor.GREEN +  "Displaying (" + send.length + "/" + this.npcList.getRawArrayLength() + ") Spawned NPC's, Page (1/" + this.npcList.getPages() + ")");
 						int c = 0;
 						for (String s : send) {
 							c++;
 							QuestX.logChat(p, "#" + (c) + " - " + s);
 						}
 						QuestX.logChat(p, StaticStrings.separator);
 						return true;
 					} else if (args.length == 3) {
 						int pg;
 						try {
 							pg = Integer.parseInt(args[2]);
 							if (pg > this.npcList.getPages()) {
 								QuestX.logChatError(p, ChatColor.RED + "There are not that many pages!");
 								return true;
 							}
 							String[] send = this.npcList.getStringsToSend(pg);
 							QuestX.logChat(p, "Displaying (" + send.length + "/" + this.npcList.getRawArrayLength() + ") Spawned NPC's, Page (" + pg + "/" + this.npcList.getPages() + ")");
 							int c = 0;
 							for (String s : send) {
 								c++;
 								QuestX.logChat(p, "#" + (((pg - 1) * 10) + c) + " - " + s);
 							}
 							QuestX.logChat(p, StaticStrings.separator);
 						} catch (NumberFormatException nfe) {
 							QuestX.logChatError(p, ChatColor.RED + "Page number must be an integer!");
 						}
 						return true;
 					}
 
 					return true;
 				}
 
 				if (args.length == 3 && args[0].equalsIgnoreCase("npc") && args[1].equalsIgnoreCase("delete") && QPerms.hasPermission(p, "questx.npcs.delete")) {
 					String toDel = args[2];
 					try {
 						FileUtils.deleteDirectory(FileLocator.getNPCRootDir(toDel));
 						handle.getSimpleNPCByName(toDel).destroyNPCObject();
 						if (FixedLoadingTable.doesNPCHaveFixedSpawn(toDel)) {
 							FixedLoadingTable.removeFixedNPCSpawn(p, toDel, handle);
 						}
 						QuestX.logChat(p, ChatColor.GREEN + "NPC deleted successfully.");
 
 					} catch (IOException e) {
 						QuestX.logChatError(p, ChatColor.RED + "Error while deleting NPC '" + toDel + "'");
 						e.printStackTrace();
 					}
 				}
 
 				if (args.length == 3 && args[0].equalsIgnoreCase("npc") && args[1].equalsIgnoreCase("find") && QPerms.hasPermission(p, "questx.npcs.find")) {
 					String npcName = args[2];
 					SimpleNPC npc = this.handle.getSimpleNPCByName(npcName);
 					if (npc == null) {
 						QuestX.logChat(p, "NPC with this name is not spawned");
 						return true;
 					} else {
 						if (npc.isNPCSpawned()) {
 							Location npcLoc = npc.getHumanNPC().getBukkitEntity().getLocation();
 							LocationGuider guide = new LocationGuider(p.getName(), npcLoc.getWorld().getName(), npcLoc.getBlockX(), npcLoc.getBlockY(), npcLoc.getBlockZ());
 							guide.drawPath();
 						} else {
 							QuestX.logChatError(p, "This NPC has not spawned.");
 						}
 						return true;
 					}
 				}
 
 				if (args.length == 3 && args[0].equalsIgnoreCase("npc") && args[1].equalsIgnoreCase("tele") && QPerms.hasPermission(p, "questx.npcs.tele")) {
 					String npcName = args[2];
 					SimpleNPC npc = this.handle.getSimpleNPCByName(npcName);
 					if (npc == null) {
 						QuestX.logChat(p, "An NPC with this name has not spawned");
 						return true;
 					} else {
 						p.teleport(npc.getHumanNPC().getBukkitEntity().getLocation());
 						QuestX.logChat(p, "Teleported to NPC '" + npc + "'.");
 						return true;
 					}
 				}
 
 				/*
 				 * NPC Commands (END)
 				 */
 
 				/*
 				 * Task Commands (START)
 				 */
 
 				if (args.length == 2 && args[0].equalsIgnoreCase("task")) {
 					if (args[1].equalsIgnoreCase("info")) {
 						TaskDisplay.displayTaskInfo(p);
 					} else if (args[1].equalsIgnoreCase("cancel")) {
 						TaskRegister.cancelPlayerTask(p);
 					}
 					return true;
 				}
 
 				/*
 				 * Task Commands (END)
 				 */
 
 				/*
 				 * Fixed Spawn Commands (START)
 				 */
 
 				if (args.length == 3 && args[0].equalsIgnoreCase("fixedspawns")) {
 					String npcName = args[2];
 					if (args[1].equalsIgnoreCase("delete") && QPerms.hasPermission(p, "questx.fixedspawns.delete")) {
 						FixedLoadingTable.removeFixedNPCSpawn(p, npcName, handle);
 						return true;
 					} else if (args[1].equalsIgnoreCase("edit") && QPerms.hasPermission(p, "questx.fixedspawns.edit")) {
 						FixedLoadingTable.editFixedNPCSpawn(p, npcName, handle);
 						return true;
 					} else if (args[1].equalsIgnoreCase("deleteall")) {
 						if (QPerms.hasPermission(p, "questx.fixedspawns.deleteall")) {
 							FixedLoadingTable.deleteAllFixedSpawns(p, handle);
 						} else {
 							QuestX.logChatError(p, "You must be an Operator to perform this command");
 						}
 						return true;
 					}
 				}
 
 				if (args.length >= 2 && args[0].equalsIgnoreCase("fixedspawns") && QPerms.hasPermission(p, "questx.fixedspawns.list")) {
 					if (args[1].equalsIgnoreCase("list")) {
 						if (args.length == 2) {
 							FixedSpawnsDisplay.display(p, 1);
 						} else if (args.length == 3) {
 							int pg = 1;
 							try {
 								pg = Integer.parseInt(args[2]);
 							} catch (NumberFormatException nfe) {
 								QuestX.logChat(p, ChatColor.RED + "Page number must be an integer! /q display fixedspawns <page>");
 							}
 							FixedSpawnsDisplay.display(p, pg);
 						}
 						return true;
 					}
 				}
 
 				if (args.length == 3 && args[0].equalsIgnoreCase("fixedspawns") && args[1].equalsIgnoreCase("add") && QPerms.hasPermission(p, "questx.fixedspawns.add")) {
 					String npcName = args[2];
 					boolean suc = FixedLoadingTable.addFixedNPCSpawn(p, npcName, p.getLocation(), handle);
 					if (suc) {
 						SimpleNPC snpc = this.handle.getSimpleNPCByName(npcName);
 						if (snpc != null) {
 							snpc.spawnNPC();
 						}
 					}
 					return true;
 				}
 
 				/*
 				 * Fixed Spawn Commands (END)
 				 */
 
 				/*
 				 * Hotspot Commands (START)
 				 */
 
 				if (args.length >= 2 && args[0].equalsIgnoreCase("hotspots") && args[1].equalsIgnoreCase("list") && QPerms.hasPermission(p, "questx.hotspots.list")) {
 					if (args.length == 2) {
 						Pages pages = new Pages(HotspotManager.getAlphabeticalHotspots(), 10);
 						String[] send = pages.getStringsToSend(1);
 						QuestX.logChat(p, "Displaying (" + send.length + "/" + this.npcList.getRawArrayLength() + ") Hotspots, Page (1/" + pages.getPages() + ")");
 						int c = 0;
 						for (String s : send) {
 							c++;
 							QuestX.logChat(p, "#" + (c) + " - " + s);
 						}
 						QuestX.logChat(p, StaticStrings.separator);
 					} else if (args.length == 3) {
 						int pg;
 						try {
 							pg = Integer.parseInt(args[2]);
 							Pages pages = new Pages(HotspotManager.getAlphabeticalHotspots(), 10);
 							String[] send = pages.getStringsToSend(pg);
 							QuestX.logChat(p, "Displaying (" + send.length + "/" + this.npcList.getRawArrayLength() + ") Hotspots, Page (1/" + pages.getPages() + ")");
 							int c = 0;
 							for (String s : send) {
 								c++;
 								QuestX.logChat(p, "#" + ((pg * 10) + c) + " - " + s);
 							}
 							QuestX.logChat(p, StaticStrings.separator);
 						} catch (NumberFormatException nfe) {
 							QuestX.logChatError(p, ChatColor.RED + "Page number must be an integer value!");
 							QuestX.logChatError(p, ChatColor.RED + "Format : /questx hotspots list <page-number>");
 							return true;
 						}
 					}
 					return true;
 				}
 
 				if (args.length == 5 && args[0].equalsIgnoreCase("hotspots") && args[1].equalsIgnoreCase("add") && QPerms.hasPermission(p, "questx.hotspots.add")) {
 					String name = args[2];
 
 					if (HotspotManager.doesHotspotExist(name)) {
 						QuestX.logChatError(p, ChatColor.RED + "A hotspot with this name already exists");
 						return true;
 					} else {
 						int range, maxSpawns;
 						try {
 							range = Integer.parseInt(args[3]);
 							maxSpawns = Integer.parseInt(args[4]);
 							Location l = p.getLocation();
 							Hotspot h = new Hotspot(l.getBlockX(), l.getBlockY(), l.getBlockZ(), range, maxSpawns, name, l.getWorld().getName());
 							HotspotManager.createHotspot(h);
 							QuestX.logChat(p, ChatColor.GREEN + "Hotspot '" + name + "' was created successfully.");
 						} catch (NumberFormatException nfe) {
 							QuestX.logChatError(p, ChatColor.RED + "Range and Maximum spawns must be integer values!");
 							QuestX.logChatError(p, ChatColor.RED + "Format : /questx hotspots add <range> <maxspawns>");
 						}
 					}
 
 					return true;
 				}
 
 				if (args.length == 3 && args[0].equalsIgnoreCase("hotspots") && args[1].equalsIgnoreCase("delete") && QPerms.hasPermission(p, "questx.hotspots.delete")) {
 					String name = args[2];
 					if (HotspotManager.doesHotspotExist(name)) {
 						HotspotManager.deleteHotspot(name);
 						QuestX.logChat(p, ChatColor.GREEN + "Hotspot deleted successfully.");
 					} else {
 						QuestX.logChatError(p, ChatColor.RED + "A hotspot with this name does not exist");
 					}
 					return true;
 				}
 
 				if (args.length == 5 && args[0].equalsIgnoreCase("hotspots") && args[1].equalsIgnoreCase("edit") && QPerms.hasPermission(p, "questx.hotspots.edit")) {
 					String name = args[2];
 
 					if (!HotspotManager.doesHotspotExist(name)) {
 						QuestX.logChatError(p, ChatColor.RED + "A hotspot with this name does not exist");
 						return true;
 					} else {
 						int range, maxSpawns;
 						try {
 							range = Integer.parseInt(args[3]);
 							maxSpawns = Integer.parseInt(args[4]);
 							HotspotManager.editHotspot(name, range, maxSpawns);
 							QuestX.logChat(p, ChatColor.GREEN + "Hotspot '" + name + "' was editied successfully.");
 						} catch (NumberFormatException nfe) {
 							QuestX.logChatError(p, ChatColor.RED + "Range and Maximum spawns must be integer values!");
 							QuestX.logChatError(p, ChatColor.RED + "Format : /questx hotspots add <range> <maxspawns>");
 						}
 					}
 
 					return true;
 				}
 
 				/*
 				 * Hotspot Commands (END)
 				 */
 			}
 		}
 		return true;
 	}
 }
