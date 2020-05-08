 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.compatibility.other.PEX;
 import com.turt2live.antishare.config.ASConfig;
 import com.turt2live.antishare.config.ConfigConvert;
 import com.turt2live.antishare.manager.BlockManager;
 import com.turt2live.antishare.manager.CuboidManager;
 import com.turt2live.antishare.manager.HookManager;
 import com.turt2live.antishare.manager.InventoryManager;
 import com.turt2live.antishare.manager.MoneyManager;
 import com.turt2live.antishare.manager.RegionManager;
 import com.turt2live.antishare.manager.WorldConfigManager;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.util.Action;
 import com.turt2live.antishare.util.GamemodeAbstraction;
 import com.turt2live.antishare.util.Messages;
 import com.turt2live.antishare.util.PermissionNodes;
 import com.turt2live.antishare.util.SelfCompatibility;
 import com.turt2live.antishare.util.UpdateChecker;
 import com.turt2live.metrics.EMetrics;
 import com.turt2live.metrics.graph.PieGraph;
 import com.turt2live.metrics.tracker.FixedTracker;
 
 /**
  * AntiShare plugin class
  * 
  * @author turt2live
  */
 public class AntiShare extends PluginWrapper{
 
 	/**
 	 * AntiShare instance
 	 */
 	public static AntiShare p;
 	/**
 	 * AntiShare tool material
 	 */
 	public static final Material ANTISHARE_TOOL = Material.BLAZE_ROD;
 	/**
 	 * AntiShare tool for creating cuboids
 	 */
 	public static final Material ANTISHARE_CUBOID_TOOL = Material.SLIME_BALL;
 	/**
 	 * Used to force-set a block
 	 */
 	public static final Material ANTISHARE_SET_TOOL = Material.BLAZE_POWDER;
 	/**
 	 * AntiShare tool data value. This is to avoid competition with general actions
 	 */
 	public static final short ANTISHARE_TOOL_DATA = 1;
 
 	// Trackers
 	public static final PieGraph<Action> LEGAL_ACTIONS = new PieGraph<Action>("5.4.0 Legal Actions");
 	public static final PieGraph<Action> ILLEGAL_ACTIONS = new PieGraph<Action>("5.4.0 Illegal Actions");
 
 	// Folder locations
 	public File generalDataDirectory;
 	public File inventoriesDirectory;
 	public File blocksDirectory;
 	public File entitiesDirectory;
 	public File worldConfigurations;
 	public File regionConfigurations;
 
 	// Files
 	private File simpleNoticeFile;
 
 	// Private stuff
 	private EMetrics metrics;
 	private String build;
 	private Messages messages;
 	private ASConfig config;
 	private HookManager hooks;
 	private WorldConfigManager worldConfigs;
 	private MoneyManager money;
 	private CuboidManager cuboids;
 	private BlockManager blocks;
 	private InventoryManager inventories;
 	private RegionManager regions;
 	private final List<String> disabledSNPlayers = new ArrayList<String>();
 	private static PEX pex;
 
 	@Override
 	public void onEnable(){
 		p = this;
 
 		// Start PEX
 		pex = new PEX();
 
 		// Setup graphs
 		for(Action action : Action.values()){
 			ILLEGAL_ACTIONS.addSlice(action, action.name());
 			LEGAL_ACTIONS.addSlice(action, action.name());
 		}
 
 		// Create folder structure
 		generalDataDirectory = new File(getDataFolder(), "data");
 		inventoriesDirectory = new File(generalDataDirectory, "inventories");
 		blocksDirectory = new File(generalDataDirectory, "blocks");
 		entitiesDirectory = new File(generalDataDirectory, "entities");
 		worldConfigurations = new File(getDataFolder(), "world_configurations");
 		regionConfigurations = new File(getDataFolder(), "region_configurations");
 		if(!getDataFolder().exists()){
 			getDataFolder().mkdirs();
 		}
 		if(!generalDataDirectory.exists()){
 			generalDataDirectory.mkdirs();
 		}
 		if(!inventoriesDirectory.exists()){
 			inventoriesDirectory.mkdirs();
 		}
 		if(!blocksDirectory.exists()){
 			blocksDirectory.mkdirs();
 		}
 		if(!entitiesDirectory.exists()){
 			entitiesDirectory.mkdirs();
 		}
 		if(!worldConfigurations.exists()){
 			worldConfigurations.mkdirs();
 		}
 		if(!regionConfigurations.exists()){
 			regionConfigurations.mkdirs();
 		}
 
 		// Setup files
 		simpleNoticeFile = new File(generalDataDirectory, "disabled-simplenotice-users.txt");
 
 		// Load disable SimpleNotice users
 		try{
 			if(simpleNoticeFile.exists()){
 				BufferedReader in = new BufferedReader(new FileReader(simpleNoticeFile));
 				String line;
 				while((line = in.readLine()) != null){
 					disabledSNPlayers.add(line);
 				}
 				in.close();
 			}else{
 				simpleNoticeFile.createNewFile();
 			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
 		// Start messages
 		messages = new Messages();
 
 		// Do configuration conversion before cleaning it up
 		ConfigConvert.doConvert();
 
 		// Check configuration
 		getConfig().loadDefaults(getResource("config.yml"));
 		if(getConfig().needsUpdate()){
 			getConfig().saveDefaults();
 		}
 		getConfig().load();
 
 		// Get build number
 		try{
 			BufferedReader in = new BufferedReader(new InputStreamReader(getResource("plugin.yml")));
 			String line;
 			while((line = in.readLine()) != null){
 				if(line.startsWith("build: ")){
 					line = line.replace("build: ", "");
 					build = line;
 					break;
 				}
 			}
 		}catch(IOException e){}
 
 		// Set value in configuration
 		String val = getDescription().getVersion() + "|" + getServer().getVersion() + "|" + getServer().getOnlineMode() + "|" + build;
 		if(!getConfig().getString("other.version_string", "").equalsIgnoreCase(val)){
 			getConfig().set("other.version_string", val);
 			saveConfig();
 		}
 
 		// Start configuration
 		config = new ASConfig(getConfig());
 
 		// Start update checker - internal enabled state
 		UpdateChecker.start();
 
 		// Pre-load
 		blocks = new BlockManager();
 		blocks.load();
 
 		// Cleanup / Compatibility
 		SelfCompatibility.migrateWorldConfigurations();
 		SelfCompatibility.migratePlayerData();
 		SelfCompatibility.cleanup520Inventories();
 		SelfCompatibility.cleanup530Inventories();
 		SelfCompatibility.cleanupOldInventories();
 		SelfCompatibility.cleanup520blocks();
 		SelfCompatibility.cleanup530FileStructure();
 		SelfCompatibility.cleanupYAML();
 		SelfCompatibility.convertTo540Inventories();
 
 		// Start managers
 		hooks = new HookManager();
 		worldConfigs = new WorldConfigManager();
 		money = new MoneyManager();
 		cuboids = new CuboidManager();
 		inventories = new InventoryManager();
 		regions = new RegionManager();
 
 		// Load managers
 		hooks.load();
 		worldConfigs.load();
 		money.load();
 		cuboids.load();
 		inventories.load();
 		regions.load();
 
 		// Start listeners
 		getServer().getPluginManager().registerEvents(worldConfigs, this);
 		getServer().getPluginManager().registerEvents(new ASListener(), this);
 
 		// Start command stuff
 		getCommand("antishare").setExecutor(new CommandHandler());
 
 		// Offline Mode Warning
 		if(!getServer().getOnlineMode()){
 			getLogger().severe("**********************");
 			getLogger().severe(messages.getMessage("offlinemode"));
 			getLogger().severe("**********************");
 		}
 
 		// Spam console
 		getLogger().info(messages.getMessage("enabled"));
 
 		// Load player information
 		loadPlayerInformation();
 
 		// Start metrics
 		try{
 			metrics = new EMetrics(this);
 			metrics.addGraph(LEGAL_ACTIONS);
 			metrics.addGraph(ILLEGAL_ACTIONS);
 			Plugin mcmmo = getServer().getPluginManager().getPlugin("mcMMO");
 			metrics.addTracker(new FixedTracker("mcMMO Servers", mcmmo != null ? "Found" : "Not Found"));
 			metrics.startMetrics();
 		}catch(IOException e){ // Metrics error
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onDisable(){
 		// Save managers
 		blocks.save();
 		cuboids.save();
 		regions.save();
 		inventories.save();
 		money.save();
 
 		// Save disabled SimpleNotice users
 		try{
 			BufferedWriter out = new BufferedWriter(new FileWriter(simpleNoticeFile, false));
 			for(String user : disabledSNPlayers){
 				out.write(user + "\r\n");
 			}
 			out.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
 		// Shut down tasks
 		getServer().getScheduler().cancelTasks(this);
 
 		// Spam console
 		getLogger().info(messages.getMessage("disabled"));
 	}
 
 	/**
 	 * Reload AntiShare
 	 */
 	public void reload(){
 		reloadConfig();
 		config = new ASConfig(getConfig());
 		messages.reload();
 		hooks.reload();
 		worldConfigs.reload();
 		cuboids.reload();
 		regions.reload();
 		inventories.reload();
 		blocks.reload();
 		loadPlayerInformation();
 	}
 
 	private void loadPlayerInformation(){
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			@Override
 			public void run(){
 				for(Player player : getServer().getOnlinePlayers()){
 					inventories.loadPlayer(player.getName());
 					Region playerRegion = regions.getRegion(player.getLocation());
 					if(playerRegion != null){
 						playerRegion.alertSilentEntry(player);
 					}
 				}
 				if(inventories != null){
 					int loaded = inventories.getLoaded();
 					if(loaded > 0){
 						getLogger().info(messages.getMessage("inventories-loaded", String.valueOf(loaded)));
 					}
 				}
 			}
 		});
 	}
 
 	/**
 	 * Gets the active block manager
 	 * 
 	 * @return the block manager
 	 */
 	public BlockManager getBlockManager(){
 		return blocks;
 	}
 
 	/**
 	 * Gets the active inventory manager
 	 * 
 	 * @return the inventory manager
 	 */
 	public InventoryManager getInventoryManager(){
 		return inventories;
 	}
 
 	/**
 	 * Gets the active region manager
 	 * 
 	 * @return the region manager
 	 */
 	public RegionManager getRegionManager(){
 		return regions;
 	}
 
 	/**
 	 * Gets the active money manager
 	 * 
 	 * @return the money manager
 	 */
 	public MoneyManager getMoneyManager(){
 		return money;
 	}
 
 	/**
 	 * Gets the active cuboid manager
 	 * 
 	 * @return the cuboid manager
 	 */
 	public CuboidManager getCuboidManager(){
 		return cuboids;
 	}
 
 	/**
 	 * Gets the active world configuration manager
 	 * 
 	 * @return the world configuration manager
 	 */
 	public WorldConfigManager getWorldConfigs(){
 		return worldConfigs;
 	}
 
 	/**
 	 * Gets the active hook manager
 	 * 
 	 * @return the hook manager
 	 */
 	public HookManager getHookManager(){
 		return hooks;
 	}
 
 	/**
 	 * Gets the AntiShare configuration settings
 	 * 
 	 * @return the AntiShare configuration settings
 	 */
 	public ASConfig settings(){
 		return config;
 	}
 
 	/**
 	 * Gets the message handler
 	 * 
 	 * @return the message handler
 	 */
 	public Messages getMessages(){
 		return messages;
 	}
 
 	/**
 	 * Determines if a player decided to turn off SimpleNotice support
 	 * 
 	 * @param name the player name
 	 * @return true if enabled (gets messages through SimpleNotice)
 	 */
 	public boolean isSimpleNoticeEnabled(String name){
 		return !disabledSNPlayers.contains(name);
 	}
 
 	/**
 	 * Enables SimpleNotice support for a user
 	 * 
 	 * @param name the user
 	 */
 	public void enableSimpleNotice(String name){
 		disabledSNPlayers.remove(name);
 	}
 
 	/**
 	 * Disables SimpleNotice support for a user
 	 * 
 	 * @param name the user
 	 */
 	public void disableSimpleNotice(String name){
 		disabledSNPlayers.add(name);
 	}
 
 	/**
 	 * Gets the AntiShare build number
 	 * 
 	 * @return the build number
 	 */
 	public String getBuild(){
 		return build;
 	}
 
 	/**
 	 * Gets the message prefix for AntiShare
 	 * 
 	 * @return the message prefix
 	 */
 	public String getPrefix(){
 		return messages.getMessage("prefix");
 	}
 
 	/**
 	 * Gets the active PEX compatibility instance, this will never be null
 	 * 
 	 * @return the active PEX compatibility instance
 	 */
 	public PEX getPEX(){
 		return pex;
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param material the material applied to the permissions (or null for none)
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, Material material){
 		return isBlocked(player, allowPermission, denyPermission, material, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param material the material applied to the permissions (or null for none)
 	 * @param specialOnly true to only check permission.[item] permissions
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, Material material, boolean specialOnly){
		return !(!isBlocked(player, allowPermission, denyPermission, material == null ? null : material.name(), specialOnly)
		|| !isBlocked(player, allowPermission, denyPermission, String.valueOf(material == null ? null : material.getId()), specialOnly));
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param target the target to apply to this permission, spaces will removed
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, String target){
 		return isBlocked(player, allowPermission, denyPermission, target, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param target the target to apply to this permission, spaces will removed
 	 * @param specialOnly true to only check permission.[item] permissions
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, String target, boolean specialOnly){
 		if(target != null){
 			if(denyPermission != null && hasPermission(player, denyPermission + "." + target)){
 				System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f2");
 				return true;
 			}
 			if(hasPermission(player, allowPermission + "." + target)){
 				System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f1");
 				return false;
 			}
 		}
 		if(specialOnly){
 			System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f3");
 			return false;
 		}
 		if(denyPermission != null && hasPermission(player, denyPermission)){
 			System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f5");
 			return true;
 		}
 		if(hasPermission(player, allowPermission)){
 			System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f4");
 			return false;
 		}
 		if(GamemodeAbstraction.isCreative(player.getGameMode()) && GamemodeAbstraction.isAdventureCreative()){
 			if(hasPermission(player, PermissionNodes.AFFECT_CREATIVE) || hasPermission(player, PermissionNodes.AFFECT_ADVENTURE)){
 				System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f6");
 				return true;
 			}
 		}
 		if(hasPermission(player, PermissionNodes.AFFECT_CREATIVE) && player.getGameMode() == GameMode.CREATIVE){
 			System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f7");
 			return true;
 		}
 		if(hasPermission(player, PermissionNodes.AFFECT_SURVIVAL) && player.getGameMode() == GameMode.SURVIVAL){
 			System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f8");
 			return true;
 		}
 		if(hasPermission(player, PermissionNodes.AFFECT_ADVENTURE) && player.getGameMode() == GameMode.ADVENTURE){
 			System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f9");
 			return true;
 		}
 		System.out.println(allowPermission + "  " + denyPermission + "  " + target + "  f10");
 		return false;
 	}
 
 	/**
 	 * Determines if the target has a permission
 	 * 
 	 * @param target the target
 	 * @param permission the permission
 	 * @return true if they have the permission
 	 */
 	public static boolean hasPermission(CommandSender target, String permission){
 		boolean has = target.hasPermission(permission);
 		System.out.println("============ " + permission + ": " + has + " (" + target.getName() + ")");
 		return has;
 		//		if(!(target instanceof Player)){
 		//			return target.hasPermission(permission);
 		//		}
 		//		Player player = (Player) target;
 		//		if(pex.hasPEX()){
 		//			//return pex.getAbstract().has(player, permission, player.getWorld()) || player.hasPermission(permission);
 		//		}
 		//		return player.hasPermission(permission);
 	}
 }
