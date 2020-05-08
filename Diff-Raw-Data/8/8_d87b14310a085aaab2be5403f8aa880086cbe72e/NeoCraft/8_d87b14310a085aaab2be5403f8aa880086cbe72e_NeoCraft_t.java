 package MMC.neocraft;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.*;
 import cpw.mods.fml.common.event.*;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import MMC.neocraft.block.NCblock;
 import MMC.neocraft.gen.NCgenerator;
 import MMC.neocraft.gui.NCguiHandler;
 import MMC.neocraft.item.NCitem;
 import MMC.neocraft.lib.*;
 import MMC.neocraft.recipe.NCcrafter;
 import MMC.neocraft.recipe.RecipeRegistry;
 import MMC.neocraft.registry.*;
 import MMC.neocraft.registry.proxy.CommonProxy;
 
 /**
  * NeoCraft
  * 
  * @author Scott Fasone
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME /* , version = Reference.VERSION */ )
 @NetworkMod(clientSideRequired = true, serverSideRequired= false)
 public class NeoCraft
 {
 	@SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.COMMON_PROXY)
 	public static CommonProxy proxy = new CommonProxy();
 	@Instance(Reference.MOD_ID)
 	public static NeoCraft instance = new NeoCraft();
 	
 	NCgenerator worldGen = new NCgenerator();
 	NCcrafter crafter = new NCcrafter();
 	NCguiHandler guiHandler = new NCguiHandler();
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent pie)
 	{
 		NCblock.init();
 		NCitem.init();
 		
 		BlockRegistry.registerBlocks();
 		BlockRegistry.registerNames();
 		BlockRegistry.registerTileEntities();
 
 		ItemRegistry.registerNames();
 		
 		RecipeRegistry.registerRecipes();
 		RecipeRegistry.registerShapelessRecipes();
 		RecipeRegistry.registerSmeltingRecipes();
 	}
 	@Init
 	public void init(FMLInitializationEvent ie)
 	{
 		GameRegistry.registerWorldGenerator(worldGen);
 		GameRegistry.registerCraftingHandler(crafter);
 		NetworkRegistry.instance().registerGuiHandler(instance, guiHandler);
 		proxy.registerRenderers();
 		
 	}
 	@PostInit
 	public void postInit(FMLPostInitializationEvent pie)
 	{
 		
 	}
 }
