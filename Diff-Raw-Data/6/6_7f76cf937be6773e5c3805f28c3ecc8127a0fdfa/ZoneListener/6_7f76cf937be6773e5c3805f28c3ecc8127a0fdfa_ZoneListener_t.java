 package com.censoredsoftware.demigods.engine.listener;
 
 import com.censoredsoftware.demigods.engine.player.DPlayer;
 import com.censoredsoftware.demigods.engine.util.Zones;
import com.google.common.collect.Sets;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldUnloadEvent;
 
 import java.util.Set;
 
 public class ZoneListener implements Listener
 {
 	// TODO Special listener to prevent interactions between Demigods worlds and non-Demigods worlds.
 
 	@EventHandler(priority = EventPriority.HIGHEST)
 	public void onDemigodsChat(DemigodsChatEvent event)
 	{
        Set<Player> modified = Sets.newHashSet(event.getRecipients());
        for(Player player : event.getRecipients())
 			if(Zones.inNoDemigodsZone(player.getLocation())) modified.remove(player);
 		if(modified.size() < 1) event.setCancelled(true);
 		event.setRecipients(modified);
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onWorldSwitch(PlayerChangedWorldEvent event)
 	{
 		// Only continue if the player is a character
 		Player player = event.getPlayer();
 		DPlayer playerSave = DPlayer.Util.getPlayer(player);
 
 		if(playerSave.getCurrent() == null) return;
 
 		// Leaving a disabled world
 		if(Zones.isNoDemigodsWorld(event.getFrom()) && !Zones.isNoDemigodsWorld(player.getWorld()))
 		{
 			playerSave.saveMortalInventory(player.getInventory());
 			playerSave.getCurrent().applyToPlayer(player);
 			player.sendMessage(ChatColor.YELLOW + "Demigods is enabled in this world.");
 		}
 		// Entering a disabled world
 		else if(!Zones.isNoDemigodsWorld(event.getFrom()) && Zones.isNoDemigodsWorld(player.getWorld()))
 		{
 			playerSave.setToMortal();
 			player.sendMessage(ChatColor.GRAY + "Demigods is disabled in this world.");
 		}
 	}
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onWorldLoad(WorldLoadEvent event) {
         // TODO For option to disable newly created worlds.
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onWorldLoad(WorldUnloadEvent event) {
         // TODO For bukkit enabled worlds.
     }
 }
