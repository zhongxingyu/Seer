 package com.xpansive.bukkit.expansiveterrain;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import org.bukkit.Location;
 import org.bukkit.block.Biome;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.util.noise.*;
 import com.xpansive.bukkit.expansiveterrain.util.VoronoiNoise;
 import com.xpansive.bukkit.expansiveterrain.populators.*;
 
 public class ExpansiveTerrainChunkGenerator extends ChunkGenerator {
     private VoronoiNoise voronoi;
     private Random random;
     private PerlinNoiseGenerator perlin;
     private boolean initalized;
     private String worldName;
 
     private final int OCEAN_LEVEL = 55;
    private final int SNOW_LEVEL = 110;
     private final int MIN_DIRT_DEPTH = 3;
     private final int MAX_DIRT_DEPTH = 7;
 
     public ExpansiveTerrainChunkGenerator(String worldName) {
         this.worldName = worldName;
     }
 
     public byte[] generate(World world, Random r, int cx, int cz) {
         if (!initalized) {
             random = new Random(world.getSeed());
             voronoi = new VoronoiNoise(random, worldName);
             perlin = new PerlinNoiseGenerator(random);
             initalized = true;
         }
         byte[] result = new byte[32768];
         int[][] voronoiBuf = voronoi.genChunks(cx * 16, cz * 16, 16, 16, 2);
 
         for (int x = 0; x < 16; x++) {
             for (int z = 0; z < 16; z++) {
                 Biome biome = world.getBiome(cx * 16 + x, cz * 16 + z);
 
                 // This is where the magic happens
                 int height = OCEAN_LEVEL;
                 height *= perlin.noise(((double) (cx * 16 + x)) / 300, ((double) (cz * 16 + z)) / 300, 3, 2, 0.7) * 0.8 + 0.8;
                 height += Math.min(perlin.noise(((double) (cx * 16 + x)) / 50, ((double) (cz * 16 + z)) / 50, 3, 2, 0.7) + 1, 1) * 15;
                 height += voronoiBuf[x][z] / 17;
 
                height = Math.min(height, 126);
 
                 fillColumn(x, z, height, biome, result);
             }
         }
 
         return result;
 
     }
 
     void fillColumn(int x, int z, int height, Biome biome, byte[] buffer) {
         int dirtHeight = MIN_DIRT_DEPTH + (int) (random.nextDouble() * ((MAX_DIRT_DEPTH - MIN_DIRT_DEPTH) + 1));
 
         for (int y = 0; y <= Math.max(height, OCEAN_LEVEL); y++) {
             int offset = getOffset(x, y, z);
             if (y <= height) {
                 // cover the bottom layer with bedrock
                 if (y == 0) {
                     buffer[offset] = (byte) Material.BEDROCK.getId();
                 }
 
                 // top layer gets grass
                 else if (y == height && y >= OCEAN_LEVEL) {
                     if (biome == Biome.DESERT) {
                         buffer[offset] = (byte) Material.SAND.getId();
                     } else {
                         buffer[offset] = (byte) Material.GRASS.getId();
                     }
                 }
 
                 else if (y > height - dirtHeight) {
                     if (biome == Biome.DESERT) {
                         buffer[offset] = (byte) Material.SANDSTONE.getId();
                     } else {
                         buffer[offset] = (byte) Material.DIRT.getId();
                     }
                 }
 
                 else {
                     buffer[offset] = (byte) Material.STONE.getId();
                 }
 
                 if (((y > SNOW_LEVEL && biome != Biome.DESERT) || biome == Biome.TUNDRA || biome == Biome.TAIGA) && y + 1 < 128) {
                     buffer[getOffset(x, y + 1, z)] = (byte) Material.SNOW.getId();
                 }
 
             } else {
                 if (y <= OCEAN_LEVEL && buffer[offset] == 0) {
                     buffer[offset] = (byte) Material.STATIONARY_WATER.getId();
                 }
                 if (y == OCEAN_LEVEL && biome == Biome.TAIGA || biome == Biome.TUNDRA) {
                     buffer[offset] = (byte) Material.ICE.getId();
                 }
             }
         }
     }
 
     int getOffset(int x, int y, int z) {
         return (x * 16 + z) * 128 + y;
     }
 
     public boolean canSpawn(World world, int x, int z) {
         return true;
     }
 
     public List<BlockPopulator> getDefaultPopulators(World world) {
         return Arrays.asList((BlockPopulator) new PumpkinPopulator(), new FlowerPopulator(), new TreePopulator(), new OrePopulator(), new MushroomPopulator(), new WildGrassPopulator(), new CactusPopulator());
     }
 
     @Override
     public Location getFixedSpawnLocation(World world, Random random) {
         int x = 0;// random.nextInt(250) - 250;
         int z = 0;// random.nextInt(250) - 250;
         int y = world.getHighestBlockYAt(x, z);
         return new Location(world, x, y, z);
     }
 }
