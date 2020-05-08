 package com.gmail.zariust.mcplugins.othergrowth;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 
 
 
 public class RunSync implements Runnable {
 
 	private final OtherGrowth plugin;
 	public RunSync(OtherGrowth plugin) {
 		this.plugin = plugin;
 	}
 	
 /*
 	private void checkNearAndReplace(Block block, Material require, Material replacement, Double chance) {
 		for (BlockFace face : BlockFace.values()) {
 			Block relativeBlock = block.getRelative(face);
 			if (relativeBlock !=null && relativeBlock.getType() == require) {
 				// note: can't modify in async so store in a map that a sync thread will check
 				// or trigger an event
 				if (rolldice(chance)) {
 					triggerEvent(block, replacement); 
 				}
 			}
 		}
 	}
 
 	//world level conditions: world, time, weather
 	//block level conditions: biome, region, height, lightlevel
 
 	private void triggerEvent(Block block, Material replacement) {
 		Bukkit.getServer().broadcastMessage("Working!");		
 	}
 
 */
 
 	private void SyncProcess() {
 		Log.high("Starting sync replacements...");
		int count = 0;
 		
 		if (OtherGrowthConfig.globalScanLoadedChunks) {
 			gatherLoadedChunks();
 		} else {
 			gatherChunks();
 		}
 
 		MatchResult result = OtherGrowth.results.poll();
 		while (result != null) {
			count++;
 			ChunkSnapshot chunkSnapshot = result.getChunkSnapshot();
			Location loc = result.getLocation();
 			World world = Bukkit.getServer().getWorld(chunkSnapshot.getWorldName());
 			
 			Block block = world.getChunkAt(chunkSnapshot.getX(), chunkSnapshot.getZ()).getBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 
 			Recipe recipe = result.getRecipe();
 			
 			Log.highest("Got a result: "+block.getType().toString());
 			// check faces
 			boolean neededMatch = false;
 			
 			// FIXME: don't really want to do this once per recipe - rearrange so 
 			// we can do this outside the recipe and save the number/type of blocks in a map or something
 			if (recipe.needed != null) {
 				Log.highest("Searching for needed: "+recipe.needed);
 				for (BlockFace face : BlockFace.values()) {
 					if (block.getRelative(face).getType() == recipe.needed) neededMatch = true;
 				} 
 			} else {
 				neededMatch = true; // null material needed
 			}
 
 			// match regions
 //			if (!isInRegion(world, block.getLocation().clone(), recipe.regions)) {
 //				neededMatch = false;
 //			}
 			
 			
 			if (neededMatch) {
 				Log.highest("Replacing with: "+recipe.replacementMat);
 				block.setType(recipe.replacementMat);
 			}
 			//	Block block = world.getBlock(x,y,z);
 			//		recipe = recipeSet.get(block.getType());
 			//		if (recipe != null) {
 			//			if (block.getBiome().matches(recipe.biome)) {
 			//					checkNearAndReplace(block, recipe.require, recipe.replacement, recipe.chance);
 			//				}
 			//	}
 			//	}
 			//	}
 
 			
 			result = OtherGrowth.results.poll();
 		}
 		Log.high("Sync complete, replaced ("+count+" blocks)");
 	}
 //
 //	private boolean isInRegion(World world, Location location, Map<String, Boolean> regionConditions) {
 //		Set<String> regions = new HashSet<String>();
 //		
 //		Map<String, ProtectedRegion> regionMap = Dependencies.getWorldGuard().getGlobalRegionManager().get(world).getRegions();
 //		Vector vec = new Vector(location.getX(), location.getY(), location.getZ());
 //		for(String region : regionMap.keySet()) {
 //			if(regionMap.get(region).contains(vec))
 //				if (regionConditions.containsKey(region)) return true;
 //		}
 //		
 //		return regions;
 //	}    
 
 	public void run() {
 		SyncProcess();
 	}
 	
 
 	private void gatherLoadedChunks() {
 		OtherGrowth.gatheredChunks.clear();
 
 		for (World world : Bukkit.getServer().getWorlds()) {
 			if (world.getLoadedChunks().length > 0) {
 				Log.high("World: "+world.toString());
				Set<ChunkSnapshot> chunkSnaps = new HashSet<ChunkSnapshot>();
 				for (Chunk chunk : world.getLoadedChunks()) {
 					OtherGrowth.gatheredChunks.add(chunk.getChunkSnapshot());
 				}
				Log.high("Chunks gathered: "+chunkSnaps.size()+", World: "+world.toString()+": "+world.getLoadedChunks().length);
 			}
 		}
 	}
 
 	private void gatherChunks() {
 		OtherGrowth.gatheredChunks.clear();
 
 		Set<Chunk> chunksSeen = new HashSet<Chunk>();
 		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
 
 			int chunkRadius = OtherGrowthConfig.globalChunkScanRadius;
 
 			ChunkSnapshot playerChunk = player.getLocation().getChunk().getChunkSnapshot();
 			Log.high("Scanning chunks "+chunkRadius+" radius around "+player.getDisplayName()+" ("+playerChunk.getX()+", "+playerChunk.getZ()+")");
 			Log.high("Total loaded chunks = "+player.getWorld().getLoadedChunks().length);
 
 			for (int x = (-1*chunkRadius); x<=(chunkRadius); x++) {
 				for (int z = (-1*chunkRadius); z<=(chunkRadius); z++) {
 					if (!chunksSeen.contains(player.getWorld().getChunkAt(playerChunk.getX()+x, playerChunk.getZ()+z))) {
 
 						chunksSeen.add(player.getWorld().getChunkAt(playerChunk.getX()+x, playerChunk.getZ()+z));
 
 						Chunk chunk = player.getWorld().getChunkAt(playerChunk.getX()+x, playerChunk.getZ()+z);
 						if (chunk.isLoaded()) {
 							ChunkSnapshot currentChunkSnapShot = chunk.getChunkSnapshot();
 							Log.highest("Saving chunk "+currentChunkSnapShot.getX()+", "+currentChunkSnapShot.getZ());
 
 							OtherGrowth.gatheredChunks.add(currentChunkSnapShot);
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
