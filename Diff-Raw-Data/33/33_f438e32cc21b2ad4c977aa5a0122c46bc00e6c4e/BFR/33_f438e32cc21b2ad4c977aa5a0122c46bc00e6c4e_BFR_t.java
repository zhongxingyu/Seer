 package adanaran.mods.bfr;
 
 import java.util.logging.Logger;
 
import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
 
 /**
  * 
  * @author Adanaran
  */
 @Mod(modid = "bfr", name = "Better Food Recipes", version = "0.1")
 public class BFR {
 
 	@Instance("bfr")
 	public static BFR instance;
 	
	@SidedProxy(clientSide = "adanaran.mods.bfr.ClientProxy", serverSide = "adanaran.mods.bfr.Proxy")
 	public static Proxy proxy;
 	
 	// Log
 	public static Logger logger;
 	
 	// ID-Section
	// public static int idWhatever;
 	
 	// Block-Section
	// public static BlockWhatever blockWhatever; 
 	
 	@EventHandler
 	public static void preInit(FMLPreInitializationEvent event){
		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
		logger = event.getModLog();
		Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
		try{
			cfg.load();
			// Enter Block and Item IDs here for config-values
			// Style:
			// varName = cfg.getBlock("blockName", idPlanned).getInt(idPlanned);
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			cfg.save();
		}
 	}
 	
 	@EventHandler
 	public static void Init(FMLInitializationEvent event){
		// Items registrieren
 	}
 	
 	@EventHandler
 	public static void postInit(FMLPostInitializationEvent event){
		// Gut aussehen :P
 	}
 }
