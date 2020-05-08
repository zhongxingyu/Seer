 package dreamLand.utils.handlers;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import dreamLand.blocks.ModBlocks;
 import dreamLand.items.ModItems;
 
 public class RecipeHandler {
 
     public static void add() {
         // Creates Shaped Recipes
         addShapedRecipes();
        
         // Creates Shapeless Recipes
         addShapelessRecipes();
     }
 
     private static void addShapedRecipes() {
         GameRegistry.addRecipe(new ItemStack(ModBlocks.portalObsidian), new Object[] { "XXX", "XOX", "XXX", 'X', Block.obsidian, 'O', Block.blockDiamond });
        
         GameRegistry.addRecipe(new ItemStack(ModItems.itemlighter), new Object[] { "  X", " X ", "X  ", 'X', Item.emerald });
        
         GameRegistry.addRecipe(new ItemStack(ModItems.itemSwordPhantomIron), new Object[] { " O ", " O ", " X ", 'X', Item.emerald, 'O', Item.diamond });
     
     }
    
     private static void addShapelessRecipes() {
         //Logs to Planks
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 0), new Object[] {new ItemStack(ModBlocks.dreamLogs, 1, 0)});
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 2), new Object[] {new ItemStack(ModBlocks.dreamLogs, 1, 1)});
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 4), new Object[] {new ItemStack(ModBlocks.dreamLogs, 1, 2)});
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 6), new Object[] {new ItemStack(ModBlocks.dreamLogs, 1, 3)});
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 1), new Object[] {new ItemStack(ModBlocks.nmLogs, 1, 0)});
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 3), new Object[] {new ItemStack(ModBlocks.nmLogs, 1, 1)});
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 5), new Object[] {new ItemStack(ModBlocks.nmLogs, 1, 2)});
         GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.dreamPlanks, 4, 7), new Object[] {new ItemStack(ModBlocks.nmLogs, 1, 3)});
      
     }
 }
