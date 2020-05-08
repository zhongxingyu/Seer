 package com.selfequalsthis.grubsplugin.modules.gametweaks;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
 
 import com.selfequalsthis.grubsplugin.GrubsMessager;
 
 public class GameTweaksEventListeners implements Listener {
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockDamage(BlockDamageEvent event) {
 		Player player = event.getPlayer();
 		
 		// obsidian is protected
 		if (event.getBlock().getType() == Material.OBSIDIAN) {
 			// if not an op, don't let them even break it
 			if (!player.isOp()) {
 				event.setCancelled(true);
 				return;
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockIgnite(BlockIgniteEvent event) {
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onBlockBurn(BlockBurnEvent event)  {
 		event.setCancelled(true);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onEntityDamage(EntityDamageEvent event) {
 
 		if (event.getEntity() instanceof Player) {
 			Player source = (Player) event.getEntity();
 
 			if (event.getCause() == DamageCause.DROWNING) {
				ItemStack helmet = source.getInventory().getHelmet();
				if (helmet != null) {
					if (helmet.getType() == Material.GOLD_HELMET) {
						event.setCancelled(true);
					}
 				}
 			}
 			
 			if (event.getCause() == DamageCause.FALL) {
 				event.setCancelled(true);
 			}
 			
 			if (event.getCause() == DamageCause.SUFFOCATION) {
 				event.setCancelled(true);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerJoin(PlayerJoinEvent event) {
 		Player p = event.getPlayer();
 		World w = p.getWorld();
 		GrubsMessager.sendMessage(p, GrubsMessager.MessageLevel.PLAIN, "Welcome, " + p.getDisplayName() + "!");
 		GrubsMessager.sendMessage(p, GrubsMessager.MessageLevel.INFO, "Current game time is: " + w.getTime());
 		
 		if (w.getPlayers().size() > 0) {
 			String playerListStr = "";
 			boolean useSeparator = false;
 			
 			for (Player player : w.getPlayers()) {
 				if (useSeparator) {
 					playerListStr += ", ";
 				}
 				else {
 					useSeparator = true;
 				}
 				playerListStr += player.getDisplayName();
 			}
 			
 			GrubsMessager.sendMessage(p, GrubsMessager.MessageLevel.INQUIRY, "Currently playing: " + playerListStr);
 		}
 		else {
 			GrubsMessager.sendMessage(p, GrubsMessager.MessageLevel.INQUIRY, "No other players currently here.");
 		}
 	}
 }
