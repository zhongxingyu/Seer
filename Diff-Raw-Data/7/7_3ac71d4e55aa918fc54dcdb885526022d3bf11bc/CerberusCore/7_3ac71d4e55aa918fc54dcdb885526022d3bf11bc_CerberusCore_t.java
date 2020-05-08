 package teamcerberus.cerberuscore;
 
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.Property;
 import teamcerberus.cerberuscore.command.CerberusCommandManager;
 import teamcerberus.cerberuscore.handlers.ClientTickHandler;
 import teamcerberus.cerberuscore.multiblock.MultiblockManager;
 import teamcerberus.cerberuscore.util.CerberusLogger;
 import teamcerberus.cerberuscore.util.ClientUtil;
 import teamcerberus.cerberuscore.util.ServerUtil;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class CerberusCore {
 	public static final String	id				= "CerberusCore";
 	public static final String	channelPrefix	= "CC_";
 	public static final String	version			= "@VERSION@";
 
 	protected static void preInit(FMLPreInitializationEvent ev) {
 		CerberusLogger.logInfo("Cerberus Core Loading...");
 		CerberusLogger.logInfo("Version " + version);
 		
 		Configuration config = new Configuration(ev.getSuggestedConfigurationFile());
 		config.load();
 		Property prop = config.get("multiblocks", "enabled", true);
 		prop.comment = "Should multiblocks be loaded?";
 		MultiblockManager.enabled = prop.getBoolean(true);
 		prop = config.get("multiblocks", "blockId", 599);
 		prop.comment = "The block id used be all multiblocks in the world.\n(All multiblocks that are added in the game are just items and this is the block that holds all of them together)";
 		MultiblockManager.blockMultiblockId = prop.getInt();
 		config.save();
 		
 		CerberusCommandManager.init();
		if (ClientUtil.isClient()) {
 			ClientUtil.init();
			TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
		}
 		ServerUtil.init();
 		MultiblockManager.init();
 		CerberusLogger.logInfo("Loaded!");
 	}
 	
 	protected static void init() {
 	}
 
 	protected static void preMCInit() {
 		CerberusLogger.init();
 		CerberusLogger.logInfo("PreMinecraft Initialization Complete!");
 	}
 }
