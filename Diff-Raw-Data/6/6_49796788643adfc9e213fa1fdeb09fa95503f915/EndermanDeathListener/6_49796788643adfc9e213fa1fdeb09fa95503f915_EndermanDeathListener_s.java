 package com.warrows.plugins.EtherHardcore;
 
 import org.bukkit.entity.Enderman;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 
 public class EndermanDeathListener implements Listener
 {
 	@EventHandler(ignoreCancelled = true)
 	public void onMobDeathEvent(EntityDeathEvent event)
 	{
 		if (! (event.getEntity() instanceof Enderman))
 			return;
 		
 		Player joueur = ((Enderman) event.getEntity()).getKiller();
 		
 		EtherHardcore.economy.depositPlayer(joueur.getName(), 1.0);
 		joueur.sendMessage("Vous avez tu cette trange crature et un Hurbron est entr en vous.");
 	}
 }
