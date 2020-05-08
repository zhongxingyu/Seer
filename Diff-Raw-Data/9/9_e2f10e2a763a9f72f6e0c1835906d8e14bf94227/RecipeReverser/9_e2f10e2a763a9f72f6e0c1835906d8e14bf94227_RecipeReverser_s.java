 package net.mcft.copy.vanilladj.recipe;
 
 import java.util.ArrayList;
 import java.util.List;
 
import net.mcft.copy.betterstorage.misc.Constants;
 import net.mcft.copy.vanilladj.misc.Utils;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.item.crafting.ShapelessRecipes;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 /** Adds reverse crafting recipes for recipes of some items. */
 public class RecipeReverser implements IRecipeListener {
 	
 	private final ItemStack[] reverse;
 	
 	public RecipeReverser(Object... reverse) {
 		
 		this.reverse = new ItemStack[reverse.length];
 		for (int i = 0; i < reverse.length; i++)
 			this.reverse[i] = Utils.makeStack(reverse[i]);
 		
 	}
 	
 	@Override
 	public void doSomethingWith(RecipeIterator iterator, IRecipe recipe) {
 		
 		ItemStack output = recipe.getRecipeOutput();
 		if ((output == null) || !Utils.contains(reverse, output)) return;
 		reverse(recipe);
 		
 	}
 	
 	public static void reverse(IRecipe recipe) {
 		
 		ItemStack output = recipe.getRecipeOutput();
 		int resultAmount = output.stackSize;
 		int recipeAmount = 0;
 		
 		RecipeContainer container = new RecipeContainer(recipe);
 		
 		ItemStack recipeItem = null;
 		for (ItemStack item : container.getItemStacksOnly()) {
 			if (recipeItem == null) recipeItem = item.copy();
 			else if (!Utils.matches(recipeItem, item)) return;
 			recipeAmount++;
 		}
 		
 		int gcd = gcd(resultAmount, recipeAmount);
 		resultAmount /= gcd;
 		recipeAmount /= gcd;
 		if (resultAmount > 9) return;
 		
 		List<ItemStack> recipeItems = new ArrayList<ItemStack>(resultAmount);
 		for (int i = 0; i < resultAmount; i++) recipeItems.add(output);
 		
 		output = recipeItem.copy();
 		if (output.getItemDamage() == Constants.anyDamage)
 			output.setItemDamage(0);
 		System.out.println(output);
 		output.stackSize = recipeAmount;
 		
 		GameRegistry.addRecipe(new ShapelessRecipes(output, recipeItems));
 		
 	}
 	
 	private static int gcd(int a, int b) {
 		return ((b == 0) ? a : gcd(b, a % b));
 	}
 	
 }
