 package net.yeticraft.xxtraineexx.mobspawncontrol;
 
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.UUID;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.event.world.ChunkUnloadEvent;
 
 public class MSCListener implements Listener{
 
 	public static MobSpawnControl plugin;
 	
 	HashMap<Block, MSCSpawner> activeSpawners = new HashMap<Block, MSCSpawner>();
 	HashMap<UUID, MSCMob> activeMobs = new HashMap<UUID, MSCMob>();
 	
 	public MSCListener(MobSpawnControl plugin) {
 		plugin.getServer().getPluginManager().registerEvents(this, plugin);
 		MSCListener.plugin = plugin;
 	}
 	
 	
 	public void onPluginEnable (PluginEnableEvent event) {
 		
 		plugin.log.info(("Plugin detected: " + event.getPlugin().toString()));
 		
 	}
 	
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onCreatureSpawn(CreatureSpawnEvent e) {
 		
 		
 		// If this didn't come from a spawner, return out.
 		if (!e.getSpawnReason().toString().equalsIgnoreCase("SPAWNER")){
 
 			return;
 			
 		}
 		
 		// Find the spawner this monster came from.
 		Block spawnedMobLoc = e.getLocation().getBlock();
 		Block currentBlock = null;
 		Block mobSpawner = null;
 		UUID spawnedMobUUID = e.getEntity().getUniqueId();
 		Entity spawnedMob = e.getEntity();
 		Player player = null;
 		
 		// Mobs can only spawn within a 8x3x8 area
 		int lowerX = spawnedMobLoc.getX() - 7;
 		int upperX = spawnedMobLoc.getX() + 7;
 		int lowerY = spawnedMobLoc.getY() - 2;
 		int upperY = spawnedMobLoc.getY() + 2;
 		int lowerZ = spawnedMobLoc.getZ() - 7;
 		int upperZ = spawnedMobLoc.getZ() + 7;
 		boolean keepLooping = true;
 		
 		
 		
 		// Searching all nearby blocks to find the spawner
 		for (int y = lowerY; y <= upperY && keepLooping; y++){
 			for (int x = lowerX; x <= upperX && keepLooping; x++){
 				for (int z = lowerZ; z <= upperZ; z++){
 					
 					currentBlock = e.getLocation().getWorld().getBlockAt(x, y, z);
 					if (currentBlock.getTypeId() == 52){
 						mobSpawner = currentBlock;
 						keepLooping = false;
 						break;
 					}
 					
 				}
 			}
 		}
 				
 		
 		// If mobSpawner is still null we must have missed the spawner somehow.
 		if (mobSpawner == null){
 			plugin.log.info(plugin.prefix + "Spawner not found for spawned creature at: " + spawnedMob.getLocation().toString());
 			return;
 		}
 		
 		// Checking for nearby players
 		for (Player nearby : Bukkit.getServer().getOnlinePlayers()) {	
 			double nearbyDistance = nearby.getLocation().distance(mobSpawner.getLocation());
 			if (nearbyDistance <= 17){
 				player = nearby;
 				break;
 			}
 			
 		}
 
 		// Lets create a Hashset to store the mobs associated with a spawner
 		Set<UUID> mobList;
 		
 		// If the spawner is NOT in the hashmap we need to add this spawner to the hashmap and add this mob to the active mobs
 		if (!activeSpawners.containsKey(mobSpawner)){
 			mobList = new HashSet<UUID>();
 			mobList.add(spawnedMobUUID);
 			activeSpawners.put(mobSpawner, new MSCSpawner(player, mobList));
 			activeMobs.put(spawnedMobUUID, new MSCMob(spawnedMob, mobSpawner));
 			
 			e.setCancelled(false);
 			if (plugin.debug){ plugin.log.info(plugin.prefix + "NEW Spawner: " + mobSpawner.getLocation().toString() + " Owner: [" + player.getName() + "] Mob: [" + spawnedMob.getType().getName() + "] Spawn Count: [" + mobList.size() + "]");}
 			return;
 		}
 		
 		// Looks like the mobSpawner is already in the spawnerSet. 
 		mobList = activeSpawners.get(mobSpawner).getMobList();
 		
 		// Before we see if the mobList has reached it's limit, we should make sure none of the mob's have despawned.
 		Iterator<UUID> it = mobList.iterator();
 		int despawnedMobs = 0;
 		while(it.hasNext()) {
 
 			UUID mobUUID = it.next();
 			if (activeMobs.get(mobUUID).getMobEntity().isDead()){
 				activeMobs.remove(mobUUID);
 				despawnedMobs++;
				it.remove();
				
 			}
 		}
 		if (plugin.debug){ plugin.log.info(plugin.prefix + "Removed [" + despawnedMobs + "] despawned Mobs in spawner: " + mobSpawner.getLocation().toString());}
 		
 		// Lets check to see if this set has reached its limit
 		if (mobList.size() >= plugin.spawnsAllowed){
 			if (plugin.debug){ plugin.log.info(plugin.prefix + "FULL Spawner: " + mobSpawner.getLocation().toString() + " Owner: [" + player.getName() + "] Mob: [" + spawnedMob.getType().getName() + "] Spawn Count: [" + mobList.size() + "]");}
 			e.setCancelled(true);
 			return;
 		}
 		
 		// Looks like the current mobSpawner is not at its maximum. Let's increment.
 		mobList.add(spawnedMobUUID);
 		activeMobs.put(spawnedMobUUID, new MSCMob(spawnedMob, mobSpawner));
 		if (plugin.debug){ plugin.log.info(plugin.prefix + "EXISTING Spawner: " + mobSpawner.getLocation().toString() + " Owner: [" + player.getName() + "] Mob: [" + spawnedMob.getType().getName() + "] Spawn Count: [" + mobList.size() + "]");}
 		e.setCancelled(false);
 		return;
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDeath(EntityDeathEvent e) {
 		
 		UUID deadMobUUID = e.getEntity().getUniqueId();
 		
 		if (activeMobs.containsKey(deadMobUUID)){
 			
 			// Finding the spawner this entity is attached to
 			Block mobSpawner = activeMobs.get(deadMobUUID).getMobSpawner();
 			
 			// Finding the MobList associated with this spawner
 			Set<UUID> mobList = activeSpawners.get(mobSpawner).getMobList();
 			
 			// Removing this mob from the mobSet, spawnList, and UUID map
 			mobList.remove(deadMobUUID);
 			activeMobs.remove(deadMobUUID);
 			
 			if (plugin.debug){ plugin.log.info(plugin.prefix + "MOB removed from Spawner: " + mobSpawner.getLocation().toString() + " Mob: [" + e.getEntity().getType().getName() + "] Spawn Count: [" + mobList.size() + "]");}
 				
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onChunkUnloadEvent(ChunkUnloadEvent e) {
 		
 		// Code to keep track of mobs in a chunk that is about to be unloaded
 		Chunk unloadingChunk = e.getChunk();
 		int detachedMobs = 0;
 		
 		for (Entity unloadingMob : unloadingChunk.getEntities()) {	
 			
 			if (activeMobs.containsKey(unloadingMob.getUniqueId())){
 			
 				// Setting their Entity object to NULL so we know they've been popped by the server
 				activeMobs.get(unloadingMob.getUniqueId()).setMobEntity(null);
 				detachedMobs++;
 				
 			}
 			
 		}
 		
 		if (plugin.debug && detachedMobs > 0){ plugin.log.info(plugin.prefix + detachedMobs + " spawner attached mobs were processed in UN-LOADING chunk: ." + unloadingChunk.toString());}
 		
 	}
 	
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onChunkLoadEvent(ChunkLoadEvent e) {
 		
 		// Code to keep track of mobs that were in a previously unloaded chunk
 		Chunk loadingChunk = e.getChunk();
 		int attachedMobs = 0;
 				
 		for (Entity loadingMob : loadingChunk.getEntities()) {	
 					
 			if (activeMobs.containsKey(loadingMob.getUniqueId())){
 					
 				// Setting their new entity object in the hashmap so we can use it later.
 				activeMobs.get(loadingMob.getUniqueId()).setMobEntity(loadingMob);
 				attachedMobs++;
 						
 			}
 					
 		}
 					
 		if (plugin.debug && attachedMobs > 0){ plugin.log.info(plugin.prefix + attachedMobs + " spawner attached mobs were processed in LOADING chunk: ." + loadingChunk.toString());}
 		
 		
 	}
 	
 }
