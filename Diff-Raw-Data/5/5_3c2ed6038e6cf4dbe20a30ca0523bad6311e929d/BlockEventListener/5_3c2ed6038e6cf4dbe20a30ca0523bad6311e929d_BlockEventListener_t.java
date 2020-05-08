 package com.araeosia.ArcherGames.listeners;
 
 import com.araeosia.ArcherGames.ArcherGames;
 import com.araeosia.ArcherGames.ScheduledTasks;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class BlockEventListener implements Listener {
 
 	public ArcherGames plugin;
 
 	public BlockEventListener(ArcherGames plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onBlockBreak(final BlockBreakEvent event) {
 		if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.blockedit")) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 		}
 	}
 
 	@EventHandler
 	public void onBlockBurn(final BlockBurnEvent event) {
 		if (ScheduledTasks.gameStatus == 1) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockPlace(final BlockPlaceEvent event) {
 		if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.blockedit")) {
 			event.setCancelled(true);
 			event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 		}
 	}
 
 	@EventHandler
 	public void onBlockIgnite(final BlockIgniteEvent event) {
 		if (event.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)) {
 			if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.blockedit")) { // ERROR!
 				event.setCancelled(true);
 				event.getPlayer().sendMessage(plugin.strings.get("noblockediting"));
 			}
 		}
 	}
 
 	@EventHandler
 	public void onInventoryOpen(final InventoryOpenEvent event) {
 		if (ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.invedit")) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
 	public void onBlockInteract(final PlayerInteractEvent event) {
 		if (event.hasBlock()) {
 			if(plugin.debug){
 				if(event.getClickedBlock().getState() instanceof Sign){
 					Sign sign = (Sign) event.getClickedBlock().getState();
 					plugin.log.info(sign.getLine(0));
 				}
 			}
 			if(event.getClickedBlock() instanceof Chest){
 				if(ScheduledTasks.gameStatus == 1 && !event.getPlayer().hasPermission("archergames.overrides.invedit")){
 					event.setCancelled(true);
 				}
 			}
 			if (event.getClickedBlock().getState() instanceof Sign) {
 				Sign sign = (Sign) event.getClickedBlock().getState();
				if (sign.getLine(0).equals("§1[Enchant]")) {
 					event.setCancelled(true);
 					// Line 2: Any, Line 3: Enchantment:Level Line 4: Price
 					double price = new Double(sign.getLine(4).substring(1));
 					if (plugin.econ.hasBalance(event.getPlayer().getName(), price)) {
 						String[] data = sign.getLine(3).split(":");
 						Enchantment enchantment = Enchantment.getByName(data[0]);
 						ItemStack itemInHand = event.getPlayer().getInventory().getItemInHand();
 						Boolean isOkay = true;
 						for (Enchantment e : itemInHand.getEnchantments().keySet()) {
 							if (e.conflictsWith(enchantment) || e.equals(enchantment)) {
 								isOkay = false;
 								event.getPlayer().sendMessage(plugin.strings.get("enchantmentconflict"));
 							}
 						}
 						if (enchantment.canEnchantItem(event.getPlayer().getInventory().getItemInHand()) && isOkay) {
 							ItemStack newItemStack = itemInHand;
 							newItemStack.addEnchantment(enchantment, Integer.parseInt(data[1]));
 							event.getPlayer().getInventory().setItemInHand(newItemStack);
 						}
 						plugin.econ.takePlayer(event.getPlayer().getName(), price);
 					} else {
 						event.getPlayer().sendMessage(plugin.strings.get("notenoughmoney"));
 					}
				} else if (sign.getLine(0).equals("§1[Buy]")) {
 					event.setCancelled(true);
 					// Line 2: Quantity, Line 3: Item name, Line 4: Price
 					double price = new Double(sign.getLine(4).substring(1));
 					if (plugin.econ.hasBalance(event.getPlayer().getName(), price)) {
 						String[] data = sign.getLine(3).split(":");
 						ItemStack itemToGive = new ItemStack(Material.getMaterial(data[0]), Integer.parseInt(data[1]));
 						event.getPlayer().getInventory().addItem(itemToGive);
 						plugin.econ.takePlayer(event.getPlayer().getName(), price);
 					} else {
 						event.getPlayer().sendMessage(plugin.strings.get("notenoughmoney"));
 					}
 				}
 			}
 		}
 	}
 }
