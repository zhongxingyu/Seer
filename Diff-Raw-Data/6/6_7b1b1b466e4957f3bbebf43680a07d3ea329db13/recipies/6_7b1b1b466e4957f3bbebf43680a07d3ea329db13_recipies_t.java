 package com.mrgreaper.twisted.items;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 import appeng.api.Materials;
 
 import com.mrgreaper.twisted.TwistedMod;
 import com.mrgreaper.twisted.config.configInfo;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class recipies {
 	
 	/*		for (int i = 0; i < ItemInfo.CARD_NAMES.length; i++) {
 	LanguageRegistry.addName(new ItemStack(card,  1, i), ItemInfo.CARD_NAMES[i]);
 }*/
 //GameRegistry.addRecipe(new ItemStack(wand, 10) would make 10
 public static void registerRecipes() {
 /*		GameRegistry.addRecipe(new ItemStack(wand),//output item
 	new Object[] { 	"  X",//crafting grid
 					" / ",
 					"/  ",
 					
 					'X', Item.goldenCarrot, //items used in the above list
 					'/', Item.stick 
 				 });	*/	
 //my recipes
 GameRegistry.addRecipe(new ItemStack(Items.bunnyd),//output item
		new Object[] { 	"C C",//crafting grid
						" D ",
						"C C",
 						
 						'D', Block.dirt, //items used in the above list
 						'C', Item.carrot 
 					 });	
 if (configInfo.DEBUG){GameRegistry.addRecipe(new ItemStack(Items.bunnya),//make sure this recipie is only available in debug mode
 		new Object[] { 	"  D",//crafting grid
 						" C ",
 						"  D",
 						
 						'D', Block.dirt, //items used in the above list
 						'C', Item.carrot 
 					 });}
 GameRegistry.addRecipe(new ItemStack(Items.deathorb),//output item
 		new Object[] { 	" F ",//crafting grid
 						"FIF",
 						" F ",
 						
 						'F', Item.flint, //items used in the above list
 						'I', Item.ingotIron 
 					 });
 if (configInfo.EASYMODE){GameRegistry.addRecipe(new ItemStack(Items.bunnya),//output item
 		new Object[] { 	"DTD",//crafting grid
 						"RBR",
 						"DRD",
 						
 						'D', Item.diamond, //items used in the above list
 						'B', Items.bunnyd, 
 						'T', Items.orphantear,
 						'R', Item.redstone 
 					 });}
 
 
 //smelting recipies
 if (configInfo.EASYMODE){GameRegistry.addSmelting(Items.bunnya.itemID ,new ItemStack(Item.netherStar) , 0.1f);}
 
 //from craft
 if (configInfo.LSMELT){GameRegistry.addSmelting(Item.rottenFlesh.itemID, new ItemStack(Item.leather), 0.1f);}
 if (configInfo.SCRAFT){GameRegistry.addShapelessRecipe(new ItemStack(Item.slimeBall), new ItemStack(Item.rottenFlesh));}
 if (configInfo.IC2HELPER && configInfo.IC2Loaded){GameRegistry.addShapelessRecipe(ic2.api.item.Items.getItem("resin"), new ItemStack(Item.slimeBall));}
 
 
 if (configInfo.appEngLoaded && configInfo.APPENG){
 	GameRegistry.addShapelessRecipe(new ItemStack(Item.netherQuartz), ItemStack.copyItemStack(Materials.matQuartz));
 	GameRegistry.addShapelessRecipe(ItemStack.copyItemStack(Materials.matQuartz), new ItemStack(Item.netherQuartz));
 	GameRegistry.addShapelessRecipe(ItemStack.copyItemStack(Materials.matQuartzDustNether), ItemStack.copyItemStack(Materials.matQuartzDust));
 	GameRegistry.addShapelessRecipe(ItemStack.copyItemStack(Materials.matQuartzDust), ItemStack.copyItemStack(Materials.matQuartzDustNether));
 }
 }
 }
 
