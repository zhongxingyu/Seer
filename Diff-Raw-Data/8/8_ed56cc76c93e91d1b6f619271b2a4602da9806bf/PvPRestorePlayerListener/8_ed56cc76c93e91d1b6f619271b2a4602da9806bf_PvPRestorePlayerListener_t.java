 package com.github.triarry.PvPRestore;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class PvPRestorePlayerListener implements Listener {
 	
 	private PvPRestore plugin;
 	 
 	public PvPRestorePlayerListener(PvPRestore plugin) { 
 		this.plugin = plugin;
 	}
 	
 	public HashMap<Player , ItemStack[]> items = new HashMap<Player , ItemStack[]>();
 	public HashMap<Player , ItemStack[]> armor = new HashMap<Player , ItemStack[]>();
 
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent event) {
 		Player player = null;
 
 		if (event.getEntity() instanceof Player) {
 			player = event.getEntity();
 		}
 
 		Player killer = player.getKiller();
 		if (killer != null) {
			if (player.hasPermission("pvprestore.keep") && plugin.getConfig().getBoolean("keep-inventory") == true && plugin.getConfig().getBoolean("keep-xp") == true) {
 				event.setKeepLevel(true);
 				player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory and XP have been saved.");
 				event.setDroppedExp(0);
 				if (plugin.getConfig().getBoolean("death-message") == true) {
 					event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.GREEN + ", and their XP and inventory was saved!");
 				}
 				ItemStack[] content = player.getInventory().getContents();
 		        ItemStack[] content_armor = player.getInventory().getArmorContents();
 		        armor.put(player, content_armor);
 		        items.put(player, content);
 		        player.getInventory().clear();
 		        event.getDrops().clear();				
 			}
			else if ((player.hasPermission("pvprestore.keep.xp") || player.hasPermission("pvprestore.keep")) && plugin.getConfig().getBoolean("keep-xp") == true) {
 				if (player.hasPermission("pvprestore.keep.inventory")) {
 					event.setKeepLevel(true);
 					player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory and XP have been saved.");
 					event.setDroppedExp(0);
 					if (plugin.getConfig().getBoolean("death-message") == true) {
 						event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.GREEN + ", and their XP and inventory was saved!");
 					}
 					ItemStack[] content = player.getInventory().getContents();
 			        ItemStack[] content_armor = player.getInventory().getArmorContents();
 			        armor.put(player, content_armor);
 			        items.put(player, content);
 			        player.getInventory().clear();
 			        event.getDrops().clear();
 				}
 				else {
 					event.setKeepLevel(true);
 					player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your XP has been saved.");
 					if (plugin.getConfig().getBoolean("death-message") == true) {
 						event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.GREEN + ", and their XP was saved!");
 					}
 					event.setDroppedExp(0);
 				}
 			}
			else if ((player.hasPermission("pvprestore.keep.inventory") || player.hasPermission("pvprestore.keep")) && plugin.getConfig().getBoolean("keep-inventory") == true) {
 				if (player.hasPermission("pvprestore.keep.xp")) {
 					event.setKeepLevel(true);
 					player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory and XP have been saved.");
 					event.setDroppedExp(0);
 					if (plugin.getConfig().getBoolean("death-message") == true) {
 						event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.GREEN + ", and their XP and inventory was saved!");
 					}
 					ItemStack[] content = player.getInventory().getContents();
 			        ItemStack[] content_armor = player.getInventory().getArmorContents();
 			        armor.put(player, content_armor);
 			        items.put(player, content);
 			        player.getInventory().clear();
 			        event.getDrops().clear();
 				}
 				else {
 					player.sendMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.GREEN  + "Your death was player related, so your inventory has been saved.");
 					if (plugin.getConfig().getBoolean("death-message") == true) {
 						event.setDeathMessage(ChatColor.YELLOW + "[PVP_Restore] " + ChatColor.RED + player.getName() + ChatColor.GREEN + " was killed by " + ChatColor.RED + killer.getName() + ChatColor.GREEN + ", and their inventory was saved!");
 					}
 					ItemStack[] content = player.getInventory().getContents();
 			        ItemStack[] content_armor = player.getInventory().getArmorContents();
 			        armor.put(player, content_armor);
 			        items.put(player, content);
 			        player.getInventory().clear();
 			        event.getDrops().clear();	
 				}
 			}
 			else {
 				player.sendMessage(ChatColor.RED + "Your death was not player related, so your inventory and XP have dropped where you died.");
 			}
 		} else {
 			player.sendMessage(ChatColor.RED + "Your death was not player related, so your inventory and XP have dropped where you died.");
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerRespawn(PlayerRespawnEvent event) {
         if(items.containsKey(event.getPlayer())){
             event.getPlayer().getInventory().clear();
             event.getPlayer().getInventory().setContents(items.get(event.getPlayer()));
             items.remove(event.getPlayer());
         }
         if(armor.containsKey(event.getPlayer()) && armor.size() != 0) {
             event.getPlayer().getInventory().setArmorContents(armor.get(event.getPlayer()));
             armor.remove(event.getPlayer());
         }
 	}
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (event.getPlayer().isDead()) {
 	        if(items.containsKey(event.getPlayer())){
 	            event.getPlayer().getInventory().clear();
 	            event.getPlayer().getInventory().setContents(items.get(event.getPlayer()));
 	            items.remove(event.getPlayer());
 	        }
 	        if(armor.containsKey(event.getPlayer()) && armor.size() != 0) {
 	            event.getPlayer().getInventory().setArmorContents(armor.get(event.getPlayer()));
 	            armor.remove(event.getPlayer());
 	        }
 		}
 	}
 	public void onPlayerKick(PlayerKickEvent event) {
 		if (event.getPlayer().isDead()) {
 	        if(items.containsKey(event.getPlayer())){
 	            event.getPlayer().getInventory().clear();
 	            event.getPlayer().getInventory().setContents(items.get(event.getPlayer()));
 	            items.remove(event.getPlayer());
 	        }
 	        if(armor.containsKey(event.getPlayer()) && armor.size() != 0) {
 	            event.getPlayer().getInventory().setArmorContents(armor.get(event.getPlayer()));
 	            armor.remove(event.getPlayer());
 	        }
 		}
 	}
 }
