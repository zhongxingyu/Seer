 package airbreather.mods.airbreathercore.recipe;
 
 import java.util.ArrayList;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import airbreather.mods.airbreathercore.item.ItemDefinition;
 import airbreather.mods.airbreathercore.item.ItemRegistry;
 
 // Implements RecipeRegistrar using the FML GameRegistry.
 public final class FmlRecipeRegistrar implements RecipeRegistrar
 {
     // register the given recipes with the underlying system.
     public void RegisterRecipes(RecipeConfiguration recipeConfiguration, ItemRegistry itemRegistry)
     {
         for (Recipe recipe : recipeConfiguration.GetRecipes())
         {
             switch (recipe.GetRecipeType())
             {
                 case Smelting:
                     RegisterSmeltingRecipe(recipe, itemRegistry);
                     break;
 
                 case ShapedCrafting:
                     RegisterShapedCraftingRecipe(recipe, itemRegistry);
                     break;
             }
         }
     }
 
     private static void RegisterSmeltingRecipe(Recipe recipe, ItemRegistry itemRegistry)
     {
         SmeltingRecipe smeltingRecipe = (SmeltingRecipe)recipe;
 
        ItemDefinition input = smeltingRecipe.GetInput();
 
         RecipeResult result = smeltingRecipe.GetResult();
         ItemStack resultItemStack = GetResultItemStack(result, itemRegistry);
 
        int inputID = input.GetItemID();
         float experience = smeltingRecipe.GetExperience();
 
         GameRegistry.addSmelting(inputID, resultItemStack, experience);
     }
 
     private static void RegisterShapedCraftingRecipe(Recipe recipe, ItemRegistry itemRegistry)
     {
         ShapedCraftingRecipe shapedCraftingRecipe = (ShapedCraftingRecipe)recipe;
 
         // Gotta convert ItemDefinition to their Item.
         Object[] weakInputs = shapedCraftingRecipe.GetInputs();
         ArrayList<Object> strongInputs = new ArrayList<Object>(weakInputs.length);
         for (Object weakInput : weakInputs)
         {
             if (weakInput instanceof ItemDefinition)
             {
                 ItemDefinition definition = (ItemDefinition)weakInput;
                 Item strongInput = itemRegistry.FetchItem(definition);
                 strongInputs.add(strongInput);
             }
             else
             {
                 strongInputs.add(weakInput);
             }
         }
 
         RecipeResult result = shapedCraftingRecipe.GetResult();
         ItemStack resultItemStack = GetResultItemStack(result, itemRegistry);
 
         GameRegistry.addRecipe(resultItemStack, strongInputs.toArray());
     }
 
     private static ItemStack GetResultItemStack(RecipeResult result, ItemRegistry itemRegistry)
     {
         ItemDefinition resultItemDefinition = result.GetItemDefinition();
 
         Item resultItem = itemRegistry.FetchItem(resultItemDefinition);
         ItemStack resultItemStack = new ItemStack(resultItem);
         int requestedStackSize = Math.max(1, result.GetCount());
 
         resultItemStack.stackSize = Math.min(requestedStackSize, resultItemStack.getMaxStackSize());
         return resultItemStack;
     }
 }
