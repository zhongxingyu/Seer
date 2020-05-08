 package wustendorf;
 
 import com.googlecode.flyway.core.Flyway;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.TickType;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartedEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.event.FMLServerStoppingEvent;
 import cpw.mods.fml.common.network.IConnectionHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.registry.TickRegistry;
 import java.io.*;
 import java.lang.reflect.*;
 import java.sql.*;
 import javax.sql.*;
 import java.util.*;
 import java.util.zip.GZIPInputStream;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraft.src.Block;
 import net.minecraft.src.DamageSource;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityDragon;
 import net.minecraft.src.EntityFlying;
 import net.minecraft.src.EntityLiving;
 import net.minecraft.src.EntityLightningBolt;
 import net.minecraft.src.EntityMob;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityPlayerMP;
 import net.minecraft.src.EntitySlime;
 import net.minecraft.src.EnumGameType;
 import net.minecraft.src.EnumSkyBlock;
 import net.minecraft.src.INetworkManager;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.MathHelper;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.NBTBase;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.NetHandler;
 import net.minecraft.src.NetServerHandler;
 import net.minecraft.src.Packet131MapData;
 import net.minecraft.src.Packet250CustomPayload;
 import net.minecraft.src.Packet1Login;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldServer;
 
 @Mod(
     modid="Wustendorf",
     name="Wustendorf",
     version="%conf:VERSION%",
     dependencies=""
 )
 @NetworkMod(
     clientSideRequired=true,
     serverSideRequired=false,
     packetHandler=Wustendorf.PacketHandler.class,
     channels={"wustendorf_light"},
     versionBounds="%conf:VERSION_BOUNDS%"
 )
 public class Wustendorf implements ITickHandler, IPlayerTracker {
 
     public Random random = new Random();
     private Connection masterDB = null;
     private Map<WorldServer, WustendorfDB> worldDBs = new HashMap<WorldServer, WustendorfDB>();
     private Map<WorldServer, Boolean> activeInWorld = new HashMap<WorldServer, Boolean>();
 
     public static boolean ranUpdate = false;
     public static final boolean DEBUG = false;
     public static Configuration config;
     public static Wustendorf instance = null;
     public static Set<World> lightUpdated = new HashSet<World>();
 
     public Map<Integer, Set<LightSource>> clientLightCache = new HashMap<Integer, Set<LightSource>>();
     public Map<Integer, Set<LightSource>> serverLightCache = new HashMap<Integer, Set<LightSource>>();
 
     @SidedProxy(clientSide = "wustendorf.ClientProxy", serverSide = "wustendorf.CommonProxy")
     public static CommonProxy proxy;
 
     public static int real_mod(long number, int modulus) {
         int mod = (int) (number % modulus);
         if (mod < 0) {
             // Java is a fucking idiot.
             mod += modulus;
         }
 
         return mod;
     }
 
     @SuppressWarnings("unchecked")
     public static DataSource getH2DataSource(File dbFile) throws Exception {
         // We create initialize the DataSource via reflection, to avoid a
         // trivial compile-time dependency.
 
         // (Seriously, why couldn't Flyway just take a JDBC URL?)
         // (Or even an actual Connection!)
         Class h2DataSource = Class.forName("org.h2.jdbcx.JdbcDataSource");
         DataSource ds = (DataSource) h2DataSource.newInstance();
         Method setURL = h2DataSource.getMethod("setURL", String.class);
         setURL.invoke(ds, "jdbc:h2:" + dbFile);
 
         return ds;
     }
 
     @Mod.PreInit
     public void preInit(FMLPreInitializationEvent event) {
         instance = this;
 
         // Load config file.
         config = new Configuration(event.getSuggestedConfigurationFile());
 
         File dbFile = new File(event.getModConfigurationDirectory(),
                                "wustendorf");
 
         try {
             // Get a reference to the database.
             DataSource ds = getH2DataSource(dbFile);
 
             // Use Flyway to create or upgrade the database, if needed.
             Flyway updater = new Flyway();
             updater.setDataSource(ds);
             updater.setLocations("wustendorf.flyway.masterdb");
             updater.setInitOnMigrate(true);
             updater.migrate();
 
             // And finally connnect to the database.
             masterDB = ds.getConnection();
         } catch (Exception e) {
             System.out.println("Wustendorf: Unable to initialize database:");
             e.printStackTrace();
         }
     }
 
     @Mod.Init
     public void init(FMLInitializationEvent event) {
         Side side = getSide();
 
         try {
             config.load();
         } catch (RuntimeException e) {} // Just regenerate the config if it's
                                         // broken.
         String id_strs[] = {
             config.get("wustendorf.marker", config.CATEGORY_BLOCK, 195).value,
         };
         config.save();
 
         int ids[] = new int[id_strs.length];
         try {
             for (int i=0; i<id_strs.length; i++) {
                 ids[i] = Integer.parseInt(id_strs[i]);
             }
         } catch (NumberFormatException e) {
         }
 
         // Register with ModLoader.
         WustendorfMarker marker = new WustendorfMarker(ids[0]);
         ItemStack one_marker = new ItemStack(marker, 1);
         GameRegistry.registerBlock(marker);
         //GameRegistry.registerTileEntity(MarkerTileEntity.class, "House Flag");
 
         proxy.init(marker);
 
         // Add crafting recipes
         GameRegistry.addRecipe(one_marker,
                 "|# ",
                 "|##",
                 "|  ",
             Character.valueOf('|'), Item.stick,
             Character.valueOf('#'), Block.cloth
         );
 
         // Register the GUI handler.
         //NetworkRegistry.instance().registerGuiHandler(this, new WustendorfGuiHandler());
 
         // We want to handle server-side ticks.
         TickRegistry.registerTickHandler(this, Side.SERVER);
 
         // And player connect events.
         GameRegistry.registerPlayerTracker(this);
     }
 
     public static Side getSide() {
         return FMLCommonHandler.instance().getEffectiveSide();
     }
 
     public static Wustendorf instance() {
         return instance;
     }
 
     @Mod.ServerStarting
     public void beforeServerStart(FMLServerStartingEvent event) {
         event.registerServerCommand(new CommandWustendorf());
     }
 
     @Mod.ServerStarted
     public void onServerStart(FMLServerStartedEvent event) {
         for (WustendorfDB worldDB : worldDBs.values()) {
             worldDB.close();
         }
         clientLightCache.clear();
         serverLightCache.clear();
         worldDBs.clear();
         activeInWorld.clear();
     }
 
     @Mod.ServerStopping
     public void onServerStopping(FMLServerStoppingEvent event) {
         for (WustendorfDB worldDB : worldDBs.values()) {
             worldDB.close();
         }
         clientLightCache.clear();
         serverLightCache.clear();
         worldDBs.clear();
         activeInWorld.clear();
     }
 
     public static int getRegionSafety(WorldServer world, int x, int z) {
         WustendorfDB worldDB = getWorldDB(world);
 
         Integer safety = worldDB.getBestInRange("protect", x, z);
 
         if (safety == null) {
             return 0;
         }
 
         return safety;
     }
 
     public static int overrideLightDisplay(World world, int x, int y, int z) {
         int overrideLevel = overrideLight(world, x, y, z);
 
         if (overrideLevel < 0) {
             return -1;
         } else {
             return (overrideLevel << 4) | (overrideLevel << 20);
         }
     }
 
     public static int overrideLight(World world, int x, int y, int z) {
         int dimension = world.provider.dimensionId;
 
 
         Set<LightSource> lights;
         if (getSide() == Side.CLIENT) {
             lights = instance.clientLightCache.get(dimension);
         } else {
             lights = instance.serverLightCache.get(dimension);
         }
 
         if (lights == null || lights.isEmpty()) {
             return -1;
         }
 
         int best = -1;
         for (LightSource light : lights) {
             if (light.contains(x, z)) {
                 best = Math.max(best, light.strength);
             }
         }
 
         return best;
     }
 
     public static boolean isHostile(EntityLiving mob) {
         if (mob instanceof EntityMob) { // Spider, Zombie, etc.
             return true;
         } else if (mob instanceof EntityDragon) { // Dragon
             return true;
         } else if (mob instanceof EntityFlying) { // Ghast
             return true;
         } else if (mob instanceof EntitySlime) { // Slime, Magma Cube
             return true;
         } else {
             return false;
         }
     }
 
     public static void considerKillingCritter(EntityLiving mob) {
         if (!isHostile(mob) || !mob.isEntityAlive()) {
             return;
         }
 
         if (mob.worldObj instanceof WorldServer) {
             WorldServer world = (WorldServer) mob.worldObj;
 
             if (instance.activeInWorld.get(world) != Boolean.TRUE) {
                 return;
             }
 
             int x = MathHelper.floor_double(mob.posX);
             int z = MathHelper.floor_double(mob.posZ);
             int safety = getRegionSafety(world, x, z);
             if (safety < 1) {
                 return;
             }
 
             zot(world, mob, safety);
         }
     }
 
     public static void zot(WorldServer world, Entity entity, int safety) {
         if (safety > 60) {
             safety = 60;
         }
 
         int roll = instance.random.nextInt(600);
         if (safety < roll) {
             return;
         }
 
         // TODO: Some form of cost/counting?
 
         // Zap it with a lightning bolt.
         world.addWeatherEffect(
             new EntityLightningBolt(world, entity.posX, entity.posY,
                                            entity.posZ));
 
         if (safety > 10) {
             // Add some unblockable damage, too.
             entity.hurtResistantTime = 0;
             entity.attackEntityFrom(DamageSource.magic, safety - 10);
         }
     }
 
     public static House.BlockType getTypeOverride(int id, int meta) {
         return null;
     }
 
     public static int randint(int min, int max) {
         int values = (max - min) + 1;
         return instance.random.nextInt(values) - min;
     }
 
     public static WustendorfDB getWorldDB(WorldServer world) {
         WustendorfDB worldDB = instance.worldDBs.get(world);
         if (worldDB != null) {
             return worldDB;
         } else {
             worldDB = new WustendorfDB(world);
             instance.worldDBs.put(world, worldDB);
             return worldDB;
         }
     }
 
     public Packet250CustomPayload buildLightCachePacket(int dimension) {
         Set<LightSource> lights = serverLightCache.get(dimension);
 
         if (lights == null) {
             lights = new HashSet<LightSource>();
         }
 
         try {
             ByteArrayOutputStream arrayOutput = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(arrayOutput);
 
             data.writeInt(dimension);
             data.writeInt(lights.size());
 
             for (LightSource light : lights) {
                 data.writeInt(light.x);
                 data.writeInt(light.y);
                 data.writeInt(light.z);
                 data.writeInt(light.range);
                 data.writeInt(light.strength);
             }
 
             byte[] bytes = arrayOutput.toByteArray();
             return new Packet250CustomPayload("wustendorf_light", bytes);
         } catch (Exception e) {
             System.out.println("Wustendorf: Error building packet:");
             e.printStackTrace();
             return null;
         }
     }
 
     public void updateLightCache(Integer dimension, Set<LightSource> newCache) {
         if (newCache.equals(serverLightCache.get(dimension))) {
             // Nothing to do.
         } else {
             serverLightCache.put(dimension, newCache);
             Packet250CustomPayload packet = buildLightCachePacket(dimension);
             PacketDispatcher.sendPacketToAllPlayers(packet);
         }
     }
 
     public void setFlight(EntityPlayer player, boolean flight) {
         setFlight(player, flight, false);
     }
 
     public void setFlight(EntityPlayer player, boolean flight, boolean force) {
         if (!flight && (force || player.capabilities.allowFlying)
                     && !player.capabilities.isCreativeMode) {
             player.fallDistance = 0.0F;
            player.capabilities.isFlying = false;
             player.capabilities.allowFlying = false;
             player.sendPlayerAbilities();
         } else if (flight && (force || !player.capabilities.allowFlying)) {
             player.capabilities.allowFlying = true;
             player.sendPlayerAbilities();
         }
     }
 
     @SuppressWarnings("unchecked")
     public void tickStart(EnumSet<TickType> type, Object... tickData) {
         if (!(tickData[0] instanceof WorldServer)) {
             return;
         }
 
         WorldServer world = (WorldServer) tickData[0];
         long tick = world.getWorldInfo().getWorldTime();
         int phase = real_mod(tick, 100);
         int dimension = world.provider.dimensionId;
         WustendorfDB worldDB = getWorldDB(world);
         int markerCount = worldDB.getMarkerCount();
 
         activeInWorld.put(world, (markerCount > 0));
 
         if (markerCount <= 0) {
             // Not much to do, just make sure there aren't any light sources
             // cached, since none exist.
             updateLightCache(dimension, new HashSet<LightSource>());
 
             // And that nobody's using the flight from a marker that's gone.
             for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
                 setFlight(player, false);
             }
             return;
         }
 
         // Load the currently-existing light sources.
         Set<LightSource> newCache = new HashSet<LightSource>();
         List<DBMarker> lightMarkers = worldDB.getMarkersWithTag("light");
         if (lightMarkers != null) {
             for (DBMarker marker : lightMarkers) {
                 int x        = marker.x;
                 int y        = marker.y;
                 int z        = marker.z;
                 int range    = marker.range;
                 int strength = marker.getTag("light");
                 LightSource source = new LightSource(x, y, z, range, strength);
 
                 newCache.add(source);
             }
         }
 
         // Make sure the cached light sources match the actual ones.
         updateLightCache(dimension, newCache);
 
         // Tick updates for markers.
         List<DBMarker> phaseMarkers = worldDB.getMarkersInPhase(phase);
         if (phaseMarkers != null) {
             for (DBMarker marker : phaseMarkers) {
                 WustendorfMarker.tick(world, marker);
             }
         }
 
         int player_phase = 0;
         for (EntityPlayer player : (List<EntityPlayer>) world.playerEntities) {
             int x = MathHelper.floor_double(player.posX);
             int z = MathHelper.floor_double(player.posZ);
             Integer flight = worldDB.getBestInRange("flight", x, z);
 
             // Ensure their flying is correctly on/off.
             setFlight(player, (flight != null && flight > 0));
 
             // And re-send their capabilities occasionally, because some mods
             // (*cough* Mystcraft *cough*) screw it up client-side.
             if (phase == player_phase) {
                 player.sendPlayerAbilities();
             }
 
             player_phase += 1;
             if (player_phase >= 100) {
                 player_phase = 0;
             }
         }
     }
 
     public void tickEnd(EnumSet<TickType> type, Object... tickData) { }
 
     public EnumSet<TickType> ticks() {
         return EnumSet.of(TickType.WORLD);
     }
 
     public String getLabel() {
         return "Wustendorf.lighting";
     }
 
 //public interface IPlayerTracker
 //{
     public void onPlayerLogin(EntityPlayer player) {
         for (int dimension : serverLightCache.keySet()) {
             PacketDispatcher.sendPacketToPlayer(buildLightCachePacket(dimension), (Player) player);
         }
     }
 
     public void onPlayerLogout(EntityPlayer player) {}
 
     public void onPlayerChangedDimension(EntityPlayer player) {}
 
     public void onPlayerRespawn(EntityPlayer player) {}
 //}
 
     public static class PacketHandler implements IPacketHandler {
         public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
             if (DEBUG) {
                 System.out.println("Got packet on channel " + packet.channel + ":");
                 String data_str = "";
                 for (byte datum : packet.data) {
                     data_str += String.format("%02x ", ((int)datum) & 0xFF);
                 }
                 System.out.println(data_str);
             }
 
             if (Wustendorf.getSide() == Side.CLIENT) {
                 if (packet.channel.equals("wustendorf_light")) {
                     try {
                         ByteArrayInputStream arrayInput = new ByteArrayInputStream(packet.data);
                         DataInputStream data = new DataInputStream(arrayInput);
 
                         int dimension = data.readInt();
                         int count = data.readInt();
 
                         Set<LightSource> lights = new HashSet<LightSource>();
                         for (int i=0; i<count; i++) {
                             int x        = data.readInt();
                             int y        = data.readInt();
                             int z        = data.readInt();
                             int range    = data.readInt();
                             int strength = data.readInt();
                             LightSource light = new LightSource(x, y, z, range, strength);
 
                             lights.add(light);
                         }
 
                         Set<LightSource> oldLights = instance.clientLightCache.get(dimension);
                         if (oldLights == null) {
                             oldLights = new HashSet<LightSource>();
                         }
 
                         World world = proxy.getLocalPlayer().worldObj;
                         if (dimension == world.provider.dimensionId) {
                             // Force a visual update on any new light sources.
                             for (LightSource light : lights) {
                                 if (!oldLights.contains(light)) {
                                     light.markDirty(world);
                                 } else {
                                     oldLights.remove(light);
                                 }
                             }
 
                             // Force a visual update on any removed light sources.
                             for (LightSource oldLight : oldLights) {
                                 oldLight.markDirty(world);
                             }
                         }
 
                         instance.clientLightCache.put(dimension, lights);
                     } catch (Exception e) {
                         System.out.println("Wustendorf: Packet error:");
                         e.printStackTrace();
                     }
                 }
             } else { // Server/bukkit side
             }
         }
     }
 
     public static class LightSource {
         public int x;
         public int y;
         public int z;
         public int range;
         public int strength;
 
         public LightSource(int x, int y, int z, int range, int strength) {
             this.x = x;
             this.y = y;
             this.z = z;
             this.range = range;
             this.strength = strength;
         }
 
         public int hashCode() {
             int hash = 0;
             hash |= (this.x & 0xff);
             hash |= (this.y & 0xff) << 8;
             hash |= (this.z & 0xff) << 16;
 
             // We don't bother hashing range/strength, as only one LightSource
             // can be at any given x,y,z at a time.
             return hash;
         }
 
         public boolean equals(Object generic_other) {
             if (generic_other instanceof LightSource) {
                 LightSource other = (LightSource) generic_other;
                 if (this.x == other.x && this.y == other.y && this.z == other.z
                     && this.range == other.range
                     && this.strength == other.strength) {
                     return true;
                 }
             }
             return false;
         }
 
         public boolean contains(int target_x, int target_z) {
             int dx = Math.abs(x - target_x);
             if (dx > range) {
                 return false;
             }
 
             int dz = Math.abs(z - target_z);
             if (dz > range) {
                 return false;
             }
 
             return true;
         }
 
         public void markDirty(World world) {
             world.markBlockRangeForRenderUpdate(x-range, 0, z-range, x+range, 255, z+range);
         }
 
         public String toString() {
             return x + "," + y + "," + z + ": range " + range + ", strength " + strength;
         }
     }
 }
