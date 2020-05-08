 package com.github.catageek.BCProtect.Listeners;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import com.github.catageek.BCProtect.BCProtect;
 import com.github.catageek.BCProtect.Util;
 
 public final class CanBuildListener implements Listener {
 
 	@EventHandler (ignoreCancelled = true)
 	public void onBlockPlace(BlockPlaceEvent event) {
		if (! Util.checkPermission(event.getPlayer(), event.getBlock().getLocation(BCProtect.location), "canBuild")) {
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler (ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent event) {
		if (! Util.checkPermission(event.getPlayer(), event.getBlock().getLocation(BCProtect.location), "canBuild")) {
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler (ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent event) {
 
 		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
 			Material m = event.getClickedBlock().getType();
 
 			switch (m) {
 			case LEVER:
 			case WOOD_BUTTON:
 			case STONE_BUTTON:
 			case DIODE:
 			case REDSTONE_COMPARATOR:
 				if (! Util.checkPermission((Player)event.getPlayer(),
 						event.getClickedBlock().getLocation(BCProtect.location),
						"canBuild")) {
 					event.setCancelled(true);
 				}
 			default:
 				break;
 			}
 		}
 	}
 
 	
 }
