 package com.bugfullabs.depochests;
 
import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 
 
 public class PlayerListener implements Listener{
 	
 	public static DepoChests plugin;
 	
 	public PlayerListener(DepoChests p){
 		plugin = p;
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 	}
 	
 	@EventHandler
 	public void onUserLogin(PlayerLoginEvent event){
 		
 		Inventory inv  = FileAPI.loadInventory(event.getPlayer(), plugin.getDataFolder(), plugin);
 		
 		if(inv != null){
 			plugin.log.info(event.getPlayer().getName() + " - DepoChest exists");
 			plugin.ChestsInv.put(event.getPlayer().getName(), inv);
 		}else{
			inv = plugin.getServer().createInventory(null, 54, "DepoChest");
 			plugin.ChestsInv.put(event.getPlayer().getName(), inv);
 			
 			FileAPI.saveInventory(event.getPlayer(), inv, plugin.getDataFolder());
 			
 			plugin.log.info(event.getPlayer().getName() + " - DepoChest created");	
 		}
 		
 	}
 	
 	
 	@EventHandler
 	public void onUserLeave(PlayerQuitEvent event){
 		
 		FileAPI.saveInventory(event.getPlayer(), plugin.ChestsInv.get(event.getPlayer().getName()), plugin.getDataFolder());
 		
 	}
 	
 
 	@EventHandler
 	public void onItemUsed(PlayerInteractEvent event){
 		
		if(event == null || event.getClickedBlock() == null)
 			return;
 		
 		if(event.getClickedBlock().getType().equals(Material.CHEST)){
 			
			
			
 			if(plugin.Chests.contains(event.getClickedBlock().getLocation())){
			
				event.setCancelled(true);
				
				if(event.getAction() == Action.LEFT_CLICK_BLOCK){
				event.getPlayer().sendMessage(ChatColor.GOLD + "[" + DepoChests.PLUGIN_NAME  + "]" + ChatColor.WHITE + "You can not destroy DepoChest!");
				}else{	
 			Inventory pInv = plugin.ChestsInv.get(event.getPlayer().getName());
 			event.getPlayer().openInventory(pInv);	
 			}
			}
 		}
 	}
 	
 	
 }
