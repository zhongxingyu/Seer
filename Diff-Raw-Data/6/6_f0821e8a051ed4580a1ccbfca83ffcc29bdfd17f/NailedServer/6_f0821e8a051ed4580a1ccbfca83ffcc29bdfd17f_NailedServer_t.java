 package jk_5.nailed;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.*;
 import cpw.mods.fml.common.network.NetworkCheckHandler;
 import cpw.mods.fml.relauncher.FMLLaunchHandler;
 import cpw.mods.fml.relauncher.Side;
 import jk_5.nailed.achievement.AchievementEventListener;
 import jk_5.nailed.achievement.NailedAchievements;
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.blocks.NailedBlocks;
 import jk_5.nailed.ipc.IpcManager;
 import jk_5.nailed.irc.IrcBot;
 import jk_5.nailed.map.MapLoader;
 import jk_5.nailed.map.gen.NailedWorldProvider;
 import jk_5.nailed.map.instruction.InstructionReader;
 import jk_5.nailed.map.instruction.RegisterInstructionEvent;
 import jk_5.nailed.map.stat.RegisterStatTypeEvent;
 import jk_5.nailed.map.stat.StatTypeManager;
 import jk_5.nailed.network.NailedConnectionHandler;
 import jk_5.nailed.network.NailedNetworkHandler;
 import jk_5.nailed.players.PlayerRegistry;
 import jk_5.nailed.server.command.*;
 import jk_5.nailed.teamspeak.TeamspeakClient;
 import jk_5.nailed.util.config.ConfigFile;
 import jk_5.nailed.util.invsee.InvSeeTicker;
 import lombok.Getter;
 import net.minecraft.command.CommandHandler;
 import net.minecraftforge.common.DimensionManager;
 import net.minecraftforge.common.MinecraftForge;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
import java.util.Map;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 @Mod(modid = NailedServer.modid, version = "0.1", useMetadata = true, certificateFingerprint = "87401ecb3314a1a18fb267281b2432975a7e2e84")
 public class NailedServer {
 
     @Getter protected static final String modid = "Nailed";
     @Getter private static final Logger logger = LogManager.getLogger("Nailed");
     @Getter private static ConfigFile config;
     @Getter private static int providerID;
 
     @Getter @Instance(modid) private static NailedServer instance;
     @Getter private static IrcBot ircBot;
     @Getter private static TeamspeakClient teamspeakClient;
     @Getter private static Collection<Integer> registeredDimensions;
 
     public NailedServer(){
         NailedAPI.setMappackRegistrar(MapLoader.instance());
         if(FMLLaunchHandler.side().isClient()){
             throw new RuntimeException("Nailed-Server is server-only, don\'t use it on the client!");
         }
     }
 
     @NetworkCheckHandler
    public boolean acceptEnvironment(Map<String, String> data, Side side){
        NailedLog.info("Accepting connection from " + side.name());
         return true;
     }
 
     @EventHandler
     public void preInit(FMLPreInitializationEvent event){
         NailedLog.info("Creating config file");
         config = new ConfigFile(event.getSuggestedConfigurationFile()).setComment("Nailed main config file");
 
         if(MapLoader.getMapsFolder().exists()){
             NailedLog.info("Clearing away old maps folder");
             new File(".", "mapbackups").mkdirs();
             File dest = new File(new File(".", "mapbackups"), new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
             MapLoader.getMapsFolder().renameTo(dest);
         }
 
         NailedLog.info("Loading achievements");
         NailedAchievements.addAchievements();
 
         NailedLog.info("Initializing network and event handlers");
         NailedNetworkHandler.registerChannel();
 
         NailedLog.info("Registering network handlers");
         MinecraftForge.EVENT_BUS.register(PlayerRegistry.instance());
         MinecraftForge.EVENT_BUS.register(new AchievementEventListener());
         FMLCommonHandler.instance().bus().register(new InvSeeTicker());
         FMLCommonHandler.instance().bus().register(new NailedConnectionHandler());
 
         NailedLog.info("Registering blocks");
         NailedBlocks.init();
 
         NailedLog.info("Registering Nailed WorldProvider");
         NailedServer.providerID = NailedServer.config.getTag("providerId").setComment("The id for the nailed world provider").getIntValue(10);
         DimensionManager.registerProviderType(NailedServer.providerID, NailedWorldProvider.class, false);
 
         NailedLog.info("Overriding Default WorldProviders");
         DimensionManager.unregisterProviderType(-1);
         DimensionManager.unregisterProviderType(0);
         DimensionManager.unregisterProviderType(1);
         DimensionManager.registerProviderType(-1, NailedWorldProvider.class, false);
         DimensionManager.registerProviderType(0, NailedWorldProvider.class, true);
         DimensionManager.registerProviderType(1, NailedWorldProvider.class, false);
         DimensionManager.unregisterDimension(-1);
         DimensionManager.unregisterDimension(1);
 
         ircBot = new IrcBot();
         teamspeakClient = new TeamspeakClient();
     }
 
     @EventHandler
     public void init(FMLInitializationEvent event){
         MinecraftForge.EVENT_BUS.post(new RegisterInstructionEvent(InstructionReader.instance().getInstructionMap()));
         MinecraftForge.EVENT_BUS.post(new RegisterStatTypeEvent(StatTypeManager.instance().getStatTypes()));
 
         NailedLog.info("Registering achievements");
         NailedAchievements.init();
     }
 
     @EventHandler
     public void postInit(FMLPostInitializationEvent event){
         NailedLog.info("Loading the mappacks");
         MapLoader.instance().loadMappacks();
     }
 
     @EventHandler
     public void serverStarting(FMLServerStartingEvent event){
         ircBot.connect();
         teamspeakClient.connect();
 
         CommandHandler ch = (CommandHandler) event.getServer().getCommandManager();
 
         ch.registerCommand(new CommandGoto());
         ch.registerCommand(new CommandTeam());
         ch.registerCommand(new CommandStartGame());
         ch.registerCommand(new CommandIrc());
         ch.registerCommand(new CommandMap());
         ch.registerCommand(new CommandSetWinner());
         ch.registerCommand(new CommandReloadMappacks());
         ch.registerCommand(new CommandTime());
         ch.registerCommand(new CommandSudo());
         ch.registerCommand(new CommandInvsee());
         ch.registerCommand(new CommandFirework());
         ch.registerCommand(new CommandLobby());
         ch.registerCommand(new CommandReloadMap());
         ch.registerCommand(new CommandKickall());
         ch.registerCommand(new CommandSaveMappack());
     }
 
     /*@EventHandler
     public void remapIds(FMLMissingMappingsEvent event){
         NailedLog.severe("-------------------------------------------------1");
         List<FMLMissingMappingsEvent.MissingMapping> mappings = event.get();
         for(FMLMissingMappingsEvent.MissingMapping mapping : mappings){
             NailedLog.severe(mapping.type + " " + mapping.name);
         }
         NailedLog.severe("-------------------------------------------------1");
     }
 
     @EventHandler
     public void remapMod(FMLModIdMappingEvent event){
         NailedLog.severe("-------------------------------------------------2");
         List<FMLModIdMappingEvent.ModRemapping> mappings = event.remappedIds;
         for(FMLModIdMappingEvent.ModRemapping mapping : mappings){
             NailedLog.severe(mapping.remapTarget + " " + mapping.oldId + " " + mapping.newId);
         }
         NailedLog.severe("-------------------------------------------------2");
     }*/
 
     @EventHandler
     public void serverAboutToStart(FMLServerAboutToStartEvent event){
         IpcManager.instance().start();
     }
 }
