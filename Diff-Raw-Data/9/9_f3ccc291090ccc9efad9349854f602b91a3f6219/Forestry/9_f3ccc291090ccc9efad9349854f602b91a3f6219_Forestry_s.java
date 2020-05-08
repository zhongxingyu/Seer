 package ro.narc.liquiduu.integration;
 
 import ro.narc.liquiduu.LiquidUU;
 
 import net.minecraftforge.liquids.LiquidStack;
 import net.minecraftforge.liquids.LiquidContainerData;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import buildcraft.api.recipes.RefineryRecipe;
 
 import forestry.api.core.ItemInterface;
 import forestry.api.recipes.RecipeManagers;
 
 import ic2.common.Ic2Items;
 
 import net.minecraft.src.Item;
 import net.minecraft.src.ItemStack;
 
 public class Forestry {
     public static boolean init() {
 
         ItemStack canEmpty = ItemInterface.getItem("canEmpty");
         ItemStack ingotTin = ItemInterface.getItem("ingotTin");
 
         RecipeManagers.squeezerManager.addRecipe(5, new ItemStack[]{Ic2Items.matter}, LiquidUU.liquidUUStack);
         RecipeManagers.squeezerManager.addRecipe(5, new ItemStack[]{LiquidUU.cannedUU}, LiquidUU.liquidUUStack, ingotTin, 5);
 
         RecipeManagers.bottlerManager.addRecipe(5, LiquidUU.liquidUUStack, Ic2Items.cell, Ic2Items.matter);
         RecipeManagers.bottlerManager.addRecipe(5, LiquidUU.liquidUUStack, canEmpty, LiquidUU.cannedUU);
 
         initBuildcraftLiquids(canEmpty);
         initRefineryRecipes();
 
         return true;
     }
 
     @SuppressWarnings("unchecked")
     public static void initBuildcraftLiquids(ItemStack canEmpty) {
         LiquidContainerData cannedUUData = new LiquidContainerData(LiquidUU.liquidUUStack, LiquidUU.cannedUU, canEmpty);
         LiquidContainerRegistry.registerLiquid(cannedUUData);
     }
 
     public static void initRefineryRecipes() {
         Item liquidBiomass = ItemInterface.getItem("liquidBiomass").getItem();
         Item liquidBiofuel = ItemInterface.getItem("liquidBiofuel").getItem();
         Item liquidSeedOil = ItemInterface.getItem("liquidSeedOil").getItem();
         Item liquidJuice   = ItemInterface.getItem("liquidJuice").getItem();
         Item liquidHoney   = ItemInterface.getItem("liquidHoney").getItem();
         Item liquidUU      = LiquidUU.liquidUU.getItem();
 
         RefineryRecipe.registerRefineryRecipe(
             new RefineryRecipe(
                 new LiquidStack(liquidUU, 1),
                 new LiquidStack(liquidBiomass, 1),
                 new LiquidStack(liquidBiomass, 2),
                 5, 1
             )
         );
         RefineryRecipe.registerRefineryRecipe(
             new RefineryRecipe(
                 new LiquidStack(liquidUU, 2),
                 new LiquidStack(liquidBiofuel, 1),
                 new LiquidStack(liquidBiofuel, 2),
                 5, 1
             )
         );
         RefineryRecipe.registerRefineryRecipe(
             new RefineryRecipe(
                 new LiquidStack(liquidUU, 1),
                 new LiquidStack(liquidSeedOil, 1),
                 new LiquidStack(liquidSeedOil, 4),
                 5, 1
             )
         );
         RefineryRecipe.registerRefineryRecipe(
             new RefineryRecipe(
                 new LiquidStack(liquidUU, 1),
                 new LiquidStack(liquidJuice, 1),
                 new LiquidStack(liquidJuice, 4),
                 5, 1
             )
         );
         RefineryRecipe.registerRefineryRecipe(
             new RefineryRecipe(
                 new LiquidStack(liquidUU, 1),
                 new LiquidStack(liquidHoney, 1),
                 new LiquidStack(liquidHoney, 2),
                 5, 1
             )
         );
     }
 }
