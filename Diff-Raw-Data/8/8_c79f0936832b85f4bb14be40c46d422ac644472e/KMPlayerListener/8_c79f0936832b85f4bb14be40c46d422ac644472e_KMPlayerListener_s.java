 package com.mitsugaru.KarmicMarket.events;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 
 import com.mitsugaru.KarmicMarket.KarmicMarket;
 import com.mitsugaru.KarmicMarket.config.MarketConfig;
 import com.mitsugaru.KarmicMarket.exceptions.MarketPackageNotFoundException;
 import com.mitsugaru.KarmicMarket.inventory.MarketInfo;
 import com.mitsugaru.KarmicMarket.inventory.MarketInventoryHolder;
 import com.mitsugaru.KarmicMarket.tasks.DelayInventoryOpen;
 
 public class KMPlayerListener implements Listener {
     private KarmicMarket plugin;
 
     public KMPlayerListener(KarmicMarket plugin) {
 	this.plugin = plugin;
     }
 
     public void onPlayerQuit(final PlayerQuitEvent event) {
 	// since event that doesn't throw an inventory close event
 	// Do holder removal logic just to double check
 	if (event.getPlayer() == null) {
 	    return;
 	}
 	// Iterate through all open markets and remove player from viewer set
 	for (MarketInventoryHolder holder : KarmicMarket.openMarkets.values()) {
 	    holder.removeViewer(event.getPlayer().getName());
 	}
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
 	// Grab type of click
 	boolean right = false, left = false;
 	if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 	    right = true;
 	} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
 	    left = true;
 	}
 	// Grab type of block
 	final Block block = event.getClickedBlock();
 	if (block == null) {
 	    return;
 	}
 	// Chest logic
 	if (block.getType().equals(Material.CHEST)) {
 	    if (block.getRelative(BlockFace.UP).getType() == Material.WALL_SIGN) {
 		Sign sign = (Sign) block.getRelative(BlockFace.UP).getState();
 		if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
 			KarmicMarket.TAG)) {
 		    // Assume activated
 		    // TODO check if they have permission
 		    if (left) {
 			// cycle
 			cycleMarketPackage(sign, true);
 		    } else if (right) {
 			// Stop them from opening the chest since we have
 			// our
 			// own inventory to show
 			event.setCancelled(true);
 			// Show inventory
 			showMarketInventory(event.getPlayer(), sign);
 		    }
 		}
 	    }
 	} else if (block.getType().equals(Material.WALL_SIGN)
 		|| block.getType().equals(Material.SIGN)
 		|| block.getType().equals(Material.SIGN_POST)) {
 	    final Sign sign = (Sign) block.getState();
 	    if (ChatColor.stripColor(sign.getLine(1)).equalsIgnoreCase(
 		    KarmicMarket.TAG)) {
 		// Check if its activated via the chat color
 		if (signIsActivated(sign)) {
 		    // TODO check if they have permission
 		    // Show inventory IF chests are disabled
 		    if (right) {
 			if (plugin.getRootConfig().needsChest) {
 			    // Cycle
 			    cycleMarketPackage(sign, false);
 			} else {
 			    showMarketInventory(event.getPlayer(), sign);
 			    event.setCancelled(true);
 			}
 		    } else {
 			// cycle
 			cycleMarketPackage(sign, true);
 		    }
 		} else {
 		    // IGNORE
 		    event.setCancelled(true);
 		}
 	    }
 	}
     }
 
     private void cycleMarketPackage(final Sign sign, final boolean backward) {
 	// Grab market name
 	final String marketName = ChatColor.stripColor(sign.getLine(0));
 	// Grab package name
 	final String packageName = ChatColor.stripColor(sign.getLine(3));
 	// Grab config
 	final MarketConfig marketConfig = plugin.getRootConfig()
 		.getMarketConfig(marketName);
 	if (marketConfig == null) {
 	    return;
 	}
 	// Check if the market has no packages
 	if (marketConfig.isEmpty()) {
 	    sign.setLine(3, "");
 	    sign.update();
 	} else {
 	    // Grab list
 	    final String[] packageList = marketConfig.getPackageSet().toArray(
 		    new String[0]);
 	    // Find current position. If the package isn't found, we'll default
 	    // to
 	    // the first in the list;
 	    int index = 0;
 	    for (int i = 0; i < packageList.length; i++) {
 		if (packageList[i].equals(packageName)) {
 		    index = i;
 		    break;
 		}
 	    }
 	    // Increment or decrement
 	    if (backward) {
 		index--;
 		// If negative, loop to end of list
 		if (index < 0) {
 		    index = packageList.length - 1;
 		}
 	    } else {
 		index++;
 		// If past array length, loop to beginning
 		if (index >= packageList.length) {
 		    index = 0;
 		}
 	    }
 	    // Set and update sign
 	    sign.setLine(3, packageList[index]);
 	    sign.update();
 	}
     }
 
     private void showMarketInventory(final Player player, final Sign sign) {
 	// Grab market name
 	final String marketName = ChatColor.stripColor(sign.getLine(0));
 	// Grab package name
 	final String packageName = ChatColor.stripColor(sign.getLine(3));
 	// Ignore if there is no package defined
 	if (packageName.equals("")) {
 	    // nofity player
 	    player.sendMessage(ChatColor.YELLOW + KarmicMarket.TAG
 		    + " No packages for this market...");
 	    return;
 	}
 	// Generate market info object
 	MarketInfo market;
 	try {
 	    market = new MarketInfo(marketName, packageName);
 	} catch (MarketPackageNotFoundException e) {
 	    player.sendMessage(ChatColor.RED + KarmicMarket.TAG + " Package '"
		    + ChatColor.GOLD + "' not found!");
 	    return;
 	}
 	// See if the market inventory is already open
 	Inventory inventory = null;
 	if (KarmicMarket.openMarkets.containsKey(market)) {
 	    // Show the existing inventory to that player
 	    inventory = KarmicMarket.openMarkets.get(market).getInventory();
 	} else {
 	    // Generate new inventory to show
 	    final MarketInventoryHolder holder = new MarketInventoryHolder(
 		    market);
 	    holder.setInventory(plugin.getServer().createInventory(holder, 54,
 		    marketName + " - " + packageName));
 	    inventory = holder.getInventory();
 	}
 	final int id = plugin
 		.getServer()
 		.getScheduler()
 		.scheduleSyncDelayedTask(plugin,
 			new DelayInventoryOpen(plugin, player, inventory), 3);
 	if (id == -1) {
 	    plugin.getLogger().warning("Could not open market inventory!");
 	    player.sendMessage(ChatColor.RED + KarmicMarket.TAG
 		    + " Could not open market inventory!");
 	}
     }
 
     private boolean signIsActivated(final Sign sign) {
 	if (plugin.getRootConfig().needsChest) {
 	    if (!sign.getBlock().getRelative(BlockFace.DOWN).getType()
 		    .equals(Material.CHEST)) {
 		return false;
 	    }
 	}
 	return true;
     }
 }
