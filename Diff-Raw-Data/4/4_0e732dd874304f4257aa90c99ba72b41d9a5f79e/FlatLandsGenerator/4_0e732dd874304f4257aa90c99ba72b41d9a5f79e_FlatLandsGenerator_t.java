 package edu.unca.smmattic.FlatLands;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.TreeType;
 import org.bukkit.World;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 public class FlatLandsGenerator extends ChunkGenerator {
 
 	private FlatLands plugin;
 
 	public FlatLandsGenerator(FlatLands instance) {
 		this.plugin = instance;
 	}
 
 	public List<BlockPopulator> getDefaultPopulators(World world) {
 		ArrayList<BlockPopulator> populators = new ArrayList<BlockPopulator>();
 
 		populators.add(new FlatLandsTreePopulator());
 
 		return populators;
 	}
    
 	public Location getFixedSpawnLocation(World world, Random random) {
 		return new Location(world, 20, 20, 20);
 	}
 
 	private int coordsToInt(int x, int y, int z) {
 		return (x * 16 + z) * 128 + y;
 	}
 
 	public byte[] generate(World world, Random random, int chunkX, int chunkZ) {
 		byte[] blocks = new byte[32768];
 		int x, y, z;
 
 		for (x = 0; x < 16; ++x) {
 			for (z = 0; z < 16; ++z) {
 				blocks[this.coordsToInt(x, 0, z)] = (byte) Material.BEDROCK.getId();
 
 				for (y = 1; y < 20; ++y) {
 					if (y == 1) {
 						if (random.nextInt(100) <= 35) {
 							int block = (random.nextInt(100) <= 35) ? Material.DIAMOND_ORE.getId() : Material.STONE.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 					} else if (y == 2) {
 						if (random.nextInt(100) <= 55) {
 							int block = (random.nextInt(100) <= 55) ? Material.EMERALD_ORE.getId() : Material.STONE.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 					} else if (3 == y || y == 4) {
 						blocks[this.coordsToInt(x, y, z)] = (byte) Material.GLOWSTONE.getId();
 					} else if (5 == y) {
 						if (random.nextInt(100) <= 65) {
 							int block = (random.nextInt(100) <= 65) ? Material.IRON_ORE.getId() : Material.STONE.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 
 					} else if (6 == y) {
 
 						if (random.nextInt(100) <= 75) {
 							int block = (random.nextInt(100) <= 75) ? Material.COAL.getId() : Material.STONE.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 					} else if (7 == y || y == 8) {
 						if (random.nextInt(100) <= 65) {
 							int block = (random.nextInt(100) <= 65) ? Material.IRON_ORE.getId() : Material.STONE.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 					} else if (9 == y || y == 10) {
 						blocks[this.coordsToInt(x, y, z)] = (byte) Material.STONE.getId();
 					} else if (11 == y || y == 12) {
 						if (random.nextInt(100) <= 45) {
 							int block = (random.nextInt(100) <= 45) ? Material.IRON_ORE.getId() : Material.STONE.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 					} else if (13 == y || y == 14) {
 						if (random.nextInt(100) <= 85) {
 							int block = (random.nextInt(100) <= 85) ? Material.COAL.getId() : Material.STONE.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 					} else if (15 == y) {
 						blocks[this.coordsToInt(x, y, z)] = (byte) Material.STONE.getId();
 					} else if (16 == y) {
 						blocks[this.coordsToInt(x, y, z)] = (byte) Material.COBBLESTONE.getId();
 					} else if (17 == y) {
 						blocks[this.coordsToInt(x, y, z)] = (byte) Material.DIRT.getId();
 					} else if (18 == y) {
 						blocks[this.coordsToInt(x, y, z)] = (byte) Material.SAND.getId();
 					} else if (19 == y) {
 						if (random.nextInt(100) <= 85) {
 							int block = (random.nextInt(100) <= 85) ? Material.GRASS.getId() : Material.WATER.getId();
 							blocks[this.coordsToInt(x, y, z)] = (byte) block;
 						}
 					}
 				}
 				if (y == 20) {
 					if (random.nextInt(100) <= 65) {
 						int block = (random.nextInt(100) <= 70) ? Material.WATER.getId() : Material.GRASS.getId();
 						blocks[this.coordsToInt(x, y, z)] = (byte) block;
 
 					}
 				}
 			}
 		}
 		return blocks;
 	}
 }
