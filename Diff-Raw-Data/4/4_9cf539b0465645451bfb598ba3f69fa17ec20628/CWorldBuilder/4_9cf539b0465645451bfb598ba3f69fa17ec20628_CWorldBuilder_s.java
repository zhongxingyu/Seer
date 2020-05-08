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
 
 import java.util.ArrayDeque;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 import castro.base.plugin.CPlugin;
 import castro.base.plugin.CPluginSettings;
 import castro.blocks.CBlock;
 
 import com.sk89q.worldedit.Vector;
 
 
 public class CWorldBuilder extends CPlugin implements Runnable
 {
 	private static CWorldBuilder instance;
 	
 	public static Player commandPlayer;
 	public static World  commandWorld;
 	
 	public static Player executePlayer; // current player while executing run()
 	public static HashMap<String, CWBPlayer> players = new HashMap<>();
 	
 	public static Queue<BlockQueue>  queues = new ArrayDeque<BlockQueue>();
 	public static Queue<BlockQueue> lqueues = new ArrayDeque<BlockQueue>();
 	public static BlockQueue lastQueue; // for adding
 	
 	
 	private static boolean eq(Player p1, Player p2) { return p1.getName().equals(p2.getName()); }
 	private static void sendMsg(Player player) { get().sendMessage(player, "&cMozesz miec tylko jedna aktywna kolejke. Wpisz &a/cwb stop &caby zatrzymac kolejke."); }
 	public static boolean addQueue(Player p, boolean message)
 	{
 		flush(); // In case we have another queue
 		
 		// Check if player is already in queue
 		for(BlockQueue q :  queues) if(eq(q.player, p)) { if(message) sendMsg(p); return false; }
 		for(BlockQueue q : lqueues) if(eq(q.player, p)) { if(message) sendMsg(p); return false; }
 		
 		loadChunksForWorld(p.getWorld().getName());
 		
 		commandPlayer = p;
 		commandWorld  = p.getWorld();
 		lastQueue = new BlockQueue(p);
 		return true;
 	}
 	
 	
 	public static void addBlock(CBlock b)
 	{
 		if(lastQueue == null)
 			return;
 		
 		if(b.getId() == 0)
 			if(lastQueue.omitAir)
 				return;
 		lastQueue.queue.add(b);
 	}
 	
 	
 	public static void flush()
 	{
 		// Add last queue to appropriate queues list
 		if(lastQueue == null)
 			return;
 		
 		if(lastQueue.queue.size() > 0)
 			if(lastQueue.queue.size() > 50000)
 				lqueues.add(lastQueue);
 			else
 				queues.add(lastQueue);
 		lastQueue = null;
 	}
 	
 	
 	public static void remove(Player p)
 	{
 		for(BlockQueue q :  queues) if(eq(q.player, p)) {  queues.remove(q); return; }
 		for(BlockQueue q : lqueues) if(eq(q.player, p)) { lqueues.remove(q); return; }
 	}
 	
 	
 	public static HashMap<String, boolean[][]> lockedChunksWorlds = new HashMap<>();
 	public static boolean[][] lockedChunks = new boolean[50][50];
 	private static void loadChunksForWorld(String wname)
 	{
 		if(lockedChunksWorlds.containsKey(wname))
 			lockedChunks = lockedChunksWorlds.get(wname);
 		else
 		{
 			lockedChunks = new boolean[50][50];
 			lockedChunksWorlds.put(wname, lockedChunks);
 		}
 	}
 	
 	public static boolean isLocked(World world, int x, int z)
 	{
 		try { return lockedChunks[x][z]; } catch(ArrayIndexOutOfBoundsException e) { return false; }
 	}
 	
 	private static boolean loadChunk(Block block) { return loadChunk(block.getWorld(), block.getX() >> 4, block.getZ() >> 4); }
 	public static boolean loadChunk(World world, int x, int z)
 	{
 		if(isLocked(world, x, z))
 			return false;
 		
 		if (!world.isChunkLoaded(x, z)) // Try to load the chunk
 		{
 			world.loadChunk(x, z);
 			if(!world.isChunkLoaded(x, z)) // If chunk is still not loaded, add to limited chunks in order not to load it again
 			{
 				try { lockedChunks[x][z] = true; } catch(IndexOutOfBoundsException e) {}
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	
 	@Override
 	public void run()
 	{
 		//run_impl();
 		/**/
 		long start = System.currentTimeMillis();	
 		
 		while(System.currentTimeMillis() - start < 15)
 		{
 			//reset();
 			if(!run_impl())
 				return;
 			//timeStep("100 blocks");
 		}
 		/**/
 	}
 	
 	
 	private Queue<BlockQueue> getNextQueues()
 	{
 		if(queues.isEmpty())
 			if(lqueues.isEmpty())
 				return null;
 			else
 				return lqueues;
 		return queues;
 	}
 	
 	private boolean run_impl()
 	{
 		Queue<BlockQueue> currentQueues = getNextQueues();
 		if(currentQueues == null)
 			return false;
 		
 		BlockQueue queue = currentQueues.peek();
 		loadChunksForWorld(queue.player.getWorld().getName());
 		executePlayer = queue.player;
 		try
 		{
 			for(int i = 0; i < 100; ++i)
 			{
 				CBlock block = queue.queue.remove();
 				Block b = block.getBlock();
 				
 				if(!loadChunk(b))
 					continue;
 				
 				if(!queue.omitPerm)
 					if(!block.canBuild(b))
 						continue;
 				
 				block.execute(b);
 				/** TEMPORARY REMOVED LOGGING
 				if(queue.omitLog)
 					block.execute(b);
 				else
 				{
 					BlockState before = b.getState();
 					block.execute(b);
 					BlockState after = b.getState();
 					CConnector.registerChange(executePlayer, before, after);
 				}
 				*/
 			}
 		} catch(NoSuchElementException e) {}
 		
 		if(queue.queue.isEmpty())
 			currentQueues.remove();
 		
 		return true;
 	}
 	
 	
 	public static Location getLocation(World world, Vector pt)
 	{
 		return new Location(world, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
 	}
 	
 	
 	@Override
 	protected CPluginSettings getSettings()
 	{
 		CPluginSettings settings = new CPluginSettings();
 		settings.commandMgr = new CommandMgr();
 		return settings;
 	}
 	
 	
 	@Override
 	protected void init()
 	{
		instance = this;
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 1, 1); // schedule run to run every tick
 	}
 	
 	
 	public static CWorldBuilder get()
 	{
 		return instance;
 	}
 }
