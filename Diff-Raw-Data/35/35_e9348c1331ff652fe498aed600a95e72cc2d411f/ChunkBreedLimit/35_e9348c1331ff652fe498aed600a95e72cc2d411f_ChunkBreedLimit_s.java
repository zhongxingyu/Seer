 package com.github.ephemeralis.chunkbreedlimit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ChunkBreedLimit extends JavaPlugin {
 	
	private BreederListener spawnListener;
 	public int entitySpawnCap;
 	public List<EntityType> allowedEntityList = new ArrayList<EntityType>();
 	
 	@Override
 	public void onEnable()
 	{
 		this.saveDefaultConfig();
 		entitySpawnCap = this.getConfig().getInt("spawn-cap", 100);
 		
 		List<String> entityListLoad = this.getConfig().getStringList("entity-list");
 		
 		if (entityListLoad.size() == 0)
 		{
 			getLogger().info("Entity list appears invalid! Reverting to defaults..");
 			
 			allowedEntityList.add(EntityType.COW);
 			allowedEntityList.add(EntityType.CHICKEN);
 			allowedEntityList.add(EntityType.PIG);
 			allowedEntityList.add(EntityType.SHEEP);
 		} else {
 			//load list from config
 			
 			getLogger().info("Loading breeding deny list from config..");
 			
 			for (String ent : entityListLoad)
 			{
 					EntityType temp = EntityType.fromName(ent);
 					allowedEntityList.add(temp);
 			}
 		}
 		
		spawnListener = new BreederListener(this);
 		getLogger().info(String.format("Now watching: %s when entity chunk count above %s", allowedEntityList.toString(), entitySpawnCap));
 	}
 	
 	@Override
 	public void onDisable()
 	{
		CreatureSpawnEvent.getHandlerList().unregister(spawnListener);
 		getLogger().info("ChunkBreedLimit unloaded!");
 	}
 }
