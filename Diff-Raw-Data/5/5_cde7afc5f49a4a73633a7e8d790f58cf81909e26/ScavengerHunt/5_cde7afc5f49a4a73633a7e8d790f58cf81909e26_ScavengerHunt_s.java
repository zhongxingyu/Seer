 package net.mysticrealms.fireworks.scavengerhunt;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Dye;
 import org.bukkit.material.Wool;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ScavengerHunt extends JavaPlugin {
 
 	public boolean isRunning;
 	public Configuration config;
 	public List<ItemStack> currentItems = new ArrayList<ItemStack>();
 	public List<ItemStack> items = new ArrayList<ItemStack>();
 	public List<ItemStack> rewards = new ArrayList<ItemStack>();
 
 	public Map<String, Map<EntityType, Integer>> ofMaps = new ConcurrentHashMap<String, Map<EntityType, Integer>>();
 
 	public Map<EntityType, Integer> mobs = new HashMap<EntityType, Integer>();
 
 	public int numOfItems = 0;
 	public int duration = 0;
 	public double money = 0;
 	public long end = 0;
 
 	public static Permission permission = null;
 	public static Economy economy = null;
 
 	@Override
 	public void onEnable() {
 
 		setupEconomy();
 
 		if (!loadConfig()) {
 			this.getLogger().severe("Something is wrong with the config! Disabling!");
 			this.setEnabled(false);
 			return;
 		}
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScavengerInventory(this), 0, 40);
 
 		this.getServer().getPluginManager().registerEvents(new ScavengerListener(this), this);
 	}
 
 	private boolean setupEconomy() {
 		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
 			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			if (economyProvider != null) {
 				economy = economyProvider.getProvider();
 			}
 
 			this.getLogger().info("Vault found and loaded.");
 			return (economy != null);
 		}
 		economy = null;
 		this.getLogger().info("Vault not found - money reward will not be used.");
 		return false;
 	}
 
 	public boolean loadConfig() {
 
 		this.reloadConfig();
 		items.clear();
 		rewards.clear();
 		mobs.clear();
 
 		if (!new File(this.getDataFolder(), "config.yml").exists()) {
 			this.saveDefaultConfig();
 		}
 		config = this.getConfig();
 
 		if (config.isList("mobs")) {
 			for (String i : config.getStringList("mobs")) {
 				try {
 					final String[] parts = i.split(" ");
 					final int mobQuantity = Integer.parseInt(parts[1]);
 					final EntityType mobName = EntityType.fromName(parts[0]);
 					mobs.put(mobName, mobQuantity);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}
 
 		if (config.isDouble("money"))
 			money = config.getDouble("money");
 		else if (config.isInt("money"))
 			money = config.getInt("money");
 		else
 			return false;
 
 		if (config.isInt("duration"))
 			duration = config.getInt("duration");
 		else
 			return false;
 
 		if (config.isInt("numOfItems"))
 			numOfItems = config.getInt("numOfItems");
 		else
 			return false;
 
 		if (config.isList("items")) {
 			for (Object i : config.getStringList("items")) {
 				if (i instanceof String) {
 					final String[] parts = ((String) i).split(" ");
 					final int[] intParts = new int[parts.length];
 					for (int e = 0; e < parts.length; e++) {
 						try {
 							intParts[e] = Integer.parseInt(parts[e]);
 						} catch (final NumberFormatException exception) {
 							return false;
 						}
 					}
 					if (parts.length == 1) {
 						this.items.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						this.items.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						this.items.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
 					}
 				} else {
 					return false;
 				}
 			}
 		} else {
 			return false;
 		}
 
 		if (config.isList("rewards")) {
 			for (Object i : config.getStringList("rewards")) {
 				if (i instanceof String) {
 					final String[] parts = ((String) i).split(" ");
 					final int[] intParts = new int[parts.length];
 					for (int e = 0; e < parts.length; e++) {
 						try {
 							intParts[e] = Integer.parseInt(parts[e]);
 						} catch (final NumberFormatException exception) {
 							return false;
 						}
 					}
 					if (parts.length == 1) {
 						this.rewards.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						this.rewards.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						this.rewards.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
 					}
 				} else {
 					return false;
 				}
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean isUsingMoney() {
 		if (this.money > 0)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		if (cmd.getName().equalsIgnoreCase("scavengerstart"))
 			runScavengerEvent();
 		if (cmd.getName().equalsIgnoreCase("scavengerstop"))
 			stopScavengerEvent();
 		if (cmd.getName().equalsIgnoreCase("scavengeritems"))
 			listScavengerEventItems(sender);
 		if (cmd.getName().equalsIgnoreCase("scavengerrewards"))
 			listScavengerEventRewards(sender);
 		if (cmd.getName().equalsIgnoreCase("scavengerhelp"))
 			listHelp(sender);
 		if (cmd.getName().equalsIgnoreCase("scavengerreload")) {
 			if (this.loadConfig())
 				sender.sendMessage(ChatColor.GOLD + "Config reloaded!");
 			else
 				sender.sendMessage(ChatColor.GOLD + "Config failed to reload!");
 		}
 
 		return true;
 	}
 	
 	public void listHelp(CommandSender sender){
 		sender.sendMessage(ChatColor.DARK_RED + "== Scavenger Help Guide ==");
 		sender.sendMessage(ChatColor.GOLD + " * /scavengerItems - List items/objectives for current scavenger event.");
 		sender.sendMessage(ChatColor.GOLD + " * /scavengerRewards - List rewards for the winner.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerStart - Start a scavenger event.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerStop - End current scavenger vent.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerReload - Reload the config.");
 	}
 
 	public void listScavengerEventItems(CommandSender sender) {
 		if (isRunning) {
 			if (!currentItems.isEmpty()) {
				this.getServer().broadcastMessage(ChatColor.DARK_RED + "Current scavenger items: ");
 				for (ItemStack i : currentItems) {
 					sender.sendMessage(ChatColor.GOLD + configToString(i));
 				}
 			}
 
 			if (!mobs.isEmpty()) {
				this.getServer().broadcastMessage(ChatColor.DARK_RED + "You need to kill: ");
 				for (Map.Entry<EntityType, Integer> entry : mobs.entrySet()) {
 					sender.sendMessage(ChatColor.GOLD + " * " + entry.getValue() + " " + entry.getKey().getName().toLowerCase().replace("_", " "));
 				}
 			}
 		} else {
 			sender.sendMessage(ChatColor.GOLD + "No scavenger event is currently running.");
 		}
 	}
 
 	public void listScavengerEventRewards(CommandSender sender) {
 		sender.sendMessage(ChatColor.GOLD + "Current scavenger rewards: ");
 		for (ItemStack i : rewards) {
 			sender.sendMessage(ChatColor.GOLD + configToString(i));
 		}
 		if (this.isUsingMoney())
 			sender.sendMessage(ChatColor.GOLD + " * " + economy.format(money));
 
 	}
 
 	public void stopScavengerEvent() {
 		this.getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt has ended with no winner.");
 		isRunning = false;
 	}
 
 	public void runScavengerEvent() {
 
 		this.currentItems.clear();
 		this.ofMaps.clear();
 
 		List<ItemStack> clone = new ArrayList<ItemStack>();
 
 		for (ItemStack i : items) {
 			clone.add(i);
 		}
 
 		Random r = new Random();
 
 		if (numOfItems <= 0) {
 			currentItems = clone;
 		} else {
 			for (int i = 0; i < numOfItems && !clone.isEmpty(); i++) {
 				currentItems.add(clone.remove(r.nextInt(clone.size())));
 			}
 		}
 
 		this.getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt is starting! Good luck!");
 
 		if (duration != 0) {
 			this.getServer().broadcastMessage(ChatColor.DARK_RED + "You have: " + ChatColor.GOLD + duration + " seconds!");
 		}
 		if (!currentItems.isEmpty()) {
 			this.getServer().broadcastMessage(ChatColor.DARK_RED + "You need to collect: ");
 			for (ItemStack i : currentItems) {
 				this.getServer().broadcastMessage(ChatColor.GOLD + configToString(i));
 			}
 		}
 
 		if (!mobs.isEmpty()) {
 			this.getServer().broadcastMessage(ChatColor.DARK_RED + "You need to kill: ");
 			for (Map.Entry<EntityType, Integer> entry : mobs.entrySet()) {
 				this.getServer().broadcastMessage(ChatColor.GOLD + " * " + entry.getValue() + " " + entry.getKey().getName().toLowerCase().replace("_", " "));
 			}
 		}
 
 		isRunning = true;
 
 		if (duration == 0) {
 			end = 0;
 		} else {
 			end = duration * 1000 + System.currentTimeMillis();
 		}
 	}
 
 	public synchronized Map<EntityType, Integer> getMap(String s) {
 		Map<EntityType, Integer> map = ofMaps.get(s);
 
 		if (map == null) {
 			map = new ConcurrentHashMap<EntityType, Integer>();
 			for (EntityType e : EntityType.values()) {
 				map.put(e, 0);
 			}
 			ofMaps.put(s, map);
 		}
 
 		return map;
 
 	}
 
 	public String configToString(ItemStack item) {
 
 		return " * " + item.getAmount() + " " + itemFormatter(item).toLowerCase().replace("_", " ");
 	}
 	
 	public String itemFormatter(ItemStack item){
 		if (item.getType() == Material.WOOL){
 			return ((Wool)item.getData()).getColor().toString() + " wool";
 		}else if(item.getType() == Material.INK_SACK){
 			return ((Dye)item.getData()).getColor().toString() + " dye";		
 		}else{
 			return item.getType().toString();
 		}
 	}
 }
