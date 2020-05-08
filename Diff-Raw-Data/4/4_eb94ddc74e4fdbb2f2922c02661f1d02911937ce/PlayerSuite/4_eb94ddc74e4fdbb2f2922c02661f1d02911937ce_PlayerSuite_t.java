 package com.etriacraft.etriabending.suites;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.PluginCommand;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.etriacraft.etriabending.EtriaBending;
 import com.etriacraft.etriabending.Strings;
 import com.etriacraft.etriabending.util.Utils;
 
 public class PlayerSuite {
 
 	// Methods
 	public static void Helps() {
 		File helps = new File(EtriaBending.getInstance().getDataFolder(), "helps.yml");
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(helps);
 
 		if (!helps.exists()) {
 			config.set("default", new String[] {"&eYou can change this and add your own content to the default page"});
 			try { config.save(helps); } catch (IOException e) { e.printStackTrace(); }
 		}
 
 		for (String key : config.getKeys(true)) {
 			helpPagesDb.put(key.toLowerCase(), config.getStringList(key));
 		}
 		EtriaBending.log.info("Successfully loaded " + helpPagesDb.size() + " help pages");
 	}
 
 	public static void setVanished(Player p, boolean state) {
 		for (Player o : Bukkit.getOnlinePlayers()) {
 			if (state) {
 				if (o.hasPermission("eb.vanish.seehidden")) continue;
 				o.hidePlayer(p);
 			} else o.showPlayer(p);
 		}
 		if (state) vanishDb.add(p.getName());
 		else vanishDb.remove(p.getName());
 	}
 	public static boolean isVanished(Player p) {
 		return vanishDb.contains(p.getName());
 	}
 
 	public static void silentChestOpen(Player p) {
 		chestUserDb.add(p.getName());
 	}
 
 	public static void silentChestClose(Player p) {
 		chestUserDb.remove(p.getName());
 	}
 
 	public static boolean silentChestInUse(Player p) {
 		return chestUserDb.contains(p.getName());
 	}
 
 	public static Set<String> godDb = new HashSet();
 	public static HashMap<String, List<String>> helpPagesDb = new HashMap();
 	public static Set<String> vanishDb = new HashSet<String>();
 	public static Set<String> chestUserDb = new HashSet<String>();
 	public static Set<String> noexpdropDB = new HashSet<String>();
 	
 	EtriaBending plugin;
 
 	public PlayerSuite(EtriaBending instance) {
 		this.plugin = instance;
 		init();
 	}
 
 	private void init() {
 		PluginCommand gamemode = plugin.getCommand("gamemode");
 		PluginCommand getpos = plugin.getCommand("getpos");
 		PluginCommand god = plugin.getCommand("god");
 		PluginCommand hat = plugin.getCommand("hat");
 		PluginCommand help = plugin.getCommand("help");
 		PluginCommand vanish = plugin.getCommand("vanish");
 		PluginCommand workbench = plugin.getCommand("workbench");
 		PluginCommand enchantingtable = plugin.getCommand("enchantingtable");
 		PluginCommand savexp = plugin.getCommand("savexp");
 		CommandExecutor exe;
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.gamemode")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					final Player p;
 					if (args.length >= 1) p = Bukkit.getPlayer(args[0]);
 					else {
 						if (!(s instanceof Player)) return false;
 						p = (Player) s;
 					}
 					if (p == null) {
 						s.sendMessage("cThat player is not online.");
 						return true;
 					}
 
 					p.setGameMode((p.getGameMode().equals(GameMode.CREATIVE))? GameMode.SURVIVAL : GameMode.CREATIVE);
 					if (s != p) s.sendMessage("aSete " + p.getName() + "'s agamemode toe" + Strings.toTitle(p.getGameMode().name()));
 					return true;
 				} return true;
 			}
 		}; gamemode.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.getpos")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					if (!(s instanceof Player)) return false;
 
 					Location loc = ((Player) s).getLocation();
 
 					s.sendMessage("aYour position:");
 					s.sendMessage("aWorld:e " + loc.getWorld().getName());
 					s.sendMessage(String.format("aCoords: X:e %1$s aY:e %2$s aZ:e %3$s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
 					return true;
 				} return true;
 			}
 		}; getpos.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.god")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					final Player p;
 					if (args.length >= 1) {
 						if (!s.hasPermission("ec.god.other")) {
 							s.sendMessage("cYou don't have permission to do that!");
 							return true;
 						}
 						p = Bukkit.getPlayer(args[0]);
 					} else {
 						if (!(s instanceof Player)) return false;
 						p = (Player) s;
 					}
 					if (p == null) {
 						s.sendMessage("cThat player is not online!");
 						return true;
 					}
 
 					if (godDb.contains(p.getName())) godDb.remove(p.getName());
 					else godDb.add(p.getName());
 
 					final String action = (godDb.contains(p.getName()))? " enabled " : " disabled";
 					if (p == s) s.sendMessage("aGod mod" + action);
 					else {
 						s.sendMessage("aGod mode" + action + "one " + p.getName());
 						p.sendMessage("aGod mode" + action);
 					}
 					return true;
 				} return true;
 			}
 		}; god.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.hat")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					if (!(s instanceof Player)) return false;
 
 					final Player p = (Player) s;
 					final ItemStack oldhat = p.getInventory().getHelmet();
 					if (oldhat == null) {
 						final PlayerInventory inv = p.getInventory();
 						final ItemStack hat = new ItemStack(p.getItemInHand().getType(), 1, p.getItemInHand().getDurability());
 
 						if (hat.getTypeId() >= 256 || hat.getTypeId() == 0) {
 							s.sendMessage("cYou can't wear that! Please use a block.");
 							return true;
 						}
 
 						inv.removeItem(hat);
 						inv.setHelmet(hat);
 						s.sendMessage("aNice hat bro :O");
 						return true;
 					} else {
 						s.sendMessage("cTake off your old hat first.");
 						return true;
 					}
 				} return true;
 			}
 		}; hat.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.help")) {
 					s.sendMessage("cYou don't have permission to do that!");
 					return true;
 				}
 
 					String page = "default";
 					if (args.length >= 1)
 						page = args[0].toLowerCase();
 
 					if (helpPagesDb.containsKey(page)) {
 						for (String sent : helpPagesDb.get(page)) {
 							s.sendMessage(sent.replaceAll("(?i)&([a-fk-or0-9])", "\u00A7$1"));
 						}
 					} else s.sendMessage("cThat page does not exist.");
 					return true;
 			}
 		}; help.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.vanish")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 
 					final Player p;
 					if (args.length >= 1) p = Bukkit.getPlayer(args[0]);
 					else {
 						if (!(s instanceof Player)) return false;
 						p = (Player) s;
 					}
 					if (p == null) {
 						s.sendMessage("cThat player is not online.");
 						return true;
 					}
 
 					if (!isVanished(p)) {
 						godDb.add(p.getName());
 						p.sendMessage("aPoof!");
 					} else {
 						if (p.getAllowFlight()) {
 							s.sendMessage("cYou can't unvanish right now, perhaps you're in creative mode?");
 							return true;
 						}
 						godDb.remove(p.getName());
 						p.sendMessage("aYou are now visible.");
 					}
 					setVanished(p, !isVanished(p));
 					Utils.serverBroadcast("e" + p.getName() + " ahas " + (isVanished(p)? "vanished" : "reappeared"), "eb.vanish.alert");
 					return true;
 				} return true;
 			}
 		}; vanish.setExecutor(exe);
 
 		exe = new CommandExecutor() {
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				if (!s.hasPermission("eb.workbench")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					if (!(s instanceof Player)) return false;
 					Player p = (Player) s;
 					p.openWorkbench(null, true);
 					p.sendMessage("aOpened workbench.");
 					return true;
 				} return true;
 			}
 		}; workbench.setExecutor(exe);
 		
 		exe = new CommandExecutor() {
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
				if (!s.hasPermission("eb.enchantingtable")) {
 					s.sendMessage("cYou don't have permission to do that!");
 				} else {
 					if (!(s instanceof Player)) return false;
 					Player p = (Player) s;
 					p.openEnchanting(null, true);
 					p.sendMessage("aHave an enchanting table.");
 					return true;
 				} return true;
 			}
 		}; enchantingtable.setExecutor(exe);
 		
 		exe = new CommandExecutor() {
 			@Override
 			public boolean onCommand(CommandSender s, Command c, String label, String[] args) {
 				final Player p = (Player) s;
 				if (!(s instanceof Player)) {
 					s.sendMessage("cThis command is only available to players.");
 				}
 				if (!s.hasPermission("eb.savexp")) {
 					s.sendMessage("cYou don't have permission to do that!");
					return true;
 				} if (!(noexpdropDB.contains(s.getName()))) {
 					noexpdropDB.add(s.getName());
 					s.sendMessage("aYour exp will now be saved when you die.");
 				} else if (noexpdropDB.contains(s.getName())) {
 					noexpdropDB.remove(s.getName());
 					s.sendMessage("aYour exp will now drop on death.");
 				}
 				return true;
 			}
 		}; savexp.setExecutor(exe);
 	}
 
 }
