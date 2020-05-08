 package com.github.ephemeralis.chunkbreedlimit;
 
import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import org.bukkit.entity.*;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 
 public class BreederListener implements Listener {
 	
	//private ChunkBreedLimit basePlugin;
	private int spawnCap;
	private List<EntityType> allowedEntities;
	
	public BreederListener(ChunkBreedLimit plugin, int cap, List<EntityType> allowedEnts)
 	{
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		spawnCap = cap;
		allowedEntities = allowedEnts;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onCreatureSpawnEvent(CreatureSpawnEvent event)
 	{
 		if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BREEDING 
 				|| event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG)
 		{
 			Entity[] entitiesInEventChunk = event.getLocation().getChunk().getEntities();
 			
 			int entcount = 0;
 			for (Entity ent : entitiesInEventChunk)
 			{
 				//we have entity list, so iterate through it
 				//to determine just how many of the same type
 				//we have on the same chunk
				if (allowedEntities.contains(ent.getType()))
 				{
 					//same type detected, so increase
 					entcount++;
 				}
 			}
 			
			if (entcount > spawnCap) //replace this with configuratio0n reader
 			{
 				event.setCancelled(true); //must be a better way to can the event
 			}
 		}
 	
 	}
 }
 
