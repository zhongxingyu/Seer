 package com.ftwinston.Killer;
 
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.lang.ref.SoftReference;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
import net.minecraft.server.Convertable;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.RegionFile;
import net.minecraft.server.WorldLoaderServer;
 import net.minecraft.server.WorldServer;
 import net.minecraft.server.WorldType;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.WorldCreator;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 
 public class WorldManager
 {
 		public static WorldManager instance;
 		
 		private Killer plugin;
 		public WorldManager(Killer killer)
 		{
 			plugin = killer;
 			instance = this;
 			
 			if ( plugin.stopServerToRecreateWorld )
 				holdingWorld = null;
 			else
 			{
 				seedGen = new Random();
 				bindRegionFiles();
 				serverFolder = plugin.getServer().getWorldContainer();
 				holdingWorld = getOrCreateHoldingWorld();
 			}
 		}
 		
 		public void onDisable()
 		{
 			regionfiles = null;
 			rafField = null;
 			serverFolder = null;
 		}
 		
 		static File serverFolder;
 		Random seedGen;
 		
 		@SuppressWarnings("rawtypes")
 		private static HashMap regionfiles;
 		private static Field rafField;
 		
 		private final String holdingWorldName = "holding";
 		public World holdingWorld;
 		
 		private MinecraftServer getMinecraftServer()
 		{
 			try
 			{
 				CraftServer server = (CraftServer)plugin.getServer();
 				Field f = server.getClass().getDeclaredField("console");
 				f.setAccessible(true);
 				MinecraftServer console = (MinecraftServer)f.get(server);
 				f.setAccessible(false);
 				return console;}
 			catch ( IllegalAccessException ex )
 			{
 			}
 			catch  ( NoSuchFieldException ex )
 			{
 			}
 			
 			return null;
 		}
 				
 		private World getOrCreateHoldingWorld() 
 		{
 	        World world = plugin.getServer().getWorld(holdingWorldName);
 	        if ( world == null )
 	        {
 		        WorldCreator wc = new WorldCreator(holdingWorldName);
 		        wc.generateStructures(false);
 		        wc.generator(new HoldingWorldGenerator());
 				wc.environment(Environment.THE_END);
 				world = CreateWorld(wc, true, true, 8, 1, 8);
 	        }
 	        world.setAutoSave(false); // don't save changes to the holding world
 	        return world;
 		}
 		
 		static final int plinthPeakHeight = 76, spaceBetweenPlinthAndGlowstone = 4;
 		static final int plinthSpawnOffsetX = 20, plinthSpawnOffsetZ = 0;
 		public Location createPlinth(World world)
 		{
 			Location spawnPoint = world.getSpawnLocation();
 			int x = spawnPoint.getBlockX() + plinthSpawnOffsetX;
 			int z = spawnPoint.getBlockZ() + plinthSpawnOffsetZ;
 			
 			// a 3x3 column from bedrock to the plinth height
 			for ( int y = 0; y < plinthPeakHeight; y++ )
 				for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(Material.BEDROCK);
 					}
 			
 			// with one block sticking up from it
 			int y = plinthPeakHeight;
 			for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(ix == x && iz == z ? Material.BEDROCK : Material.AIR);
 					}
 			
 			// that has a pressure plate on it
 			y = plinthPeakHeight + 1;
 			Location pressurePlateLocation = new Location(world, x, y, z);
 			for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(ix == x && iz == z ? Material.STONE_PLATE : Material.AIR);
 					}
 					
 			// then a space
 			for ( y = plinthPeakHeight + 2; y <= plinthPeakHeight + spaceBetweenPlinthAndGlowstone; y++ )
 				for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(Material.AIR);
 					}
 			
 			// and then a 1x1 pillar of glowstone, up to max height
 			for ( y = plinthPeakHeight + spaceBetweenPlinthAndGlowstone + 1; y < world.getMaxHeight(); y++ )
 				for ( int ix = x - 1; ix < x + 2; ix++ )
 					for ( int iz = z - 1; iz < z + 2; iz++ )
 					{
 						Block b = world.getBlockAt(ix, y, iz);
 						b.setType(ix == x && iz == z ? Material.GLOWSTONE : Material.AIR);
 					}
 			
 			return pressurePlateLocation;
 		}
 
 		// for both the CraftServer and the MinecraftServer, remove the holding world from the front of their world lists, and add it back onto the end 
 		private void sortWorldOrder()
 		{
 			try
 			{
 				Field f = plugin.getServer().getClass().getDeclaredField("worlds");
 				f.setAccessible(true);
 				@SuppressWarnings("unchecked")
 				Map<String, World> worlds = (Map<String, World>)f.get(plugin.getServer());
 				f.setAccessible(false);
 				
 				worlds.remove(holdingWorldName);
 				worlds.put(holdingWorldName, holdingWorld);
 
 				/*plugin.log.info("CraftServer worlds:");
 				for ( Map.Entry<String, World> map : worlds.entrySet() )
 					plugin.log.info(" " + map.getKey() + " : " + map.getValue().getName());
 				
 						plugin.log.info("");
 						plugin.log.info("accessible format:");
 				for ( World world : plugin.getServer().getWorlds() )
 					plugin.log.info(" " + world.getName());*/
 			}
 			catch ( IllegalAccessException ex )
 			{
 				plugin.log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 			}
 			catch  ( NoSuchFieldException ex )
 			{
 				plugin.log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 			}
 			
 			WorldServer holdingWorldServer = ((CraftWorld)holdingWorld).getHandle();
 			
 			MinecraftServer ms = getMinecraftServer();
 			ms.worlds.remove(ms.worlds.indexOf(holdingWorldServer));
 			ms.worlds.add(holdingWorldServer);
 			
 			/*plugin.log.info("");
 			plugin.log.info("MinecraftServer worlds:");
 			for ( WorldServer world : ms.worlds )
 				plugin.log.info(" " + world.dimension);*/
 		}
 			
 		public World CreateWorld(WorldCreator wc, boolean loadChunks, boolean setSpawnLocation, int spawnX, int spawnY, int spawnZ)
 		{
 			World world = plugin.getServer().createWorld(wc);
 			
 			if (world != null)
 			{
 				if ( setSpawnLocation )
 					world.setSpawnLocation(spawnX, spawnY, spawnZ);
 				
 				if ( loadChunks )
 				{
 					final int keepdimension = 7;
 					int spawnx = world.getSpawnLocation().getBlockX() >> 4;
 				    int spawnz = world.getSpawnLocation().getBlockZ() >> 4;
 					for (int x = -keepdimension; x < keepdimension; x++)
 						for (int z = -keepdimension; z < keepdimension; z++)
 							world.loadChunk(spawnx + x, spawnz + z);
 				}
 				world.setKeepSpawnInMemory(loadChunks);
 				plugin.log.info("World '" + world.getName() + "' has been created successfully!");
 			}
 			else
 				plugin.log.info("World creation failed!");
 			
 			return world;
 		}
 		
 		public void removeAllItems(World world)
 		{
 			List<Entity> list = world.getEntities();
 			Iterator<Entity> entities = list.iterator();
 			while (entities.hasNext())
 			{
 				Entity entity = entities.next();
 				if (entity instanceof Item)
 					entity.remove();
 			}
 		}
 		
 		@SuppressWarnings("rawtypes")
 		private void bindRegionFiles()
 		{
 	        try
 	        {
 	        	Field a = net.minecraft.server.RegionFileCache.class.getDeclaredField("a");
 	        	a.setAccessible(true);
 				regionfiles = (HashMap) a.get(null);
 				rafField = net.minecraft.server.RegionFile.class.getDeclaredField("c");
 				rafField.setAccessible(true);
 	        	plugin.log.info("Successfully bound variable to region file cache.");
 	        	plugin.log.info("File references to unloaded worlds will be cleared!");
 			}
 	        catch (Throwable t)
 	        {
 	        	plugin.log.warning("Failed to bind to region file cache.");
 	        	plugin.log.warning("Files will stay referenced after being unloaded!");
 				t.printStackTrace();
 			}
 		}
 		
 		@SuppressWarnings("rawtypes")
 		private boolean clearWorldReference(String worldName)
 		{
 			if (regionfiles == null) return false;
 			if (rafField == null) return false;
 			
 			ArrayList<Object> removedKeys = new ArrayList<Object>();
 			try
 			{
 				for (Object o : regionfiles.entrySet())
 				{
 					Map.Entry e = (Map.Entry) o;
 					File f = (File) e.getKey();
 					
 					if (f.toString().startsWith("." + File.separator + worldName))
 					{
 						SoftReference ref = (SoftReference) e.getValue();
 						try
 						{
 							RegionFile file = (RegionFile) ref.get();
 							if (file != null)
 							{
 								RandomAccessFile raf = (RandomAccessFile) rafField.get(file);
 								raf.close();
 								removedKeys.add(f);
 							}
 						}
 						catch (Exception ex)
 						{
 							ex.printStackTrace();
 						}
 					}
 				}
 			}
 			catch (Exception ex)
 			{
 				plugin.log.warning("Exception while removing world reference for '" + worldName + "'!");
 				ex.printStackTrace();
 			}
 			for (Object key : removedKeys)
 				regionfiles.remove(key);
 			
 			return true;
 		}
 		
 		private void forceUnloadWorld(World world, World movePlayersTo)
 		{
 			world.setAutoSave(false);
 			if ( movePlayersTo == null )
 				for ( Player player : world.getPlayers() )
 					player.kickPlayer("World is being regenerated... and you were in it!");
 			else
 			{
 				for ( Player player : world.getPlayers() )
 					plugin.playerManager.putPlayerInWorld(player,  movePlayersTo);
 			}
 			
 			CraftServer server = (CraftServer)plugin.getServer();
 			CraftWorld craftWorld = (CraftWorld)world;
 			
 			try
 			{
 				Field f = server.getClass().getDeclaredField("worlds");
 				f.setAccessible(true);
 				@SuppressWarnings("unchecked")
 				Map<String, World> worlds = (Map<String, World>)f.get(server);
 				worlds.remove(world.getName().toLowerCase());
 				f.setAccessible(false);
 			}
 			catch ( IllegalAccessException ex )
 			{
 				plugin.log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 			}
 			catch  ( NoSuchFieldException ex )
 			{
 				plugin.log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 			}
 			
 			MinecraftServer ms = getMinecraftServer();
 			ms.worlds.remove(ms.worlds.indexOf(craftWorld.getHandle()));
 		}
 
 		public void deleteWorlds(final Runnable runWhenDone)
 		{
 			plugin.log.info("Clearing out old worlds...");
 			
 			List<World> worlds = plugin.getServer().getWorlds();
 			String[] worldNames = new String[worlds.size()-1];
 			int i=0;
 			
 			for ( World world : worlds )
 			{
 				if ( world.getName() == holdingWorldName )
 					continue;
 				
 				worldNames[i++] = world.getName();
 				
 				forceUnloadWorld(world, holdingWorld);
 				plugin.log.info("Unloaded " + world.getName());
 			}
 			
 			// now we want to try to delete the world folders
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new WorldDeleter(worldNames, new Runnable() {
 				public void run()
 				{
 					plugin.log.info("Generating new worlds...");
 					MinecraftServer ms = getMinecraftServer();
 
 					String s = ms.getPropertyManager().getString("level-name", "world");
 					String s2 = ms.getPropertyManager().getString("level-type", "DEFAULT");
 					WorldType worldtype = WorldType.getType(s2);
 
 					if (worldtype == null)
 						worldtype = WorldType.NORMAL;
 					
 					Method a;
 					try
 					{
						a = ms.getClass().getDeclaredMethod("a", Convertable.class, String.class, long.class, WorldType.class);
 					}
 					catch ( NoSuchMethodException ex )
 					{
 						plugin.log.warning("Unable to trigger default world generation, shutting down");
 						plugin.getServer().shutdown();
 						return;
 					}
 					
 					try
 					{
 						a.setAccessible(true);
						a.invoke(ms, new WorldLoaderServer(ms.server.getWorldContainer()), s, seedGen.nextLong(), worldtype);
 						a.setAccessible(false);
 					}
 					catch ( IllegalAccessException ex )
 					{
 						plugin.log.warning("Illegal access: " + ex.getMessage());
 					}
 					catch ( InvocationTargetException ex )
 					{
 						plugin.log.warning("Invocation target exception: " + ex.getMessage());
 					}
 
 					// now we want to ensure that the holding world gets put on the end of the worlds list, instead of staying at the beginning
 					// also ensure that the other worlds on the list are in the right order
 					sortWorldOrder();
 
 					// run whatever task was passed in, before putting players back in the main world
 					if ( runWhenDone != null )
 						runWhenDone.run();
 					
 					// move ALL players back into the main world
 					World defaultWorld = plugin.getServer().getWorlds().get(0);
 					for ( Player player : plugin.getServer().getOnlinePlayers() )
 					{
 						plugin.playerManager.resetPlayer(player, true);
 						plugin.playerManager.putPlayerInWorld(player,  defaultWorld);
 					}
 				}
 
 			}), 80);
 		}
 
 		private class WorldDeleter implements Runnable
 	    {
 	    	String[] worlds;
 	    	Runnable runWhenDone;
 	    	
 	    	static final long retryDelay = 30;
 	    	static final int maxRetries = 5;
 	    	int attempt;
 	    	
 	    	public WorldDeleter(String[] worlds, Runnable runWhenDone)
 	    	{
 	    		attempt = 0;
 	    		this.worlds = worlds;
 	    		this.runWhenDone = runWhenDone;
     		}
 	    	
 	    	public void run()
 	    	{
 	    		boolean allGood = true;
     			for ( String worldName : worlds )
     			{
     				clearWorldReference(worldName);
     				
 		    		try
 					{
 		    			if ( !delete(new File(serverFolder + File.separator + worldName)) )
 		    				allGood = false;
 					}
 					catch ( Exception e )
 					{
 						plugin.log.info("An error occurred when deleting the " + worldName + " world: " + e.getMessage());
 					}
     			}
 	    			
 	    		if ( !allGood )
 		    		if ( attempt < maxRetries )
 	    			{
 	    				plugin.log.info("Retrying world data deletion...");
 	    				attempt++;
 						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, retryDelay);
 						return;
 	    			}
 		    		else
 		    			plugin.log.warning("Failed to delete some world information. Continuing...");
 	    		
 	    		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runWhenDone, 20);
 	    	}
 	    }
 		
 		private boolean delete(File folder)
 		{
 			if ( !folder.exists() )
 				return true;
 			boolean retVal = true;
 			if (folder.isDirectory())
 				for (File f : folder.listFiles())
 					if (!delete(f))
 					{
 						retVal = false;
 						//plugin.log.warning("Failed to delete file: " + f.getName());
 					}
 			return folder.delete() && retVal;
 		}
 		
 		public class HoldingWorldGenerator extends org.bukkit.generator.ChunkGenerator
 		{
 			public byte[][] generateBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
 			{
 				byte[][] result = new byte[1][];
 				if ( cx == 0 && cz == 0)
 				{
 					for ( int x = 0; x < 16; x++ )
 						for ( int z = 0; z < 16; z++ )
 							setBlock(result, x, 0, z, bedrock);
 
 					for ( int y=1; y<15; y++ )
 					{
 						for ( int x = 0; x < 16; x++ )
 						{
 							setBlock(result, x, y, 0, bedrock);
 							setBlock(result, x, y, 15, bedrock);
 						}
 						for ( int z = 0; z < 16; z++ )
 						{
 							setBlock(result, 0, y, z, bedrock);
 							setBlock(result, 15, y, z, bedrock);
 						}
 					}
 					
 					setBlock(result, 1, 15, 1, glowstone);
 					setBlock(result, 1, 15, 14, glowstone);
 					setBlock(result, 14, 15, 1, glowstone);
 					setBlock(result, 14, 15, 14, glowstone);
 				}
 				return result;
 			}
 			
 			final byte bedrock = 7, glowstone = 89;
 			private void setBlock(byte[][] result, int x, int y, int z, byte blkid)
 			{
 		        if (result[y >> 4] == null)
 		            result[y >> 4] = new byte[4096];
 		            
 		        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
 		    }
 			
 			public Location getFixedSpawnLocation(World world, Random random)
 			{
 				return new Location(world, 8, 1, 8);
 			}
 		}
 }
