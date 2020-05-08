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
 package com.turt2live.antishare.inventory;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.feildmaster.lib.configuration.EnhancedConfiguration;
 import com.turt2live.antishare.inventory.ASInventory.InventoryType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.ASRegion;
 import com.turt2live.antishare.util.ASUtils;
 
 /**
  * Manages inventories within AntiShare
  * 
  * @author turt2live
  */
 public class InventoryManager implements Listener {
 
 	private ConcurrentHashMap<String, ASInventory> creative = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, ASInventory> survival = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, ASInventory> adventure = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, ASInventory> enderCreative = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, ASInventory> enderSurvival = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, ASInventory> enderAdventure = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, TemporaryASInventory> playerTemp = new ConcurrentHashMap<String, TemporaryASInventory>();
 	private List<LinkedInventory> links = new ArrayList<LinkedInventory>();
 	private AntiShare plugin = AntiShare.getInstance();
 
 	/**
 	 * Creates a new Inventory Manager
 	 */
 	public InventoryManager(){
 		// Prepare linked inventories
 		EnhancedConfiguration links = new EnhancedConfiguration(new File(plugin.getDataFolder(), "linked-inventories.yml"), plugin);
 		links.loadDefaults(plugin.getResource("resources/linked-inventories.yml"));
 		if(!links.fileExists() || !links.checkDefaults()){
 			links.saveDefaults();
 		}
 		links.load();
 
 		load();
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onInventoryClick(InventoryClickEvent event){
 		if(event.isCancelled()){
 			return;
 		}
 		Inventory inventory = event.getInventory();
 		InventoryHolder holder = inventory.getHolder();
 		if(holder != null && holder instanceof Player && inventory.getType() == org.bukkit.event.inventory.InventoryType.PLAYER){
 			Player player = (Player) holder;
 			// Refresh inventories
 			if(!isInTemporary(player)){
 				/* We're in monitor, so if anyone changes the cancelled
 				 * state, we can claim it on bad coding practices on the 
 				 * conflicting plugin. This also means that we can force
 				 * AntiShare to manually update a slot assuming the event
 				 * has not been cancelled thus far.
 				 */
 				refreshInventories(player);
 				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new InventoryWatcher(player), 5);
 			}
 		}
 	}
 
 	/**
 	 * Loads a specific player from file
 	 * 
 	 * @param player the player
 	 */
 	public void loadPlayer(Player player){
 		// Check archive first
 		File expected = new File(plugin.getDataFolder(), "inventories" + File.separator + InventoryType.PLAYER.getRelativeFolderName() + File.separator + player.getName() + ".yml");
 		if(!expected.exists()){
 			// Check archive
 			File archive = new File(plugin.getDataFolder(), "inventories" + File.separator + InventoryType.PLAYER.getRelativeFolderName() + File.separator + player.getName() + ".yml");
 			if(archive.exists()){
 				// Move file
 				archive.renameTo(expected);
 			}
 		}
 
 		// Standard inventories
 		List<ASInventory> list = ASInventory.generateInventory(player.getName(), InventoryType.PLAYER);
 
 		// Null check
 		if(list != null){
 			for(ASInventory inventory : list){
 				World world = inventory.getWorld();
 				GameMode gamemode = inventory.getGameMode();
 				switch (gamemode){
 				case CREATIVE:
 					creative.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case SURVIVAL:
 					survival.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case ADVENTURE:
 					adventure.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				}
 			}
 		}
 
 		// Ender inventories
 		list = ASInventory.generateInventory(player.getName(), InventoryType.ENDER);
 
 		// Null check
 		if(list != null){
 			for(ASInventory inventory : list){
 				World world = inventory.getWorld();
 				GameMode gamemode = inventory.getGameMode();
 				switch (gamemode){
 				case CREATIVE:
 					enderCreative.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case SURVIVAL:
 					enderSurvival.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				case ADVENTURE:
 					enderAdventure.put(player.getName() + "." + world.getName(), inventory);
 					break;
 				}
 			}
 		}
 
 		// Temporary inventories
 		list = ASInventory.generateInventory(player.getName(), InventoryType.TEMPORARY);
 
 		// Null check
 		if(list != null){
 			for(ASInventory inventory : list){
 				TemporaryASInventory spec = new TemporaryASInventory(ASInventory.generate(player, InventoryType.PLAYER), inventory);
 				playerTemp.put(player.getName(), spec);
 			}
 		}
 	}
 
 	/**
 	 * Cleans up player data
 	 * 
 	 * @param player the player
 	 */
 	public void releasePlayer(final Player player){
 		if(player == null){
 			return;
 		}
 
 		final String name = player.getName();
 		final List<String> worlds = new ArrayList<String>();
 		for(World w : Bukkit.getWorlds()){
 			worlds.add(w.getName());
 		}
 
 		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable(){
 			@Override
 			public void run(){
 				// Release
 				if(isInTemporary(player)){
 					removeFromTemporary(player);
 				}
 
 				refreshInventories(player);
 				// Cleanup
 				for(String world : worlds){
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
 					creative.remove(name + "." + world);
 					survival.remove(name + "." + world);
 					adventure.remove(name + "." + world);
 					enderCreative.remove(name + "." + world);
 					enderSurvival.remove(name + "." + world);
 					enderAdventure.remove(name + "." + world);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Refreshes inventories of a player
 	 * 
 	 * @param player the player
 	 * @param customSlots the slots to force items upon
 	 */
 	public void refreshInventories(Player player, Slot... customSlots){
 		// Save
 		ASInventory premerge = ASInventory.generate(player, InventoryType.PLAYER);
 		ASInventory enderPremerge = ASInventory.generate(player, InventoryType.ENDER);
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, player.getWorld(), customSlots);
 			premerge = getCreativeInventory(player, player.getWorld());
 			saveEnderCreativeInventory(player, player.getWorld(), customSlots);
 			enderPremerge = getEnderCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, player.getWorld(), customSlots);
 			premerge = getSurvivalInventory(player, player.getWorld());
 			saveEnderSurvivalInventory(player, player.getWorld(), customSlots);
 			enderPremerge = getEnderSurvivalInventory(player, player.getWorld());
 			break;
 		case ADVENTURE:
 			saveAdventureInventory(player, player.getWorld());
 			premerge = getAdventureInventory(player, player.getWorld());
 			saveEnderAdventureInventory(player, player.getWorld(), customSlots);
 			enderPremerge = getEnderAdventureInventory(player, player.getWorld());
 			break;
 		}
 
 		// Merge all inventories if needed
 		ASInventory merge = premerge.clone();
 		ASInventory enderMerge = enderPremerge.clone();
 		if(AntiShare.getInstance().getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			for(World world : Bukkit.getWorlds()){
 				merge.setGamemode(GameMode.CREATIVE);
 				enderMerge.setGamemode(GameMode.CREATIVE);
 				creative.put(player.getName() + "." + world.getName(), merge);
 				enderCreative.put(player.getName() + "." + world.getName(), enderMerge);
 				merge.setGamemode(GameMode.SURVIVAL);
 				enderMerge.setGamemode(GameMode.SURVIVAL);
 				survival.put(player.getName() + "." + world.getName(), merge);
 				enderSurvival.put(player.getName() + "." + world.getName(), enderMerge);
 				merge.setGamemode(GameMode.ADVENTURE);
 				enderMerge.setGamemode(GameMode.ADVENTURE);
 				adventure.put(player.getName() + "." + world.getName(), merge);
 				enderAdventure.put(player.getName() + "." + world.getName(), enderMerge);
 			}
 		}
 	}
 
 	/**
 	 * Updates ender chest inventories for the player's current world
 	 * 
 	 * @param player the player
 	 * @param to the game mode (to)
 	 * @param from the game mode (from)
 	 */
 	public void updateEnderChest(Player player, GameMode to, GameMode from){
 		if(!plugin.getConfig().getBoolean("handled-actions.gamemode-ender-chests")){
 			return;
 		}
 		switch (to){
 		case CREATIVE:
 			saveEnderCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			saveEnderSurvivalInventory(player, player.getWorld());
 			break;
 		case ADVENTURE:
 			saveEnderAdventureInventory(player, player.getWorld());
 			break;
 		}
 		switch (from){
 		case CREATIVE:
 			getEnderCreativeInventory(player, player.getWorld()).setTo(player);
 			break;
 		case SURVIVAL:
 			getEnderSurvivalInventory(player, player.getWorld()).setTo(player);
 			break;
 		case ADVENTURE:
 			getEnderAdventureInventory(player, player.getWorld()).setTo(player);
 			break;
 		}
 	}
 
 	/**
 	 * Updates ender chest inventories for the player's current game mode
 	 * 
 	 * @param player the player
 	 * @param to the world (to)
 	 * @param from the world (from)
 	 */
 	public void updateEnderChest(Player player, World to, World from){
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveEnderCreativeInventory(player, from);
 			getEnderCreativeInventory(player, to).setTo(player);
 			break;
 		case SURVIVAL:
 			saveEnderSurvivalInventory(player, from);
 			getEnderSurvivalInventory(player, to).setTo(player);
 			break;
 		case ADVENTURE:
 			saveEnderAdventureInventory(player, from);
 			getEnderAdventureInventory(player, to).setTo(player);
 			break;
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
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, player.getWorld());
 			saveEnderCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, player.getWorld());
 			saveEnderSurvivalInventory(player, player.getWorld());
 			break;
 		case ADVENTURE:
 			saveAdventureInventory(player, player.getWorld());
 			saveEnderAdventureInventory(player, player.getWorld());
 			break;
 		}
 
 		// Set to temp
 		TemporaryASInventory spec = new TemporaryASInventory(ASInventory.generate(player, InventoryType.PLAYER), inventory);
 		playerTemp.put(player.getName(), spec);
 		if(inventory == null){
 			player.getInventory().clear();
 			player.updateInventory();
 		}else{
 			inventory.setTo(player);
 		}
 	}
 
 	/**
 	 * Removes the player from their temporary inventory, discarding it
 	 * 
 	 * @param player the player
 	 */
 	public void removeFromTemporary(Player player){
 		TemporaryASInventory inventory = playerTemp.get(player.getName());
 		if(inventory != null){
 			inventory.getLastInventory().setTo(player);
 			playerTemp.remove(player.getName());
 		}
 	}
 
 	/**
 	 * Determines if a player is in a temporary inventory
 	 * 
 	 * @param player the player
 	 * @return true if they are in temporary
 	 */
 	public boolean isInTemporary(Player player){
 		return playerTemp.containsKey(player.getName());
 	}
 
 	/**
 	 * Saves a player's creative inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @param customSlots the slots to force items upon
 	 */
 	public void saveCreativeInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		inventory.setWorld(world);
 		creative.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's creative ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @param customSlots the slots to force items upon
 	 */
 	public void saveEnderCreativeInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.ENDER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		inventory.setWorld(world);
 		enderCreative.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's survival inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @param customSlots the slots to force items upon
 	 */
 	public void saveSurvivalInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		inventory.setWorld(world);
 		survival.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's survival ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @param customSlots the slots to force items upon
 	 */
 	public void saveEnderSurvivalInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.ENDER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		inventory.setWorld(world);
 		enderSurvival.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's adventure inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @param customSlots the slots to force items upon
 	 */
 	public void saveAdventureInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		inventory.setWorld(world);
 		adventure.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Saves a player's adventure ender chest inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 * @param customSlots the slots to force items upon
 	 */
 	public void saveEnderAdventureInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.ENDER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		inventory.setWorld(world);
 		enderAdventure.put(player.getName() + "." + world.getName(), inventory);
 		inventory.save();
 	}
 
 	/**
 	 * Gets a player's creative inventory
 	 * 
 	 * @param player the player
 	 * @return the inventory
 	 * @param world the world
 	 */
 	public ASInventory getCreativeInventory(Player player, World world){
 		ASInventory inventory = creative.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.PLAYER, player.getName(), world, player.getGameMode());
 			creative.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's creative ender chest inventory
 	 * 
 	 * @param player the player
 	 * @return the inventory
 	 * @param world the world
 	 */
 	public ASInventory getEnderCreativeInventory(Player player, World world){
 		ASInventory inventory = enderCreative.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.ENDER, player.getName(), world, player.getGameMode());
 			enderCreative.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's survival inventory
 	 * 
 	 * @param player the player
 	 * @return the inventory
 	 * @param world the world
 	 */
 	public ASInventory getSurvivalInventory(Player player, World world){
 		ASInventory inventory = survival.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.PLAYER, player.getName(), world, player.getGameMode());
 			survival.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's survival ender chest inventory
 	 * 
 	 * @param player the player
 	 * @return the inventory
 	 * @param world the world
 	 */
 	public ASInventory getEnderSurvivalInventory(Player player, World world){
 		ASInventory inventory = enderSurvival.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.ENDER, player.getName(), world, player.getGameMode());
 			enderSurvival.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's adventure inventory
 	 * 
 	 * @param player the player
 	 * @return the inventory
 	 * @param world the world
 	 */
 	public ASInventory getAdventureInventory(Player player, World world){
 		ASInventory inventory = adventure.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.PLAYER, player.getName(), world, player.getGameMode());
 			adventure.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Gets a player's adventure ender chest inventory
 	 * 
 	 * @param player the player
 	 * @return the inventory
 	 * @param world the world
 	 */
 	public ASInventory getEnderAdventureInventory(Player player, World world){
 		ASInventory inventory = enderAdventure.get(player.getName() + "." + world.getName());
 		if(inventory == null){
 			inventory = new ASInventory(InventoryType.ENDER, player.getName(), world, player.getGameMode());
 			enderAdventure.put(player.getName() + "." + world.getName(), inventory);
 		}
 		return inventory;
 	}
 
 	/**
 	 * Loads the inventory manager
 	 */
 	public void load(){
 		// Loads regions
 		for(ASRegion region : AntiShare.getInstance().getRegionManager().getAllRegions()){
 			String UID = region.getUniqueID();
 			List<ASInventory> inventory = ASInventory.generateInventory(UID, InventoryType.REGION);
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
 		this.links.clear();
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
 					this.links.add(link);
 				}else{
 					AntiShare.getInstance().log("Invalid linked inventory. Please check the linked-inventories.yml file", Level.WARNING);
 				}
 			}
 		}
 
 		// Status
 		int loaded = creative.size() + survival.size() + playerTemp.size() + adventure.size() + enderCreative.size() + enderSurvival.size() + enderAdventure.size();
 		AntiShare.getInstance().log("Inventories Loaded: " + loaded, Level.INFO);
 		AntiShare.getInstance().log("Linked Inventories: " + this.links.size(), Level.INFO);
 	}
 
 	/**
 	 * Saves the inventory manager
 	 */
 	public void save(){
 		// Save players
		if(Bukkit.getOnlinePlayers() != null)
			for(Player player : Bukkit.getOnlinePlayers()){
				releasePlayer(player);
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
 		for(ASRegion region : plugin.getRegionManager().getAllRegions()){
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
 		creative.clear();
 		survival.clear();
 		adventure.clear();
 		enderCreative.clear();
 		enderSurvival.clear();
 		enderAdventure.clear();
 		playerTemp.clear();
 		load();
 	}
 
 	/**
 	 * Fixes up world inventories by merging the specified world with the others
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void fixInventory(Player player, World world){
 		ASInventory c, s, a, ec, es, ea;
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, world);
 			saveEnderCreativeInventory(player, world);
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, world);
 			saveEnderSurvivalInventory(player, world);
 			break;
 		case ADVENTURE:
 			saveAdventureInventory(player, world);
 			saveEnderAdventureInventory(player, world);
 			break;
 		}
 		c = getCreativeInventory(player, world);
 		s = getSurvivalInventory(player, world);
 		a = getAdventureInventory(player, world);
 		ec = getEnderCreativeInventory(player, world);
 		es = getEnderSurvivalInventory(player, world);
 		ea = getEnderAdventureInventory(player, world);
 		for(World w : Bukkit.getWorlds()){
 			String p = player.getName() + "." + w.getName();
 			creative.put(p, c);
 			survival.put(p, s);
 			adventure.put(p, a);
 			enderCreative.put(p, ec);
 			enderSurvival.put(p, es);
 			enderAdventure.put(p, ea);
 		}
 	}
 
 	/**
 	 * Checks the player for linked worlds. This must be called AFTER saving
 	 * 
 	 * @param player the player
 	 * @param to the 'going to' world
 	 * @param from the 'coming from' world
 	 */
 	public void checkLinks(Player player, World to, World from){
 		GameMode gamemode = player.getGameMode();
 		for(LinkedInventory link : links){
 			if(link.isGameModeAffected(gamemode)){
 				if(link.isWorldAffected(from)){
 					String[] allWorlds = link.getAffectedWorlds();
 					ASInventory inventory = null;
 					ASInventory enderInventory = null;
 					switch (gamemode){
 					case SURVIVAL:
 						inventory = getSurvivalInventory(player, from);
 						enderInventory = getEnderSurvivalInventory(player, from);
 						break;
 					case CREATIVE:
 						inventory = getCreativeInventory(player, from);
 						enderInventory = getEnderCreativeInventory(player, from);
 						break;
 					case ADVENTURE:
 						inventory = getAdventureInventory(player, from);
 						enderInventory = getEnderAdventureInventory(player, from);
 						break;
 					}
 					for(String world : allWorlds){
 						String p = player.getName() + "." + world;
 						if(world.equalsIgnoreCase(from.getName())){
 							continue;
 						}
 						switch (gamemode){
 						case CREATIVE:
 							creative.put(p, inventory);
 							enderCreative.put(p, enderInventory);
 							break;
 						case SURVIVAL:
 							survival.put(p, inventory);
 							enderSurvival.put(p, enderInventory);
 							break;
 						case ADVENTURE:
 							adventure.put(p, inventory);
 							enderAdventure.put(p, enderInventory);
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 }
