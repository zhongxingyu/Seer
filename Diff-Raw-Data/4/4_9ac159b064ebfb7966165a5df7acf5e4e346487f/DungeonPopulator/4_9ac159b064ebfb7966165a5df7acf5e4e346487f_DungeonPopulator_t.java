 package net.nexisonline.spade.populators;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
import net.nexisonline.spade.SpadeConf;
 import net.nexisonline.spade.SpadePlugin;
 
 import org.bukkit.Chunk;
 import org.bukkit.World;
 
 import toxi.math.noise.SimplexNoise;
 
 public class DungeonPopulator extends SpadeEffectGenerator
 {
 	private Random m_random;
 	private SimplexNoise m_density;
 	private int maxRooms;
 	public List<Dungeon> queuedDungeons = new ArrayList<Dungeon>();
 	
 	public DungeonPopulator(SpadePlugin plugin, Map<String,Object> node, long seed) {
 		super(plugin, node, seed);
 		
		this.maxRooms = SpadeConf.getInt(node,"num-rooms",13);
 		
 		m_density = new SimplexNoise(seed + 343543);
 		m_density.setFrequency(0.01);
 		m_density.setAmplitude(maxRooms * 0.1);
 
 		m_random = new Random(seed + 3531244);
 	}
 	
 
 	public static SpadeEffectGenerator getInstance(SpadePlugin plugin, Map n, long seed) {
 	    Map<String,Object> node = (Map<String,Object>)n;
 		return new DungeonPopulator(plugin,node,seed);
 	}
 
 	@Override
 	public void populate(World w, Random rnd, Chunk chunk)
 	{
 //		SpadeLogging.info(String.format("Generating dungeons in chunk (%d,%d) (maxrooms = %d)",chunk.getX(),chunk.getZ(),this.maxRooms));
 		world=w;
 		double density = m_density.noise(chunk.getX() * 16, chunk.getZ() * 16);
 		//Logger.getLogger("Minecraft").info(String.format("Density: %.2f", density));
 
 		int chunkY = m_random.nextInt(64);
 
 		if (density > 0.8)
 		{
 			int roomCount = (int)(density * 10);
 
 			for (int i = 0; i < roomCount; i++)
 			{
 				int x = m_random.nextInt(16);
 				int y = chunkY + m_random.nextInt(2);
 				int z = m_random.nextInt(16);
 
 				int width = m_random.nextInt(16) + 1;
 				int height = m_random.nextInt(8) + 2;
 				int depth = m_random.nextInt(16) + 1;
 
 				x += chunk.getX() * 16;
 				z += chunk.getZ() * 16;
 
 				//Logger.getLogger("Minecraft").info(String.format("Width: %d Height: %d Depth: %d", width, height, depth));
 				queuedDungeons.add(new Dungeon(this, world, x, y, z, width, height, depth));
 			}
 		}		
 	}
 	
 	@Override
 	public Map<String, Object> getConfiguration() {
 		Map<String,Object> n = new HashMap<String,Object>();
 		n.put("max-rooms",maxRooms);
 		return n;
 	}
 
 
 	public void onChunkLoad(int x, int z) {
 		for(Dungeon d : new ArrayList<Dungeon>(queuedDungeons)) {
 			d.onChunkLoaded(x, z);
 		}
 	}
 }
