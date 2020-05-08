 package com.martinbrook.tesseractuhc.listeners;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityTargetEvent;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.event.vehicle.VehicleDamageEvent;
 import org.bukkit.inventory.DoubleChestInventory;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 import com.martinbrook.tesseractuhc.MatchPhase;
 import com.martinbrook.tesseractuhc.UhcMatch;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 
 public class SpectateListener implements Listener {
 	private UhcMatch m;
 	public SpectateListener(UhcMatch m) { this.m = m; }
 
 	@EventHandler
 	public void onInteractEntityEvent(PlayerInteractEntityEvent e) {
 		Player p = e.getPlayer();
 		UhcPlayer pl = m.getPlayer(p);
 		if (!pl.isSpectator()) return;
 
 		Entity clicked = e.getRightClicked();
 
 		if (clicked.getType() == EntityType.PLAYER) {
 			if (m.getPlayer((Player) clicked).isActiveParticipant())
 				pl.getSpectator().showInventory((Player) clicked);
 		}
 
 		if (m.getPlayer(e.getPlayer()).isNonInteractingSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onProjectileLaunch(ProjectileLaunchEvent e) {
 		Entity launcher = e.getEntity().getShooter();
 		if (launcher != null && launcher.getType() == EntityType.PLAYER) {
 			if (m.getPlayer((Player) launcher).isSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 		}
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent e) {
 		if (m.getPlayer(e.getPlayer()).isNonInteractingSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 	}        
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerBucketFill(PlayerBucketFillEvent e) {
 		if (m.getPlayer(e.getPlayer()).isNonInteractingSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
 		if (m.getPlayer(e.getPlayer()).isNonInteractingSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onPlayerDropItem(PlayerDropItemEvent e) {
 		if (m.getPlayer(e.getPlayer()).isNonInteractingSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onVehicleDamage(VehicleDamageEvent e) {
 		Entity attacker = e.getAttacker();
 		if (attacker != null && attacker.getType() == EntityType.PLAYER) {
 			if (m.getPlayer((Player) attacker).isNonInteractingSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 		}
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityTarget(EntityTargetEvent e) {
 		Entity target = e.getTarget();
 		if (target != null && target.getType() == EntityType.PLAYER) {
 			if (m.getPlayer((Player) target).isSpectator() && m.getMatchPhase() == MatchPhase.MATCH) e.setCancelled(true);
 		}
 	}
 	/**
 	 * Prevent ops from interacting with anything when match is in progress.
 	 * 
 	 */
	 @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
 	public void onPlayerInteract(PlayerInteractEvent e) {
 		 if (m.getPlayer(e.getPlayer()).isNonInteractingSpectator() && m.getMatchPhase() == MatchPhase.MATCH) {
 
 			 // Handle right-clicks on inventory blocks
 			 if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof InventoryHolder) {
 
 				 InventoryHolder invh = (InventoryHolder) e.getClickedBlock().getState();
 				 Inventory inv = invh.getInventory();
 
 				 ItemStack[] contents = inv.getContents();
 				 for (int i = 0; i < contents.length; ++i)
 					 if (contents[i] != null) contents[i] = contents[i].clone();
 
 				 Inventory newinv;
 				 if (inv instanceof DoubleChestInventory)
 					 newinv = Bukkit.getServer().createInventory(null, 54, "Large Chest");
 				 else newinv = Bukkit.getServer().createInventory(null, inv.getType());
 				 newinv.setContents(contents);
 
 				 e.getPlayer().openInventory(newinv);
 				 e.setCancelled(true);
 				 return;
 			 }
 			 // Cancel all other actions
 			 e.setCancelled(true);
 		 }
 	 }
 
 	 @EventHandler(ignoreCancelled = true)
 	 public void onBlockPlace(BlockPlaceEvent e) {
 		 UhcPlayer pl = m.getPlayer(e.getPlayer());
 		 if (pl.isAdmin()) return; // Ignore this event for admins.
 
 		 // Cancel the event if player is a spec, or match hasn't started
 		 if (pl.isNonInteractingSpectator()
 				 || (m.getMatchPhase() == MatchPhase.PRE_MATCH || m.getMatchPhase() == MatchPhase.LAUNCHING)) {
 			 e.setCancelled(true);
 			 return;
 		 }
 	 }
 
 	 @EventHandler(ignoreCancelled = true)
 	 public void onBlockBreak(BlockBreakEvent e) {
 		 UhcPlayer pl = m.getPlayer(e.getPlayer());
 		 if (pl.isAdmin()) return; // Ignore this event for admins.
 
 		 // Cancel the event if player is a spec, or match hasn't started
 		 if (pl.isNonInteractingSpectator()
 				 || (m.getMatchPhase() == MatchPhase.PRE_MATCH || m.getMatchPhase() == MatchPhase.LAUNCHING)) {
 			 e.setCancelled(true);
 			 return;
 		 }
 	 }
 }
