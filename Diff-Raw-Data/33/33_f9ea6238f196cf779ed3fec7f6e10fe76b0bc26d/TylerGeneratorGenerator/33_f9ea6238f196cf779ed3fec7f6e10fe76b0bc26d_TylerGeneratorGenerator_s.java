 package edu.unca.thaithco.TylerGenerator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.util.noise.SimplexOctaveGenerator;
 
 
 public class TylerGeneratorGenerator extends ChunkGenerator{
 
 	@SuppressWarnings("unused")
 	private TylerGenerator plugin;
 	
 	public TylerGeneratorGenerator(TylerGenerator instance){
 		this.plugin = instance;
 	}
 	
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		ArrayList<BlockPopulator> populators = new ArrayList<BlockPopulator>();
 		
 		populators.add(new TylerGeneratorTreePopulator());
 		
 		return populators;
 	}
 	
 	public Location getFixedSpawnLocation(World world, Random random) {
 		return new Location(world, 0, world.getHighestBlockYAt(0,0)+1, 0);
 	}
 	
 	private int coordsToInt(int x, int y, int z){
 		return 128 * ((16 * x) + z) + y;
 	}
 	
 	public byte[] generate(World world, Random random, int chunkX, int chunkZ){
 		byte[] blocks = new byte[32768];
 		int x, y, z;
 		
 		Random rand = new Random(world.getSeed());
 		SimplexOctaveGenerator octave = new SimplexOctaveGenerator(rand, 8);
 		
 		octave.setScale(1 / 64.0);
 		
 		for(x = 0; x < 16; x++) {
 			for(z = 0; z < 16; z++) {
 				
 				blocks[this.coordsToInt(x, 0, z)] = (byte)Material.BEDROCK.getId();
 				
 				double noise = octave.noise((chunkX * 16) + x, (chunkZ * 16) + z, 0.25, 0.25) * 32;
 				for(y = 1; y < noise + 64; y++) {
 					blocks[this.coordsToInt(x, y, z)] = (byte)Material.STONE.getId();
 				}
				for(y = (int)(noise + 64); y < noise + 71; y++) {
 					blocks[this.coordsToInt(x, y, z)] = (byte)Material.DIRT.getId();
 				}
				for(y = (int)(noise + 71); y < noise + 72; y++) {
					blocks[this.coordsToInt(x, y, z)] = (byte)Material.GRASS.getId();
				}
				if(blocks[this.coordsToInt(x, y, z)] == (byte)Material.AIR.getId() && y <= 64){
					blocks[this.coordsToInt(x, y, z)] = (byte)Material.STATIONARY_WATER.getId();
 				}
 			}
 		}
 		
 		return blocks;
 	}
 }
