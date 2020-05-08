 package com.turt2live.antishare;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Jukebox;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.Painting;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.PoweredMinecart;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.entity.ThrownExpBottle;
 import org.bukkit.entity.Vehicle;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPistonExtendEvent;
 import org.bukkit.event.block.BlockPistonRetractEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.ExpBottleEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerGameModeChangeEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.vehicle.VehicleDestroyEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.turt2live.antishare.notification.Alert.AlertTrigger;
 import com.turt2live.antishare.notification.Alert.AlertType;
 import com.turt2live.antishare.permissions.PermissionNodes;
 import com.turt2live.antishare.regions.ASRegion;
 import com.turt2live.antishare.storage.PerWorldConfig;
 import com.turt2live.antishare.storage.PerWorldConfig.ListType;
 
 /**
  * The core listener - Listens to all events needed by AntiShare and handles them
  * 
  * @author turt2live
  */
 public class ASListener implements Listener {
 
 	private AntiShare plugin = AntiShare.instance;
 	private ConcurrentHashMap<World, PerWorldConfig> config = new ConcurrentHashMap<World, PerWorldConfig>();
 
 	/**
 	 * Creates a new Listener
 	 */
 	public ASListener(){
 		reload();
 	}
 
 	/**
 	 * Reloads lists
 	 */
 	public void reload(){
 		config.clear();
 		for(World world : Bukkit.getWorlds()){
 			config.put(world, new PerWorldConfig(world));
 		}
 	}
 
 	/**
 	 * Prints out each world to the writer
 	 * 
 	 * @param out the writer
 	 * @throws IOException for internal handling
 	 */
 	public void print(BufferedWriter out) throws IOException{
 		for(World world : config.keySet()){
 			out.write("## WORLD: " + world.getName() + " \r\n");
 			config.get(world).print(out);
 		}
 	}
 
 	// ################# World Load
 
 	@EventHandler
 	public void onWorldLoad(WorldLoadEvent event){
 		World world = event.getWorld();
 		config.put(world, new PerWorldConfig(world));
 	}
 
 	// ################# World Unload
 
 	@EventHandler
 	public void onWorldUnload(WorldUnloadEvent event){
 		World world = event.getWorld();
 		config.remove(world);
 	}
 
 	// ################# Block Break
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		AlertType type = AlertType.ILLEGAL;
 		boolean special = false;
 		boolean region = false;
 		Boolean drops = null;
 		AlertType specialType = AlertType.LEGAL;
 		String blockGM = "Unknown";
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_BREAK, block.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(block.getWorld()).isBlocked(block.getType(), ListType.BLOCK_BREAK)){
 			type = AlertType.LEGAL;
 		}
 
 		// Check creative/survival blocks
 		if(!plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 			GameMode blockGamemode = plugin.getBlockManager().getType(block);
 			if(blockGamemode != null){
 				special = true;
 				blockGM = blockGamemode.name().toLowerCase();
 				String oGM = blockGM.equalsIgnoreCase("creative") ? "survival" : "creative";
 				if(player.getGameMode() != blockGamemode){
 					boolean deny = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + blockGM + "-blocks.deny");
 					drops = plugin.getConfig().getBoolean("settings." + oGM + "-breaking-" + blockGM + "-blocks.block-drops");
 					if(deny){
 						specialType = AlertType.ILLEGAL;
 					}
 				}
 			}
 		}
 
 		// Check regions
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_BREAK)){
 			ASRegion playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 			ASRegion blockRegion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				special = true;
 				region = true;
 				specialType = AlertType.ILLEGAL;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL || specialType == AlertType.ILLEGAL){
 			event.setCancelled(true);
		}else{
			plugin.getBlockManager().removeBlock(block);
 		}
 
 		// Handle drops
 		if(drops != null){
 			if(drops){
 				if(event.isCancelled()){
					plugin.getBlockManager().removeBlock(block);
 					block.breakNaturally();
 				}
 			}else{
				plugin.getBlockManager().removeBlock(block);
 				block.setType(Material.AIR);
 			}
 		}
 
 		// Alert
 		if(special){
 			if(region){
 				if(specialType == AlertType.ILLEGAL){
 					String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (specialType == AlertType.ILLEGAL ? " tried to break " : " broke  ") + (specialType == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " in a region.";
 					String specialPlayerMessage = ChatColor.RED + "You cannot break blocks that are not in your region";
 					plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, specialType, AlertTrigger.BLOCK_BREAK);
 				}
 			}else{
 				String specialMessage = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (specialType == AlertType.ILLEGAL ? " tried to break the " + blockGM + " block " : " broke the " + blockGM + " block ") + (specialType == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 				String specialPlayerMessage = plugin.getMessage("blocked-action." + blockGM + "-block-break");
 				plugin.getAlerts().alert(specialMessage, player, specialPlayerMessage, specialType, (blockGM.equalsIgnoreCase("creative") ? AlertTrigger.CREATIVE_BLOCK : AlertTrigger.SURVIVAL_BLOCK));
 			}
 		}else{
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to break " : " broke ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			String playerMessage = plugin.getMessage("blocked-action.break-block");
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_BREAK);
 		}
 
 		// Check for 'attached' blocks and internal inventories
 		if(player.getGameMode() == GameMode.CREATIVE && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING)){
 			// Check inventories
 			if(config.get(block.getWorld()).clearBlockInventoryOnBreak()){
 				if(block.getState() instanceof Chest){
 					Chest state = (Chest) block.getState();
 					state.getBlockInventory().clear();
 				}else if(block.getState() instanceof Jukebox){
 					Jukebox state = (Jukebox) block.getState();
 					state.setPlaying(null);
 				}else if(block.getState() instanceof Furnace){
 					Furnace state = (Furnace) block.getState();
 					state.getInventory().clear();
 				}
 			}
 
 			// Check for attached blocks
 			if(config.get(block.getWorld()).removeAttachedBlocksOnBreak()){
 				for(BlockFace face : BlockFace.values()){
 					Block rel = block.getRelative(face);
 					if(ASUtils.isDroppedOnBreak(rel, block)){
						plugin.getBlockManager().removeBlock(rel);
 						rel.setType(Material.AIR);
 					}
 				}
 			}
 		}
 	}
 
 	// ################# Block Place
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getBlock();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Sanity check
 		if(block.getType() == Material.AIR){
 			return;
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_BLOCK_PLACE, block.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(block.getWorld()).isBlocked(block.getType(), ListType.BLOCK_PLACE)){
 			type = AlertType.LEGAL;
 		}
 
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_PLACE)){
 			ASRegion playerRegion = plugin.getRegionManager().getRegion(player.getLocation());
 			ASRegion blockRegion = plugin.getRegionManager().getRegion(block.getLocation());
 			if(playerRegion != blockRegion){
 				type = AlertType.ILLEGAL;
 				region = true;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}else{
 			// Handle block place for tracker
 			if(!plugin.getPermissions().has(player, PermissionNodes.FREE_PLACE)){
 				plugin.getBlockManager().addBlock(player.getGameMode(), block);
 			}
 		}
 
 		// Alert
 		if(region){
 			if(type == AlertType.ILLEGAL){
 				String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to place " : " placed ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ") + ChatColor.WHITE + " in a region.";
 				String playerMessage = ChatColor.RED + "You cannot place blocks in another region!";
 				plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
 			}
 		}else{
 			String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to place " : " placed ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			String playerMessage = plugin.getMessage("blocked-action.place-block");
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.BLOCK_PLACE);
 		}
 	}
 
 	// ################# Player Interact Block
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onInteract(PlayerInteractEvent event){
 		Player player = event.getPlayer();
 		Block block = event.getClickedBlock();
 		Action action = event.getAction();
 		AlertType type = AlertType.LEGAL;
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.RIGHT_CLICK;
 
 		// Check for AntiShare tool
 		if(plugin.getPermissions().has(player, PermissionNodes.TOOL_USE) && player.getItemInHand() != null
 				&& (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK)){
 			if(player.getItemInHand().getType() == AntiShare.ANTISHARE_TOOL){
 				String blockname = block.getType().name().replaceAll("_", " ").toLowerCase();
 				String gamemode = (plugin.getBlockManager().getType(block) != null ? plugin.getBlockManager().getType(block).name() : "natural").toLowerCase();
 				ASUtils.sendToPlayer(player, "That " + ChatColor.YELLOW + blockname + ChatColor.WHITE + " is a " + ChatColor.YELLOW + gamemode + ChatColor.WHITE + " block.");
 
 				// Cancel and stop the check
 				event.setCancelled(true);
 				return;
 			}
 		}
 
 		// Right click list
 		if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK){
 			// Check if they should be blocked
 			if(config.get(block.getWorld()).isBlocked(block.getType(), ListType.RIGHT_CLICK)){
 				type = AlertType.ILLEGAL;
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, block.getWorld())){
 				type = AlertType.LEGAL;
 			}
 
 			// Set messages
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			playerMessage = plugin.getMessage("blocked-action.right-click");
 		}
 
 		// If this event is triggered as legal from the right click, check use lists
 		if(type == AlertType.LEGAL){
 			if(config.get(block.getWorld()).isBlocked(block.getType(), ListType.USE)){
 				type = AlertType.ILLEGAL;
 			}
 			// Check if they should be blocked
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, block.getWorld())){
 				type = AlertType.LEGAL;
 			}
 
 			// Set messages
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + block.getType().name().replace("_", " ");
 			playerMessage = plugin.getMessage("blocked-action.right-click");
 		}
 
 		// If the event is triggered as legal from the use lists, check the player's item in hand
 		if(type == AlertType.LEGAL && action == Action.RIGHT_CLICK_BLOCK && player.getItemInHand() != null){
 			// Check if they should be blocked
 			if(config.get(player.getWorld()).isBlocked(player.getItemInHand().getType(), ListType.USE)){
 				type = AlertType.ILLEGAL;
 			}
 			if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 				type = AlertType.LEGAL;
 			}
 
 			// Set messages
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + player.getItemInHand().getType().name().replace("_", " ");
 			playerMessage = plugin.getMessage("blocked-action.use-item");
 			trigger = AlertTrigger.USE_ITEM;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		if(type != AlertType.LEGAL){
 			plugin.getAlerts().alert(message, player, playerMessage, type, trigger);
 		}
 	}
 
 	// ################# Player Interact Entity
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onInteractEntity(PlayerInteractEntityEvent event){
 		Player player = event.getPlayer();
 		AlertType type = AlertType.ILLEGAL;
 
 		// Convert entity -> item ID
 		Material item = Material.AIR;
 		if(event.getRightClicked() instanceof StorageMinecart){
 			item = Material.STORAGE_MINECART;
 		}else if(event.getRightClicked() instanceof PoweredMinecart){
 			item = Material.POWERED_MINECART;
 		}else if(event.getRightClicked() instanceof Boat){
 			item = Material.BOAT;
 		}else if(event.getRightClicked() instanceof Minecart){
 			item = Material.MINECART;
 		}else if(event.getRightClicked() instanceof Painting){
 			item = Material.PAINTING;
 		}
 
 		// If the entity is not found, ignore the event
 		if(item == Material.AIR){
 			return;
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_RIGHT_CLICK, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(player.getWorld()).isBlocked(item, ListType.RIGHT_CLICK)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to right click " : " right clicked ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + item.name();
 		String playerMessage = plugin.getMessage("blocked-action.right-click");
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.RIGHT_CLICK);
 	}
 
 	// ################# Cart Death Check
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onCartDeath(VehicleDestroyEvent event){
 		Entity attacker = event.getAttacker();
 		Vehicle potentialCart = event.getVehicle();
 
 		// Sanity checks
 		if(attacker == null || !(potentialCart instanceof StorageMinecart)){
 			return;
 		}
 		if(!(attacker instanceof Player)){
 			return;
 		}
 
 		// Setup
 		Player player = (Player) attacker;
 		StorageMinecart cart = (StorageMinecart) potentialCart;
 
 		// Check internal inventories
 		if(player.getGameMode() == GameMode.CREATIVE && !plugin.getPermissions().has(player, PermissionNodes.BREAK_ANYTHING)){
 			// Check inventories
 			if(config.get(player.getWorld()).clearBlockInventoryOnBreak()){
 				cart.getInventory().clear();
 			}
 		}
 	}
 
 	// ################# Egg Check
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onEggThrow(PlayerEggThrowEvent event){
 		Player player = event.getPlayer();
 		AlertType type = AlertType.ILLEGAL;
 		Material item = Material.EGG;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(player.getWorld()).isBlocked(item, ListType.USE)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setHatching(false);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + item.name();
 		String playerMessage = plugin.getMessage("blocked-action.use-item");
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 	}
 
 	// ################# Experience Bottle Check
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onExpBottle(ExpBottleEvent event){
 		ThrownExpBottle bottle = event.getEntity();
 		LivingEntity shooter = bottle.getShooter();
 		AlertType type = AlertType.ILLEGAL;
 		Material item = Material.EXP_BOTTLE;
 
 		// Sanity Check
 		if(!(shooter instanceof Player)){
 			return;
 		}
 
 		// Setup
 		Player player = (Player) shooter;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_USE, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(player.getWorld()).isBlocked(item, ListType.USE)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setExperience(0);
 			event.setShowEffect(false);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use " : " used ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + item.name();
 		String playerMessage = plugin.getMessage("blocked-action.use-item");
 		if(type == AlertType.ILLEGAL){ // We don't want to show legal events because of spam
 			plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.USE_ITEM);
 		}
 	}
 
 	// ################# Drop Item
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onDrop(PlayerDropItemEvent event){
 		Player player = event.getPlayer();
 		Item item = event.getItemDrop();
 		ItemStack itemStack = item.getItemStack();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DROP, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(player.getWorld()).isBlocked(itemStack.getType(), ListType.DROP)){
 			type = AlertType.LEGAL;
 		}
 
 		// Region Check
 		if(plugin.getRegionManager().getRegion(player.getLocation()) != plugin.getRegionManager().getRegion(item.getLocation()) && type == AlertType.LEGAL){
 			if(!plugin.getPermissions().has(player, PermissionNodes.REGION_THROW)){
 				type = AlertType.ILLEGAL;
 				region = true;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + itemStack.getType().name().replace("_", " ");
 		String playerMessage = plugin.getMessage("blocked-action.drop-item");
 		if(region){
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to throw " : " threw ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + itemStack.getType().name().replace("_", " ") + ChatColor.WHITE + " into a region.";
 			playerMessage = ChatColor.RED + "You cannot throw items into another region!";
 		}
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.ITEM_DROP);
 	}
 
 	// ################# Pickup Item
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPickup(PlayerPickupItemEvent event){
 		Player player = event.getPlayer();
 		Item item = event.getItem();
 		ItemStack itemStack = item.getItemStack();
 		AlertType type = AlertType.ILLEGAL;
 		boolean region = false;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_PICKUP, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(player.getWorld()).isBlocked(itemStack.getType(), ListType.PICKUP)){
 			type = AlertType.LEGAL;
 		}
 
 		// Region Check
 		if(plugin.getRegionManager().getRegion(player.getLocation()) != plugin.getRegionManager().getRegion(item.getLocation()) && type == AlertType.LEGAL){
 			if(!plugin.getPermissions().has(player, PermissionNodes.REGION_PICKUP)){
 				type = AlertType.ILLEGAL;
 				region = true;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to pickup " : " picked up ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + itemStack.getType().name().replace("_", " ");
 		String playerMessage = plugin.getMessage("blocked-action.pickup-item");
 		if(region){
 			message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to pickup " : " picked up ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + itemStack.getType().name().replace("_", " ") + ChatColor.WHITE + " from a region.";
 			playerMessage = ChatColor.RED + "You cannot pickup items from another region!";
 		}
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.ITEM_PICKUP);
 	}
 
 	// ################# Player Death
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onDeath(PlayerDeathEvent event){
 		Player player = event.getEntity();
 		List<ItemStack> drops = event.getDrops();
 		AlertType type = AlertType.ILLEGAL;
 		int illegalItems = 0;
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_DEATH, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			List<ItemStack> remove = new ArrayList<ItemStack>();
 			for(ItemStack item : drops){
 				if(config.get(player.getWorld()).isBlocked(item.getType(), ListType.DEATH)){
 					illegalItems++;
 					remove.add(item);
 				}
 			}
 			// Remove items
 			for(ItemStack item : remove){
 				drops.remove(item);
 			}
 		}
 
 		// Determine new status
 		if(illegalItems == 0){
 			type = AlertType.LEGAL;
 		}
 
 		// Alert
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " died with " + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + illegalItems + " illegal item(s).";
 		String playerMessage = plugin.getMessage("blocked-action.die-with-item");
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.PLAYER_DEATH);
 	}
 
 	// ################# Player Command
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onCommand(PlayerCommandPreprocessEvent event){
 		Player player = event.getPlayer();
 		String command = event.getMessage().toLowerCase();
 		AlertType type = AlertType.ILLEGAL;
 
 		// Game Mode command
 		GameModeCommand.onPlayerCommand(event);
 		if(event.isCancelled()){
 			return;
 		}
 
 		// Check if they should be blocked
 		if(!plugin.isBlocked(player, PermissionNodes.ALLOW_PICKUP, player.getWorld())){
 			type = AlertType.LEGAL;
 		}
 		if(!config.get(player.getWorld()).isBlocked(command, ListType.COMMAND)){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert (with sanity check)
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to use the command " : " used the command ") + (type == AlertType.ILLEGAL ? ChatColor.RED : ChatColor.GREEN) + command;
 		String playerMessage = plugin.getMessage("blocked-action.command");
 		plugin.getAlerts().alert(message, player, playerMessage, type, AlertTrigger.COMMAND);
 	}
 
 	// ################# Player Move
 
 	@EventHandler (ignoreCancelled = true)
 	public void onMove(PlayerMoveEvent event){
 		Player player = event.getPlayer();
 		ASRegion currentRegion = plugin.getRegionManager().getRegion(event.getFrom());
 		ASRegion toRegion = plugin.getRegionManager().getRegion(event.getTo());
 
 		// Check world split
 		config.get(player.getWorld()).checkSplit(player);
 
 		// Check regions
 		if(currentRegion != toRegion){
 			if(currentRegion != null){
 				currentRegion.alertExit(player);
 			}
 			if(toRegion != null){
 				toRegion.alertEntry(player);
 			}
 		}
 	}
 
 	// ################# Player Game Mode Change
 
 	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onGameModeChange(PlayerGameModeChangeEvent event){
 		Player player = event.getPlayer();
 		GameMode from = player.getGameMode();
 		GameMode to = event.getNewGameMode();
 		boolean ignore = true;
 		boolean checkRegion = true;
 
 		// Check to see if we should even bother
 		if(!plugin.getConfig().getBoolean("handled-actions.gamemode-inventories")){
 			return;
 		}
 
 		// Tag check
 		if(player.hasMetadata("antishare-regionleave")){
 			player.removeMetadata("antishare-regionleave", plugin);
 			checkRegion = false;
 		}
 
 		// Region Check
 		if(!plugin.getPermissions().has(player, PermissionNodes.REGION_ROAM) && checkRegion){
 			ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 			if(region != null){
 				ASUtils.sendToPlayer(player, ChatColor.RED + "You are in a region and therefore cannot change Game Mode");
 				event.setCancelled(true);
 				return;
 			}
 		}
 
 		// Check temp
 		if(plugin.getInventoryManager().isInTemporary(player)){
 			plugin.getInventoryManager().removeFromTemporary(player);
 		}
 
 		if(!plugin.getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			// Save from
 			switch (from){
 			case CREATIVE:
 				plugin.getInventoryManager().saveCreativeInventory(player, player.getWorld());
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().saveSurvivalInventory(player, player.getWorld());
 				break;
 			}
 
 			// Set to
 			switch (to){
 			case CREATIVE:
 				plugin.getInventoryManager().getCreativeInventory(player, player.getWorld()).setTo(player);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().getSurvivalInventory(player, player.getWorld()).setTo(player);
 				break;
 			}
 
 			// For alerts
 			ignore = false;
 		}
 
 		// Alerts
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " changed to Game Mode " + ChatColor.YELLOW + to.name();
 		String playerMessage = ignore ? "no message" : "Your inventory has been changed to " + ChatColor.YELLOW + to.name();
 		plugin.getAlerts().alert(message, player, playerMessage, AlertType.GENERAL, AlertTrigger.GENERAL);
 	}
 
 	// ################# Player Combat
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onCombat(EntityDamageByEntityEvent event){
 		DamageCause cause = event.getCause();
 		Entity attacker = event.getDamager();
 		Entity target = event.getEntity();
 		AlertType type = AlertType.ILLEGAL;
 		boolean playerCombat = false;
 		Player playerAttacker = null;
 
 		// Check case
 		switch (cause){
 		case ENTITY_ATTACK:
 			// attacker = entity
 			if(attacker instanceof Player){
 				playerAttacker = (Player) attacker;
 			}else{
 				return;
 			}
 			break;
 		case PROJECTILE:
 			// attacker = Projectile
 			Projectile projectile = (Projectile) attacker;
 			LivingEntity shooter = projectile.getShooter();
 			if(shooter instanceof Player){
 				playerAttacker = (Player) shooter;
 			}else{
 				return;
 			}
 			break;
 		default:
 			return;
 		}
 
 		// Determine if we are hitting a mob or not, and whether it is legal
 		if(target instanceof Player){
 			// target = Player
 			playerCombat = true;
 			if(!plugin.isBlocked(playerAttacker, PermissionNodes.ALLOW_COMBAT_PLAYERS, playerAttacker.getWorld())){
 				type = AlertType.LEGAL;
 			}
 		}else{
 			// target = other entity
 			if(!plugin.isBlocked(playerAttacker, PermissionNodes.ALLOW_COMBAT_MOBS, playerAttacker.getWorld())){
 				type = AlertType.LEGAL;
 			}
 		}
 
 		// Check if we need to continue based on settings
 		if(playerCombat){
 			if(!plugin.getConfig().getBoolean("blocked-actions.combat-against-players")){
 				return;
 			}
 		}else{
 			if(!plugin.getConfig().getBoolean("blocked-actions.combat-against-mobs")){
 				return;
 			}
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 
 		// Alert
 		String message = "no message";
 		String playerMessage = "no message";
 		AlertTrigger trigger = AlertTrigger.HIT_MOB;
 		if(playerCombat){
 			String playerName = ((Player) target).getName();
 			message = ChatColor.YELLOW + playerAttacker.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to hit " + ChatColor.RED : " hit " + ChatColor.GREEN) + playerName;
 			playerMessage = plugin.getMessage("blocked-action.hit-player");
 			trigger = AlertTrigger.HIT_PLAYER;
 		}else{
 			String targetName = target.getClass().getName().replace("Craft", "").replace("org.bukkit.craftbukkit.entity.", "").trim();
 			message = ChatColor.YELLOW + playerAttacker.getName() + ChatColor.WHITE + (type == AlertType.ILLEGAL ? " tried to hit a " + ChatColor.RED : " hit a " + ChatColor.GREEN) + targetName;
 			playerMessage = plugin.getMessage("blocked-action.hit-mob");
 		}
 		plugin.getAlerts().alert(message, playerAttacker, playerMessage, type, trigger);
 	}
 
 	// ################# Entity Target
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onEntityTarget(EntityTargetEvent event){
 		Entity target = event.getTarget();
 		Player playerTarget = null;
 		AlertType type = AlertType.ILLEGAL;
 
 		// Check target
 		if(target instanceof Player){
 			playerTarget = (Player) target;
 		}else{
 			return;
 		}
 
 		// Check permissions
 		if(!plugin.isBlocked(playerTarget, PermissionNodes.ALLOW_COMBAT_MOBS, playerTarget.getWorld())){
 			type = AlertType.LEGAL;
 		}
 
 		// Handle event
 		if(type == AlertType.ILLEGAL){
 			event.setCancelled(true);
 		}
 	}
 
 	// ################# Piston Move (Extend)
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPistonExtend(BlockPistonExtendEvent event){
 		for(Block block : event.getBlocks()){
 			// Check for block type
 			GameMode type = plugin.getBlockManager().getType(block);
 
 			// Sanity
 			if(type == null){
 				continue;
 			}
 
 			// Setup
 			Location oldLocation = block.getLocation();
 			Location newLocation = block.getRelative(event.getDirection()).getLocation();
 
 			// Move
 			plugin.getBlockManager().moveBlock(oldLocation, newLocation);
 		}
 	}
 
 	// ################# Piston Move (Retract)
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onPistonRetract(BlockPistonRetractEvent event){
 		if(!event.isSticky()){ // Only handle moving blocks
 			return;
 		}
 		Block block = event.getBlock().getRelative(event.getDirection()).getRelative(event.getDirection());
 
 		// Check for block type
 		GameMode type = plugin.getBlockManager().getType(block);
 
 		// Sanity
 		if(type == null){
 			return;
 		}
 
 		// Setup
 		Location oldLocation = block.getLocation();
 		Location newLocation = block.getRelative(event.getDirection().getOppositeFace()).getLocation();
 
 		// Move
 		plugin.getBlockManager().moveBlock(oldLocation, newLocation);
 	}
 
 	// ################# Player Join
 
 	@EventHandler (ignoreCancelled = true)
 	public void onJoin(PlayerJoinEvent event){
 		Player player = event.getPlayer();
 
 		// Tell the inventory manager to prepare this player
 		plugin.getInventoryManager().loadPlayer(player);
 
 		// Check region
 		ASRegion region = plugin.getRegionManager().getRegion(player.getLocation());
 		if(region != null){
 			region.alertSilentEntry(player); // Sets inventory and Game Mode
 			// This must be done because when the inventory manager releases
 			// a player it resets the inventory to "non-temp"
 		}
 	}
 
 	// ################# Player Quit
 
 	@EventHandler (ignoreCancelled = true)
 	public void onQuit(PlayerQuitEvent event){
 		Player player = event.getPlayer();
 
 		// Tell the inventory manager to release this player
 		plugin.getInventoryManager().releasePlayer(player);
 	}
 
 	// ################# Player Kicked
 
 	@EventHandler (ignoreCancelled = true)
 	public void onKick(PlayerKickEvent event){
 		Player player = event.getPlayer();
 
 		// Tell the inventory manager to release this player
 		plugin.getInventoryManager().releasePlayer(player);
 	}
 
 	// ################# Player World Change
 
 	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onWorldChange(PlayerChangedWorldEvent event){
 		Player player = event.getPlayer();
 		World to = player.getWorld();
 		World from = event.getFrom();
 		boolean ignore = true;
 
 		// Check to see if we should even bother checking
 		if(!plugin.getConfig().getBoolean("handled-actions.world-transfers")){
 			return;
 		}
 
 		// Check temp
 		if(plugin.getInventoryManager().isInTemporary(player)){
 			plugin.getInventoryManager().removeFromTemporary(player);
 		}
 
 		// Inventory check
 		if(!plugin.getPermissions().has(player, PermissionNodes.NO_SWAP)){
 			// Save from
 			switch (player.getGameMode()){
 			case CREATIVE:
 				plugin.getInventoryManager().saveCreativeInventory(player, from);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().saveSurvivalInventory(player, from);
 				break;
 			}
 
 			// Set to
 			switch (player.getGameMode()){
 			case CREATIVE:
 				plugin.getInventoryManager().getCreativeInventory(player, to).setTo(player);
 				break;
 			case SURVIVAL:
 				plugin.getInventoryManager().getSurvivalInventory(player, to).setTo(player);
 				break;
 			}
 
 			// For alerts
 			ignore = false;
 		}
 
 		// Alerts
 		String message = ChatColor.YELLOW + player.getName() + ChatColor.WHITE + " changed to world " + ChatColor.YELLOW + to.getName();
 		String playerMessage = ignore ? "no message" : "Your inventory has been changed to " + ChatColor.YELLOW + to.getName();
 		plugin.getAlerts().alert(message, player, playerMessage, AlertType.GENERAL, AlertTrigger.GENERAL);
 	}
 
 }
