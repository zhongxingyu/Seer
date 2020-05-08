 package net.mysticrealms.fireworks.scavengerhunt;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 import net.mysticrealms.fireworks.scavengerhunt.cron.InvalidPatternException;
 import net.mysticrealms.fireworks.scavengerhunt.cron.Scheduler;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Dye;
 import org.bukkit.material.Wool;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.Potion;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class ScavengerHunt extends JavaPlugin {
 
 	public Scheduler scheduler = new Scheduler();
 	public WorldGuardPlugin wg;
 
 	public static Economy economy = null;
 	public static Permission permission = null;
 	public Configuration config;
 	public List<ItemStack> currentItems = new ArrayList<ItemStack>();
 	public Map<EntityType, Integer> currentMobs = new HashMap<EntityType, Integer>();
 	public int duration = 0;
 	public long end = 0;
 	public boolean isRunning, usingScheduler, shortMessages, enableRegions, riddleMode;
 	public List<ItemStack> items = new ArrayList<ItemStack>();
 	public Map<EntityType, Integer> mobs = new HashMap<EntityType, Integer>();
 	public double money = 0;
 	public int numOfItems = 0;
 	public Map<String, Map<EntityType, Integer>> playerMobs = new ConcurrentHashMap<String, Map<EntityType, Integer>>();
 	public List<ItemStack> rewards = new ArrayList<ItemStack>();
 	public int numOfMobs;
 	public String schedule = "";
 	public String task;
 	public List<ScavengerRegion> activeRegions = new ArrayList<ScavengerRegion>();
 	public List<String> riddles = new ArrayList<String>();
 
 	public String configToString(ItemStack item, int current) {
 		return " * " + ((current != -1) ? current + "/" : "") + item.getAmount() + " " + itemFormatter(item).toLowerCase().replace("_", " ");
 	}
 
 	public String configToString(ItemStack item) {
 		return configToString(item, -1);
 	}
 
 	public synchronized Map<EntityType, Integer> getMap(String s) {
 		Map<EntityType, Integer> map = playerMobs.get(s);
 		if (map == null) {
 			map = new ConcurrentHashMap<EntityType, Integer>();
 			for (EntityType e : EntityType.values()) {
 				map.put(e, 0);
 			}
 			playerMobs.put(s, map);
 		}
 		return map;
 	}
 
 	public boolean isUsingMoney() {
 		if (money > 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public String itemFormatter(ItemStack item) {
 		if (item.getType() == Material.WOOL) {
 			return ((Wool) item.getData()).getColor().toString() + " wool";
 		} else if (item.getType() == Material.INK_SACK) {
 			return ((Dye) item.getData()).getColor().toString() + " dye";
 		} else if (item.getType() == Material.POTION){
 			return Potion.fromItemStack(item).getType().toString() + " potion";
 		} else {
 			return item.getType().toString();
 		}
 	}
 
 	public void listHelp(CommandSender sender) {
 		sender.sendMessage(ChatColor.DARK_RED + "== Scavenger Help Guide ==");
 		sender.sendMessage(ChatColor.GOLD + " * /scavengerItems - List items/objectives for current scavenger event.");
 		sender.sendMessage(ChatColor.GOLD + " * /scavengerRewards - List rewards for the winner.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerStart - Start a scavenger event.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerStop - End current scavenger vent.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerReload - Reload the config.");
 	}
 
 	public void listScavengerEventItems(CommandSender sender) {
 		if (!(sender instanceof Player)) {
 			return;
 		}
 		Player p = (Player) sender;
 		if (isRunning) {
 			if (riddleMode){
 				sender.sendMessage(ChatColor.DARK_RED + "Current scavenger clues: ");
 				for (String i : riddles){
 					sender.sendMessage(ChatColor.GOLD + i);
 				}
 			}
 			if (!currentItems.isEmpty() && !riddleMode) {
 				sender.sendMessage(ChatColor.DARK_RED + "Current scavenger items: ");
 				for (ItemStack i : currentItems) {
 					sender.sendMessage(ChatColor.GOLD + configToString(i, count(p.getInventory(), i)));
 				}
 			}
 			if (!currentMobs.isEmpty() && !riddleMode) {
 				sender.sendMessage(ChatColor.DARK_RED + "You need to kill: ");
 				Map<EntityType, Integer> status = getMap(sender.getName());
 				
 				for (Map.Entry<EntityType, Integer> entry : currentMobs.entrySet()) {
 	
					sender.sendMessage(ChatColor.GOLD + " * " + status.get(entry.getKey()) + "/" + entry.getValue() + " " + entry.getKey().getName().toLowerCase().replace("_", " "));
 				}
 			}
 		} else {
 			sender.sendMessage(ChatColor.GOLD + "No scavenger event is currently running.");
 		}
 	}
 
 	public void listScavengerEventRewards(CommandSender sender) {
 		sender.sendMessage(ChatColor.DARK_RED + "Current scavenger rewards: ");
 		for (ItemStack i : rewards) {
 			sender.sendMessage(ChatColor.GOLD + configToString(i));
 		}
 		if (isUsingMoney()) {
 			sender.sendMessage(ChatColor.GOLD + " * " + economy.format(money));
 		}
 	}
 
 	public boolean loadConfig() {
 
 		items.clear();
 		rewards.clear();
 		mobs.clear();
 		activeRegions.clear();
 		riddles.clear();
 		
 		if (task != null) {
 			scheduler.deschedule(task);
 			task = null;
 		}
 
 		if (!new File(getDataFolder(), "config.yml").exists()) {
 			saveDefaultConfig();
 		}
 		
 		reloadConfig();
 		config = getConfig();
 		
 		if(config.isBoolean("riddleMode")){
 			riddleMode = config.getBoolean("riddleMode");
 		}
 		
 		if(config.isBoolean("shortMessageMode")){
 			shortMessages = config.getBoolean("shortMessageMode");			
 		}
 
 		if (config.isBoolean("enableScheduler")) {
 			usingScheduler = config.getBoolean("enableScheduler");
 		}
 
 		if (config.isString("schedule") && usingScheduler) {
 			schedule = config.getString("schedule");
 			try {
 				task = scheduler.schedule(schedule, new Runnable() {
 
 					@Override
 					public void run() {
 						runScavengerEvent();
 
 					}
 				});
 			} catch (InvalidPatternException e) {
 			}
 		}
 		
 		if(config.isBoolean("enableRegions")){
 			enableRegions = config.getBoolean("enableRegions");
 		}
 
 		if(config.isList("regions")){
 			for (String i : config.getStringList("regions")){
 				String[] regions2 = i.split(":");
 				if(regions2.length == 2){
 					World w = this.getServer().getWorld(regions2[0]);
 					if(w != null){
 						activeRegions.add(new ScavengerRegion(w, regions2[1]));
 					}
 				}
 			}
 			
 		}
 		
 		if(config.isList("riddles")){
 			for (String i : config.getStringList("riddles")){
 				riddles.add(i.toString());
 			}
 		}
 		
 		if (config.isList("mobs")) {
 			for (Object i : config.getList("mobs", new ArrayList<String>())) {
 				try {
 					final String[] parts = i.toString().split(" ");
 					final int mobQuantity = Integer.parseInt(parts[1]);
 					final EntityType mobName = EntityType.valueOf(parts[0].toUpperCase());
 					mobs.put(mobName, mobQuantity);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}
 		if (config.isDouble("money")) {
 			money = config.getDouble("money");
 		} else if (config.isInt("money")) {
 			money = config.getInt("money");
 		} else {
 			return false;
 		}
 		if (config.isInt("duration")) {
 			duration = config.getInt("duration");
 		} else {
 			return false;
 		}
 		if (config.isInt("numOfItems")) {
 			numOfItems = config.getInt("numOfItems");
 		} else {
 			return false;
 		}
 		if (config.isInt("numOfMobs")) {
 			numOfMobs = config.getInt("numOfMobs");
 		} else {
 			return false;
 		}
 		if (config.isList("items")) {
 			for (Object i : config.getList("items", new ArrayList<String>())) {
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
 						items.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						items.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						items.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
 					}
 				} else {
 					return false;
 				}
 			}
 		} else {
 			return false;
 		}
 		if (config.isList("rewards")) {
 			for (Object i : config.getList("rewards", new ArrayList<String>())) {
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
 						rewards.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						rewards.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						rewards.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
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
 
 	public boolean checkLocation(Location l){
 		if(enableRegions && wg != null && !activeRegions.isEmpty()){
 			RegionManager rm = wg.getRegionManager(l.getWorld());
 			for(ProtectedRegion pr : rm.getApplicableRegions(l)){
 				for(ScavengerRegion sr : activeRegions){
 					if(sr.getWorld().equals(l.getWorld())){
 						if(sr.getRegion().equalsIgnoreCase(pr.getId())){
 							return true;
 						}
 					}
 				}
 			}
 			return false;
 		}else{
 			return true;
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("scavengerstart")) {
 			runScavengerEvent();
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerstop")) {
 			stopScavengerEvent();
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengeritems")) {
 			listScavengerEventItems(sender);
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerrewards")) {
 			listScavengerEventRewards(sender);
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerhelp")) {
 			listHelp(sender);
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerreload")) {
 			if (loadConfig()) {
 				sender.sendMessage(ChatColor.GOLD + "Config reloaded!");
 			} else {
 				sender.sendMessage(ChatColor.GOLD + "Config failed to reload!");
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void onEnable() {
 		Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");
 		
 		if(plugin != null){
 			wg = (WorldGuardPlugin)plugin;
 		}else{
 			getLogger().info("WorldGuard not found - not using regions.");
 		}
 		
 		scheduler.start();
 		startMetrics();
 		setupEconomy();
 		if (!loadConfig()) {
 			getLogger().severe("Something is wrong with the config! Disabling!");
 			setEnabled(false);
 			return;
 		}
 
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScavengerInventory(this), 0, 40);
 		getServer().getPluginManager().registerEvents(new ScavengerListener(this), this);
 	}
 
 	public void onDisable() {
 		scheduler.stop();
 	}
 
 	public int count(Inventory inv, ItemStack item) {
 		int count = 0;
 		for (ItemStack check : inv.getContents()) {
 			if (check != null && check.getType() == item.getType() && check.getData().equals(item.getData())) {
 				count += check.getAmount();
 			}
 		}
 		return count;
 	}
 
 	public void runScavengerEvent() {
 		String mobString = mobs.toString();
 		System.out.println(mobString);
 		currentItems.clear();
 		playerMobs.clear();
 		currentMobs.clear();
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
 		List<Map.Entry<EntityType, Integer>> mobClone = new ArrayList<Map.Entry<EntityType, Integer>>(mobs.entrySet());
 		for (int i = 0; (numOfMobs <= 0 || i < numOfMobs) && !mobClone.isEmpty(); i++) {
 			Map.Entry<EntityType, Integer> entry = mobClone.remove(r.nextInt(mobClone.size()));
 			currentMobs.put(entry.getKey(), entry.getValue());
 		}
 		getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt is starting! Good luck!");
 		if (duration > 0) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "You have: " + ChatColor.GOLD + duration + " seconds!");
 		}
 		
 		if(!currentItems.isEmpty() && shortMessages){
 			getServer().broadcastMessage(ChatColor.GOLD + "/scavengerItems" + ChatColor.DARK_RED + " to view objectives.");
 		}
 		
 		if (!currentItems.isEmpty() && !shortMessages && !riddleMode) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "You need to collect: ");
 			for (ItemStack i : currentItems) {
 				getServer().broadcastMessage(ChatColor.GOLD + configToString(i));
 			}
 		}
 		
 		if(!currentItems.isEmpty() && !shortMessages && riddleMode){
 			getServer().broadcastMessage(ChatColor.DARK_RED + "Here are the clues: ");
 			for (String i : riddles){
 				getServer().broadcastMessage(ChatColor.GOLD + i);
 			}
 		}
 		
 		if (!currentMobs.isEmpty() && !shortMessages && !riddleMode) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "You need to kill: ");
 			for (Map.Entry<EntityType, Integer> entry : currentMobs.entrySet()) {
				getServer().broadcastMessage(ChatColor.GOLD + " * " + entry.getValue() + " " + entry.getKey().getName().toLowerCase().replace("_", " "));
 			}
 		}
 		isRunning = true;
 		if (duration == 0) {
 			end = 0;
 		} else {
 			end = duration * 1000 + System.currentTimeMillis();
 		}
 	}
 
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			if (economyProvider != null) {
 				economy = economyProvider.getProvider();
 			}
 			getLogger().info("Vault found and loaded.");
 			return economy != null;
 		}
 		economy = null;
 		getLogger().info("Vault not found - money reward will not be used.");
 		return false;
 	}
 
 	public void startMetrics() {
 		try {
 			new MetricsLite(this).start();
 		} catch (IOException e) {
 			getLogger().warning("MetricsLite did not enable! Statistic usage disabled.");
 			e.printStackTrace();
 			return;
 		}
 	}
 
 	public void stopScavengerEvent() {
 		getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt has ended with no winner.");
 		isRunning = false;
 	}
 }
