 package com.turt2live.antishare.inventory;
 
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 import com.turt2live.antishare.AntiShare;
 import com.turt2live.antishare.AntiShare.LogType;
 import com.turt2live.antishare.inventory.ASInventory.InventoryType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.ASRegion;
 
 /**
  * Manages inventories within AntiShare
  * 
  * @author turt2live
  */
 public class InventoryManager implements Listener {
 
 	private ConcurrentHashMap<String, ASInventory> creative = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, ASInventory> survival = new ConcurrentHashMap<String, ASInventory>();
 	// TODO: 1.3
 	//	private ConcurrentHashMap<String, ASInventory> adventure = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, ASInventory> region = new ConcurrentHashMap<String, ASInventory>();
 	private ConcurrentHashMap<String, TemporaryASInventory> playerTemp = new ConcurrentHashMap<String, TemporaryASInventory>();
 	private AntiShare plugin = AntiShare.getInstance();
 
 	/**
 	 * Creates a new Inventory Manager
 	 */
 	public InventoryManager(){
 		load();
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 
 	@EventHandler (priority = EventPriority.MONITOR)
 	public void onInventoryClick(InventoryClickEvent event){
 		if(event.isCancelled())
 			return;
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
 				//TODO: 1.3
 				//				case ADVENTURE:
 				//					adventure.put(player.getName()+"."+world.getName(), inventory);
 				//					break;
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
 	public void releasePlayer(Player player){
 		if(player == null){
 			return;
 		}
 
 		// Release
 		if(isInTemporary(player)){
 			removeFromTemporary(player);
 		}
 
 		refreshInventories(player);
 
 		// Cleanup
 		for(World world : Bukkit.getWorlds()){
 			if(creative.get(player.getName() + "." + world.getName()) != null){
 				creative.get(player.getName() + "." + world.getName()).save();
 			}
 			if(survival.get(player.getName() + "." + world.getName()) != null){
 				survival.get(player.getName() + "." + world.getName()).save();
 			}
 			// TODO: 1.3
 			//			if(adventure.get(player.getName()+"."+world.getName())!=null){
 			//				adventure.get(player.getName()+"."+world.getName()).save();
 			//			}
 			creative.remove(player.getName() + "." + world.getName());
 			survival.remove(player.getName() + "." + world.getName());
 			// TODO: 1.3
 			//adventure.remove(player.getName()+"."+world.getName());
 		}
 	}
 
 	/**
 	 * Refreshes inventories of a player
 	 * 
 	 * @param player the player
 	 */
 	public void refreshInventories(Player player, Slot... customSlots){
 		// Save
 		ASInventory premerge = ASInventory.generate(player, InventoryType.PLAYER);
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, player.getWorld(), customSlots);
 			premerge = getCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, player.getWorld(), customSlots);
 			premerge = getSurvivalInventory(player, player.getWorld());
 			break;
 		// TODO: 1.3
 		//		case ADVENTURE:
 		//			saveAdventureInventory(player, player.getWorld());
 		//			premerge = getAdventureInventory(player, player.getWorld());
 		//			break;
 		}
 
 		// Merge all inventories if needed
 		ASInventory merge = premerge.clone();
 		if(AntiShare.getInstance().getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			for(World world : Bukkit.getWorlds()){
 				merge.setGamemode(GameMode.CREATIVE);
 				creative.put(player.getName() + "." + world.getName(), merge);
 				merge.setGamemode(GameMode.SURVIVAL);
 				survival.put(player.getName() + "." + world.getName(), merge);
 				// TODO: 1.3
 				//merge.setGamemode(GameMode.ADVENTURE);
 				//adventure.put(player.getName()+"."+world.getName(), merge);
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
 		switch (player.getGameMode()){
 		case CREATIVE:
 			saveCreativeInventory(player, player.getWorld());
 			break;
 		case SURVIVAL:
 			saveSurvivalInventory(player, player.getWorld());
 			break;
 		// TODO: 1.3
 		//		case ADVENTURE:
 		//			saveAdventureInventory(player, player.getWorld());
 		//			break;
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
 	 * Gets the temporary inventory handler for a player
 	 * 
 	 * @param player the player
 	 * @return the temporary inventory handler, this can be null if the player doesn't have one
 	 */
 	public TemporaryASInventory getTemporaryInventoryHandler(Player player){
 		return playerTemp.get(player.getName());
 	}
 
 	/**
 	 * Saves a player's creative inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveCreativeInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		creative.put(player.getName() + "." + world.getName(), inventory);
 	}
 
 	/**
 	 * Saves a player's survival inventory
 	 * 
 	 * @param player the player
 	 * @param world the world
 	 */
 	public void saveSurvivalInventory(Player player, World world, Slot... customSlots){
 		ASInventory inventory = ASInventory.generate(player, InventoryType.PLAYER);
 		if(customSlots != null && customSlots.length > 0){
 			for(Slot slot : customSlots){
 				inventory.set(slot.slot, slot.item);
 			}
 		}
 		survival.put(player.getName() + "." + world.getName(), inventory);
 	}
 
 	// TODO: 1.3
 	//	/**
 	//	 * Saves a player's adventure inventory
 	//	 * @param player the player
 	//	 * @param world the world
 	//	 */
 	//	public void saveAdventureInventory(Player player, World world){
 	//		adventure.put(player.getName() + "." + world.getName(), ASInventory.generate(player, InventoryType.PLAYER));
 	//	}
 
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
 
 	// TODO: 1.3
 	//	/**
 	//	 * Gets a player's adventure inventory
 	//	 * 
 	//	 * @param player the player
 	//	 * @return the inventory
 	//	 * @param world the world
 	//	 */
 	//	public ASInventory getAdventureInventory(Player player, World world){
 	//		ASInventory inventory = adventure.get(player.getName() + "." + world.getName());
 	//		if(inventory == null){
 	//			inventory = new ASInventory(InventoryType.PLAYER, player.getName(), world, player.getGameMode());
 	//			adventure.put(player.getName() + "." + world.getName(), inventory);
 	//		}
 	//		return inventory;
 	//	}
 
 	/**
 	 * Gets a region's inventory
 	 * 
 	 * @param region the region
 	 * @return the inventory
 	 */
 	public ASInventory getRegionInventory(ASRegion region){
 		ASInventory inventory = new ASInventory(InventoryType.REGION, region.getUniqueID(), region.getWorld(), region.getGameMode());
 		inventory.set(0, new ItemStack(Material.BOAT));
 		return inventory;
 		//return this.region.get(region.getUniqueID());
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
 
 		// Status
 		int loaded = creative.size() + survival.size() + region.size() + playerTemp.size();
 		AntiShare.getInstance().getMessenger().log("Inventories Loaded: " + loaded, Level.INFO, LogType.INFO);
 	}
 
 	/**
 	 * Saves the inventory manager
 	 */
 	public void save(){
 		// Save players
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
 		// TODO: 1.3
 		//		for(String key : adventure.keySet()){
 		//			adventure.get(key).save();
 		//		}
 		for(String key : region.keySet()){
 			region.get(key).save();
 		}
 	}
 
 	/**
 	 * Reloads the inventory manager
 	 */
 	public void reload(){
 		save();
 		creative.clear();
 		survival.clear();
 		// TODO: 1.3
 		//		adventure.clear();
 		playerTemp.clear();
 		region.clear();
 		load();
 	}

	/**
	 * Fixes up world inventories by merging the specified world with the others
	 * 
	 * @param player the player
	 * @param world the world
	 */
	public void fixInventory(Player player, World world){
		// TODO: 1.3 - Entire method fix
		ASInventory c, s;
		switch (player.getGameMode()){
		case CREATIVE:
			saveCreativeInventory(player, world);
			break;
		case SURVIVAL:
			saveSurvivalInventory(player, world);
			break;
		}
		c = getCreativeInventory(player, world);
		s = getSurvivalInventory(player, world);
		for(World w : Bukkit.getWorlds()){
			String p = player.getName() + "." + w.getName();
			creative.put(p, c);
			survival.put(p, s);
		}
	}
 }
