 package Fly_Craft;
 
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.item.*;
 
 
 @Mod(modid="JJMACCA_FlyCraft",name="Fly Craft",version="1.0")
 @NetworkMod(clientSideRequired=true,serverSideRequired=false)
 
 
 public class FlyCraft {
 	
 	//Mod ID
 	public static final String modid = "JJMACCA_FlyCraft";
 	
 	//Items
 	public static Item IngotSteel;
 	
 	//ID's
 	int ItemIngotSteelID = 500;
 	
 	//Creative Tab
 	//public static CreativeTabs TabFlyCraft = new TabFlyCraft(CreativeTabs.getNextID(), "Fly Craft");
 	
 	//Proxy
 	@SidedProxy(clientSide = "Fly_Craft.FlyCraftClient", serverSide = "Fly_Craft.FlyCraftProxy" )
 	public static FlyCraftProxy proxy;
 	
 	@PreInit
 	public void PreLoad(FMLInitializationEvent event)
 	{
 		
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event)
 	{
 		//Texture Functions
 		proxy.registerRenderInformation();
 		
 		//Steel Ingot
 		IngotSteel = new ItemIngotSteel(ItemIngotSteelID).setUnlocalizedName("IngotSteel");
 		LanguageRegistry.addName(IngotSteel, "Steel Ingot");
		GameRegistry.addRecipe(new ItemStack(IngotSteel, 1), new Object[] {"xxx","yyy","xxx",
			Character.valueOf('x'), Item.coal,
			Character.valueOf('y'), Material.iron});
 		}
 		
 		
 	}
 
