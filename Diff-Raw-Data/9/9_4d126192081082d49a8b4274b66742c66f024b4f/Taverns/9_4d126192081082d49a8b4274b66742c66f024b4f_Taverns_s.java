 package mods.taverns;
 
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 
 /**
  * Taverns
  *
  * @author AngleWyrm
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  *
  */
 @Mod(modid = Taverns.modID, name = Taverns.modID, version = Taverns.releaseDate)
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
 
 public class Taverns 
 {
 	@Instance public static Taverns instance;
     @SidedProxy(clientSide = "mods.taverns.ClientProxy", serverSide = "mods.taverns.ServerProxy")
     public static ServerProxy proxy;
 	public static final String modID = "Taverns";
	public static final String releaseDate = "20-Jun-2013";
 	public static Settings config;
 
     public Taverns() {
         instance = this;
     }
 
     @PreInit
     public void preInit(FMLPreInitializationEvent event) {
         config = new Settings(event.getSuggestedConfigurationFile());
         config.loadSettings();
     }
 
     @Init
     public void load(FMLInitializationEvent event) 
     {
     	if(config.generateTaverns)
     	{
     		TavernCreationHandler tavernCreator = new TavernCreationHandler();
     		VillagerRegistry.instance().registerVillageCreationHandler(tavernCreator);
     		
             VillageHandlerBarWench barWench = new VillageHandlerBarWench();
      		VillagerRegistry.instance().registerVillageTradeHandler(VillageHandlerBarWench.BAR_WENCH, barWench);
      		VillagerRegistry.instance().registerVillagerType(VillageHandlerBarWench.BAR_WENCH, "/mods/taverns/textures/entity/barwench.png");
     		
     		ComponentVillageTavern.registerTavernChest();
     	}
 
         proxy.registerRenderHandlers();
     }
 
     @Mod.PostInit
     public void postInit(FMLPostInitializationEvent event) {
         if (Loader.isModLoaded("MCA")){
         	config.minecraftComesAlive = true;
         }
         if(Loader.isModLoaded("BiomesOPlenty")){
         	config.biomesOPlenty = true;
         }
     }
     
     public String Version(){
     	return releaseDate;
     }
 }
