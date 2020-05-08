 package ch.k42.metropolis.generator;
 
 import ch.k42.metropolis.WorldEdit.ClipboardProviderWorldEdit;
 import ch.k42.metropolis.generator.populators.BedrockFloorPopulator;
 import ch.k42.metropolis.generator.populators.CavePopulator;
 import ch.k42.metropolis.generator.populators.OrePopulator;
 import ch.k42.metropolis.model.provider.*;
 import ch.k42.metropolis.plugin.ContextConfig;
 import ch.k42.metropolis.plugin.MetropolisPlugin;
 import org.bukkit.*;
 import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
 import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.generator.ChunkGenerator;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Thomas
  * Date: 16.09.13
  * Time: 00:29
  * To change this template use File | Settings | File Templates.
  */
 public class MetropolisGenerator extends ChunkGenerator {
 
     private class MetropolisBlockPopulator extends BlockPopulator {
 
         @Override
         public void populate(World aWorld, Random random, Chunk chunk) {
             try {
                 MetropolisGenerator.this.initializeWorldInfo(aWorld);
                 gridProvider.populate(MetropolisGenerator.this, chunk);
                 gridProvider.postPopulate(MetropolisGenerator.this, chunk);
             } catch (Exception e) {
                 reportException("BlockPopulator FAILED", e);
             }
         }
     }
 
     private MetropolisPlugin plugin;
     private World world;
     private Long worldSeed;
 
     public String worldName;
     public World.Environment worldEnvironment;
 
     private ClipboardProviderWorldEdit clipboardProvider;
     private GridProvider gridProvider;
     private ContextProvider contextProvider;
     public DecayProvider decayProvider;
     public EnvironmentProvider natureDecay;
 
     public MetropolisGenerator(MetropolisPlugin plugin, String worldName, World.Environment env) {
         this.plugin = plugin;
         this.worldName = worldName;
         this.worldEnvironment = env;
     }
 
     public ClipboardProviderWorldEdit getClipboardProvider() {
         return clipboardProvider;
     }
 
     public GridProvider getGridProvider() {
         return gridProvider;
     }
 
     public ContextProvider getContextProvider() {
         return contextProvider;
     }
 
     public DecayProvider getDecayProvider() {
         return decayProvider;
     }
 
     public EnvironmentProvider getNatureDecayProvider() {
         return natureDecay;
     }
 
     public MetropolisPlugin getPlugin() {
         return plugin;
     }
 
     public String getPluginName() {
         return plugin.getName();
     }
 
     public World getWorld() {
         return world;
     }
 
     public Long getWorldSeed() {
         return worldSeed;
     }
 
     @Override
     public List<BlockPopulator> getDefaultPopulators(World world) {
         List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
         populators.add(new MetropolisBlockPopulator());
         populators.add(new CavePopulator());
         populators.add(new OrePopulator(world, plugin.getPopulatorConfig().getOres())); // last place some ore
         populators.add(new BedrockFloorPopulator());
         return populators;
     }
 
     public void initializeWorldInfo(World aWorld) {
 
         // initialize the shaping logic
         if (world == null) {
             world = aWorld;
 
             worldSeed = world.getSeed();
 
             if (worldEnvironment == World.Environment.NETHER) {
                 decayProvider = new DecayProviderNether(this, new Random(worldSeed + 6));
                 natureDecay = new NetherEnvironmentProvider(worldSeed);
             } else {
                 decayProvider = new DecayProviderNormal(this, new Random(worldSeed + 6));
                 natureDecay = new NormalEnvironmentProvider(worldSeed);
             }
 
             gridProvider = new GridProvider(this);
             contextProvider = new ContextProvider(this, plugin.getContextConfig());
 
             try {
                 clipboardProvider = new ClipboardProviderWorldEdit(this);
             } catch (Exception e) {
                 plugin.getLogger().warning("Failed to load clipboard: " + e.getMessage());
             }
         }
     }
 
     @Override
     public byte[][] generateBlockSections(World aWorld, Random random, int chunkX, int chunkZ, BiomeGrid biomes) {
         try {
 
             initializeWorldInfo(aWorld);
 
             byte[][] chunk = new byte[world.getMaxHeight() / 16][];
             for (int x = 0; x < 16; x++) { //loop through all of the blocks in the chunk that are lower than maxHeight
                 for (int z = 0; z < 16; z++) {
                     int maxHeight = 65; //how thick we want out flat terrain to be
                     for (int y = 1; y < maxHeight; y++) {
                         Material decay = natureDecay.checkBlock(world, (chunkX * 16) + x, y, (chunkZ * 16) + z);
                         if (decay != null) {
                             setBlock(x, y, z, chunk, decay);
                         } else {
                             setBlock(x, y, z, chunk, Material.STONE);
                         }
                     }
                 }
             }
             return chunk;//byteChunk.blocks;
 
         } catch (Exception e) {
             reportException("ChunkPopulator FAILED: " + e.getMessage(), e);
             return null;
         }
     }
 
     @Override
     public byte[] generate(World world, Random random, int x, int z) {
         //reportDebug("generate !!!");
         return super.generate(world, random, x, z);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     @Override
     public short[][] generateExtBlockSections(World world, Random random, int x, int z, BiomeGrid biomes) {
         //reportDebug("generateExtBlockSections !!!");
         return super.generateExtBlockSections(world, random, x, z, biomes);    //To change body of overridden methods use File | Settings | File Templates.
     }
 
     private void setBlock(int x, int y, int z, byte[][] chunk, Material material) {
         if (chunk[y >> 4] == null)
             chunk[y >> 4] = new byte[16 * 16 * 16];
         if (!(y <= 256 && y >= 0 && x <= 16 && x >= 0 && z <= 16 && z >= 0))
             return;
         try {
             chunk[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = (byte) material
                     .getId();
         } catch (Exception e) {
             // do nothing
         }
     }
 
     private final static int spawnRadius = 100;
 
     @Override
     public Location getFixedSpawnLocation(World world, Random random) {
         int spawnX = random.nextInt(spawnRadius * 2) - spawnRadius;
         int spawnZ = random.nextInt(spawnRadius * 2) - spawnRadius;
 
         // find the first non empty spot;
         int spawnY = world.getMaxHeight();
         while ((spawnY > 0) && world.getBlockAt(spawnX, spawnY - 1, spawnZ).isEmpty()) {
             spawnY--;
         }
 
         // return the location
         return new Location(world, spawnX, spawnY, spawnZ);
     }
 
 
     public void reportMessage(String message) {
         plugin.getLogger().info(message);
     }
 
     public void reportDebug(String message) {
         if (plugin.getMetropolisConfig().isDebugEnabled())
             plugin.getLogger().info("[====DEBUG====]" + message);
     }
 
     public void reportMessage(String message1, String message2) {
         plugin.getLogger().info(message1 + "     " + message2);
     }
 
     public void reportException(String message, Exception e) {
         plugin.getLogger().warning(message + " ---- " + e.getMessage());
         e.printStackTrace();
     }
 }
