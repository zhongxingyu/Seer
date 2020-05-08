 package com.censoredsoftware.demigods.listener;
 
 import com.censoredsoftware.demigods.Demigods;
 import com.censoredsoftware.demigods.battle.Battle;
 import com.censoredsoftware.demigods.battle.Participant;
 import com.censoredsoftware.demigods.data.DataManager;
 import com.censoredsoftware.demigods.location.DLocation;
 import com.censoredsoftware.demigods.player.DCharacter;
 import com.censoredsoftware.demigods.player.Pet;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 public class BattleListener implements Listener
 {
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public static void onDamageBy(EntityDamageByEntityEvent event)
 	{
 		if(event.isCancelled()) return;
 		if(Demigods.MiscUtil.isDisabledWorld(event.getEntity().getLocation())) return;
 		Entity damager = event.getDamager();
 		if(damager instanceof Projectile) damager = ((Projectile) damager).getShooter();
 		if(!Battle.Util.canParticipate(event.getEntity()) || !Battle.Util.canParticipate(damager)) return;
 
 		// Define participants
 		Participant damageeParticipant = Battle.Util.defineParticipant(event.getEntity());
 		Participant damagerParticipant = Battle.Util.defineParticipant(damager);
 
 		// Various things that should cancel the event
 		if(damageeParticipant.equals(damagerParticipant) || DCharacter.Util.areAllied(damageeParticipant.getRelatedCharacter(), damagerParticipant.getRelatedCharacter()))
 		{
 			event.setCancelled(true);
 			return;
 		}
 		if(damageeParticipant instanceof DCharacter && DataManager.hasTimed(damageeParticipant.getId().toString(), "just_finished_battle"))
 		{
 			((Player) damager).sendMessage(ChatColor.YELLOW + "That player is in cooldown from a recent battle.");
 			event.setCancelled(true);
 			return;
 		}
 		if(damagerParticipant instanceof DCharacter && DataManager.hasTimed(damagerParticipant.getId().toString(), "just_finished_battle"))
 		{
 			((Player) damager).sendMessage(ChatColor.YELLOW + "You are still in cooldown from a recent battle.");
 			event.setCancelled(true);
 			return;
 		}
 
 		// Calculate midpoint location
 		Location midpoint = damagerParticipant.getCurrentLocation().toVector().getMidpoint(event.getEntity().getLocation().toVector()).toLocation(damagerParticipant.getCurrentLocation().getWorld());
 
 		if(Battle.Util.isInBattle(damageeParticipant) || Battle.Util.isInBattle(damagerParticipant))
 		{
 			// Add to existing battle
 			Battle battle = Battle.Util.isInBattle(damageeParticipant) ? Battle.Util.getBattle(damageeParticipant) : Battle.Util.getBattle(damagerParticipant);
 
 			// Add participants from this event
 			battle.addParticipant(damageeParticipant);
 			battle.addParticipant(damagerParticipant);
 
 			// Battle death
 			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
 			{
 				event.setCancelled(true);
 				Battle.Util.battleDeath(damagerParticipant, damageeParticipant, battle);
 			}
 			return;
 		}
 
 		if(!Battle.Util.existsNear(midpoint) && !Battle.Util.existsInRadius(midpoint))
 		{
 			// Create new battle
 			Battle battle = Battle.Util.create(damagerParticipant, damageeParticipant);
 
 			// Battle death
 			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
 			{
 				event.setCancelled(true);
 				Battle.Util.battleDeath(damagerParticipant, damageeParticipant, battle);
 			}
 
 			battle.sendMessage(ChatColor.YELLOW + "You are now in battle!");
 		}
 		else
 		{
 			// Add to existing battle
 			Battle battle = Battle.Util.getNear(midpoint) != null ? Battle.Util.getNear(midpoint) : Battle.Util.getInRadius(midpoint);
 
 			// Add participants from this event
 			battle.addParticipant(damageeParticipant);
 			battle.addParticipant(damagerParticipant);
 
 			// Battle death
 			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
 			{
 				event.setCancelled(true);
 				Battle.Util.battleDeath(damagerParticipant, damageeParticipant, battle);
 			}
 		}
 
 		// Pets
 		if(damager instanceof LivingEntity) for(Pet pet : damageeParticipant.getRelatedCharacter().getPets())
 		{
 			LivingEntity entity = pet.getEntity();
 			if(entity != null) ((Monster) entity).setTarget((LivingEntity) damager);
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOW)
 	public void onDamage(EntityDamageEvent event)
 	{
 		if(Demigods.MiscUtil.isDisabledWorld(event.getEntity().getLocation())) return;
 		if(event instanceof EntityDamageByEntityEvent || !Battle.Util.canParticipate(event.getEntity())) return;
 
 		Participant participant = Battle.Util.defineParticipant(event.getEntity());
 
 		// Battle death
 		if(Battle.Util.isInBattle(participant) && event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
 		{
 			event.setCancelled(true);
 			Battle.Util.battleDeath(participant, Battle.Util.getBattle(participant));
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBattleMove(PlayerMoveEvent event)
 	{
 		if(Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getLocation())) return;
 		if(!Battle.Util.canParticipate(event.getPlayer())) return;
 		Participant participant = Battle.Util.defineParticipant(event.getPlayer());
 		if(Battle.Util.isInBattle(participant))
 		{
 			Battle battle = Battle.Util.getBattle(participant);
 			boolean to = DLocation.Util.distanceFlat(event.getTo(), battle.getStartLocation()) > battle.getRange();
 			boolean from = DLocation.Util.distanceFlat(event.getFrom(), battle.getStartLocation()) > battle.getRange();
 			if(to && !from) DataManager.saveTemp(participant.getId().toString(), "battle_safe_location", event.getFrom());
 			if(to)
 			{
 				if(DataManager.hasKeyTemp(participant.getId().toString(), "battle_safe_location"))
 				{
 					event.getPlayer().teleport((Location) DataManager.getValueTemp(participant.getId().toString(), "battle_safe_location"));
 					DataManager.removeTemp(participant.getId().toString(), "battle_safe_location");
 				}
 				else event.getPlayer().teleport(Battle.Util.randomRespawnPoint(battle));
 			};
 		}
 	}
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onBattleMove(PlayerTeleportEvent event)
 	{
 		if(Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getLocation())) return;
 		if(!Battle.Util.canParticipate(event.getPlayer())) return;
 		Participant participant = Battle.Util.defineParticipant(event.getPlayer());
 		if(Battle.Util.isInBattle(participant))
 		{
 			Battle battle = Battle.Util.getBattle(participant);
 			if(!event.getTo().getWorld().equals(battle.getStartLocation().getWorld())) return;
 			if(DLocation.Util.distanceFlat(event.getTo(), battle.getStartLocation()) > battle.getRange()) event.setCancelled(true);
 		}
 	}
 }
