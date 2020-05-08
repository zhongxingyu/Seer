 package si.meansoft.logisticraft.common.recipes;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.minecraft.src.Block;
 import net.minecraft.src.CraftingManager;
 import net.minecraft.src.FurnaceRecipes;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import si.meansoft.logisticraft.common.blocks.LCBlocks;
 import si.meansoft.logisticraft.common.items.LCItems;
 
 public class RecipesBlocks {
 	
 	public static void blockRecipes() {
 		
 		/* Ore block smelting */
 		FurnaceRecipes.smelting().addSmelting(LCBlocks.ores.blockID, 0, new ItemStack(LCItems.ingotPlatinum));
 		FurnaceRecipes.smelting().addSmelting(LCBlocks.ores.blockID, 1, new ItemStack(LCItems.ingotSilver));
 		FurnaceRecipes.smelting().addSmelting(LCBlocks.ores.blockID, 2, new ItemStack(LCItems.ingotCopper));
 		
 		/* Colored glass */
 		for (int i = 0; i < 16; i++) {
 			GameRegistry.addShapelessRecipe(new ItemStack(LCBlocks.coloredGlass, 1, i), new Object[]{Block.glass, new ItemStack(Item.dyePowder, 1, i)});
 		}
 		
 		GameRegistry.addRecipe(new ItemStack(LCBlocks.chimney, 3, 0), new Object[]{ "#$#", "#$#", "#$#", Character.valueOf('#'), Block.brick, Character.valueOf('$'), Block.cobblestone});
 		GameRegistry.addRecipe(new ItemStack(LCBlocks.chimney, 2, 3), new Object[]{ "###", "# #", "###", Character.valueOf('#'), LCItems.cardboard});
 		GameRegistry.addRecipe(new ItemStack(LCBlocks.chimney, 2, 2), new Object[]{ "###", "#$#", "###", Character.valueOf('#'), LCItems.boards, Character.valueOf('$'), LCItems.nails});
 		GameRegistry.addRecipe(new ItemStack(LCBlocks.stackBench, 1, 0), new Object[]{"$&$","X#X","XXX", Character.valueOf('#'), Block.workbench, Character.valueOf('X'), Block.cobblestone, Character.valueOf('$'), Item.ingotIron, Character.valueOf('&'), new ItemStack(Item.dyePowder, 1 , 4)});
 		GameRegistry.addRecipe(new ItemStack(LCBlocks.machines, 1, 0), new Object[]{"$&$","X#X","XXX", Character.valueOf('#'), Item.bucketWater, Character.valueOf('X'), Block.cobblestone, Character.valueOf('$'), Item.ingotIron, Character.valueOf('&'), Item.diamond});
 		GameRegistry.addRecipe(new ItemStack(LCBlocks.machines, 1, 3), new Object[]{"XXX","X#X","XXX", Character.valueOf('#'), Item.bed, Character.valueOf('X'), Block.stone});
		GameRegistry.addRecipe(new ItemStack(LCBlocks.machines, 1, 4), new Object[]{"X$X","X#X","XXX", Character.valueOf('#'), Block.redstoneLampIdle, Character.valueOf('X'), Block.stone, Character.valueOf('$'), Block.glass});
 	}
 }
