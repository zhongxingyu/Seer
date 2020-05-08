 package SimpleOres.plugins.fusion;
 
 import SimpleOres.plugins.fusion.api.FusionRecipes;
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 public class Recipes 
 {
 	public static SimpleOres.core.Items coreItems;
 	/**
 	 * The constructor for the recipes. This is called by the main mod class.
 	 * This is where all the recipes are created, from tools and armor to block and smelting recipes.
 	 * 
 	 * Forge OreDictionary results are set here.
 	 */
 	public static void doRecipes()
 	{
 		//Forge OreDictionary
 		OreDictionary.registerOre("ingotBronze", new ItemStack(Content.bronzeIngot));
 		OreDictionary.registerOre("ingotThyrium", new ItemStack(Content.thyriumIngot));
 		OreDictionary.registerOre("ingotSinisite", new ItemStack(Content.sinisiteIngot));
 		
 		//Block Recipes
 			//Storage Content
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeBlock, true, new Object[]{
 				"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotBronze"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumBlock, true, new Object[]{
 				"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotThyrium"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteBlock, true, new Object[]{
 				"XXX", "XXX", "XXX", Character.valueOf('X'), "ingotSinisite"}));
 		
 			//Special Furnace Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.fusionFurnace, true, new Object[]{
 				"XWX", "ZYZ", "XWX", Character.valueOf('X'), Block.brick, Character.valueOf('Y'), Block.furnaceIdle, Character.valueOf('W'), Item.coal, Character.valueOf('Z'), Item.ingotIron}));
 			
 		//Item Recipes
 			//Ingot Recipes
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.bronzeIngot, 9), new Object[] { 
 				Content.bronzeBlock });
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.thyriumIngot, 9), new Object[] { 
 				Content.thyriumBlock });
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.sinisiteIngot, 9), new Object[] { 
 				Content.sinisiteBlock });
 			
 			//Bronze Ingot
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.largeBronzeChunk, 1), new Object[] { 
 				Content.smallBronzeChunk, Content.smallBronzeChunk, Content.smallBronzeChunk, Content.smallBronzeChunk, Content.smallBronzeChunk });
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.largeBronzeChunk, 1), new Object[] { 
 				Content.mediumBronzeChunk, Content.mediumBronzeChunk, Content.mediumBronzeChunk });
 			
 			//Thyrium Ingot
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.largeThyriumChunk, 1), new Object[] { 
 				Content.smallThyriumChunk, Content.smallThyriumChunk, Content.smallThyriumChunk, Content.smallThyriumChunk, Content.smallThyriumChunk });
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.largeThyriumChunk, 1), new Object[] { 
 				Content.mediumThyriumChunk, Content.mediumThyriumChunk, Content.mediumThyriumChunk });
 			
 			//Sinisite Ingot
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.largeSinisiteChunk, 1), new Object[] { 
 				Content.smallSinisiteChunk, Content.smallSinisiteChunk, Content.smallSinisiteChunk, Content.smallSinisiteChunk, Content.smallSinisiteChunk });
 			GameRegistry.addShapelessRecipe(new ItemStack(Content.largeSinisiteChunk, 1), new Object[] { 
 				Content.mediumSinisiteChunk, Content.mediumSinisiteChunk, Content.mediumSinisiteChunk });
 			
 			//Rods
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumRod, true, new Object[]{
 				"X", "X", Character.valueOf('X'), "ingotThyrium"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteRod, true, new Object[]{
 				"X", "X", Character.valueOf('X'), "ingotSinisite"}));
 			
 		//Tool Recipes
 			//Bronze Tool Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzePick, true, new Object[]{
 				"XXX", " Y ", " Y ", Character.valueOf('X'), "ingotBronze", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeAxe, true, new Object[]{
 				"XX ", "XY ", " Y ", Character.valueOf('X'), "ingotBronze", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeShovel, true, new Object[]{
 				"X", "Y", "Y", Character.valueOf('X'), "ingotBronze", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeSword, true, new Object[]{
 				"X", "X", "Y", Character.valueOf('X'), "ingotBronze", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeHoe, true, new Object[]{
 				"XX ", " Y ", " Y ", Character.valueOf('X'), "ingotBronze", Character.valueOf('Y'), Item.stick}));
 			
 			//Thyrium Tool Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumPick, true, new Object[]{
 				"XXX", " Y ", " Y ", Character.valueOf('X'), "ingotThyrium", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumAxe, true, new Object[]{
 				"XX ", "XY ", " Y ", Character.valueOf('X'), "ingotThyrium", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumShovel, true, new Object[]{
 				"X", "Y", "Y", Character.valueOf('X'), "ingotThyrium", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumSword, true, new Object[]{
 				"X", "X", "Y", Character.valueOf('X'), "ingotThyrium", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumHoe, true, new Object[]{
 				"XX ", " Y ", " Y ", Character.valueOf('X'), "ingotThyrium", Character.valueOf('Y'), Item.stick}));
 			
 			//Sinisite Tool Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisitePick, true, new Object[]{
 				"XXX", " Y ", " Y ", Character.valueOf('X'), "ingotSinisite", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteAxe, true, new Object[]{
 				"XX ", "XY ", " Y ", Character.valueOf('X'), "ingotSinisite", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteShovel, true, new Object[]{
 				"X", "Y", "Y", Character.valueOf('X'), "ingotSinisite", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteSword, true, new Object[]{
 				"X", "X", "Y", Character.valueOf('X'), "ingotSinisite", Character.valueOf('Y'), Item.stick}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteHoe, true, new Object[]{
 				"XX ", " Y ", " Y ", Character.valueOf('X'), "ingotSinisite", Character.valueOf('Y'), Item.stick}));
 			
 			//Bow Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumBow, true, new Object[]{
 				" XY", "Z Y", " XY", Character.valueOf('X'), Content.thyriumRod, Character.valueOf('Y'), Item.silk, Character.valueOf('Z'), Item.ingotGold}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteBow, true, new Object[]{
 				" XY", "Z Y", " XY", Character.valueOf('X'), Content.sinisiteRod, Character.valueOf('Y'), Item.silk, Character.valueOf('Z'), "gemOnyx"}));
 			
 		//Armour Recipes
 			//Bronze Armour Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeHelm, true, new Object[]{
 				"XXX", "X X", Character.valueOf('X'), "ingotBronze"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeChest, true, new Object[]{
 				"X X", "XXX", "XXX", Character.valueOf('X'), "ingotBronze"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeLegs, true, new Object[]{
 				"XXX", "X X", "X X", Character.valueOf('X'), "ingotBronze"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.bronzeBoots, true, new Object[]{
 				"X X", "X X", Character.valueOf('X'), "ingotBronze"}));
 			
 			//Thyrium Armour Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumHelm, true, new Object[]{
 				"XXX", "X X", Character.valueOf('X'), "ingotThyrium"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumChest, true, new Object[]{
 				"X X", "XXX", "XXX", Character.valueOf('X'), "ingotThyrium"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumLegs, true, new Object[]{
 				"XXX", "X X", "X X", Character.valueOf('X'), "ingotThyrium"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.thyriumBoots, true, new Object[]{
 				"X X", "X X", Character.valueOf('X'), "ingotThyrium"}));
 			
 			//Sinisite Armour Recipes
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteHelm, true, new Object[]{
 				"XXX", "X X", Character.valueOf('X'), "ingotSinisite"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteChest, true, new Object[]{
 				"X X", "XXX", "XXX", Character.valueOf('X'), "ingotSinisite"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteLegs, true, new Object[]{
 				"XXX", "X X", "X X", Character.valueOf('X'), "ingotSinisite"}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(Content.sinisiteBoots, true, new Object[]{
 				"X X", "X X", Character.valueOf('X'), "ingotSinisite"}));
 			
 		//Smelting Recipes
 			//Fusion Furnace
 				//Bronze
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.copperIngot), new ItemStack(coreItems.tinIngot), new ItemStack(Item.dyePowder, 15), new ItemStack(Content.smallBronzeChunk), 2.0F);
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.copperIngot), new ItemStack(coreItems.tinIngot), new ItemStack(Item.gunpowder), new ItemStack(Content.mediumBronzeChunk), 3.0F);
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.copperIngot), new ItemStack(coreItems.tinIngot), new ItemStack(Item.redstone), new ItemStack(Content.largeBronzeChunk), 10.0F);
 		    	
 		    	//Thyrium
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.mythrilIngot), new ItemStack(coreItems.adamantiumIngot), new ItemStack(Item.redstone), new ItemStack(Content.smallThyriumChunk), 6.0F);
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.mythrilIngot), new ItemStack(coreItems.adamantiumIngot), new ItemStack(Item.dyePowder, 4), new ItemStack(Content.mediumThyriumChunk), 10.0F);
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.mythrilIngot), new ItemStack(coreItems.adamantiumIngot), new ItemStack(Item.glowstone), new ItemStack(Content.largeThyriumChunk), 30.0F);
 		    	
 		    	//Sinisite				    	
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.onyxGem), new ItemStack(coreItems.mythrilIngot), new ItemStack(Item.glowstone), new ItemStack(Content.smallSinisiteChunk), 12.0F);
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.onyxGem), new ItemStack(coreItems.mythrilIngot), new ItemStack(Item.blazePowder), new ItemStack(Content.mediumSinisiteChunk), 20.0F);
 		    	FusionRecipes.smelting().addSmelting(new ItemStack(coreItems.onyxGem), new ItemStack(coreItems.mythrilIngot), new ItemStack(Item.ghastTear), new ItemStack(Content.largeSinisiteChunk), 60.0F);
 	
 		    //Regular Furnace
 				GameRegistry.addSmelting(Content.largeBronzeChunk.itemID, new ItemStack(Content.bronzeIngot, 1, 0), 0.3F);
				GameRegistry.addSmelting(Content.largeThyriumChunk.itemID, new ItemStack(Content.thyriumIngot, 1, 0), 0.6F);
				GameRegistry.addSmelting(Content.largeSinisiteChunk.itemID, new ItemStack(Content.sinisiteIngot, 1, 0), 1.0F);
 	}
 }
