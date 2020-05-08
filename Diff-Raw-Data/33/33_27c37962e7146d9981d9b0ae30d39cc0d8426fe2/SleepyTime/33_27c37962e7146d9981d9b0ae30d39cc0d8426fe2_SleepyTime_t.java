 package com.theswak.sleepytime;
 
 import java.util.HashSet;
 import java.util.logging.Level;
 
 import net.minecraft.command.ICommandManager;
 import net.minecraft.command.ServerCommandManager;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ModLoader;
 
 import com.theswak.sleepytime.commands.*;
 import com.theswak.sleepytime.common.*;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarted;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.Mod.ServerStopping;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartedEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.event.FMLServerStoppingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 /**
  * 
  * @author theSwak.com : Shaun Wakashige
  * a forge mod for minecraft that delivers the clients sleeping and away from keyboard status
  */
 @Mod( modid = SleepyTime.ID, name = SleepyTime.MODNAME, version= SleepyTime.VERSION )
 @NetworkMod
 (
 		clientSideRequired = true,
 		serverSideRequired = false,
 		packetHandler = PacketHandler.class,
 		connectionHandler = PacketHandler.class,
 		channels = { SleepyTime.CHANNEL }
 )
 public class SleepyTime
 {
 	public static final String ID = "SleepyTimeMod";
 	public static final String MODNAME = "SleepyTime : the AFK Mod";
 	public static final String CHANNEL = "SleepyTimeChan";
 	public static final String MCVERSION = "1.4.6";
	public static final String VERSION = "0.5.1";
 	
	public static final boolean DEBUG_MODE = true;
 	
 	@SidedProxy(clientSide="com.theswak.sleepytime.common.ClientProxy", serverSide="com.theswak.sleepytime.common.CommonProxy")
 	public static CommonProxy proxy;
 	public static MinecraftServer server;
 	
 	@Instance(ID)
 	public static SleepyTime instance;
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		proxy.preInit(event);
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 	}
 	
 	@Init
 	public void init(FMLInitializationEvent event)
 	{
 		proxy.init(event);
 		proxy.registerRenderInformation();
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event)
 	{
 		proxy.postInit(event);
 	}
 	
 	@ServerStarting
 	public void serverStarting(FMLServerStartingEvent event)
 	{
 		server = ModLoader.getMinecraftServerInstance();
 		ICommandManager commandManager = server.getCommandManager();
 		ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager); 
 		addCommands(serverCommandManager);
 	}
 
 	@ServerStarted
 	public void serverStarted(FMLServerStartedEvent event)
 	{
 		LogHandler.log(Level.INFO, "successfully started.");
 	}
 
 	@ServerStopping
 	public void serverStopping(FMLServerStoppingEvent event)
 	{
 		LogHandler.log(Level.INFO, "is shutting down.");
 	}
 	
 	/* ADD COMMANDS */
 	private void addCommands(ServerCommandManager manager)
 	{
 		manager.registerCommand(new CommandBedTime());
 		manager.registerCommand(new CommandAfk());
 	}
 }
