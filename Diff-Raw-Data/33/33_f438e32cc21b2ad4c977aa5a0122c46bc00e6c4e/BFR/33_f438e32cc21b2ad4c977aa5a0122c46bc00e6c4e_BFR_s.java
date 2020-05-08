 package adanaran.mods.bfr;
 
 import java.util.logging.Logger;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 /**
  * 
  * @author Adanaran
  */
 @Mod(modid = "bfr", name = "Better Food Recipes", version = "0.1")
 public class BFR {
 
 	@Instance("bfr")
 	public static BFR instance;
 	
	@SidedProxy(clientSide = "adanaran.mods.ClientProxy", serverSide = "adanaran.mods.Proxy")
 	public static Proxy proxy;
 	
 	// Log
 	public static Logger logger;
 	
 	// ID-Section
	
 	
 	// Block-Section
 	
 	@EventHandler
 	public static void preInit(FMLPreInitializationEvent event){
		
 	}
 	
 	@EventHandler
 	public static void Init(FMLInitializationEvent event){
		
 	}
 	
 	@EventHandler
 	public static void postInit(FMLPostInitializationEvent event){
		
 	}
 }
