 package com.legit2.Demigods.Events;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 import com.legit2.Demigods.Events.Player.PlayerBetrayPlayerEvent;
 import com.legit2.Demigods.Events.Player.PlayerKillPlayerEvent;
 import com.legit2.Demigods.Utilities.DMiscUtil;
 import com.legit2.Demigods.Utilities.DPlayerUtil;
 
 public class DEventCreator implements Listener
 {
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onEntityDeath(EntityDeathEvent event)
 	{
 		Entity entity = event.getEntity();
 		if(entity instanceof Player)
 		{
 			Player player = (Player) entity;
 			EntityDamageEvent damageEvent = player.getLastDamageCause();
 			
 			if(damageEvent instanceof EntityDamageByEntityEvent)
 			{
 				EntityDamageByEntityEvent damageByEvent = (EntityDamageByEntityEvent) damageEvent;
 				Entity damager = damageByEvent.getDamager();
 				
 				if(damager instanceof Player)
 				{
 					Player attacker = (Player) damager;
 					if(DMiscUtil.areAllied(attacker, player))
 					{
						PlayerBetrayPlayerEvent betrayEvent = new PlayerBetrayPlayerEvent(attacker, player, DPlayerUtil.getCurrentAlliance(player));
						DMiscUtil.getPlugin().getServer().getPluginManager().callEvent(betrayEvent);
 					}
 					else
 					{
						PlayerKillPlayerEvent killEvent = new PlayerKillPlayerEvent(attacker, player);
						DMiscUtil.getPlugin().getServer().getPluginManager().callEvent(killEvent);
 					}
 				}
 			}
 		}
 	}
 }
