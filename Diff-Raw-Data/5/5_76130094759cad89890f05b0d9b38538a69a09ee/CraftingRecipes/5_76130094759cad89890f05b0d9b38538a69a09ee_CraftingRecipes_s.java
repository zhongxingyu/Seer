 package mods.minecraft.darth.dc.recipe;
 
 import mods.minecraft.darth.dc.core.util.RecipeUtil;
 import mods.minecraft.darth.dc.item.ModItems;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class CraftingRecipes
 {
 
     public static void init()
     {
         
         //Science Notebook
         GameRegistry.addRecipe(new ItemStack(ModItems.sciNotebook), "dld", "dbd", "dnd", 'd', Item.paper, 'b', Item.writableBook, 'l', ModItems.scienceDye, 'n', ModItems.notebookLock);
         
         //Monocle Lens
         GameRegistry.addRecipe(new ItemStack(ModItems.monocleLens), "ggg", "gpg", "ggg", 'g', Item.goldNugget, 'p', Block.thinGlass);
     
         //Real Monocle
         GameRegistry.addRecipe(new ItemStack(ModItems.monocleArmor), "ls ", "  s", "  s", 'l', ModItems.monocleLens, 's', Item.silk);
         
         //Throwing Knife
        GameRegistry.addRecipe(new ItemStack(ModItems.knifeThrowing), " f ", " s ", 'f', Item.flint, 's', Item.stick);
     
         //Melee Knife
         GameRegistry.addShapelessRecipe(new ItemStack(ModItems.knifeMelee), new Object[] {ModItems.knifeThrowing});
         
         //Flint Shovel
        GameRegistry.addRecipe(new ItemStack(ModItems.shovelFlint), " f ", " s ", " s ", 'f', Item.flint, 's', Item.stick);
         
         //Flint Pickaxe
         GameRegistry.addRecipe(new ItemStack(ModItems.pickaxeFlint), "fff", " s ", " s ", 'f', Item.flint, 's', Item.stick);
    
         //Flint Axe
         GameRegistry.addRecipe(new ItemStack(ModItems.axeFlint), "ff ", "fs ", " s ", 'f', Item.flint, 's', Item.stick);
         
         //Raw Goron
         GameRegistry.addRecipe(new ItemStack(ModItems.rawGoron), "iii", "gdg", "iii", 'g', Item.ingotGold, 'i', Item.ingotIron, 'd', ModItems.scienceDye);
         
         //Scientific Dye
         RecipeUtil.addOreRecipe(new ItemStack(ModItems.scienceDye, 2), new Object[] {"ddd", "dpd", "ddd", 'd', new ItemStack(Item.dyePowder, 1, 4), 'p', new ItemStack(ModItems.dirtPellet)});
         
         //Crafting Initializer MK 1
         GameRegistry.addRecipe(new ItemStack(ModItems.craftingUpgrade), "ggg", "dcd", "ddd", 'g', ModItems.goronIngot, 'd', ModItems.scienceDye, 'c', Block.workbench);
         
         //Special Flint Shovel
         GameRegistry.addRecipe(new ItemStack(ModItems.specialShovelFlint), "fff", "fhf", "fsf", 'f', Item.flint, 's', Item.stick, 'h', ModItems.shovelFlint);
         
         
     }
     
 }
