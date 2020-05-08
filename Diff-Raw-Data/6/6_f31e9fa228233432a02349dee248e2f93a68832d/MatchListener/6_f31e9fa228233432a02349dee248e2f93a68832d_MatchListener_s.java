 package com.martinbrook.tesseractuhc.listeners;
 
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.Material;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerItemConsumeEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.ChatColor;
 
 import com.martinbrook.tesseractuhc.MatchPhase;
 import com.martinbrook.tesseractuhc.TesseractUHC;
 import com.martinbrook.tesseractuhc.UhcMatch;
 import com.martinbrook.tesseractuhc.UhcParticipant;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 import com.martinbrook.tesseractuhc.customevent.UhcDeathEvent;
 import com.martinbrook.tesseractuhc.customevent.UhcDimensionChangeEvent;
 import com.martinbrook.tesseractuhc.notification.DamageNotification;
 import com.martinbrook.tesseractuhc.notification.HealingNotification;
 
 public class MatchListener implements Listener {
 	private UhcMatch m;
 	public MatchListener(UhcMatch m) { this.m = m; }
 	
 
 		
 	/**
 	 * Handle death events; add bonus items, if any.
 	 * 
 	 * @param pDeath
 	 */
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent e){
 		Player p = e.getEntity();
 		UhcPlayer pl = m.getPlayer(p);
 		
 		// If it's a pvp kill, drop bonus items
 		if (p.getKiller() != null) {
 			UhcPlayer killer = m.getPlayer(p.getKiller());
 			if (pl.isParticipant() && killer.isParticipant() && killer.getParticipant().getTeam() != pl.getParticipant().getTeam()) {
 				ItemStack bonus = m.getConfig().getKillerBonus();
 				if (bonus != null)
 					e.getDrops().add(bonus);
 			}
 			if (pl.isParticipant() && killer.isParticipant()){
 				killer.getParticipant().addKill();
 			}
 		}
 		
 		// If dropheads is enabled, drop a skull
 		if (m.getConfig().isDropHeads()) {
 			m.placeHeadDelayed(p.getLocation(), p.getName());
 
 		}
         
 		// Make death message red
 		String deathMessage = e.getDeathMessage();
 		e.setDeathMessage(ChatColor.GOLD + deathMessage);
 		
 		// Save death point
 		m.setLastDeathLocation(p.getLocation());
 		
 		// Handle the death
 		if (pl.isActiveParticipant() && m.getMatchPhase() == MatchPhase.MATCH) {
 			m.handleParticipantDeath(pl.getParticipant());
 			
 			// Trigger death event
 			m.getServer().getPluginManager().callEvent(new UhcDeathEvent(m, p.getLocation(), deathMessage, p, p.getKiller()));
 		}
 
 		// Update the tab list
 		m.schedulePlayerListUpdate(pl);
 		
 
 	}
 
 	@EventHandler
 	public void onRespawn(PlayerRespawnEvent e) {
 		// Only do anything if match is in progress or completed
 		if (m.getMatchPhase() != MatchPhase.MATCH && m.getMatchPhase() != MatchPhase.POST_MATCH) return;
 		
 		// Get the UhcPlayer
 		UhcPlayer pl = m.getPlayer(e.getPlayer());
 		
 		// If they're a dead UHC player, make sure they respawn at overworld spawn and setup their gamemode correctly
 		if (pl.isParticipant() && pl.getParticipant().isDead()) {
 			e.setRespawnLocation(m.getStartingWorld().getSpawnLocation());
 
  
 			// Make the player a spectator
 			m.handleEliminatedPlayer(pl);
 		}
 	}
 	
 
 
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityDamage(EntityDamageEvent e) {
 		// Only interested in players taking damage
 		if (e.getEntityType() != EntityType.PLAYER) return;
 		
 		// Only interested if match is in progress. Cancel damage if not.
 		if (m.getMatchPhase() != MatchPhase.MATCH) {
 			e.setCancelled(true);
 			return;
 		}
 		
 		// If damage caused by another entity, ignore it here (it will be handled by onEntityDamageByEntity)
 		if (e.getCause() == DamageCause.ENTITY_ATTACK || e.getCause() == DamageCause.ENTITY_EXPLOSION
 				|| e.getCause() == DamageCause.PROJECTILE) return;
 		
 		// If damage ticks not exceeded, the damage won't happen, so return
 		if(((LivingEntity)e.getEntity()).getNoDamageTicks() > ((LivingEntity)e.getEntity()).getMaximumNoDamageTicks()/2.0F)	return;
 		
 		// Only interested in registered, active participants
 		UhcPlayer pl = m.getPlayer((Player) e.getEntity());
 		if (!pl.isActiveParticipant()) return;
 		
 		UhcParticipant pa = pl.getParticipant();
 		
 		if (!pa.isRecentlyDamaged()) {
 			DamageNotification n = new DamageNotification(pa, e.getCause());
 			if (m.getConfig().isDamageAlerts()) 
 				m.sendNotification(n, e.getEntity().getLocation());
 			else 
 				m.sendSpectatorNotification(n, e.getEntity().getLocation());
 		}
 		pa.setDamageTimer();
 		
 		// Update the client mod
 		updateHealth(pl.getPlayer());
 
 		// Update the tab list
 		m.schedulePlayerListUpdate(pl);
 	}
 	
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
 		
 		// Cancel any damage caused by spectators
 		if (e.getDamager() instanceof Player) {
 			Player p = (Player) e.getDamager();
 			if (m.getPlayer(p).isNonInteractingSpectator()) {
 				e.setCancelled(true);
 				return;
 			}
 		}
 		
 		// Only interested in players taking damage
 		if (e.getEntityType() != EntityType.PLAYER) return;
 		
 		// Only interested in registered, active participants
 		UhcPlayer pl = m.getPlayer((Player) e.getEntity());
 		if (!pl.isActiveParticipant()) return;
 
 		UhcParticipant pa = pl.getParticipant();
 		
 		if (!pa.isRecentlyDamaged()) {
 			DamageNotification n = new DamageNotification(pl.getParticipant(), e.getCause(), e.getDamager());
 			if (m.getConfig().isDamageAlerts()) 
 				m.sendNotification(n, e.getEntity().getLocation());
 			else 
 				m.sendSpectatorNotification(n, e.getEntity().getLocation());
 		}
 		pa.setDamageTimer();
 	}
 
 	@EventHandler(ignoreCancelled = true)
 	public void onRegainHealth(EntityRegainHealthEvent e) {
 		// Only interested in players
 		if (e.getEntityType() != EntityType.PLAYER) return;
 		
 		// Only interested if match is in progress.
 		if (m.getMatchPhase() != MatchPhase.MATCH) return;
 
 		// Only interested in registered, active participants
 		UhcPlayer pl = m.getPlayer((Player) e.getEntity());
 		if (!pl.isActiveParticipant()) return;
 
 		// Cancel event if it is a natural regen due to hunger being full, and UHC is enabled
 		if (m.getConfig().isUHC() && e.getRegainReason() == RegainReason.SATIATED) {
 			e.setCancelled(true);
 			return;
 		}
 		
 		// Announce health change (UHC only)
 		if (m.getConfig().isUHC()) {
 			HealingNotification n = new HealingNotification(pl.getParticipant(), e.getAmount(), e.getRegainReason());
 			if (m.getConfig().isDamageAlerts())
 				m.sendNotification(n, e.getEntity().getLocation());
 			else 
 				m.sendSpectatorNotification(n,  e.getEntity().getLocation());
 		}
 		
 		// Send update to client mod
 		updateHealth(pl.getPlayer());	
 
 		// Update the tab list
 		m.schedulePlayerListUpdate(pl);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onEntityDeath(EntityDeathEvent e) {
 		// Modified drops for ghasts in UHC
 		if (m.getConfig().isUHC() && e.getEntityType()==EntityType.GHAST)
 			for(ItemStack i : e.getDrops())
 				if (i.getType()==Material.GHAST_TEAR) i.setType(Material.GOLD_INGOT);
 		
 		// Handle death of dragon
 		if (m.getConfig().isDragonMode() && e.getEntityType()==EntityType.ENDER_DRAGON) {
 			UhcPlayer pl = m.getPlayer(e.getEntity().getKiller());
 			if (pl != null && pl.isActiveParticipant()) m.handleDragonKill(pl.getParticipant());
 		}
 			
 	}
 	
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onBlockBreak(BlockBreakEvent e) {
 		UhcPlayer pl = m.getPlayer(e.getPlayer());
 		
 		// Apply penalties for mining stone
 		if (e.getBlock().getType() == Material.STONE && pl.isActiveParticipant()) {
 			
 			// Mining fatigue
 			pl.getParticipant().doMiningFatigue(e.getBlock().getLocation().getBlockY());
 			
 			// Hard stone
 			if (m.getConfig().isHardStone())
 				pl.getParticipant().doHardStone(e.getBlock().getLocation().getBlockY(), pl.getPlayer().getItemInHand());
 		}
 		
 		
 	}
 	
 
 	@EventHandler(ignoreCancelled = true)
 	public void onCreatureSpawn(CreatureSpawnEvent e) {
 		// If match is in progress or ended, do nothing
 		if (m.getMatchPhase() == MatchPhase.MATCH || m.getMatchPhase() == MatchPhase.POST_MATCH) return;
 		
 		// Only deal with natural or spawner spawns
 		if (e.getSpawnReason() != SpawnReason.NATURAL && e.getSpawnReason() != SpawnReason.SPAWNER) return;
 		
 		// Only worry about hostiles
 		if (!(e.getEntityType() == EntityType.CREEPER
 				|| e.getEntityType() == EntityType.SPIDER
 				|| e.getEntityType() == EntityType.SKELETON
 				|| e.getEntityType() == EntityType.CAVE_SPIDER
 				|| e.getEntityType() == EntityType.ENDERMAN
 				|| e.getEntityType() == EntityType.SILVERFISH
 				|| e.getEntityType() == EntityType.SLIME
 				|| e.getEntityType() == EntityType.WITCH
 				|| e.getEntityType() == EntityType.ZOMBIE
 				|| e.getEntityType() == EntityType.GHAST
 				|| e.getEntityType() == EntityType.PIG_ZOMBIE)) return;
 		
 		// Cancel spawn
 		e.setCancelled(true);
 	}
 	
 	@EventHandler(ignoreCancelled = true)
 	public void onWorldChange(PlayerChangedWorldEvent e) {
 		UhcPlayer pl = m.getPlayer(e.getPlayer());
 		if (pl.isActiveParticipant() && m.getMatchPhase() == MatchPhase.MATCH)
 			m.getServer().getPluginManager().callEvent(new UhcDimensionChangeEvent(m, e.getPlayer().getLocation(), e.getPlayer(), e.getPlayer().getWorld().getEnvironment()));
 		
 	}
 	
 	class HealthChangeTask implements Runnable
 	{
 		UhcParticipant pl = null;
 
 		public HealthChangeTask(UhcParticipant pl)
 		{ this.pl = pl; }
 
 		public void run()
 		{ if (pl != null) pl.updateHealth(); }
 	}
 	
 	public void updateHealth(Player pl){
 		if (pl != null && m.getPlayer(pl).isActiveParticipant()){
 			TesseractUHC.getInstance().getServer().getScheduler().runTask(TesseractUHC.getInstance(), new HealthChangeTask(m.getPlayer(pl).getParticipant()));
 		}
 	}
 	
 	class ArmorChangeTask implements Runnable
 	{
 		UhcParticipant pl = null;
 
 		public ArmorChangeTask(UhcParticipant pl)
 		{ this.pl = pl; }
 
 		public void run()
 		{ if (pl != null) pl.updateArmor(); }
 	}
 	
 	public void updateArmor(Player pl){
 		if (pl != null && m.getPlayer(pl).isActiveParticipant()){
 			TesseractUHC.getInstance().getServer().getScheduler().runTask(TesseractUHC.getInstance(), new ArmorChangeTask(m.getPlayer(pl).getParticipant()));
 		}
 	}
 	
 	class DimensionChangeTask implements Runnable
 	{
 		UhcParticipant pl = null;
 
 		public DimensionChangeTask(UhcParticipant pl)
 		{ this.pl = pl; }
 
 		public void run()
 		{ if (pl != null) pl.updateDimension(); }
 	}
 	
 	public void updateDimension(Player pl){
 		if (pl != null && m.getPlayer(pl).isActiveParticipant()){
 			TesseractUHC.getInstance().getServer().getScheduler().runTask(TesseractUHC.getInstance(), new DimensionChangeTask(m.getPlayer(pl).getParticipant()));
 		}
 	}
 	
 	class InventoryChangeTask implements Runnable
 	{
 		UhcParticipant pl = null;
 
 		public InventoryChangeTask(UhcParticipant pl)
 		{ this.pl = pl; }
 
 		public void run()
 		{ if (pl != null) pl.updateInventory(); }
 	}
 	
 	public void updateInventory(Player pl){
 		if (pl != null && m.getPlayer(pl).isActiveParticipant()){
 			TesseractUHC.getInstance().getServer().getScheduler().runTask(TesseractUHC.getInstance(), new InventoryChangeTask(m.getPlayer(pl).getParticipant()));
 		}
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onInventoryClick(InventoryClickEvent event){
 		updateArmor((Player) event.getWhoClicked());
 		updateInventory((Player) event.getWhoClicked());
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onPlayerDropItem(PlayerDropItemEvent event){
 		updateArmor(event.getPlayer());
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onPlayerDimensionChange(PlayerPortalEvent event){
 		updateDimension(event.getPlayer());
 	}	
 
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onItemDrop(PlayerDropItemEvent event){ 
 		updateInventory(event.getPlayer());
 	}
 
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onItemPickup(PlayerPickupItemEvent event){ 
 		updateInventory(event.getPlayer());
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void onItemUse(PlayerItemConsumeEvent event){ 
 		updateInventory(event.getPlayer());
 	}
 	
 	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
 	public void itemCraft(CraftItemEvent event){ 
 		updateInventory((Player) event.getWhoClicked());
 	}
 }
