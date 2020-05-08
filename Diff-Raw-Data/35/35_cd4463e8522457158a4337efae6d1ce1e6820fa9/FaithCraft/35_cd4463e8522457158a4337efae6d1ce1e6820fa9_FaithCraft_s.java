 package common;
 
 import common.blocks.BlockHandler;
 import common.items.ItemHandler;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import client.ClientProxy;
 
 @Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION)
 @NetworkMod(serverSideRequired = false, clientSideRequired = true)
 public class FaithCraft {
 
     static int startEntityId = 300;
     
     @Instance(ModInfo.NAME)
     public static FaithCraft instance;
     
     public static CreativeTabs modTab = new CreativeTab(CreativeTabs.getNextID(), "FaithCraft");
     
     @SidedProxy(clientSide = "client.ClientProxy", serverSide = "CommonProxy")
     public static CommonProxy proxy;
 
     @EventHandler
     public void preInit(FMLPreInitializationEvent event) {
         Configuration config = new Configuration(event.getSuggestedConfigurationFile());
         config.load();
         BlockHandler.configureBlocks(config);
         ItemHandler.configureItems(config);
        config.save();
        BlockHandler.registerBlocks(new GameRegistry());
        ItemHandler.registerItems(new GameRegistry());
         
         craftingRecipes();
         smeltingRecipes();
     }
 
     @EventHandler
     public void init(FMLInitializationEvent event) {
 
     }
     
     private static void craftingRecipes() {
     
     }
     
     private static void smeltingRecipes() {
         
     }
     
     
     @EventHandler
     public void postInit(FMLPostInitializationEvent event) {
 
     }
 }
