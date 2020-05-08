 package org.cubeville.itemdetector.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.cubeville.itemdetector.ItemDetector;
 
 public class DetectorListener implements Listener {
 	
 	private final ItemDetector plugin;
 	
 	public DetectorListener(ItemDetector plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void activatePressurePlate(PlayerInteractEvent event) {
 		if (event.getAction() != Action.PHYSICAL) {
 			return;
 		}
 		
 		Block block = event.getClickedBlock();
 		Player player = event.getPlayer();
 		
 		if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE) {
 			return;
 		}
 		
 		if (!plugin.isDetector(block)) {
 			return;
 		}
 		
 		boolean empty = true;
 		
 		for (ItemStack item : player.getInventory().getContents()) {
 			if (item != null) {
 				empty = false;
 				break;
 			}
 		}
 		
 		for (ItemStack item : player.getInventory().getArmorContents()) {
 			if (item.getType() != Material.AIR) {
 				empty = false;
 				break;
 			}
 		}
 		
 		if (empty) {
 			return;
 		}
 		
 		player.getInventory().setArmorContents(null);
 		player.getInventory().clear();
 		player.sendMessage(ChatColor.YELLOW + "Your inventory has been cleared!");
 	}
 	
 	@EventHandler
 	public void clickPressurePlate(PlayerInteractEvent event) {
 		if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
 			return;
 		}
 
 		Player player = event.getPlayer();
 		
 		if (plugin.getAction(player).isEmpty()) {
 			return;
 		}
 		
 		Block block = event.getClickedBlock();
 		
 		if (block.getType() != Material.STONE_PLATE && block.getType() != Material.WOOD_PLATE) {
 			return;
 		}
 		
 		if (plugin.getAction(player).equalsIgnoreCase("create")) {
 			if (plugin.isDetector(block)) {
 				player.sendMessage(ChatColor.RED + "This pressure plate already has a detector!");
 				return;
 			} else {
 				plugin.addDetector(player, block);
 				player.sendMessage(ChatColor.GREEN + "Detector plate created successfully.");
 			}
 			
			plugin.setAction(player, "");
 			event.setCancelled(true);
 		} else if (plugin.getAction(player).equalsIgnoreCase("remove")) {
 			if (plugin.isDetector(block)) {
 				if (plugin.getDetector(block).getOwner().equalsIgnoreCase(player.getName())) {
 					plugin.removeDetector(block);
 					player.sendMessage(ChatColor.GREEN + "Your detector has been removed.");
 				} else {
 					player.sendMessage(ChatColor.RED + "You don't own that detector!");
 				}
 			} else {
 				player.sendMessage(ChatColor.RED + "That pressure plate is not a detector.");
 			}
 			
			plugin.setAction(player, "");
 			event.setCancelled(true);
 		}
 	}
 
 }
