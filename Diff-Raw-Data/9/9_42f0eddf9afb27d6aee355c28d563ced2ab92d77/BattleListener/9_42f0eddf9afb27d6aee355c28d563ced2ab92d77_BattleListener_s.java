 package com.censoredsoftware.Demigods.Engine.Listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.Object.Battle.Battle;
 import com.censoredsoftware.Demigods.Engine.Object.Battle.BattleParticipant;
 
 public class BattleListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void onDamageBy(EntityDamageByEntityEvent event)
 	{
 		if(!Battle.canParticipate(event.getEntity()) || !Battle.canParticipate(event.getDamager())) return;
 
 		// Define participants
 		BattleParticipant damageeParticipant = Battle.defineParticipant(event.getEntity());
 		BattleParticipant damagerParticipant = Battle.defineParticipant(event.getDamager());
 
 		// Calculate midpoint location
 		Location midpoint = damagerParticipant.getCurrentLocation().toVector().getMidpoint(event.getEntity().getLocation().toVector()).toLocation(damagerParticipant.getCurrentLocation().getWorld());
 
 		if(!Battle.existsNear(midpoint) && !Battle.existsInRadius(midpoint))
 		{
 			// Create new battle
 			Battle battle = Battle.create(damagerParticipant, damageeParticipant);
 
 			// Teleport if needed
 			// Battle.teleportIfNeeded(damageeParticipant, battle);
 			// Battle.teleportIfNeeded(damagerParticipant, battle);
 
 			// Battle death
 			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth()) event.setCancelled(Battle.battleDeath(damagerParticipant, damageeParticipant, battle));
 
 			// Debug
 			Demigods.message.broadcast(ChatColor.YELLOW + "Battle started involving " + damagerParticipant.getRelatedCharacter().getName() + " and " + damageeParticipant.getRelatedCharacter().getName() + "!");
 		}
 		else
 		{
 			// Add to existing battle
			Battle battle = Battle.getInRadius(midpoint);
 
 			// Teleport if needed
 			// Battle.teleportIfNeeded(damageeParticipant, battle);
 			// Battle.teleportIfNeeded(damagerParticipant, battle);
 
 			// Add participants from this event
 			battle.getMeta().addParticipant(damageeParticipant);
 			battle.getMeta().addParticipant(damagerParticipant);
 
 			// Battle death
 			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth()) event.setCancelled(Battle.battleDeath(damagerParticipant, damageeParticipant, battle));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onDamage(EntityDamageEvent event)
 	{
 		if(event instanceof EntityDamageByEntityEvent || !Battle.canParticipate(event.getEntity())) return;
 
 		BattleParticipant participant = Battle.defineParticipant(event.getEntity());
 
 		// Battle death
 		if(Battle.isInBattle(participant) && event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth()) event.setCancelled(Battle.battleDeath(participant, Battle.getBattle(participant)));
 	}
 
 	// @EventHandler(priority = EventPriority.HIGHEST)
 	public void onBattleMove(PlayerMoveEvent event)
 	{
 		if(!Battle.canParticipate(event.getPlayer())) return;
 		BattleParticipant participant = Battle.defineParticipant(event.getPlayer());
 		if(onBattleMove(event.getTo(), event.getFrom(), participant)) event.setCancelled(true);
 	}
 
 	// @EventHandler(priority = EventPriority.HIGHEST)
 	public void onBattleMove(PlayerTeleportEvent event)
 	{
 		if(!Battle.canParticipate(event.getPlayer())) return;
 		BattleParticipant participant = Battle.defineParticipant(event.getPlayer());
 		if(onBattleMove(event.getTo(), event.getFrom(), participant)) event.setCancelled(true);
 	}
 
 	private static boolean onBattleMove(Location toLocation, Location fromLocation, BattleParticipant participant)
 	{
 		boolean to = Battle.existsInRadius(toLocation);
 		boolean from = Battle.existsInRadius(fromLocation);
 		boolean enter = to == true && from == false;
 		boolean exit = to == false && from == true;
 		if(enter) Battle.getInRadius(toLocation).getMeta().addParticipant(participant);
 		if(exit && Battle.isInBattle(participant)) return true;
 		return false;
 	}
 }
