 /* cWorldBuilder
  * Copyright (C) 2013 Norbert Kawinski (norbert.kawinski@gmail.com)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package castro.builder;
 
 import java.util.HashMap;
 
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.WorldUnloadEvent;
 
 
 class CWBWorld
 {
 	private static final int SIZE = 100;
 	private static final int HALF_SIZE = 50;
 	public boolean[][] lockedChunks;
 	
 	
 	public CWBWorld()
 	{
 		this.lockedChunks = new boolean[SIZE][SIZE];
 	}
 	
 	
 	public boolean isLocked(int x, int z)
 	{
 		try
 		{
 			return lockedChunks[HALF_SIZE+x][HALF_SIZE+z];
 		}
 		catch(ArrayIndexOutOfBoundsException e)
 		{
 			return false;
 		}
 	}
 	
 	
 	public void lock(int x, int z)
 	{
 		try
 		{
 			lockedChunks[HALF_SIZE+x][HALF_SIZE+z] = true;
 		}
 		catch(IndexOutOfBoundsException e)
 		{
 		}
 	}
 }
 
 
 public class CWBWorlds implements Listener
 {
 	private static HashMap<String, CWBWorld> worlds = new HashMap<>();
 	private static CWBWorld cWorld;
 	
 	
 	@EventHandler
 	public void onWorldUnload(WorldUnloadEvent event)
 	{
 		worlds.remove(event.getWorld().getName());
 	}
 	
 	
 	public static void loadChunksForWorld(String worldname)
 	{
 		cWorld = worlds.get(worldname);
 		if(cWorld == null)
		{
			cWorld = new CWBWorld();
			worlds.put(worldname, cWorld);
		}
 	}
 	
 	
 	public static boolean isLocked(int x, int z)
 	{
 		return cWorld.isLocked(x, z);
 	}
 	
 	
 	public static boolean loadChunk(Block block)
 	{
 		return loadChunk(block.getWorld(), block.getX() >> 4, block.getZ() >> 4);
 	}
 	public static boolean loadChunk(World world, int x, int z)
 	{
 		if(isLocked(x, z))
 			return false;
 		
 		world.loadChunk(x, z); // Try to load the chunk
 		if(!world.isChunkLoaded(x, z)) // If chunk is still not loaded, add to limited chunks in order not to load it again
 		{
 			cWorld.lock(x, z);
 			return false;
 		}
 		
 		return true;
 	}
 }
