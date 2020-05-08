 package com.teammetallurgy.agriculture.gui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 import com.teammetallurgy.agriculture.AgricultureItems;
 import com.teammetallurgy.agriculture.SubItem;
 
 public class OvenRecipes
 {
 	private static Map<Integer, OvenRecipes> recipes;
 
 	public static void addRecipe(Object result, Object source, int heat)
 	{
 		final ItemStack resultStack = getItemStack(result);
 		final ItemStack sourceStack = getItemStack(source);
 		final OvenRecipes recipe = new OvenRecipes(sourceStack, resultStack, heat);
 
 		final int hash = (sourceStack.itemID << 8) + sourceStack.getItemDamage();
 
 		if (recipes == null)
 		{
 			recipes = new HashMap<Integer, OvenRecipes>();
 		}
 
 		recipes.put(hash, recipe);
 	}
 
 	public static ItemStack getItemStack(Object object)
 	{
 		ItemStack stack = null;
 		if (object instanceof ItemStack)
 		{
 			stack = (ItemStack) object;
 		} else if (object instanceof SubItem)
 		{
 			stack = ((SubItem) object).getItemStack();
 		} else if (object instanceof Item)
 		{
 			stack = new ItemStack((Item) object);
 		}
 		return stack;
 	}
 
 	public static ItemStack getResult(ItemStack input, int heat)
 	{
 		final int hash = (input.itemID << 8) + input.getItemDamage();
 		final OvenRecipes result = recipes.get(hash);
 
 		if (result != null)
 		{
 			if (result.getRequiredHeat() <= heat)
 			{
 				final ItemStack itemStack = result.getResult();
 				itemStack.stackSize = 1;
 				return itemStack;
 			}
 		}
 
 		return null;
 	}
 
 	private final int requiredHeat;
 
 	private final ItemStack result;
 
 	static
 	{
 		addRecipe(AgricultureItems.caramel.getItemStack(), new ItemStack(Item.sugar), 100000);
 		addRecipe(AgricultureItems.shortcake, AgricultureItems.batter, 100000);
 		addRecipe(AgricultureItems.carrotCake, AgricultureItems.carrotCakeBatter, 100000);
 		addRecipe(AgricultureItems.cheeseSandwich, AgricultureItems.grilledCheese, 100000);
 		addRecipe(AgricultureItems.loafOfBread, AgricultureItems.dough, 100000);
 		addRecipe(AgricultureItems.pasta, AgricultureItems.pastaDough, 100000);
 		addRecipe(AgricultureItems.toastedPbjSandwichApple, AgricultureItems.pbjSandwichApple, 100000);
 		addRecipe(AgricultureItems.toastedPbjSandwichStrawberry, AgricultureItems.pbjSandwichStrawberry, 100000);
 		addRecipe(AgricultureItems.roastedPeanuts, AgricultureItems.peanuts, 100000);
 		addRecipe(AgricultureItems.applePie, AgricultureItems.rawApplePie, 100000);
 		addRecipe(AgricultureItems.frenchToast, AgricultureItems.rawFrenchToast, 100000);
 		addRecipe(AgricultureItems.hamburgerPatty, AgricultureItems.rawHamburgerPatty, 100000);
 		addRecipe(AgricultureItems.strawberryPie, AgricultureItems.rawStrawberryPie, 100000);
 		addRecipe(AgricultureItems.beefJerkey, AgricultureItems.saltedBeef, 100000);
 		addRecipe(AgricultureItems.bacon, AgricultureItems.saltedPork, 100000);
 		addRecipe(AgricultureItems.toast, AgricultureItems.sliceOfBread, 100000);
 
 		addRecipe(AgricultureItems.appleCinnamonCookie, AgricultureItems.appleCinnamonCookieDough, 100000);
 		addRecipe(AgricultureItems.butterCookie, AgricultureItems.butterCookieDough, 100000);
 		addRecipe(AgricultureItems.chocolateChipCookie, AgricultureItems.chocolateChipCookieDough, 100000);
 		addRecipe(AgricultureItems.peanutButterCookie, AgricultureItems.peanutButterCookieDough, 100000);
 		addRecipe(AgricultureItems.pumpkinCookie, AgricultureItems.pumpkinCookieDough, 100000);
 		addRecipe(AgricultureItems.snickerDoodle, AgricultureItems.snickerDoodleDough, 100000);
 		addRecipe(AgricultureItems.strawberryJellyCookie, AgricultureItems.strawberryJellyCookieDough, 100000);
 		addRecipe(AgricultureItems.sugarCookie, AgricultureItems.sugarCookieDough, 100000);
 		addRecipe(AgricultureItems.chocolateSauce, AgricultureItems.chocolate, 100000);
 		addRecipe(AgricultureItems.roastedMarshmellows, AgricultureItems.marshmellows, 100000);
 		addRecipe(AgricultureItems.toastedPBSandwich, AgricultureItems.pbSandwich, 100000);
 
 		final int burnTime = 50000;
 		addRecipe(AgricultureItems.appleCinnamonCookieBurned, AgricultureItems.appleCinnamonCookie, burnTime);
 		addRecipe(AgricultureItems.applePieBurned, AgricultureItems.applePie, burnTime);
 		addRecipe(AgricultureItems.baconBurned, AgricultureItems.bacon, burnTime);
 		addRecipe(AgricultureItems.beefJerkeyBurned, AgricultureItems.beefJerkey, burnTime);
 		addRecipe(AgricultureItems.butterCookieBurned, AgricultureItems.butterCookie, burnTime);
 		addRecipe(AgricultureItems.caramelBurned, AgricultureItems.caramel, burnTime);
 		addRecipe(AgricultureItems.carrotCakeBurned, AgricultureItems.carrotCake, burnTime);
 		addRecipe(AgricultureItems.chocolateChipCookieBurned, AgricultureItems.chocolateChipCookie, burnTime);
 		addRecipe(AgricultureItems.chocolateSauceBurned, AgricultureItems.chocolateSauce, burnTime);
 		addRecipe(AgricultureItems.frenchToastBurned, AgricultureItems.frenchToast, burnTime);
 		addRecipe(AgricultureItems.grilledCheeseBurned, AgricultureItems.grilledCheese, burnTime);
 		addRecipe(AgricultureItems.hamburgerPattyBurned, AgricultureItems.hamburgerPattyBurned, burnTime);
 		addRecipe(AgricultureItems.loafOfBreadBurned, AgricultureItems.loafOfBread, burnTime);
 		addRecipe(AgricultureItems.pastaBurned, AgricultureItems.pasta, burnTime);
 		addRecipe(AgricultureItems.peanutButterCookieBurned, AgricultureItems.peanutButter, burnTime);
 		addRecipe(AgricultureItems.pumpkinCookieBurned, AgricultureItems.pumpkinCookie, burnTime);
 		addRecipe(AgricultureItems.roastedMarshmellowsBurned, AgricultureItems.roastedMarshmellows, burnTime);
 		addRecipe(AgricultureItems.roastedPeanutsBurned, AgricultureItems.roastedMarshmellows, burnTime);
		addRecipe(AgricultureItems.shortakeBurned, AgricultureItems.shortcake, burnTime);
 		addRecipe(AgricultureItems.snickerDoodleBurned, AgricultureItems.snickerDoodle, burnTime);
 		addRecipe(AgricultureItems.strawberryJellyCookieBurned, AgricultureItems.strawberryJellyCookie, burnTime);
 		addRecipe(AgricultureItems.strawberryPieBurned, AgricultureItems.strawberryPie, burnTime);
 		addRecipe(AgricultureItems.sugarCookieBurned, AgricultureItems.sugarCookie, burnTime);
 		addRecipe(AgricultureItems.toastBurned, AgricultureItems.toast, burnTime);
 		addRecipe(AgricultureItems.toastedPBJSandwichAppleBurned, AgricultureItems.toastedPbjSandwichApple, burnTime);
 		addRecipe(AgricultureItems.toastedPBJSandwichStrawberryBurned, AgricultureItems.toastedPbjSandwichStrawberry, burnTime);
 		addRecipe(AgricultureItems.toastedPBSandwichBurned, AgricultureItems.toastedPBSandwich, burnTime);
 	}
 
 	private OvenRecipes(ItemStack source, ItemStack result, int heat)
 	{
 		this.result = result;
 		requiredHeat = heat;
 	}
 
 	private int getRequiredHeat()
 	{
 		return requiredHeat;
 	}
 
 	private ItemStack getResult()
 	{
 		return result;
 	}
 }
