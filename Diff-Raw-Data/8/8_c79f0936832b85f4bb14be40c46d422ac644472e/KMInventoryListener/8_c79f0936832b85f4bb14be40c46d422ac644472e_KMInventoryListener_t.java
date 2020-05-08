 package com.mitsugaru.KarmicMarket.events;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.inventory.InventoryEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import com.mitsugaru.KarmicMarket.KarmicMarket;
 import com.mitsugaru.KarmicMarket.inventory.MarketInventoryHolder;
 import com.mitsugaru.KarmicMarket.inventory.Item;
 import com.mitsugaru.KarmicMarket.logic.EconomyLogic;
 import com.mitsugaru.KarmicMarket.tasks.Repopulate;
 
 public class KMInventoryListener implements Listener {
     private KarmicMarket plugin;
 
     public KMInventoryListener(KarmicMarket plugin) {
 	this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onInventoryOpen(InventoryOpenEvent event) {
 	if (event.isCancelled() || event.getPlayer() == null) {
 	    return;
 	}
 	final MarketInventoryHolder holder = instanceCheck(event);
 	if (holder != null) {
 	    holder.addViewer(event.getPlayer().getName());
 	}
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onInventoryClose(InventoryCloseEvent event) {
 	if (event.getPlayer() == null) {
 	    return;
 	}
 	final MarketInventoryHolder holder = instanceCheck(event);
 	if (holder != null) {
 	    holder.removeViewer(event.getPlayer().getName());
 	}
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onInventoryClick(InventoryClickEvent event) {
 	if (event.isCancelled()) {
 	    return;
 	} else if (!(event.getWhoClicked() instanceof Player)) {
 	    return;
 	}
 	final MarketInventoryHolder holder = instanceCheck(event);
 	if (holder == null) {
 	    return;
 	}
 	// Check if they are interacting with top or bottom inventory
 	boolean fromChest = false;
 	if (event.getRawSlot() < 54) {
 	    fromChest = true;
 	}
 	/**
 	 * Market logic
 	 */
 	// Handle shift click
 	if (event.isShiftClick()) {
 	    if (event.getCurrentItem().getType().equals(Material.AIR)) {
 		return;
 	    } else if (fromChest) {
 		buyItem(event, false);
 	    } else if (event.getInventory().firstEmpty() >= 0) {
 		sellItem(event, false);
 	    }
 	} else if (fromChest) {
 	    if (event.getCurrentItem().getType().equals(Material.AIR)) {
 		// TODO If they have an item in the cursor, attempt to sell?
 		event.setCancelled(true);
 	    } else if (event.isLeftClick()) {
 		buyItem(event, true);
 	    } else if (event.isRightClick()) {
 		sellItem(event, true);
 	    }
 	}
     }
 
     private void buyItem(InventoryClickEvent event, boolean toCursor) {
 	final MarketInventoryHolder holder = instanceCheck(event);
 	final Item product = new Item(event.getCurrentItem());
 	final Player player = (Player) event.getWhoClicked();
 	double price = -1;
 	try {
 	    price *= holder.getMarketInfo().getItems()
 		    .get(event.getCurrentItem()).getAmount();
 	} catch (NullPointerException npe) {
 	    player.sendMessage(ChatColor.RED + KarmicMarket.TAG
 		    + " Something went wrong...");
 	    event.setCancelled(true);
 	    return;
 	}
 	if (toCursor) {
 	    boolean deny = false, finish = false, addCursor = false, same = false;
 	    // Check cursor if its the same
 	    if (!event.getCursor().getType().equals(Material.AIR)) {
 		final Item cursor = new Item(event.getCursor());
 		if (product.areSame(cursor)) {
 		    addCursor = true;
 		    deny = true;
 		    finish = true;
 		    same = true;
 		}
 	    } else {
 		// let it go to their cursor
 		finish = true;
 		same = true;
 	    }
 	    if (same) {
 		// check if they can pay
 		if (!EconomyLogic.denyPay(player, price)) {
 		    // Pay for item
 		    EconomyLogic.pay(player, price);
 		    if (addCursor) {
 			if (event.getCursor().getAmount()
 				+ event.getCurrentItem().getAmount() > event
 				.getCurrentItem().getMaxStackSize()) {
 			    // FIXME
 			    // See if we can add the rest to their inventory
 			    // final int rest =
 			    // event.getCurrentItem().getAmount()
 			    // - (event.getCurrentItem().getMaxStackSize() -
 			    // event
 			    // .getCurrentItem().getAmount());
 			    // final ItemStack rem = (ItemStack) event
 			    // .getCurrentItem().clone();
 			    // rem.setAmount(rest);
 			    //
 			    // final HashMap<Integer, ItemStack> remaining =
 			    // player
 			    // .getInventory().addItem(rem);
 			    // if (!remaining.isEmpty()) {
 			    // // They do not have enough space in their
 			    // // inventory
 			    //
 			    // for (Map.Entry<Integer, ItemStack> entry :
 			    // remaining
 			    // .entrySet()) {
 			    // if (rem.getType() == entry.getValue()
 			    // .getType()) {
 			    // // remove what we did add
 			    // rem.setAmount(rem.getAmount()
 			    // - entry.getValue().getAmount());
 			    // player.getInventory().removeItem(rem);
 			    // }
 			    // }
 			    // } else {
 			    // // Set cursor to max stack
 			    // event.getCursor().setAmount(
 			    // event.getCursor().getMaxStackSize());
 			    // }
 			    deny = false;
 			    finish = false;
 			} else {
 			    // Add to cursor
 			    event.getCursor().setAmount(
 				    event.getCursor().getAmount()
 					    + event.getCurrentItem()
 						    .getAmount());
 			}
 		    }
 		    player.sendMessage(ChatColor.GREEN + KarmicMarket.TAG
 			    + " Bought " + ChatColor.AQUA
 			    + event.getCurrentItem().toString()
 			    + ChatColor.GREEN + " for " + ChatColor.GOLD
 			    + price * -1);
 		} else {
 		    // denied
 		    player.sendMessage(ChatColor.YELLOW + KarmicMarket.TAG
 			    + " Cannot pay " + ChatColor.GOLD + price * -1
 			    + ChatColor.YELLOW + " for " + ChatColor.AQUA
 			    + event.getCurrentItem().toString());
 		}
 	    }
 	    if (deny) {
 		event.setCancelled(true);
 	    } else {
 		// repopulate slot that they just took
 		final ItemStack restore = product.toItemStack();
 		restore.setAmount(event.getCurrentItem().getAmount());
 		if (!repopulate(holder.getInventory(), restore)) {
 		    // Something went wrong
 		    // notify
 		    plugin.getLogger().warning(
 			    "Could not schedule repopulate task on buyItem for "
 				    + player.getName() + " on item "
 				    + event.getCurrentItem().toString());
 		    player.sendMessage(ChatColor.RED + KarmicMarket.TAG
 			    + " Something went wrong...");
 		}
 	    }
 	    if (finish) {
 		return;
 	    }
 	    // Else, we handle it to go to their inventory
 	}
 	// check if they can pay
 	if (EconomyLogic.denyPay(player, price)) {
 	    // Cannot pay
 	    player.sendMessage(ChatColor.YELLOW + KarmicMarket.TAG
 		    + " Cannot pay " + ChatColor.GOLD + price * -1
 		    + ChatColor.YELLOW + " for " + ChatColor.AQUA
 		    + event.getCurrentItem().toString());
 	    event.setCancelled(true);
 	    return;
 	}
 
 	// Handle item going to inventory
 	final HashMap<Integer, ItemStack> remaining = player.getInventory()
 		.addItem(event.getCurrentItem());
 	if (remaining.isEmpty()) {
 	    // repopulate slot that they just took
 	    final ItemStack restore = product.toItemStack();
 	    restore.setAmount(event.getCurrentItem().getAmount());
 	    if (!repopulate(holder.getInventory(), restore)) {
 		// Something went wrong
 		// notify
 		plugin.getLogger().warning(
 			"Could not schedule repopulate task on buyItem for "
 				+ player.getName() + " on item "
 				+ event.getCurrentItem().toString());
 		player.sendMessage(ChatColor.RED + KarmicMarket.TAG
 			+ " Something went wrong...");
 	    }
 	    player.sendMessage(ChatColor.GREEN + KarmicMarket.TAG + " Bought "
 		    + ChatColor.AQUA + event.getCurrentItem().toString()
 		    + ChatColor.GREEN + " for " + ChatColor.GOLD + price * -1);
 	} else {
 
 	    // notify that they have no space
 	    player.sendMessage(ChatColor.YELLOW + KarmicMarket.TAG
 		    + " No space available to purchase item...");
 	    return;
 	}
 	if (toCursor) {
 	    // Cancel
 	    event.setCancelled(true);
 	}
 	// Pay for item
 	EconomyLogic.pay(player, price);
     }
 
     private void sellItem(InventoryClickEvent event, boolean fromCursor) {
 	final MarketInventoryHolder holder = instanceCheck(event);
 	final Item product = new Item(event.getCurrentItem());
 	final Player player = (Player) event.getWhoClicked();
 	double price = 1.0;
 	try {
 	    price *= holder.getMarketInfo().getItems()
 		    .get(event.getCurrentItem()).getAmount();
 	} catch (NullPointerException npe) {
 	    player.sendMessage(ChatColor.RED + KarmicMarket.TAG
 		    + " Something went wrong...");
 	    event.setCancelled(true);
 	    return;
 	}
 	if (fromCursor) {
 	    boolean same = false;
 	    // Check cursor if its the same
 	    if (!event.getCursor().getType().equals(Material.AIR)) {
 		final Item cursor = new Item(event.getCursor());
 		if (product.areSame(cursor)
 			&& event.getCurrentItem().getAmount() <= event
 				.getCursor().getAmount()) {
 		    same = true;
 		}
 	    }
 	    if (same) {
 		// Remove amount from cursor
 		final ItemStack cursor = event.getCursor();
 		cursor.setAmount(cursor.getAmount()
 			- event.getCurrentItem().getAmount());
 		event.setCursor(cursor);
 		// Pay for item
 		EconomyLogic.pay(player, price);
 		player.sendMessage(ChatColor.GREEN + KarmicMarket.TAG
 			+ " Sold " + ChatColor.AQUA
 			+ event.getCurrentItem().toString() + ChatColor.GREEN
 			+ " for " + ChatColor.GOLD + price);
 		event.setCancelled(true);
 		return;
 	    }
 	}
 	// Check if the item they are trying to sell is in their inventory.
 	if (player.getInventory().contains(event.getCurrentItem().getType(),
 		event.getCurrentItem().getAmount())) {
 	    player.getInventory().removeItem(event.getCurrentItem());
 	    // Pay for item
 	    EconomyLogic.pay(player, price);
 	    player.sendMessage(ChatColor.GREEN + KarmicMarket.TAG + " Sold "
 		    + ChatColor.AQUA + event.getCurrentItem().toString()
 		    + ChatColor.GREEN + " for " + ChatColor.GOLD + price);
 	} else {
 	    // They don't have the item that they are trying to sell
 	    player.sendMessage(ChatColor.YELLOW + KarmicMarket.TAG
 		    + " You do not have " + ChatColor.AQUA
 		    + event.getCurrentItem().toString() + ChatColor.YELLOW
 		    + " to sell");
 	}
 	event.setCancelled(true);
     }
 
     private boolean repopulate(Inventory inventory, ItemStack item) {
 	final Repopulate task = new Repopulate(inventory, item);
 	final int id = plugin.getServer().getScheduler()
 		.scheduleSyncDelayedTask(plugin, task, 1);
 	if (id == -1) {
 	    return false;
 	}
 	return true;
     }
 
     private MarketInventoryHolder instanceCheck(InventoryEvent event) {
 	MarketInventoryHolder holder = null;
 	try {
 	    if (event.getInventory().getHolder() != null) {
 		if (event.getInventory().getHolder() instanceof MarketInventoryHolder) {
 		    holder = (MarketInventoryHolder) event.getInventory()
 			    .getHolder();
 		}
 	    }
 	} catch (NullPointerException n) {
 	    // IGNORE
 	}
 	return holder;
     }
 }
