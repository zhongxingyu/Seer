 package com.ftwinston.KillerMinecraft.CraftBukkit;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Map;
 
 import net.minecraft.server.v1_6_R2.ChunkProviderServer;
 import net.minecraft.server.v1_6_R2.IChunkProvider;
 import net.minecraft.server.v1_6_R2.ItemStack;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import net.minecraft.server.v1_6_R2.NBTTagList;
 import net.minecraft.server.v1_6_R2.Block;
 import net.minecraft.server.v1_6_R2.ChunkPosition;
 import net.minecraft.server.v1_6_R2.ChunkProviderHell;
 import net.minecraft.server.v1_6_R2.EntityEnderSignal;
 import net.minecraft.server.v1_6_R2.EntityHuman;
 import net.minecraft.server.v1_6_R2.EntityTracker;
 import net.minecraft.server.v1_6_R2.EnumGamemode;
 import net.minecraft.server.v1_6_R2.IWorldAccess;
 import net.minecraft.server.v1_6_R2.MinecraftServer;
 import net.minecraft.server.v1_6_R2.Packet201PlayerInfo;
 import net.minecraft.server.v1_6_R2.Packet205ClientCommand;
 import net.minecraft.server.v1_6_R2.RegionFile;
 import net.minecraft.server.v1_6_R2.RegionFileCache;
 import net.minecraft.server.v1_6_R2.ServerNBTManager;
 import net.minecraft.server.v1_6_R2.TileEntity;
 import net.minecraft.server.v1_6_R2.TileEntityCommand;
 import net.minecraft.server.v1_6_R2.World;
 import net.minecraft.server.v1_6_R2.WorldGenNether;
 import net.minecraft.server.v1_6_R2.WorldManager;
 import net.minecraft.server.v1_6_R2.WorldServer;
 import net.minecraft.server.v1_6_R2.WorldSettings;
 import net.minecraft.server.v1_6_R2.WorldType;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World.Environment;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.v1_6_R2.CraftChunk;
 import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
 import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
 import org.bukkit.craftbukkit.v1_6_R2.generator.NormalChunkGenerator;
 import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.plugin.Plugin;
 
 import com.ftwinston.KillerMinecraft.KillerMinecraft;
 
 public class v1_6_2 extends CraftBukkitAccess
 {
 	public v1_6_2(Plugin plugin)
 	{
 		super(plugin);
 	}
 	
 	protected MinecraftServer getMinecraftServer()
 	{
 		CraftServer server = (CraftServer)plugin.getServer();
 		return server.getServer();
 	}
 
 	public String getDefaultLevelName()
 	{
 		return getMinecraftServer().getPropertyManager().getString("level-name", "world");
 	}
 
 	public YamlConfiguration getBukkitConfiguration()
 	{
 		YamlConfiguration config = getField(CraftServer.class, plugin.getServer(), "configuration");
 		return config;
 	}
 
 	public void saveBukkitConfiguration(YamlConfiguration configuration)
 	{
 		try
 		{
 			configuration.save((File)getMinecraftServer().options.valueOf("bukkit-settings"));
 		}
 		catch ( IOException ex )
 		{
 		}
 	}
 	
 	public String getServerProperty(String name, String defaultVal)
 	{
 		return getMinecraftServer().getPropertyManager().properties.getProperty(name, defaultVal);
 	}
 	
 	public void setServerProperty(String name, String value)
 	{
 		getMinecraftServer().getPropertyManager().properties.put(name, value);
 	}
 	
 	public void saveServerPropertiesFile()
 	{
 		getMinecraftServer().getPropertyManager().savePropertiesFile();
 	}
 	
 	
 	public void sendForScoreboard(Player viewer, String name, boolean show)
 	{
 		((CraftPlayer)viewer).getHandle().playerConnection.sendPacket(new Packet201PlayerInfo(name, show, 9999));
 	}
 	
 	public void sendForScoreboard(Player viewer, Player other, boolean show)
 	{
 		((CraftPlayer)viewer).getHandle().playerConnection.sendPacket(new Packet201PlayerInfo(other.getPlayerListName(), show, show ? ((CraftPlayer)other).getHandle().ping : 9999));
 	}
 	
 	public void forceRespawn(final Player player)
     {
     	plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 Packet205ClientCommand packet = new Packet205ClientCommand();
                 packet.a = 1;
                 ((CraftPlayer) player).getHandle().playerConnection.a(packet); // obfuscated
             }
         }, 1);
 	}
 	
 	public void bindRegionFiles()
 	{
 		regionfiles = getField(RegionFileCache.class, null, "a"); // obfuscated
 		
 		try
 		{
 			rafField = RegionFile.class.getDeclaredField("c"); // obfuscated
 		}
 		catch ( NoSuchFieldException e )
 		{
 			plugin.getLogger().warning("Error binding to region file cache.");
 			e.printStackTrace();
 			rafField = null;
 		}
 	}
 	
 	public void unbindRegionFiles()
 	{
 		regionfiles = null;
 		rafField = null;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public synchronized boolean clearWorldReference(String worldName)
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
 					RegionFile file = (RegionFile) e.getValue();
 					try
 					{
 						RandomAccessFile raf = (RandomAccessFile) rafField.get(file);
 						raf.close();
 						removedKeys.add(f);
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
 			plugin.getLogger().warning("Exception while removing world reference for '" + worldName + "'!");
 			ex.printStackTrace();
 		}
 		for (Object key : removedKeys)
 			regionfiles.remove(key);
 		
 		return true;
 	}
 	
 	public void forceUnloadWorld(org.bukkit.World world)
 	{
 		world.setAutoSave(false);
 		for ( Player player : world.getPlayers() )
 			player.kickPlayer("World is being deleted... and you were in it!");
 		
 		// formerly used server.unloadWorld at this point. But it was sometimes failing, even when I force-cleared the player list
 		Map<String, org.bukkit.World> worlds = getField(CraftServer.class, plugin.getServer(), "worlds");
 		worlds.remove(world.getName().toLowerCase());
 		
 		MinecraftServer ms = getMinecraftServer();
 		CraftWorld craftWorld = (CraftWorld)world;
 		ms.worlds.remove(ms.worlds.indexOf(craftWorld.getHandle()));
 	}
 	
 	public void accountForDefaultWorldDeletion(org.bukkit.World newDefault)
 	{
 		// playerFileData in the player list must point to that of the new default world, instead of the deleted original world
 		getMinecraftServer().getPlayerList().playerFileData = ((CraftWorld)newDefault).getHandle().getDataManager().getPlayerFileData();
 	}
 	
 	public org.bukkit.World createWorld(org.bukkit.WorldType type, Environment env, String name, long seed, ChunkGenerator generator, String generatorSettings, boolean generateStructures)
     {
         final Server server = plugin.getServer();
         MinecraftServer console = getMinecraftServer();
         
         File folder = new File(server.getWorldContainer(), name);
         org.bukkit.World world = server.getWorld(name);
 
         if (world != null)
             return world;
 
         if ((folder.exists()) && (!folder.isDirectory()))
             throw new IllegalArgumentException("File exists with the name '" + name + "' and isn't a folder");
 
         
         WorldType worldType = WorldType.getType(type.getName());
         
         int dimension = 10 + console.worlds.size();
         boolean used = false;
         do {
             for (WorldServer ws : console.worlds) {
                 used = ws.dimension == dimension;
                 if (used) {
                     dimension++;
                     break;
                 }
             }
         } while(used);
         boolean hardcore = false;
 
 		WorldSettings worldSettings = new WorldSettings(seed, EnumGamemode.a(server.getDefaultGameMode().getValue()), generateStructures, hardcore, worldType); // obfuscated
 		worldSettings.a(generatorSettings); // obfuscated
 		
         final WorldServer worldServer = new WorldServer(console, new ServerNBTManager(server.getWorldContainer(), name, true), name, dimension, worldSettings, console.methodProfiler, console.getLogger(), env, generator);
 
         if (server.getWorld(name) == null)
             return null;
 
         worldServer.worldMaps = console.worlds.get(0).worldMaps;
 
         worldServer.tracker = new EntityTracker(worldServer);
         worldServer.addIWorldAccess((IWorldAccess) new WorldManager(console, worldServer));
         worldServer.difficulty = 3;
         worldServer.setSpawnFlags(true, true);
         console.worlds.add(worldServer);
 
         world = worldServer.getWorld();
         
         if (generator != null)
         	world.getPopulators().addAll(generator.getDefaultPopulators(world));
         
         return world;
     }
 	
 	public boolean isChunkGenerated(Chunk chunk)
 	{
 		return ((CraftChunk)chunk).getHandle().done;
 	}
 	
 	public Location findNearestNetherFortress(Location loc)
 	{
 		if ( loc.getWorld().getEnvironment() != Environment.NETHER )
 			return null;
 		
 		WorldServer world = ((CraftWorld)loc.getWorld()).getHandle();
 		
 		IChunkProvider chunkProvider = getField(ChunkProviderServer.class, world.chunkProviderServer, "chunkProvider");
 		if ( chunkProvider == null )
 		{
 			KillerMinecraft.instance.log.info("chunkProvider null");
 			return null;
 		}
 		
 		ChunkProviderHell hellCP = getField(NormalChunkGenerator.class, chunkProvider, "provider");
 		if ( hellCP == null )
 			return null;
 		
 		WorldGenNether fortressGenerator = (WorldGenNether)hellCP.c; // obfuscated
 		ChunkPosition pos = fortressGenerator.getNearestGeneratedFeature(world, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 		if ( pos == null )
 			return null; // this just means there isn't one nearby
 		
 		return new Location(loc.getWorld(), pos.x, pos.y, pos.z);
 	}
 	
 	public boolean createFlyingEnderEye(Player player, Location target)
 	{
 		WorldServer world = ((CraftWorld)target.getWorld()).getHandle();
 		
 		Location playerLoc = player.getLocation();
 		EntityEnderSignal entityendersignal = new EntityEnderSignal(world, playerLoc.getX(), playerLoc.getY() + player.getEyeHeight() - 0.2, playerLoc.getZ());
 
 		entityendersignal.a(target.getX(), target.getBlockY(), target.getZ()); // obfuscated
 		world.addEntity(entityendersignal);
 		world.makeSound(((CraftPlayer)player).getHandle(), "random.bow", 0.5F, 0.4F);
 		world.a((EntityHuman) null, 1002, playerLoc.getBlockX(), playerLoc.getBlockY(), playerLoc.getBlockZ(), 0); // obfuscated
 		return true;
 	}
 
 	@Override
 	public void pushButton(org.bukkit.block.Block b)
 	{
 		Block.STONE_BUTTON.interact(((CraftWorld)b.getWorld()).getHandle(), b.getX(), b.getY(), b.getZ(), null, 0, 0f, 0f, 0f);
 	}
 	
 	@Override
 	public void setCommandBlockCommand(org.bukkit.block.Block b, String command)
 	{
 		World world = ((CraftWorld)b.getWorld()).getHandle();
 		TileEntity tileEntity = world.getTileEntity(b.getX(), b.getY(), b.getZ());
 		if ( tileEntity == null || !(tileEntity instanceof TileEntityCommand) )
 			return;
 		
 		TileEntityCommand tec = (TileEntityCommand)tileEntity;
 		tec.b(command);
 	}
 	
 	@Override
 	public org.bukkit.inventory.ItemStack setEnchantmentGlow(org.bukkit.inventory.ItemStack item)
 	{
 		CraftItemStack output = CraftItemStack.asCraftCopy(item);
 		
 		ItemStack nmsStack = getField(CraftItemStack.class, output, "handle");
 		if ( nmsStack == null )
 			return item;
         NBTTagCompound compound = nmsStack.tag;
  
         // Initialize the compound if we need to
         if (compound == null) {
             compound = new NBTTagCompound();
             nmsStack.tag = compound;
         }
  
         // Empty enchanting compound
         compound.set("ench", new NBTTagList());
         return output;
 	}
 }
