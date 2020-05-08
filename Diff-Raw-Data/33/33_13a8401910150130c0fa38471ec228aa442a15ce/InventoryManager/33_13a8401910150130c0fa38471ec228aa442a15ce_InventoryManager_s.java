 package com.turt2live.antishare.manager;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.inventory.ASInventory;
 import com.turt2live.antishare.inventory.ASInventory.InventoryType;
 import com.turt2live.antishare.inventory.LinkedInventory;
 import com.turt2live.antishare.inventory.TemporaryASInventory;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.util.ASUtils;
 
 /**
  * AntiShare Inventory Manager - Entry point for all inventory stuff
  * 
  * @author turt2live
  */
 public class InventoryManager{
 
 	private AntiShare plugin = AntiShare.p;
 	private final ConcurrentHashMap<String, ASInventory> creative = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> survival = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> adventure = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> enderCreative = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> enderSurvival = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, ASInventory> enderAdventure = new ConcurrentHashMap<String, ASInventory>();
 	private final ConcurrentHashMap<String, TemporaryASInventory> temporary = new ConcurrentHashMap<String, TemporaryASInventory>();
 	private final List<LinkedInventory> linkedInventories = new ArrayList<LinkedInventory>();
 
 	/**
 	 * Creates a new inventory manager
 	 */
 	public InventoryManager(){
 		// Prepare linked inventories
 		EnhancedConfiguration linksYaml = new EnhancedConfiguration(new File(plugin.getDataFolder(), "linked-inventories.yml"), plugin);
 		linksYaml.loadDefaults(plugin.getResource("linked-inventories.yml"));
 		if(!linksYaml.fileExists() || !linksYaml.checkDefaults()){
 			linksYaml.saveDefaults();
 		}
 		linksYaml.load();
 	}
 
 	private void insert(String playername, ASInventory inventory){
 		if(!(inventory.type == InventoryType.PLAYER || inventory.type == InventoryType.ENDER)){
 			return;
 		}
 		String key = playername + "." + inventory.world;
 		boolean isEnder = inventory.type == InventoryType.ENDER;
 		switch (inventory.gamemode){
 		case SURVIVAL:
 			(isEnder ? enderSurvival : survival).put(key, inventory);
 			break;
 		case CREATIVE:
 			(isEnder ? enderCreative : creative).put(key, inventory);
 			break;
 		case ADVENTURE:
 			(isEnder ? enderAdventure : adventure).put(key, inventory);
 			break;
 		default:
 			return;
 		}
 	}
 
 	private void saveInventory(Player player){
 		ASInventory inventory = ASInventory.createEmptyInventory(player.getName(), player.getWorld().getName(), player.getGameMode(), InventoryType.PLAYER);
 		inventory.clone(player.getInventory());
 		insert(player.getName(), inventory.clone());
 		inventory = ASInventory.createEmptyInventory(player.getName(), player.getWorld().getName(), player.getGameMode(), InventoryType.ENDER);
 		inventory.clone(player.getEnderChest());
 		insert(player.getName(), inventory.clone());
 	}
 
 	private ASInventory getInventory(Player player, GameMode gamemode, InventoryType type){
 		String key = player.getName() + "." + player.getWorld().getName();
 		boolean isEnder = type == InventoryType.ENDER;
 		switch (gamemode){
 		case SURVIVAL:
			return (isEnder ? enderSurvival : survival).get(key);
 		case CREATIVE:
			return (isEnder ? enderCreative : creative).get(key);
 		case ADVENTURE:
			return (isEnder ? enderAdventure : adventure).get(key);
 		default:
			return ASInventory.EMPTY;
 		}
 	}
 
 	/**
 	 * Saves a player
 	 * 
 	 * @param player the player to save
 	 */
 	public void savePlayer(Player player){
 		saveInventory(player);
 		String name = player.getName();
 		for(World w : plugin.getServer().getWorlds()){
 			String world = w.getName();
 			if(creative.get(name + "." + world) != null){
 				creative.get(name + "." + world).save();
 			}
 			if(survival.get(name + "." + world) != null){
 				survival.get(name + "." + world).save();
 			}
 			if(adventure.get(name + "." + world) != null){
 				adventure.get(name + "." + world).save();
 			}
 			if(enderCreative.get(name + "." + world) != null){
 				enderCreative.get(name + "." + world).save();
 			}
 			if(enderSurvival.get(name + "." + world) != null){
 				enderSurvival.get(name + "." + world).save();
 			}
 			if(enderAdventure.get(name + "." + world) != null){
 				enderAdventure.get(name + "." + world).save();
 			}
 		}
 	}
 
 	/**
 	 * Sets a player to a temporary inventory, such as a region inventory
 	 * 
 	 * @param player the player
 	 * @param inventory the temporary inventory
 	 */
 	@SuppressWarnings ("deprecation")
 	public void setToTemporary(Player player, ASInventory inventory){
 		// Save current inventory
 		saveInventory(player);
 
 		// Set to temp
 		ASInventory current = ASInventory.createEmptyInventory(player.getName(), player.getWorld().getName(), player.getGameMode(), InventoryType.PLAYER);
 		current.clone(player.getInventory());
 		TemporaryASInventory spec = new TemporaryASInventory(current, inventory);
 		temporary.put(player.getName(), spec);
 		if(inventory == null){
 			player.getInventory().clear();
 			player.updateInventory();
 		}else{
 			inventory.setTo(player.getInventory());
 		}
 	}
 
 	/**
 	 * Removes the player from their temporary inventory, discarding it
 	 * 
 	 * @param player the player
 	 */
 	public void removeFromTemporary(Player player){
 		TemporaryASInventory inventory = temporary.get(player.getName());
 		if(inventory != null){
 			if(inventory.getLastInventory().type == InventoryType.ENDER){
 				inventory.getLastInventory().setTo(player.getEnderChest());
 			}else{
 				inventory.getLastInventory().setTo(player.getInventory());
 			}
 			temporary.remove(player.getName());
 		}
 	}
 
 	/**
 	 * Determines if a player is in a temporary inventory
 	 * 
 	 * @param player the player
 	 * @return true if they are in temporary
 	 */
 	public boolean isInTemporary(Player player){
 		return temporary.containsKey(player.getName());
 	}
 
 	/**
 	 * Does an inventory gamemode change on the player
 	 * 
 	 * @param player the player
 	 * @param to the gamemode the player is changing to
 	 */
 	public void onGameModeChange(Player player, GameMode to){
 		saveInventory(player);
 		ASInventory regular = getInventory(player, to, InventoryType.PLAYER);
		regular.setTo(player.getInventory());
 		ASInventory ender = getInventory(player, to, InventoryType.ENDER);
		ender.setTo(player.getEnderChest());
 	}
 
 	/**
 	 * Does an inventory world change on a player
 	 * 
 	 * @param player the player
 	 * @param to the world heading to
 	 */
 	public void onWorldChange(Player player, World to){
 		// TODO
 	}
 
 	/**
 	 * Injects an inventory into the manager
 	 * 
 	 * @param inventory the inventory to inject
 	 */
 	public void inject(ASInventory inventory){
 		// player.world = key
 		String key = inventory.owner + "." + inventory.world;
 		boolean ender = inventory.type == InventoryType.ENDER;
 		switch (inventory.gamemode){
 		case CREATIVE:
 			(ender ? enderCreative : creative).put(key, inventory);
 			break;
 		case SURVIVAL:
 			(ender ? enderSurvival : survival).put(key, inventory);
 			break;
 		case ADVENTURE:
 			(ender ? enderAdventure : adventure).put(key, inventory);
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Loads a player into the manager
 	 * 
 	 * @param player the player name to load
 	 */
 	public void loadPlayer(String player){
 		// Check archive first
 		File expected = new File(plugin.getDataFolder(), "data" + File.separator + "inventories" + File.separator + InventoryType.PLAYER.getRelativeFolderName() + File.separator + player + ".asinventory");
 		File archive = new File(plugin.getDataFolder(), "data" + File.separator + "inventories" + File.separator + InventoryType.PLAYER.getRelativeFolderName() + File.separator + player + ".asinventory");
 		if(!expected.exists()){
 			// Check archive
 			if(archive.exists()){
 				// Move file
 				archive.renameTo(expected);
 			}
 		}
 
 		// Load inventories
 		List<ASInventory> inventories = ASInventory.getAll(player, InventoryType.PLAYER);
 		for(ASInventory inventory : inventories){
 			insert(player, inventory);
 		}
 		inventories = ASInventory.getAll(player, InventoryType.ENDER);
 		for(ASInventory inventory : inventories){
 			insert(player, inventory);
 		}
 
 		// TODO: Load temp?
 	}
 
 	/**
 	 * Unloads a player from the inventory manager
 	 * 
 	 * @param player the player to unload
 	 */
 	public void unloadPlayer(Player player){
 		if(isInTemporary(player)){
 			removeFromTemporary(player);
 		}
 		savePlayer(player);
 		String playerName = player.getName();
 		for(World world : plugin.getServer().getWorlds()){
 			String worldName = world.getName();
 			creative.remove(playerName + "." + worldName);
 			survival.remove(playerName + "." + worldName);
 			adventure.remove(playerName + "." + worldName);
 			enderCreative.remove(playerName + "." + worldName);
 			enderSurvival.remove(playerName + "." + worldName);
 			enderAdventure.remove(playerName + "." + worldName);
 		}
 	}
 
 	/**
 	 * Gets the number of loaded inventories
 	 * 
 	 * @return the number of loaded inventories
 	 */
 	public int getLoaded(){
 		return creative.size() + survival.size() + temporary.size() + adventure.size() + enderCreative.size() + enderSurvival.size() + enderAdventure.size();
 	}
 
 	/**
 	 * Loads the inventory manager
 	 */
 	public void load(){
 		// Clear
 		creative.clear();
 		survival.clear();
 		adventure.clear();
 		enderCreative.clear();
 		enderSurvival.clear();
 		enderAdventure.clear();
 		temporary.clear();
 		// Loads regions
 		for(Region region : plugin.getRegionManager().getAllRegions()){
 			String UID = region.getID();
 			List<ASInventory> inventory = ASInventory.getAll(UID, InventoryType.REGION);
 			if(inventory != null){
 				if(inventory.size() >= 1){
 					region.setInventory(inventory.get(0));
 				}else{
 					region.setInventory(null);
 				}
 			}
 		}
 
 		// Load all links
 		EnhancedConfiguration links = new EnhancedConfiguration(new File(plugin.getDataFolder(), "linked-inventories.yml"), plugin);
 		links.load();
 		Set<String> worlds = links.getKeys(false);
 		this.linkedInventories.clear();
 		if(worlds != null){
 			for(String w : worlds){
 				List<String> affectedWorlds = new ArrayList<String>();
 				affectedWorlds.add(w);
 				String otherWorlds = links.getString(w + ".linked");
 				String[] otherWorldsArray = otherWorlds.split(",");
 				for(String ow : otherWorldsArray){
 					affectedWorlds.add(ow.trim());
 				}
 				GameMode gamemode = ASUtils.getGameMode(links.getString(w + ".gamemode"));
 				if(gamemode != null && affectedWorlds.size() > 0){
 					String[] allWorlds;
 					allWorlds = affectedWorlds.toArray(new String[affectedWorlds.size()]);
 					LinkedInventory link = new LinkedInventory(gamemode, allWorlds);
 					this.linkedInventories.add(link);
 				}else{
 					plugin.getLogger().warning(plugin.getMessages().getMessage("bad-file", "linked-inventories.yml"));
 				}
 			}
 		}
 
 		// Status
 		if(this.linkedInventories.size() > 0){
 			plugin.getLogger().info(plugin.getMessages().getMessage("linked-inventories-loaded", String.valueOf(this.linkedInventories.size())));
 		}
 	}
 
 	/**
 	 * Saves the inventory manager
 	 */
 	public void save(){
 		// Save players
 		if(Bukkit.getOnlinePlayers() != null){
 			for(Player player : Bukkit.getOnlinePlayers()){
 				unloadPlayer(player);
 			}
 		}
 
 		// Save inventories
 		for(String key : creative.keySet()){
 			creative.get(key).save();
 		}
 		for(String key : survival.keySet()){
 			survival.get(key).save();
 		}
 		for(String key : adventure.keySet()){
 			adventure.get(key).save();
 		}
 		for(String key : enderCreative.keySet()){
 			enderCreative.get(key).save();
 		}
 		for(String key : enderSurvival.keySet()){
 			enderSurvival.get(key).save();
 		}
 		for(String key : enderAdventure.keySet()){
 			enderAdventure.get(key).save();
 		}
 		for(Region region : plugin.getRegionManager().getAllRegions()){
 			if(region.getInventory() != null){
 				region.getInventory().save();
 			}
 		}
 	}
 
 	/**
 	 * Reloads the inventory manager
 	 */
 	public void reload(){
 		save();
 		load();
 	}
 }
