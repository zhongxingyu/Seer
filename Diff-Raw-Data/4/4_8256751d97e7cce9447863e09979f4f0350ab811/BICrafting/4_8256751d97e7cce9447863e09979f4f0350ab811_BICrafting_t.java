 package mods.Electrolysm.electro.handlers;
 
 import ic2.api.item.Items;
 import ic2.api.recipe.Recipes;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import mods.Electrolysm.api.machines.machineRecipes;
 import mods.Electrolysm.electro.electrolysmCore;
 import mods.Electrolysm.electro.recipes.ForgeRecipes;
 
 public class BICrafting {
 
 	private static String name;
 
 	//Creafting
 	public static void registerCraftingRecipes()
 	{
 		//World Generation Crafting
 			GameRegistry.addRecipe(new ItemStack(electrolysmCore.electrumDust, 3),
 					"XY ",
 					Character.valueOf('X'), electrolysmCore.mixedOre,
 					Character.valueOf('Y'), Block.cobblestone);
 			
 			GameRegistry.addRecipe(new ItemStack(electrolysmCore.ferrousDust, 5),
 					"XYY",
 					Character.valueOf('X'), electrolysmCore.mixedOre,
 					Character.valueOf('Y'), Block.cobblestone);
 			
 				//Crafting Machines
 				
 	 		       GameRegistry.addRecipe(new ItemStack(electrolysmCore.magmaticExtractor),
 	 		    		   "XYX", "ZMZ", "XNX",
 	 		    		   Character.valueOf('X'), net.minecraft.item.Item.ingotIron,
 	 		    		   Character.valueOf('Y'), net.minecraft.item.Item.redstone,
 	 		    		   Character.valueOf('Z'), net.minecraft.item.Item.ingotGold,
 	 		    		   Character.valueOf('M'), net.minecraft.item.Item.bucketEmpty,
 	 		    		   Character.valueOf('N'), electrolysmCore.tumbaga);
 	 		       
 	 		       GameRegistry.addRecipe(new ItemStack(electrolysmCore.solarCollector),
 	 		    		   "XXX", "YZY", "AAA",
 	 		    		   Character.valueOf('X'), Block.glass,
 	 		    		   Character.valueOf('Y'), net.minecraft.item.Item.redstone,
 	 		    		   Character.valueOf('Z'), electrolysmCore.tibetanSilver,
 	 		    		   Character.valueOf('A'), net.minecraft.item.Item.ingotIron);
 	 		       
 	 		       GameRegistry.addRecipe(new ItemStack(electrolysmCore.matterSythisiser),
 	 		    		   "XZX", "XMX", "XYX",
 	 		    		   Character.valueOf('X'), electrolysmCore.ionicElectrum,
 	 		    		   Character.valueOf('Y'), electrolysmCore.ironisedGold,
 	 		    		   Character.valueOf('Z'), electrolysmCore.babbitt,
 	 		    		   Character.valueOf('M'), Block.furnaceIdle);
 	 		       
 	 		       GameRegistry.addRecipe(new ItemStack(electrolysmCore.Forge),
 	 		    		   "XXX", "ZYZ", "XXX",
 	 		    		   Character.valueOf('X'),net.minecraft.item.Item.ingotIron,
 	 		    		   Character.valueOf('Z'),Block.furnaceIdle,
 	 		    		   Character.valueOf('Y'),net.minecraft.item.Item.redstone);
 	 		       
 	 		       GameRegistry.addRecipe(new ItemStack(electrolysmCore.platFurnace),
 	 		    		   "XXX", "X X", "XXX",
 	 		    		   Character.valueOf('X'), electrolysmCore.platinum);
 	 		       
 	 		       //Crafting Tools
 	 		      GameRegistry.addRecipe(new ItemStack(electrolysmCore.hiddenSword),
 	 		       			"  X", " X ", "Y  ",
 	 		       			Character.valueOf('X'), electrolysmCore.hiddenIngot,
 	 		       			Character.valueOf('Y'), net.minecraft.item.Item.diamond);
 	 		      
 	 		      GameRegistry.addRecipe(new ItemStack(electrolysmCore.hiddenPicaxe),
 	 		       			"XXX", " Y ", " Y ",
 	 		       			Character.valueOf('X'), electrolysmCore.hiddenIngot,
 	 		       			Character.valueOf('Y'), net.minecraft.item.Item.diamond);
 	 		      
 	 		      GameRegistry.addRecipe(new ItemStack(electrolysmCore.hiddenSpade),
 	 		    		  " X ", " Y ", " Y ",
 	 		    		  Character.valueOf('X'), electrolysmCore.hiddenIngot,
 	 		    		  Character.valueOf('Y'), Item.diamond);
 	 		      
 	 		      GameRegistry.addRecipe(new ItemStack(electrolysmCore.hiddenAxe),
 	 		    		  "XX ", "XY ", " Y ",
 	 		    		 Character.valueOf('X'), electrolysmCore.hiddenIngot,
 	 		    		 Character.valueOf('Y'), Item.diamond);
 	 		      
 	 		      GameRegistry.addRecipe(new ItemStack(electrolysmCore.hiddenAxe),
 	 		    		  " XX", " YX", " Y ",
 	 		    		 Character.valueOf('X'), electrolysmCore.hiddenIngot,
 	 		    		 Character.valueOf('Y'), Item.diamond);
 	 		      
 	 		      GameRegistry.addRecipe(new ItemStack(electrolysmCore.Crusher),
 	 		    		  "XYX", "YZY", "XYX",
 	 		    		  Character.valueOf('X'), Item.ingotIron,
 	 		    		  Character.valueOf('Y'), Block.pistonBase,
 	 		      		  Character.valueOf('Z'), Item.redstone);
 
 	 		     
 	 		   //Crafting Adv. Atomics
 	 		       GameRegistry.addRecipe(new ItemStack(electrolysmCore.glassLens),
 	 		    		   "XXX",
 	 		    		   Character.valueOf('X'), Block.glass);
 		
 	 		      
 	 		       GameRegistry.addRecipe(new ItemStack(electrolysmCore.nanoTech),
 	 		    		   "XX", "XX",
 	 		    		   Character.valueOf('X'), electrolysmCore.stickyString);
 	 		       
 	 		       GameRegistry.addRecipe(new ItemStack(net.minecraft.item.Item.silk),
 	 		    		   "X",
 	 		    		   Character.valueOf('X'), electrolysmCore.stickyString);
 	 		       
 	 		      GameRegistry.addShapelessRecipe(new ItemStack(electrolysmCore.atomyBook), 
 	 		    		   net.minecraft.item.Item.book,
 	 		    		   electrolysmCore.pewter);
	 		      
	 		      GameRegistry.addRecipe(new ItemStack(electrolysmCore.platinum),
	 		    		  "XXX", "XX", "XXX",
	 		    		  Character.valueOf('X'), electrolysmCore.ingotPlatinum);
 	}
 
 	public static void registerSmeltingRecipes() {
 		
 		//WorldGen Smelting
 		GameRegistry.addSmelting(electrolysmCore.hiddenDust.itemID, new ItemStack(electrolysmCore.hiddenIngot), 10F);
 		
 		GameRegistry.addSmelting(electrolysmCore.OrePlatinum.blockID, new ItemStack(electrolysmCore.ingotPlatinum), 10F);
 		
 	}
 	
 	//Other mod api recipes
 	//eg - IC2
 	
 	public static void registerMODCrafting(){
 		
 		if(Loader.isModLoaded("IC2"))
 			{
 			Recipes.macerator.addRecipe(new ItemStack(electrolysmCore.OrePlatinum), new ItemStack(electrolysmCore.dustPlatinum, 2));
 			Recipes.extractor.addRecipe(new ItemStack(electrolysmCore.ingotPlatinum, 2), new ItemStack(net.minecraft.item.Item.redstone));
 			
 			 Class Ic2Items;
 			 ItemStack electronicCircuit = Items.getItem("electronicCircuit");
 
 			GameRegistry.addRecipe(new ItemStack(electrolysmCore.matterSythisiser, 1),
 					"XZX", "XMX", "XYX",
 		    		   Character.valueOf('X'), electronicCircuit,
 		    		   Character.valueOf('Y'), electrolysmCore.ironisedGold,
 		    		   Character.valueOf('Z'), electrolysmCore.babbitt,
 		    		   Character.valueOf('M'), Block.furnaceIdle);
 			}
 		
 		if(Loader.isModLoaded("ThermalExpansion"))
 			{
 			//CraftingManagers.pulverizerManager.addRecipe(400, new ItemStack(NetherOresCore.getOreBlock(_blockIndex), 1, _metadata), pulvPriTo, pulvSecTo, 15, false);
 			}
 		
 
 	}
 	
 }
