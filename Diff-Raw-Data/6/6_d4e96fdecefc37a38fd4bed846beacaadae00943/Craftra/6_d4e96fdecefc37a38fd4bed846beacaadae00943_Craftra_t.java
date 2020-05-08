 package com.moosetra.craftra;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.EnumHelper;
 
 import com.moosetra.craftra.block.CarboniteOreBlock;
 import com.moosetra.craftra.block.RedMapleLeafBlock;
 import com.moosetra.craftra.block.TarBlock;
 import com.moosetra.craftra.block.TarmacBlock;
 import com.moosetra.craftra.event.EventManager;
 import com.moosetra.craftra.event.TreeManager;
 import com.moosetra.craftra.item.CarboniteIngot;
 import com.moosetra.craftra.item.CarboniteLighter;
 import com.moosetra.craftra.item.TarPileItem;
 import com.moosetra.craftra.item.CarboniteSword;
 import com.moosetra.craftra.lib.Reference;
 import com.moosetra.craftra.proxy.CommonProxy;
import com.moosetra.craftra.item.CarbonitePickaxe;
import com.moosetra.craftra.item.CarboniteAxe;
import com.moosetra.craftra.item.CarboniteShovel;
 
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
  
 
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 
 public class Craftra {
 	
   // Tool Materials
 	public static EnumToolMaterial carbonite = EnumHelper.addToolMaterial("Carbonite", 2, 350, 7.0F, 5, 14);
   
   // Blocks
 	public final static Block TarmacBlock = new TarmacBlock(500, Material.rock); 
 	public final static Block CarboniteOreBlock = new CarboniteOreBlock(501, Material.rock);
 	public final static Block TarBlock = new TarBlock(502, Material.ground);
 	public final static Block RedMapleLeafBLock = new RedMapleLeafBlock(503, Material.leaves);
 	
   // Tools
 	public final static Item CarboniteLighter = new CarboniteLighter(5001);
 	public final static Item CarboniteSword = new CarboniteSword(5003, carbonite);
	public final static Item CarbonitePickaxe = new CarbonitePickaxe(5004, carbonite);
 	public final static Item CarboniteShovel = new CarboniteShovel(5005, carbonite);
 	public final static Item CarboniteAxe = new CarboniteAxe(5006, carbonite);
 		
   // Other
 	public final static Item TarPileItem = new TarPileItem(5000);
 	public final static Item CarboniteIngot = new CarboniteIngot(5002);
 	
 	EventManager oreManager = new EventManager();
 	TreeManager treeManager = new TreeManager();
 	
     	@Instance("craftra")
     	public static Craftra instance;
     
    
     	@SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
     	public static CommonProxy proxy;
    
     
     	@EventHandler
     	public void preInit(FMLPreInitializationEvent event) {
             	
     	}
     	@EventHandler
     	public void load(FMLInitializationEvent event) {
     			proxy.registerRenderers();
              
     		  // Block Lang & Harvest Levels
     			GameRegistry.registerBlock(TarmacBlock, "TarmacBlock");
     			LanguageRegistry.addName(TarmacBlock, "Tarmac");
     			MinecraftForge.setBlockHarvestLevel(TarmacBlock, "pickaxe", 2);
     			
     			GameRegistry.registerBlock(CarboniteOreBlock, "CarboniteOreBlock");
     			LanguageRegistry.addName(CarboniteOreBlock, "Carbonite Ore");
     			MinecraftForge.setBlockHarvestLevel(CarboniteOreBlock, "pickaxe", 1);
     			
     			GameRegistry.registerBlock(TarBlock, "TarBlock");
     			LanguageRegistry.addName(TarBlock, "Tar");
     			MinecraftForge.setBlockHarvestLevel(TarBlock, "shovel", 0);
     			
     			GameRegistry.registerBlock(RedMapleLeafBLock, "RedMapleLeafBLock");
     			LanguageRegistry.addName(RedMapleLeafBLock, "Red Maple Leaf");
     			MinecraftForge.setBlockHarvestLevel(RedMapleLeafBLock, "axe", 0);
     			
     		  // Item Lang
     			GameRegistry.registerItem(TarPileItem, "TarPileItem");
     			LanguageRegistry.addName(TarPileItem, "Tar Pile");
     			
     			GameRegistry.registerItem(CarboniteLighter, "CarboniteLighter");
     			LanguageRegistry.addName(CarboniteLighter, "Carbonite Lighter");
     			
     			GameRegistry.registerItem(CarboniteIngot, "CarboniteIngot");
     			LanguageRegistry.addName(CarboniteIngot, "Carbonite Ingot");
     			
     		  // Tool Lang
     			GameRegistry.registerItem(CarboniteSword, "CarboniteSword");
     			LanguageRegistry.addName(CarboniteSword, "Carbonite Sword");
     			
     			GameRegistry.registerItem(CarbonitePickaxe, "CarbonitePickaxe");
     			LanguageRegistry.addName(CarbonitePickaxe, "Carbonite Pickaxe");
     			
     			GameRegistry.registerItem(CarboniteAxe, "CarboniteAxe");
     			LanguageRegistry.addName(CarboniteAxe, "Carbonite Axe");
     			
     			GameRegistry.registerItem(CarboniteShovel, "CarboniteShovel");
     			LanguageRegistry.addName(CarboniteShovel, "Carbonite Shovel");
     		  
     		  // World Gen
     			GameRegistry.registerWorldGenerator(oreManager);
     			GameRegistry.registerWorldGenerator(treeManager);
     			
     		  // Item Stacks  
                 ItemStack gravelStack = new ItemStack(Block.gravel);
                 ItemStack tarpileStack = new ItemStack(TarPileItem);
                 ItemStack tarmacStack = new ItemStack(TarmacBlock);
                 ItemStack carbonitelighterStack = new ItemStack(CarboniteLighter); 
                 ItemStack flintandsteelStack = new ItemStack(Item.flintAndSteel);
                 ItemStack ironingotStack = new ItemStack(Item.ingotIron);
                 ItemStack carboniteingotStack = new ItemStack(CarboniteIngot);
     			ItemStack stickStack = new ItemStack(Item.stick);
     			ItemStack carbonitepickaxeStack = new ItemStack(CarbonitePickaxe);
     			ItemStack carboniteaxeStack = new ItemStack(CarboniteAxe);
     			ItemStack carboniteshovelStack = new ItemStack(CarboniteShovel);
     			ItemStack carboniteswordStack = new ItemStack(CarboniteSword);
     		 
     					// Block Recipes
     		    GameRegistry.addRecipe(tarmacStack, "yxy", "xyx", "yxy",
     					'x', tarpileStack,'y', gravelStack);
     			      
     		           // Item Recipes
     			GameRegistry.addRecipe(carbonitelighterStack, "xxx", "xyx", "xxx",
     					'x', carboniteingotStack, 'y', flintandsteelStack); 
     			      
     			      // Tool Recipes
     			GameRegistry.addRecipe(carbonitepickaxeStack, "xxx", " y ", " y ",
     					'x', carboniteingotStack,'y', stickStack);
     			
     			GameRegistry.addRecipe(carboniteshovelStack, " x ", " y ", " y ",
     					'x', carboniteingotStack,'y', stickStack);
     			
     			GameRegistry.addRecipe(carboniteaxeStack, " xx", " yx", " y ",
     					'x', carboniteingotStack,'y', stickStack);
     			
     			GameRegistry.addRecipe(carboniteaxeStack, "xx ", "xy ", " y ",
     					'x', carboniteingotStack,'y', stickStack);
     			
     			GameRegistry.addRecipe(carboniteswordStack, " x ", " x ", " y ",
     					'x', carboniteingotStack,'y', stickStack);
     			
     			      // Smelting Recipes
     			GameRegistry.addSmelting(501, carboniteingotStack, 0);
     			
     			
              
     	}
    
     	@EventHandler
     	public void postInit(FMLPostInitializationEvent event) {
       
     	
       }
    
 
 }
