 package com.newliberty.enderchests;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
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
 	EnderChests plugin;
 	Random random = new Random();
 	HashMap<Integer, Location> chestDupe = new HashMap<Integer, Location>();
 	int chestSize = 36;
 	PlayerListener(EnderChests instance){
 		plugin = instance;
 	}
 	
 	@SuppressWarnings("unused")
 	@EventHandler
 	public void onBlockPlace(BlockPlaceEvent e){
 		if (e.getBlock().getType() == Material.ENDER_CHEST){
 			plugin.loadPlayers();
 			if (plugin.playerChestCount.get(e.getPlayer().getDisplayName()) != null && plugin.playerChestCount.get(e.getPlayer().getDisplayName()) == getPermission(e.getPlayer())){
 				e.getPlayer().sendMessage(ChatColor.BLUE + "You have placed your maximum number of Protected Ender Chests");
 				e.setCancelled(true);
 			} else if (plugin.playerChestCount.get(e.getPlayer().getDisplayName()) != null && plugin.playerChestCount.get(e.getPlayer().getDisplayName()) < getPermission(e.getPlayer()) && com.massivecraft.factions.P.p.isPlayerAllowedToBuildHere(e.getPlayer(), e.getBlock().getLocation()) && plugin.getWorldGuard().canBuild(e.getPlayer(), e.getBlock())){
 				Inventory inventory = Bukkit.createInventory(e.getPlayer(), chestSize, "ProtectedEnderChest");
 				 plugin.saveEnderChest(e.getBlock().getLocation(), inventory, e.getPlayer());
 				 InventoryView view = e.getPlayer().openInventory(inventory);
 				 //int temp = plugin.playerChestCount.get(e.getPlayer().getDisplayName());
 				 //plugin.playerChestCount.remove(e.getPlayer().getDisplayName());
 				 plugin.playerChestCount.put(e.getPlayer().getDisplayName(), plugin.playerChestCount.get(e.getPlayer().getDisplayName())+1);
 				 e.getPlayer().sendMessage(ChatColor.BLUE + "You have placed " + (plugin.playerChestCount.get(e.getPlayer().getDisplayName())) + "/" + getPermission(e.getPlayer()) + " EnderChests");
 				 
 				 plugin.dumpPlayers();
 				 return;
 			 } else if (plugin.playerChestCount.get(e.getPlayer().getDisplayName()) == null && getPermission(e.getPlayer()) != -1 && com.massivecraft.factions.P.p.isPlayerAllowedToBuildHere(e.getPlayer(), e.getBlock().getLocation()) && plugin.getWorldGuard().canBuild(e.getPlayer(), e.getBlock())){
 				 Inventory inventory = Bukkit.createInventory(e.getPlayer(), chestSize, "ProtectedEnderChest");
 				 plugin.saveEnderChest(e.getBlock().getLocation(), inventory, e.getPlayer());
 				 InventoryView view = e.getPlayer().openInventory(inventory);
 				 plugin.playerChestCount.put(e.getPlayer().getDisplayName(), 1);
 				 e.getPlayer().sendMessage(ChatColor.BLUE + "You have placed 1/" + getPermission(e.getPlayer()) + " EnderChests");
 				 
 				 plugin.dumpPlayers();
 				 return;
 			 } else if (getPermission(e.getPlayer()) == -1){
 				 e.getPlayer().sendMessage(ChatColor.BLUE + "You cannot place Ender Chests!");
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
 				openPack(event.getPlayer(), chestSize, block);
 				event.setCancelled(true);
 				if (event.getPlayer().hasPermission("nlenderchest.admin")){
 					event.getPlayer().sendMessage("This chest belongs to: " + plugin.getOwnerName(block.getLocation(), event.getPlayer()));
 				}
 				return;
 			}
 			if (plugin.getOwnerName(block.getLocation(), event.getPlayer()) == null){
 				event.getPlayer().sendMessage(ChatColor.BLUE + "You cannot use this EnderChest, it belongs to no one");
 				event.setCancelled(true);
 				return;
 			}
 			event.getPlayer().sendMessage(ChatColor.BLUE + "You cannot use this EnderChest, it belongs to " + ChatColor.GOLD + plugin.getOwnerName(block.getLocation(), event.getPlayer()));
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
     public void onInventoryClose(InventoryCloseEvent event) {
         String title = event.getView().getTitle();
         if (title.startsWith("ProtectedEnderChest")) {
         	String a = title.replace("ProtectedEnderChest ", "");
         	int p = -1;
        	if (a.length() < 3) {
         		p = Integer.parseInt(a);	
         	}
             Inventory inventory = event.getInventory();
             Block b = event.getPlayer().getTargetBlock(null, 5);
             Location loc;
             if (p != -1){
             	loc = chestDupe.get(p);
             } else {
             	loc = b.getLocation();
             }
 
             plugin.saveEnderChest(loc, inventory, (Player)event.getPlayer());
         }
     }
 	
 	@EventHandler
 	public void onEnderChestBreak(BlockBreakEvent event){
 		Block block = event.getBlock();
 		if (block.getType() == Material.ENDER_CHEST){
 			plugin.loadPlayers();
 			if (plugin.getOwner(block.getLocation(), event.getPlayer()) || event.getPlayer().hasPermission("nlenderchest.admin")){
 				File packFile = plugin.getPackFile(block.getLocation());
 				if (packFile.exists()){
 			        FileConfiguration packConfig = YamlConfiguration.loadConfiguration(packFile);
 			        List<?> list = packConfig.getList("inventory");
 			        if (list != null) {
 			            for (int i = 0; i < Math.min(list.size(), chestSize); i += 1) {
 			                //inv.setItem(i, (ItemStack)list.get(i));
 			            	ItemStack a = (ItemStack)list.get(i);
 			                if (a != null){
 			                	event.getPlayer().sendMessage(ChatColor.BLUE + "You cannot break this chest while there are items in it");
 			                	event.setCancelled(true);
 			                	return;
 			                }
 			            }
 			            event.getPlayer().sendMessage(ChatColor.BLUE + "You have broken your protected enderchest");
 			            //int temp = plugin.playerChestCount.get(event.getPlayer().getDisplayName());
 			            //plugin.playerChestCount.remove(event.getPlayer().getDisplayName());
 			            if (plugin.playerChestCount.get(plugin.getOwnerName(block.getLocation(), event.getPlayer())) != null) plugin.playerChestCount.put(plugin.getOwnerName(block.getLocation(), event.getPlayer()), plugin.playerChestCount.get(plugin.getOwnerName(block.getLocation(), event.getPlayer()))-1);
 						File file = plugin.getPackFile(block.getLocation());
 						if (file.exists()) file.delete();
 						
 						plugin.dumpPlayers();
 						return;
 			        }
 				}
 			} else if (plugin.getOwnerName(block.getLocation(), event.getPlayer()) == null) {
 				return;
 			}
 			event.getPlayer().sendMessage(ChatColor.BLUE + "This is not your Protected EnderChest, it belongs to " + ChatColor.GOLD + plugin.getOwnerName(block.getLocation(), event.getPlayer()));
 			event.setCancelled(true);
 		}
 	}
 
     public void openPack(Player player, int numSlots, Block b) {
    	int randomChestNumber = random.nextInt(100);
         Inventory inventory = Bukkit.createInventory(player, numSlots, "ProtectedEnderChest " + randomChestNumber);
  
         inventory = plugin.loadEnderChest(b.getLocation(), inventory, player);
         
         chestDupe.put(randomChestNumber, b.getLocation());
 
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
