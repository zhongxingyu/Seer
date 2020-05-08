 package si.meansoft.logisticraft.common.recipes;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import si.meansoft.logisticraft.common.blocks.LCBlocks;
 import si.meansoft.logisticraft.common.items.LCItems;
 import net.minecraft.src.Block;
 import net.minecraft.src.CraftingManager;
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 
 public class RecipesItems {
 
     public static void itemRecipes() {
 
 	/* OreDictionary recipes */
 	CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LCItems.coins, 8, 0), true, new Object[] { " # ", "# #", " # ", Character.valueOf('#'), LCItems.ingotCopper }));
 	CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LCItems.coins, 8, 1), true, new Object[] { " # ", "# #", " # ", Character.valueOf('#'), LCItems.ingotSilver }));
 	CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LCItems.coins, 8, 2), true, new Object[] { " # ", "# #", " # ", Character.valueOf('#'), Item.ingotGold }));
 	CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(LCItems.coins, 8, 3), true, new Object[] { " # ", "# #", " # ", Character.valueOf('#'), LCItems.ingotPlatinum }));
 
 	/* Normal item recipes */
 	GameRegistry.addRecipe(new ItemStack(LCItems.cardboard, 9, 0), new Object[] {"###", "#$#", "###", Character.valueOf('#'), Item.paper, Character.valueOf('$'), Item.slimeBall});
 	GameRegistry.addRecipe(new ItemStack(LCItems.boards, 6, 0), new Object[] {" # ", " # ", " # ", Character.valueOf('#'), Block.wood});
 	GameRegistry.addRecipe(new ItemStack(LCItems.nails, 6, 0), new Object[] {" # ", " # ", " # ", Character.valueOf('#'), Item.ingotIron});
	GameRegistry.addRecipe(new ItemStack(LCItems.knife), new Object[] {"  #", " # ", "$  ", Character.valueOf('#'), Item.ingotIron, Character.valueOf('$'), Item.stick});
     }
 }
