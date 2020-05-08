 package com.etriacraft.tamemanagement;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Ocelot;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Tameable;
 import org.bukkit.entity.Wolf;
 
 public class Commands {
 
 	TameManagement plugin;
 
 	public Commands(TameManagement instance) {
 		this.plugin = instance;
 		init();
 	}
 
	public static String Prefix;;
 	public static String noPermission;
 	public static String setVariant;
 	public static String setColor;
 	public static String claim;
 	public static String invoked;
 	public static String configReloaded;
 	public static String releaseNotAllowed;
 	public static String transfersNotAllowed;
 	public static String releaseMessage;
 	public static String playerNotOnline;
 	public static String cantTransferOwnershipToSelf;
 	public static String transferMessage;
 	
 	private void init() {
 		PluginCommand tame = plugin.getCommand("tame");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (args.length < 1) {
 					s.sendMessage("-----§6TameManagement Commands§f-----");
 					s.sendMessage("§3/tame setowner [Player]§f - Sets Owner of tamed animal.");
 					s.sendMessage("§3/tame release§f - Releases a tamed animal.");
 					s.sendMessage("§3/tame invoke [Animal Type]§f - Invoke your tamed animals.");
 					s.sendMessage("§3/tame horse§f - View Horse Specific Commands.");
 					s.sendMessage("§3/tame reload§f - Reload Config File.");
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("horse")) {
 					if (args.length == 1) {
 						s.sendMessage("-----§6TameManagement Horse Commands§f-----");
 						s.sendMessage("§3/tame horse claim§f - Claim a horse.");
 						s.sendMessage("§3/tame horse setcolor [Color]§f - Change Horse Color.");
 						s.sendMessage("§3/tame horse setvariant [Variant]§f - Change horse variant.");
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("claim")) {
 						if (!s.hasPermission("tamemanagement.horse.claim")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (MobListener.horseclaims.contains(s.getName())) {
 							MobListener.horseclaims.remove(s.getName());
 						}
 						MobListener.horseclaims.add(s.getName());
 						s.sendMessage(Prefix + claim);
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("setvariant")) {
 						if (!s.hasPermission("tamemanagement.horse.setvariant")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						// Donkey Horse Mule Skeleton Undead
 						if (args.length != 3) {
 							s.sendMessage("§6Proper Usage: §3/tame horse setvariant [variant]");
 							s.sendMessage("§aProper Variants: Horse, Donkey, Mule, Skeleton, Undead");
 							return true;
 						}
 						if (!args[2].equalsIgnoreCase("donkey") && !args[2].equalsIgnoreCase("horse") && !args[2].equalsIgnoreCase("mule") && !args[2].equalsIgnoreCase("skeleton") && !args[2].equalsIgnoreCase("undead")) {
 							s.sendMessage("§6Proper Usage: §3/tame horse setvariant [variant]");
 							s.sendMessage("§aProper Variants: Horse, Donkey, Mule, Skeleton, Undead");
 							return true;
 						}
 						if (MobListener.horsevariants.containsKey(s.getName())) {
 							MobListener.horsevariants.remove(s.getName());
 						}
 						if (args[2].equalsIgnoreCase("donkey")) {
 							MobListener.horsevariants.put(s.getName(), Horse.Variant.DONKEY);
 							s.sendMessage(Prefix + setVariant.replace("%variant", "donkey"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("horse")) {
 							MobListener.horsevariants.put(s.getName(), Horse.Variant.HORSE);
 							s.sendMessage(Prefix + setVariant.replace("%variant", "normal horse"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("mule")) {
 							MobListener.horsevariants.put(s.getName(), Horse.Variant.MULE);
 							s.sendMessage(Prefix + setVariant.replace("%variant", "mule"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("skeleton")) {
 							MobListener.horsevariants.put(s.getName(), Horse.Variant.SKELETON_HORSE);
 							s.sendMessage(Prefix + setVariant.replace("%variant", "skeleton horse"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("undead")) {
 							MobListener.horsevariants.put(s.getName(), Horse.Variant.UNDEAD_HORSE);
 							s.sendMessage(Prefix + setVariant.replace("%variant", "undead horse"));
 							return true;
 						}
 					}
 					if (args[1].equalsIgnoreCase("setcolor")) {
 						if (!s.hasPermission("tamemanagement.horse.setcolor")) {
 							s.sendMessage(Prefix + noPermission);
 							return true;
 						}
 						if (args.length != 3) {
 							// Black, Brown, Chestnut, Creamy,Darkbrown, Gray, White
 							s.sendMessage("§6Proper Usage: §3/tame horse setcolor [color]");
 							s.sendMessage("§aProper Styles: §3Black, Brown, Chestnut, Creamy, DarkBrown, Gray, White");
 							return true;
 						}
 						if (!args[2].equalsIgnoreCase("black") && !args[2].equalsIgnoreCase("brown") && !args[2].equalsIgnoreCase("chestnut") && !args[2].equalsIgnoreCase("creamy") && !args[2].equalsIgnoreCase("darkbrown") && !args[2].equalsIgnoreCase("gray") && !args[2].equalsIgnoreCase("white")) {
 							s.sendMessage("§6Proper Usage: §3/tame horse setcolor [color]");
 							s.sendMessage("§aProper Styles: §3Black, Brown, Chestnut, Creamy, DarkBrown, Gray, White");
 							return true;
 						}
 						if (MobListener.horsecolors.containsKey(s.getName())) {
 							MobListener.horsecolors.remove(s.getName());
 						}
 						if (args[2].equalsIgnoreCase("Black")) {
 							MobListener.horsecolors.put(s.getName(), Horse.Color.BLACK);
 							s.sendMessage(Prefix + setColor.replace("%color", "black"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("brown")) {
 							MobListener.horsecolors.put(s.getName(), Horse.Color.BROWN);
 							s.sendMessage(Prefix + setColor.replace("%color", "brown"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("chestnut")) {
 							MobListener.horsecolors.put(s.getName(), Horse.Color.CHESTNUT);
 							s.sendMessage(Prefix + setColor.replace("%color", "chestnut"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("creamy")) {
 							MobListener.horsecolors.put(s.getName(), Horse.Color.CREAMY);
 							s.sendMessage(Prefix + setColor.replace("%color", "creamy"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("darkbrown")) {
 							MobListener.horsecolors.put(s.getName(), Horse.Color.DARK_BROWN);
 							s.sendMessage(Prefix + setColor.replace("%color", "dark brown"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("gray")) {
 							MobListener.horsecolors.put(s.getName(), Horse.Color.GRAY);
 							s.sendMessage(Prefix + setColor.replace("%color", "gray"));
 							return true;
 						}
 						if (args[2].equalsIgnoreCase("white")) {
 							MobListener.horsecolors.put(s.getName(), Horse.Color.WHITE);
 							s.sendMessage(Prefix + setColor.replace("%color", "white"));
 							return true;
 						}
 					}
 				}
 				if (args[0].equalsIgnoreCase("invoke")) {
 					if (!s.hasPermission("tamemanagement.invoke")) {
 						s.sendMessage("§cYou don't have permission to do that.");
 						return true;
 					}
 					if (args.length != 2) {
 						s.sendMessage("§6Proper Usage: §3/tame invoke [Horse|Wolf|Ocelot]");
 						return true;
 					}
 					if (!args[1].equalsIgnoreCase("wolf") && !args[1].equalsIgnoreCase("horse") && !args[1].equalsIgnoreCase("ocelot")) {
 						s.sendMessage("§6Proper Usage: §3/tame invoke [Horse|Wolf|Ocelot]");
 						return true;
 					}
 					Set<Entity> calledEntities = new HashSet<Entity>();
 					Player player = (Player) s;
 					Location loc = player.getLocation();
 					World world = player.getWorld();
 					List<Entity> entities = world.getEntities();
 					for (Entity en: entities) {
 						if (en instanceof Tameable) {
 							if (args[1].equalsIgnoreCase("Horse")) {
 								if (en instanceof Horse) {
 									Horse horse = (Horse) en;
 									if (horse.isTamed()) {
 										if (horse.getOwner() == null) {
 											continue;
 										}
 										if (horse.getOwner().getName().equals(s.getName())) {
 											horse.teleport(loc);
 											calledEntities.add(en);
 										}
 									}
 								}
 							}
 							if (args[1].equalsIgnoreCase("Ocelot")) {
 								if (en instanceof Ocelot) {
 									Ocelot ocelot = (Ocelot) en;
 									if (ocelot.isTamed()) {
 										if (ocelot.getOwner().getName().equals(s.getName())) {
 											ocelot.teleport(loc);
 											calledEntities.add(en);
 										}
 									}
 								}
 							}
 							if (args[1].equalsIgnoreCase("Wolf")) {
 								if (en instanceof Wolf) {
 									Wolf wolf = (Wolf) en;
 									if (wolf.isTamed()) {
 										if (wolf.getOwner().getName().equals(s.getName())) {
 											wolf.teleport(loc);
 											calledEntities.add(en);
 										}
 									}
 								}
 							}
 						}
 					}
 					int size = calledEntities.size();
 					String size2 = String.valueOf(size);
 					if (args[1].equalsIgnoreCase("wolf")) {
 						s.sendMessage(Prefix + invoked.replace("%amount", size2).replace("%mob", "wolves"));
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("ocelot")) {
 						s.sendMessage(Prefix + invoked.replace("%amount", size2).replace("%mob", "ocelots"));
 						return true;
 					}
 					if (args[1].equalsIgnoreCase("horse")) {
 						s.sendMessage(Prefix + invoked.replace("%amount", size2).replace("%mob", "horses"));
 						return true;
 					}
 				}
 				if (args[0].equalsIgnoreCase("reload")) {
 					if (!s.hasPermission("tamemanagement.reload")) {
 						s.sendMessage(Prefix + noPermission);
 						return true;
 					}
 					plugin.reloadConfig();
 					s.sendMessage(Prefix + configReloaded);
 				}
 				if (args[0].equalsIgnoreCase("release")) {
 					if (!plugin.getConfig().getBoolean("AllowReleases")) {
 						s.sendMessage(Prefix + releaseNotAllowed);
 						return true;
 					}
 					if (!s.hasPermission("tamemanagement.release")) {
 						s.sendMessage(Prefix + noPermission);
 						return true;
 					}
 					if (args.length != 1) {
 						s.sendMessage("§3Proper Usage: §6/tame release");
 						return true;
 					}
 					if (MobListener.releases.containsKey(s.getName())) {
 						MobListener.releases.remove(s.getName());
 					}
 					MobListener.releases.put(s.getName(), "Release");
 					s.sendMessage(Prefix + releaseMessage);
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("setowner")) {
 					if (!plugin.getConfig().getBoolean("AllowTransfers")) {
 						s.sendMessage(Prefix + transfersNotAllowed);
 						return true;
 					}
 					if (!s.hasPermission("tamemanagement.setowner")) {
 						s.sendMessage(Prefix + noPermission);
 						return true;
 					}
 					if (args.length != 2) {
 						s.sendMessage("§3Proper Usage: §6/tame setowner [Player]");
 						return true;
 					}
 					Player p = Bukkit.getPlayer(args[1]);
 					if (p == null) {
 						s.sendMessage(Prefix + playerNotOnline);
 						return true;
 					}
 					if (p == s) {
 						s.sendMessage(Prefix + cantTransferOwnershipToSelf);
 						return true;
 					}
 					if (MobListener.transfers.containsKey(s.getName())) {
 						MobListener.transfers.remove(s.getName());
 					}
 					MobListener.transfers.put(s.getName(), p.getName());
 					s.sendMessage(Prefix + transferMessage.replace("%newowner", p.getName()));
 					return true;
 				}
 				return true;
 			}
 
 		}; tame.setExecutor(exe);
 	}
 
 }
