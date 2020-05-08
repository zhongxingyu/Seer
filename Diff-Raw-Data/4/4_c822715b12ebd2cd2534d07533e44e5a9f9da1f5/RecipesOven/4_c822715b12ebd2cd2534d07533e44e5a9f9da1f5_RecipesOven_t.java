 package ccm.harvestry.core.recipe;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import ccm.harvestry.api.fuels.OvenFuels;
 import ccm.harvestry.api.recipes.OvenRecipes;
 import ccm.harvestry.enums.items.EnumFood;
 import ccm.harvestry.enums.items.EnumItem;
 import ccm.harvestry.enums.items.EnumItemSixteen;
 import ccm.harvestry.enums.items.EnumUncookedFood;
 import ccm.harvestry.item.ModItems;
 import ccm.harvestry.utils.lib.EnumHandling;
 
 final class RecipesOven {
 
     public RecipesOven() {
         registerFuels();
         registerRecipes();
     }
 
     private static void registerFuels() {
         OvenFuels.registerOvenFuel(new ItemStack(ModItems.heWood));
         OvenFuels.registerOvenFuel(new ItemStack(ModItems.heAluminum));
         OvenFuels.registerOvenFuel(new ItemStack(ModItems.heIron));
         OvenFuels.registerOvenFuel(new ItemStack(ModItems.heGold));
         OvenFuels.registerOvenFuel(new ItemStack(ModItems.heCopper));
         OvenFuels.registerOvenFuel(new ItemStack(ModItems.heTin));
         OvenFuels.registerOvenFuel(new ItemStack(ModItems.heBronze));
     }
 
     private static void registerRecipes() {
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedBread), new ItemStack(Item.bread), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.panBread));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedPotato), new ItemStack(Item.bakedPotato));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedCake), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.cookedCake), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.panCake));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedPiePumpkin), new ItemStack(Item.pumpkinPie), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.panPie));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedPieApple), EnumHandling.enumFood.getItemIS(EnumFood.foodPieApple), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.panPie));
         // OvenRecipes.cooking().addCookingRecipe(EHandler.getItem(UncookedFoodEnum.uncookedPieBerryBlue),
         // EHandler.getItem(FoodEnum.foodBerryBlue),
         // EHandler.getItem(ItemEnumSixteen.panPie));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedPieBerryCherry), EnumHandling.enumFood.getItemIS(EnumFood.foodPieBerryCherry), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.panPie));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedCookies), EnumHandling.enumFood.getItemIS(EnumFood.foodCookie, 16), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.sheetCookie));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedCookiesSugar), EnumHandling.enumFood.getItemIS(EnumFood.foodCookieSugar, 16), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.sheetCookie));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedCookiesChocolateChip), EnumHandling.enumFood.getItemIS(EnumFood.foodCookieChocolateChip, 16), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.sheetCookie));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedPizzaCheese), EnumHandling.enumFood.getItemIS(EnumFood.foodPizzaCheese));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedPizza), EnumHandling.enumFood.getItemIS(EnumFood.foodPizza));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedMeatBall), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.cookedMeatBall));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedMeatPatty), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.cookedMeatPatty));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedNoodles), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.cookedNoodles));
         OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumUncooked.getItemIS(EnumUncookedFood.uncookedTortilla), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.itemTortilla));
        OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumItem.getItemIS(EnumItem.dustBeef), EnumHandling.enumItem16.getItemIS(EnumItemSixteen.cookedDustMeat));
        OvenRecipes.cooking().addCookingRecipe(EnumHandling.enumFood.getItemIS(EnumFood.foodEggs), new ItemStack(Item.egg));
         OvenRecipes.cooking().addCookingRecipe(new ItemStack(Block.pumpkin), EnumHandling.enumItem.getItemIS(EnumItem.rawPumpkinMash, 4));
     }
 }
