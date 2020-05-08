 package com.newliberty.enderchests;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerListener implements Listener{
 	static EnderChests plugin;
 	PlayerListener(EnderChests instance){
 		plugin = instance;
 	}
 	
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent e){
 		if (e.getBlock().getType() == Material.ENDER_CHEST){
 			if (plugin.playerChestCount.get(e.getPlayer().getDisplayName()) != null && plugin.playerChestCount.get(e.getPlayer().getDisplayName()) == getPermission(e.getPlayer())){
 				e.getPlayer().sendMessage(ChatColor.RED + "You have placed your maximum number of Protected Ender Chests");
 				e.setCancelled(true);
 			}
 			else if (plugin.playerChestCount.get(e.getPlayer().getDisplayName()) != null && plugin.playerChestCount.get(e.getPlayer().getDisplayName()) != getPermission(e.getPlayer())){
 				 Inventory inventory = Bukkit.createInventory(e.getPlayer(), 27, "ProtectedEnderChest");
 				 plugin.saveEnderChest(e.getBlock().getLocation(), inventory, e.getPlayer());
 				 InventoryView view = e.getPlayer().openInventory(inventory);
 				 //int temp = plugin.playerChestCount.get(e.getPlayer().getDisplayName());
 				 //plugin.playerChestCount.remove(e.getPlayer().getDisplayName());
 				 plugin.playerChestCount.put(e.getPlayer().getDisplayName(), plugin.playerChestCount.get(e.getPlayer().getDisplayName())+1);
 				 e.getPlayer().sendMessage(ChatColor.RED + "You have placed " + (plugin.playerChestCount.get(e.getPlayer().getDisplayName())) + "/" + getPermission(e.getPlayer()));
 				 return;
 			 } else if (plugin.playerChestCount.get(e.getPlayer().getDisplayName()) == null && getPermission(e.getPlayer()) != -1){
 				 Inventory inventory = Bukkit.createInventory(e.getPlayer(), 27, "ProtectedEnderChest");
 				 plugin.saveEnderChest(e.getBlock().getLocation(), inventory, e.getPlayer());
 				 InventoryView view = e.getPlayer().openInventory(inventory);
 				 plugin.playerChestCount.put(e.getPlayer().getDisplayName(), 1);
 				 e.getPlayer().sendMessage(ChatColor.RED + "You have placed 1/" + getPermission(e.getPlayer()));
 				 return;
 			 } else if (getPermission(e.getPlayer()) == -1){
 				 e.getPlayer().sendMessage(ChatColor.RED + "You cannot place Ender Chests!");
 				 e.setCancelled(true);
 			 }
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void playerInteractEvent(PlayerInteractEvent event){
 		if (Action.RIGHT_CLICK_BLOCK != event.getAction()){
 			return;
 		}
 		Block block = event.getClickedBlock();
 		if (block.getType() == Material.ENDER_CHEST){
 			if (plugin.getOwner(block.getLocation(), event.getPlayer())  || event.getPlayer().hasPermission("nlenderchest.admin")){
 				openPack(event.getPlayer(), 27, block);
 				event.setCancelled(true);
 				if (event.getPlayer().hasPermission("nlenderchest.admin")){
 					event.getPlayer().sendMessage("This chest belongs to: " + plugin.getOwnerName(block.getLocation(), event.getPlayer()));
 				}
 				return;
 			}
 			event.getPlayer().sendMessage(ChatColor.RED + "This is not your Protected EnderChest, it belongs to " + ChatColor.GREEN + plugin.getOwnerName(block.getLocation(), event.getPlayer()));
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
     public void onInventoryClose(InventoryCloseEvent event) {
         String title = event.getView().getTitle();
         if (title.startsWith("Protected")) {
             Inventory inventory = event.getInventory();
             Block b = event.getPlayer().getTargetBlock(null, 5);
             Location loc = b.getLocation();
 
             plugin.saveEnderChest(loc, inventory, (Player)event.getPlayer());
         }
     }
 	
 	@EventHandler
 	public void onEnderChestBreak(BlockBreakEvent event){
 		Block block = event.getBlock();
 		if (block.getType() == Material.ENDER_CHEST){
 			if (plugin.getOwner(block.getLocation(), event.getPlayer()) || event.getPlayer().hasPermission("nlenderchest.admin")){
 				File packFile = plugin.getPackFile(block.getLocation());
 				if (packFile.exists()){
 			        FileConfiguration packConfig = YamlConfiguration.loadConfiguration(packFile);
 			        List<?> list = packConfig.getList("inventory");
 			        if (list != null) {
 			            for (int i = 0; i < Math.min(list.size(), 27); i += 1) {
 			                //inv.setItem(i, (ItemStack)list.get(i));
 			            	ItemStack a = (ItemStack)list.get(i);
 			                if (a != null){
 			                	event.getPlayer().sendMessage(ChatColor.RED + "You cannot break this chest while there are items in it");
 			                	event.setCancelled(true);
 			                	return;
 			                }
 			            }
 			            event.getPlayer().sendMessage(ChatColor.RED + "You have broken your protected enderchest");
 			            //int temp = plugin.playerChestCount.get(event.getPlayer().getDisplayName());
 						 //plugin.playerChestCount.remove(event.getPlayer().getDisplayName());
 						 plugin.playerChestCount.put(event.getPlayer().getDisplayName(), plugin.playerChestCount.get(plugin.getOwnerName(block.getLocation(), event.getPlayer()))-1);
 						 File file = plugin.getPackFile(block.getLocation());
 						 file.delete();
 						 return;
 			        }
 				}
			} else if (plugin.getOwnerName(block.getLocation(), event.getPlayer()).equalsIgnoreCase("null")) {
				return;
 			}
 			event.getPlayer().sendMessage(ChatColor.RED + "This is not your Protected EnderChest, it belongs to " + ChatColor.GREEN + plugin.getOwnerName(block.getLocation(), event.getPlayer()));
 			event.setCancelled(true);
 		}
 	}
 
     public static void openPack(Player player, int numSlots, Block b) {
         Inventory inventory = Bukkit.createInventory(player, numSlots, "ProtectedEnderChest");
  
         inventory = plugin.loadEnderChest(b.getLocation(), inventory, player);
 
         InventoryView view = player.openInventory(inventory);
         // will be saved on close
     }
     
     public int getPermission(Player p){
     	for (int i = 100; i > 0; i--){
     		if (p.hasPermission("nlenderchest.place." + i)){
     			return i;
     		}
     	}
 		return -1;
     }
 }
