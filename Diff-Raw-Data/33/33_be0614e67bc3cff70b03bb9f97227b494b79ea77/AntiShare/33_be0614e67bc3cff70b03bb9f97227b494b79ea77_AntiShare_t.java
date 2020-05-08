 /*******************************************************************************
  * Copyright (c) 2012 turt2live (Travis Ralston).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
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
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.PluginWrapper;
 import com.turt2live.antishare.Systems.Manager;
 import com.turt2live.antishare.listener.BaseListener;
 import com.turt2live.antishare.manager.InventoryManager;
 import com.turt2live.antishare.manager.RegionManager;
 import com.turt2live.antishare.metrics.Metrics;
 import com.turt2live.antishare.metrics.TrackerList;
 import com.turt2live.antishare.notification.Alert;
 import com.turt2live.antishare.notification.Messages;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.permissions.Permissions;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.signs.SignList;
 import com.turt2live.antishare.tekkitcompat.TabRegister;
 import com.turt2live.antishare.util.generic.ConflictThread;
 import com.turt2live.antishare.util.generic.ItemMap;
 import com.turt2live.antishare.util.generic.SelfCompatibility;
 import com.turt2live.antishare.util.generic.UpdateChecker;
import com.turt2live.materials.ServerHas;
 
 /**
  * AntiShare
  * 
  * @author turt2live
  */
 public class AntiShare extends PluginWrapper {
 
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
 
 	private static AntiShare instance;
 	private Permissions permissions;
 	private ItemMap itemMap;
 	private BaseListener listener;
 	private Alert alerts;
 	private Messages messages;
 	private Metrics metrics;
 	private TrackerList trackers;
 	private SignList signs;
 	private List<String> disabledSNPlayers = new ArrayList<String>();
 	private String build = "Unknown build, custom?";
 	private Systems sys;
 
 	/**
 	 * Gets the active AntiShare instance
 	 * 
 	 * @return the instance
 	 */
 	public static AntiShare getInstance(){
 		return instance;
 	}
 
 	@Override
 	public void onEnable(){
 		instance = this;
 
 		// File check
 		if(!getDataFolder().exists()){
 			getDataFolder().mkdirs();
 		}
 		File data = new File(getDataFolder(), "data");
 		if(!data.exists()){
 			data.mkdirs();
 		}
 
 		// Check configuration
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Checking configuration...");
 		}
 		getConfig().loadDefaults(getResource("resources/config.yml"));
 		if(!getConfig().fileExists() || !getConfig().checkDefaults()){
 			getConfig().saveDefaults();
 		}
 		getConfig().load();
 
 		// Get build number
 		try{
 			BufferedReader in = new BufferedReader(new InputStreamReader(getResource("plugin.yml")));
 			String line;
 			while ((line = in.readLine()) != null){
 				if(line.startsWith("build: ")){
 					line = line.replace("build: ", "");
 					build = line;
 					break;
 				}
 			}
 		}catch(IOException e){}
 
 		// Create version string
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Creating version string in config.yml...");
 		}
 		String val = getDescription().getVersion() + "|" + getServer().getVersion() + "|" + getServer().getOnlineMode() + "|" + build;
 		getConfig().set("error-reporting.error-string", val);
 		saveConfig();
 
 		// Move SimpleNotice file
 		File oldSNFile = new File(getDataFolder(), "disabled-simplenotice-users.txt");
 		if(oldSNFile.exists()){
 			oldSNFile.renameTo(new File(getDataFolder(), "data" + File.separator + "disabled-simplenotice-users.txt"));
 		}
 
 		// Get all disabled SimpleNotice users
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Loading disabled SimpleNotice users...");
 		}
 		try{
 			File snFile = new File(getDataFolder(), "data" + File.separator + "disabled-simplenotice-users.txt");
 			if(snFile.exists()){
 				BufferedReader in = new BufferedReader(new FileReader(snFile));
 				String line;
 				while ((line = in.readLine()) != null){
 					disabledSNPlayers.add(line);
 				}
 				in.close();
 			}else{
 				snFile.createNewFile();
 			}
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 
 		// Check for online mode
 		if(!getServer().getOnlineMode()){
 			if(!getConfig().getBoolean("other.quiet-offline-mode-warning")){
 				getLogger().severe("**********************");
 				getLogger().severe("Your server is in Offline Mode. AntiShare does not support offline mode servers.");
 				getLogger().severe("AntiShare will still run, but you will not get help from turt2live!!");
 				getLogger().severe("You can turn this message off in the configuration.");
 				getLogger().severe("**********************");
 			}
 		}
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Setting up Metrics...");
 		}
 		try{
 			metrics = new Metrics(this);
 		}catch(IOException e1){
 			getLogger().severe("AntiShare encountered and error. Please report this to turt2live.");
 			e1.printStackTrace();
 		}
 
 		// Register SimpleNotice channel to AntiShare
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Registering SimpleNotice channel...");
 		}
 		getServer().getMessenger().registerOutgoingPluginChannel(this, "SimpleNotice");
 
 		// Convert blocks
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("[Self Compat] Converting blocks...");
 		}
 		SelfCompatibility.convertBlocks();
 
 		// Migrate world configurations
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("[Self Compat] Migrating world configurations...");
 		}
 		SelfCompatibility.migrateWorldConfigurations();
 
 		// Migrate region players (3.8.0-3.9.0)
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("[Self Compat] Migrating region player data...");
 		}
 		SelfCompatibility.migratePlayerData();
 
 		// Convert inventories (3.1.3-3.2.0/Current)
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("[Self Compat] Converting 3.1.3 inventories...");
 		}
 		SelfCompatibility.convert313Inventories();
 
 		// Convert inventories (5.2.0/Current)
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("[Self Compat] Converting 5.2.0 inventories...");
 		}
 		SelfCompatibility.cleanup520Inventories();
 
 		// Cleanup old files
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("[Self Compat] Cleaning up inventories...");
 		}
 		SelfCompatibility.cleanupOldInventories(); // Handles on/off in config internally
 
 		// Cleanup old files
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("[Self Compat] Cleaning up configuration files...");
 		}
 		SelfCompatibility.cleanupYAML();
 
 		// Pre-load
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting sign list...");
 		}
 		signs = new SignList();
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting permissions...");
 		}
 		permissions = new Permissions();
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting item map...");
 		}
 		itemMap = new ItemMap();
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting Metrics trackers...");
 		}
 		trackers = new TrackerList();
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting listener...");
 		}
 		listener = new BaseListener();
 
 		// Startup Systems Manager
 		sys = new Systems();
 		sys.load();
 
 		// Setup everything
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting alerts...");
 		}
 		alerts = new Alert();
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting messages...");
 		}
 		messages = new Messages();
 
 		// Statistics
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting update check...");
 		}
 		UpdateChecker.start();
 		// mcstats.org
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Starting Metrics...");
 		}
 		trackers.addTo(metrics);
 		metrics.start(); // Handles it's own opt-out
 
 		// Start listeners
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Registering listeners...");
 		}
 		getServer().getPluginManager().registerEvents(permissions, this);
 		getServer().getPluginManager().registerEvents(listener, this);
 
 		// Command handlers
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Setting up commands...");
 		}
 		getCommand("antishare").setExecutor(new CommandHandler());
 		if(ServerHas.tabComplete()){
 			TabRegister.register(getCommand("antishare"));
 		}
 
 		// Enabled
 		getLogger().info("Enabled!");
 
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Scheduling inventory and region updates...");
 		}
 		loadPlayerInformation();
 
 		// Conflict messages
 		if(!getConfig().getBoolean("other.more-quiet-startup")){
 			getLogger().info("Scheduling conflict messages...");
 		}
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new ConflictThread());
 	}
 
 	@Override
 	public void onDisable(){
 		// Save
 		if(metrics != null){
 			if(!getConfig().getBoolean("other.more-quiet-shutdown")){
 				getLogger().info("Flushing Metrics...");
 			}
 			metrics.flush();
 		}
 		if(sys != null){
 			sys.save();
 		}
 
 		// Disable
 		getServer().getScheduler().cancelTasks(this);
 		getLogger().info("Disabled!");
 
 		// Prepare as though it's a reload
 		permissions = null;
 		itemMap = null;
 		listener = null;
 		alerts = null;
 		messages = null;
 		metrics = null;
 		trackers = null;
 		signs = null;
 		sys = null;
 
 		// Save disabled SimpleNotice users
 		try{
 			File snFile = new File(getDataFolder(), "data" + File.separator + "disabled-simplenotice-users.txt");
 			BufferedWriter out = new BufferedWriter(new FileWriter(snFile, false));
 			for(String user : disabledSNPlayers){
 				out.write(user + "\r\n");
 			}
 			out.close();
 		}catch(IOException e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Reloads AntiShare
 	 */
 	public void reload(){
 		reloadConfig();
 		itemMap.reload();
 		signs.reload();
 		listener.reload();
 		alerts.reload();
 		messages.reload();
 		sys.reload();
 		loadPlayerInformation();
 	}
 
 	private void loadPlayerInformation(){
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
 			@Override
 			public void run(){
 				InventoryManager inventories = (InventoryManager) sys.getManager(Manager.INVENTORY);
 				RegionManager regions = (RegionManager) sys.getManager(Manager.REGION);
 				for(Player player : Bukkit.getOnlinePlayers()){
 					inventories.loadPlayer(player);
 					Region playerRegion = regions.getRegion(player.getLocation());
 					if(playerRegion != null){
 						playerRegion.alertSilentEntry(player);
 					}
 				}
 				int loaded = inventories.getLoaded();
 				if(loaded > 0){
 					AntiShare.getInstance().log("Inventories Loaded: " + loaded, Level.INFO);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Gets the general purpose systems manager
 	 * 
 	 * @return the general purpose systems manager
 	 */
 	public Systems getSystemsManager(){
 		return sys;
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
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param world the world
 	 * @param material the material applied to the permissions (or null for none)
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, World world, Material material){
 		return isBlocked(player, allowPermission, null, world, material, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param material the material applied to the permissions (or null for none)
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, Material material){
 		return isBlocked(player, allowPermission, denyPermission, world, material, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param material the material applied to the permissions (or null for none)
 	 * @param specialOnly true to only check permission.[item] permissions
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, Material material, boolean specialOnly){
 		if(material != null){
 			if(permissions.has(player, allowPermission + "." + material.getId(), world)){
 				return false;
 			}
 			if(permissions.has(player, allowPermission + "." + material.name(), world)){
 				return false;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + material.getId(), world)){
 				return true;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + material.name(), world)){
 				return true;
 			}
 		}
 		if(specialOnly){
 			return false;
 		}
 		if(permissions.has(player, allowPermission, world)){
 			return false;
 		}
 		if(denyPermission != null && permissions.has(player, denyPermission, world)){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_CREATIVE, world) && player.getGameMode() == GameMode.CREATIVE){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_SURVIVAL, world) && player.getGameMode() == GameMode.SURVIVAL){
 			return true;
 		}
 		if(ServerHas.adventureMode()){
 			if(permissions.has(player, PermissionNodes.AFFECT_ADVENTURE, world) && player.getGameMode() == GameMode.ADVENTURE){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param target the target to apply to this permission, spaces will removed
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, String target){
 		return isBlocked(player, allowPermission, denyPermission, world, target, false);
 	}
 
 	/**
 	 * Determines if a player is blocked from doing something
 	 * 
 	 * @param player the player
 	 * @param allowPermission the "allow" permission
 	 * @param denyPermission the "deny" permission
 	 * @param world the world
 	 * @param target the target to apply to this permission, spaces will removed
 	 * @param specialOnly true to only check permission.[item] permissions
 	 * @return true if blocked
 	 */
 	public boolean isBlocked(Player player, String allowPermission, String denyPermission, World world, String target, boolean specialOnly){
 		if(target != null){
 			target = target.replaceAll(" ", "");
 			if(target.startsWith("/")){
 				target = target.substring(1);
 			}
 			if(permissions.has(player, allowPermission + "." + target, world)){
 				return false;
 			}
 			if(permissions.has(player, allowPermission + "." + target, world)){
 				return false;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + target, world)){
 				return true;
 			}
 			if(denyPermission != null && permissions.has(player, denyPermission + "." + target, world)){
 				return true;
 			}
 		}
 		if(specialOnly){
 			return false;
 		}
 		if(permissions.has(player, allowPermission, world)){
 			return false;
 		}
 		if(denyPermission != null && permissions.has(player, denyPermission, world)){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_CREATIVE, world) && player.getGameMode() == GameMode.CREATIVE){
 			return true;
 		}
 		if(permissions.has(player, PermissionNodes.AFFECT_SURVIVAL, world) && player.getGameMode() == GameMode.SURVIVAL){
 			return true;
 		}
 		if(ServerHas.adventureMode()){
 			if(permissions.has(player, PermissionNodes.AFFECT_ADVENTURE, world) && player.getGameMode() == GameMode.ADVENTURE){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Gets a message
 	 * 
 	 * @param path the path to the message
 	 * @return the message
 	 */
 	public String getMessage(String path){
 		return messages.getMessage(path);
 	}
 
 	/**
 	 * Gets the messages handler in AntiShare
 	 * 
 	 * @return the messages handler
 	 */
 	public Messages getMessages(){
 		return messages;
 	}
 
 	/**
 	 * Gets the permissions handler for AntiShare
 	 * 
 	 * @return the permissions
 	 */
 	public Permissions getPermissions(){
 		return permissions;
 	}
 
 	/**
 	 * Gets the Item Map for AntiShare
 	 * 
 	 * @return the item map
 	 */
 	public ItemMap getItemMap(){
 		return itemMap;
 	}
 
 	/**
 	 * Gets the Alert instance for AntiShare
 	 * 
 	 * @return the alerts system
 	 */
 	public Alert getAlerts(){
 		return alerts;
 	}
 
 	/**
 	 * Gets the listener being used by AntiShare
 	 * 
 	 * @return the listener
 	 */
 	public BaseListener getListener(){
 		return listener;
 	}
 
 	/**
 	 * Gets the metrics being used by AntiShare
 	 * 
 	 * @return the metrics
 	 */
 	public Metrics getMetrics(){
 		return metrics;
 	}
 
 	/**
 	 * Gets the tracker list being used by AntiShare
 	 * 
 	 * @return the trackers
 	 */
 	public TrackerList getTrackers(){
 		return trackers;
 	}
 
 	/**
 	 * Gets the sign list being used by AntiShare
 	 * 
 	 * @return the sign list
 	 */
 	public SignList getSignList(){
 		return signs;
 	}
 
 	/**
 	 * Log a message
 	 * 
 	 * @param string the message
 	 * @param level the level
 	 */
 	public void log(String string, Level level){
 		getLogger().log(level, string);
 	}
 
 	/**
 	 * Gets the message prefix
 	 * 
 	 * @return the message prefix
 	 */
 	public String getPrefix(){
 		return messages.getPrefix();
 	}
 
 	/**
 	 * Gets the AntiShare build number
 	 * 
 	 * @return the build number
 	 */
 	public String getBuild(){
 		return build;
 	}
 
 }
