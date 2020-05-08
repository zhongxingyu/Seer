 package com.teammetallurgy.agriculture;
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.oredict.OreDictionary;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class AgricultureItems {
     // Oven
     public static SubItem appleCinnamonCookie;
     public static SubItem appleCinnamonCookieBurned;
     public static SubItem appleCinnamonCookieDough;
 
     // public static SubItem dough;
     public static SubItem appleGelatin;
     public static SubItem appleJelly;
     public static SubItem appleJellyToast;
     public static SubItem appleMush;
     public static SubItem applePie;
     public static SubItem applePieBurned;
     public static SubItem bacon;
     public static SubItem baconBurned;
     public static SubItem baconCheeseburger;
     public static SubItem baconCheeseFries;
     public static SubItem batter;
     public static SubItem beefJerkey;
     public static SubItem beefJerkeyBurned;
     public static SubItem beer;
     public static SubItem bowlOfRawPasta;
     public static SubItem breadCrumbs;
     public static SubItem breadedChicken;
     public static SubItem butter;
     public static SubItem butterCookie;
     public static SubItem butterCookieBurned;
     public static SubItem butterCookieDough;
     public static SubItem butteredToast;
     public static SubItem candiedApple;
     public static SubItem caramel;
     public static SubItem caramelApple;
     public static SubItem caramelAppleWithNuts;
     public static SubItem caramelBurned;
     public static SubItem carrotCake;
     public static SubItem carrotCakeBatter;
     public static SubItem carrotCakeBurned;
     public static SubItem ceramicBowl;
     public static SubItem ceramicCup;
     public static SubItem ceramicPlate;
     public static SubItem cheese;
     public static SubItem cheeseburger;
     public static SubItem cheeseFries;
     public static SubItem cheeseSandwich;
     public static SubItem cheesyBaconPotatoes;
     public static SubItem cheesyPotatoes;
     public static SubItem chickenNuggets;
     public static SubItem chocolate;
     public static SubItem chocolateChipCookie;
     public static SubItem chocolateChipCookieBurned;
     public static SubItem chocolateChipCookieDough;
     public static SubItem chocolateCoveredStrawberries;
     // IceBox
     public static SubItem chocolateIceCream;
     public static SubItem chocolateIceCreamChocolateSauce;
     public static SubItem chocolateIceCreamMix;
     public static SubItem chocolateSauce;
     public static SubItem chocolateSauceBurned;
     public static SubItem cinnamon;
     public static SubItem cinnamonAndSugar;
     public static SubItem cinnamonToast;
     public static SubItem clayBowl;
     public static SubItem clayCup;
     public static SubItem clayPlate;
     // public static SubItem flour;
     public static SubItem cookingOil;
     public static SubItem crushedPeanuts;
     public static SubItem deluxeHotCocoa;
     public static SubItem dicedPotatoes;
     public static int dishID = ConfigHandler.getItemId("Dish", 19999);
     public static SubItem doubleBaconCheeseburger;
     public static SubItem dough;
     public static SubItem flour;
     public static int foodID = ConfigHandler.getItemId("Food", 20000);
     public static SubItem frenchFries;
     public static SubItem frenchToast;
     public static SubItem frenchToastBurned;
     public static SubItem friedChicken;
     public static SubItem gelatin;
     public static SubItem grilledCheese;
     public static SubItem grilledCheeseBurned;
     public static SubItem groundBeef;
     public static SubItem groundChicken;
     public static SubItem groundCinnamon;
     public static SubItem groundPork;
     public static SubItem hamburger;
     public static SubItem hamburgerPatty;
     public static SubItem hamburgerPattyBurned;
     public static SubItem hotCocoa;
     public static SubItem iceCreamMix;
     public static SubItem loafOfBread;
 
     public static SubItem loafOfBreadBurned;
     public static SubItem macaroniAndCheese;
     public static SubItem marshmellows;
     public static SubItem mashedPotatos;
     public static SubItem milk;
     public static SubItem ovenRack;
     public static SubItem pasta;
     public static SubItem pastaBurned;
     public static SubItem pastaDough;
     public static SubItem pbjSandwichApple;
     public static SubItem pbjSandwichStrawberry;
     public static SubItem pbSandwich;
     public static SubItem peanutButter;
     public static SubItem peanutButterCookie;
     public static SubItem peanutButterCookieBurned;
     public static SubItem peanutButterCookieDough;
     public static SubItem peanuts;
     public static SubItem pumpkinCookie;
     public static SubItem pumpkinCookieBurned;
     public static SubItem pumpkinCookieDough;
     public static SubItem rawApplePie;
     public static SubItem rawChickenNuggets;
     public static SubItem rawFrenchToast;
     public static SubItem rawHamburgerPatty;
     public static SubItem rawStrawberryPie;
     public static SubItem roastedMarshmellows;
     public static SubItem roastedMarshmellowsBurned;
     public static SubItem roastedPeanuts;
     public static SubItem roastedPeanutsBurned;
     public static SubItem salt;
     public static SubItem saltedBeef;
     public static SubItem saltedPork;
     public static SubItem shortcake;
     public static SubItem shortcakeBurned;
     public static SubItem sliceOfBread;
 
     public static SubItem sliceOfCheese;
     public static SubItem snickerDoodle;
     public static SubItem snickerDoodleBurned;
     public static SubItem snickerDoodleDough;
     public static SubItem strawberry;
     public static SubItem strawberryGelatin;
     public static SubItem strawberryIceCream;
     public static SubItem strawberryIceCreamChocolateSauce;
     public static SubItem strawberryIceCreamMix;
     public static SubItem strawberryJelly;
     public static SubItem strawberryJellyCookie;
     public static SubItem strawberryJellyCookieBurned;
     public static SubItem strawberryJellyCookieDough;
     public static SubItem strawberryJellyToast;
     public static SubItem strawberryMush;
     public static SubItem strawberryPie;
     public static SubItem strawberryPieBurned;
     public static SubItem strawberryShortcake;
     public static SubItem sugarCookie;
     public static SubItem sugarCookieBurned;
     public static SubItem sugarCookieDough;
     public static SubItem toast;
     public static SubItem toastBurned;
     public static SubItem toastedPbjSandwichApple;
     public static SubItem toastedPBJSandwichAppleBurned;
     public static SubItem toastedPbjSandwichStrawberry;
     public static SubItem toastedPBJSandwichStrawberryBurned;
 
     public static SubItem toastedPBSandwich;
     // Processor
     public static SubItem toastedPBSandwichBurned;
     public static SubItem vanilla;
     public static SubItem vanillaIceCream;
     public static SubItem vanillaIceCreamChocolateSauce;
     public static SubItem vanillaIceCreamMix;
     public static SubItem vinegar;
 
     public static SubItem water;
     public static SubItem whippedCream;
 
     public static void addNames()
     {
         LanguageRegistry.addName(AgricultureItems.ovenRack.getItemStack(), "Oven Rack");
 
         LanguageRegistry.addName(AgricultureItems.clayBowl.getItemStack(), "Clay Bowl");
         LanguageRegistry.addName(AgricultureItems.clayPlate.getItemStack(), "Clay Plate");
         LanguageRegistry.addName(AgricultureItems.clayCup.getItemStack(), "Clay Cup");
         LanguageRegistry.addName(AgricultureItems.ceramicBowl.getItemStack(), "Ceramic Bowl");
         LanguageRegistry.addName(AgricultureItems.ceramicPlate.getItemStack(), "Ceramic Plate");
         LanguageRegistry.addName(AgricultureItems.ceramicCup.getItemStack(), "Ceramic Cup");
 
         LanguageRegistry.addName(AgricultureItems.strawberry.getItemStack(), "Strawberry");
         LanguageRegistry.addName(AgricultureItems.candiedApple.getItemStack(), "Candied Apple");
         LanguageRegistry.addName(AgricultureItems.water.getItemStack(), "Cup of Water");
         LanguageRegistry.addName(AgricultureItems.milk.getItemStack(), "Cup of Milk");
         LanguageRegistry.addName(AgricultureItems.hotCocoa.getItemStack(), "Hot Cocoa");
         LanguageRegistry.addName(AgricultureItems.vinegar.getItemStack(), "Vinegar");
         LanguageRegistry.addName(AgricultureItems.beer.getItemStack(), "Beer");
         LanguageRegistry.addName(AgricultureItems.rawHamburgerPatty.getItemStack(), "Raw Hamburger Patty");
         LanguageRegistry.addName(AgricultureItems.appleJellyToast.getItemStack(), "Apple Jelly Toast");
         LanguageRegistry.addName(AgricultureItems.cinnamonToast.getItemStack(), "Cinnamon Toast");
         LanguageRegistry.addName(AgricultureItems.strawberryJellyToast.getItemStack(), "Strawberry Jelly Toast");
         LanguageRegistry.addName(AgricultureItems.caramelAppleWithNuts.getItemStack(), "Caramel Apple with Nuts");
         LanguageRegistry.addName(AgricultureItems.appleJelly.getItemStack(), "Apple Jelly");
         LanguageRegistry.addName(AgricultureItems.mashedPotatos.getItemStack(), "Mashed Potatos");
         LanguageRegistry.addName(AgricultureItems.carrotCakeBatter.getItemStack(), "Carrot Cake Batter");
         LanguageRegistry.addName(AgricultureItems.rawFrenchToast.getItemStack(), "Raw French Toast");
         LanguageRegistry.addName(AgricultureItems.batter.getItemStack(), "Batter");
         LanguageRegistry.addName(AgricultureItems.macaroniAndCheese.getItemStack(), "Macaroni and Cheese");
         LanguageRegistry.addName(AgricultureItems.strawberryJelly.getItemStack(), "Strawberry Jelly");
         LanguageRegistry.addName(AgricultureItems.rawApplePie.getItemStack(), "Raw Apple Pie");
         LanguageRegistry.addName(AgricultureItems.rawStrawberryPie.getItemStack(), "Raw Strawberry Pie");
         LanguageRegistry.addName(AgricultureItems.strawberryShortcake.getItemStack(), "Strawberry Shortcake");
         LanguageRegistry.addName(AgricultureItems.hamburger.getItemStack(), "Hamburger");
         LanguageRegistry.addName(AgricultureItems.pbjSandwichApple.getItemStack(), "PB&J Sandwich (Apple)");
         LanguageRegistry.addName(AgricultureItems.pbjSandwichStrawberry.getItemStack(), "PB&J Sandwich (Strawberry)");
         LanguageRegistry.addName(AgricultureItems.cheeseSandwich.getItemStack(), "Cheese Sandwich");
         LanguageRegistry.addName(AgricultureItems.butteredToast.getItemStack(), "Buttered Toast");
         LanguageRegistry.addName(AgricultureItems.baconCheeseFries.getItemStack(), "Bacon Cheese Fries");
         LanguageRegistry.addName(AgricultureItems.baconCheeseburger.getItemStack(), "Bacon Cheeseburger");
         LanguageRegistry.addName(AgricultureItems.chocolate.getItemStack(), "Chocolate");
         LanguageRegistry.addName(AgricultureItems.cheese.getItemStack(), "Cheese");
         LanguageRegistry.addName(AgricultureItems.butter.getItemStack(), "Butter");
         LanguageRegistry.addName(AgricultureItems.whippedCream.getItemStack(), "Whipped Cream");
         LanguageRegistry.addName(AgricultureItems.dough.getItemStack(), "Dough");
         LanguageRegistry.addName(AgricultureItems.appleGelatin.getItemStack(), "Apple Gelatin");
         LanguageRegistry.addName(AgricultureItems.strawberryGelatin.getItemStack(), "Strawberry Gelatin");
         LanguageRegistry.addName(AgricultureItems.marshmellows.getItemStack(), "Marshmellows");
         LanguageRegistry.addName(AgricultureItems.pastaDough.getItemStack(), "Pasta Dough");
         LanguageRegistry.addName(AgricultureItems.cheeseFries.getItemStack(), "Cheese Fries");
         LanguageRegistry.addName(AgricultureItems.rawChickenNuggets.getItemStack(), "Raw Chicken Nuggets");
         LanguageRegistry.addName(AgricultureItems.cheeseburger.getItemStack(), "Cheeseburger");
         LanguageRegistry.addName(AgricultureItems.deluxeHotCocoa.getItemStack(), "Deluxe Hot Cocoa");
         LanguageRegistry.addName(AgricultureItems.saltedBeef.getItemStack(), "Salted Beef");
         LanguageRegistry.addName(AgricultureItems.breadedChicken.getItemStack(), "Breaded Chicken");
         LanguageRegistry.addName(AgricultureItems.saltedPork.getItemStack(), "Salted Pork");
         LanguageRegistry.addName(AgricultureItems.caramelApple.getItemStack(), "Caramel Apple");
         LanguageRegistry.addName(AgricultureItems.chocolateCoveredStrawberries.getItemStack(), "Chocolate-Covered Strawberries");
         LanguageRegistry.addName(AgricultureItems.cinnamonAndSugar.getItemStack(), "Cinnamon and Sugar");
         LanguageRegistry.addName(AgricultureItems.sliceOfCheese.getItemStack(), "Slice of Cheese");
         LanguageRegistry.addName(AgricultureItems.sliceOfBread.getItemStack(), "Slice of Bread");
         LanguageRegistry.addName(AgricultureItems.friedChicken.getItemStack(), "Fried Chicken");
         LanguageRegistry.addName(AgricultureItems.frenchFries.getItemStack(), "French Fries");
         LanguageRegistry.addName(AgricultureItems.chickenNuggets.getItemStack(), "Chicken Nuggets");
         LanguageRegistry.addName(AgricultureItems.shortcake.getItemStack(), "Shortcake");
         LanguageRegistry.addName(AgricultureItems.carrotCake.getItemStack(), "Carrot Cake");
         LanguageRegistry.addName(AgricultureItems.grilledCheese.getItemStack(), "Grilled Cheese");
         LanguageRegistry.addName(AgricultureItems.loafOfBread.getItemStack(), "Loaf of Bread");
         LanguageRegistry.addName(AgricultureItems.pasta.getItemStack(), "Pasta");
         LanguageRegistry.addName(AgricultureItems.toastedPbjSandwichApple.getItemStack(), "Toasted PB&J Sandwich (Apple)");
         LanguageRegistry.addName(AgricultureItems.toastedPbjSandwichStrawberry.getItemStack(), "Toasted PB&J Sandwich (Strawberry)");
         LanguageRegistry.addName(AgricultureItems.roastedPeanuts.getItemStack(), "Roasted Peanuts");
         LanguageRegistry.addName(AgricultureItems.applePie.getItemStack(), "Apple Pie");
         LanguageRegistry.addName(AgricultureItems.frenchToast.getItemStack(), "French Toast");
         LanguageRegistry.addName(AgricultureItems.hamburgerPatty.getItemStack(), "Hamburger Patty");
         LanguageRegistry.addName(AgricultureItems.strawberryPie.getItemStack(), "Strawberry Pie");
         LanguageRegistry.addName(AgricultureItems.beefJerkey.getItemStack(), "Beef Jerkey");
         LanguageRegistry.addName(AgricultureItems.bacon.getItemStack(), "Bacon");
         LanguageRegistry.addName(AgricultureItems.toast.getItemStack(), "Toast");
         LanguageRegistry.addName(AgricultureItems.caramel.getItemStack(), "Caramel");
         LanguageRegistry.addName(AgricultureItems.appleMush.getItemStack(), "Apple Mush");
         LanguageRegistry.addName(AgricultureItems.gelatin.getItemStack(), "Gelatin");
         LanguageRegistry.addName(AgricultureItems.breadCrumbs.getItemStack(), "Bread Crumbs");
         LanguageRegistry.addName(AgricultureItems.groundBeef.getItemStack(), "Ground Beef");
         LanguageRegistry.addName(AgricultureItems.groundChicken.getItemStack(), "Ground Chicken");
         LanguageRegistry.addName(AgricultureItems.groundPork.getItemStack(), "Ground Pork");
         LanguageRegistry.addName(AgricultureItems.crushedPeanuts.getItemStack(), "Crushed Peanuts");
         LanguageRegistry.addName(AgricultureItems.strawberryMush.getItemStack(), "Strawberry Mush");
         LanguageRegistry.addName(AgricultureItems.flour.getItemStack(), "Flour");
         LanguageRegistry.addName(AgricultureItems.peanutButter.getItemStack(), "Peanut Butter");
         LanguageRegistry.addName(AgricultureItems.cookingOil.getItemStack(), "Cooking Oil");
         LanguageRegistry.addName(AgricultureItems.salt.getItemStack(), "Salt");
         LanguageRegistry.addName(AgricultureItems.cinnamon.getItemStack(), "Cinnamon");
         LanguageRegistry.addName(AgricultureItems.groundCinnamon.getItemStack(), "Ground Cinnamon");
         LanguageRegistry.addName(AgricultureItems.peanuts.getItemStack(), "Peanuts");
 
         LanguageRegistry.addName(AgricultureItems.appleCinnamonCookieDough.getItemStack(), "Apple Cinnamon Cookie Dough");
         LanguageRegistry.addName(AgricultureItems.bowlOfRawPasta.getItemStack(), "Bowl of Raw Pasta");
         LanguageRegistry.addName(AgricultureItems.butterCookieDough.getItemStack(), "Butter Cookie Dough");
         LanguageRegistry.addName(AgricultureItems.cheesyBaconPotatoes.getItemStack(), "Cheesy Bacon Potatoes");
         LanguageRegistry.addName(AgricultureItems.cheesyPotatoes.getItemStack(), "Cheesy Potatoes");
         LanguageRegistry.addName(AgricultureItems.chocolateChipCookieDough.getItemStack(), "Chocolate Chip Cookie Dough");
         LanguageRegistry.addName(AgricultureItems.chocolateIceCreamChocolateSauce.getItemStack(), "Chocolate Ice Cream (Chocolate Sauce)");
         LanguageRegistry.addName(AgricultureItems.chocolateIceCreamMix.getItemStack(), "Chocolate Ice Cream Mix");
         LanguageRegistry.addName(AgricultureItems.dicedPotatoes.getItemStack(), "Diced Potatoes");
         LanguageRegistry.addName(AgricultureItems.doubleBaconCheeseburger.getItemStack(), "Double Bacon Cheeseburger");
         LanguageRegistry.addName(AgricultureItems.iceCreamMix.getItemStack(), "Ice Cream Mix");
         LanguageRegistry.addName(AgricultureItems.pbSandwich.getItemStack(), "PB Sandwich");
         LanguageRegistry.addName(AgricultureItems.peanutButterCookieDough.getItemStack(), "Peanut Butter Cooke Dough");
         LanguageRegistry.addName(AgricultureItems.pumpkinCookieDough.getItemStack(), "Pumpkin Cookie Dough");
         LanguageRegistry.addName(AgricultureItems.snickerDoodleDough.getItemStack(), "Snickerdoodle Dough");
         LanguageRegistry.addName(AgricultureItems.strawberryIceCreamChocolateSauce.getItemStack(), "Strawberry Ice Cream (Chocolate Sauce)");
         LanguageRegistry.addName(AgricultureItems.strawberryIceCreamMix.getItemStack(), "Strawberry Ice Cream Mix");
         LanguageRegistry.addName(AgricultureItems.strawberryJellyCookieDough.getItemStack(), "Strawberry Jelly Cookie Dough");
         LanguageRegistry.addName(AgricultureItems.sugarCookieDough.getItemStack(), "Sugar Cookie Dough");
         LanguageRegistry.addName(AgricultureItems.vanillaIceCreamChocolateSauce.getItemStack(), "Vanilla Ice Cream (Chocolate Sauce)");
         LanguageRegistry.addName(AgricultureItems.vanillaIceCreamMix.getItemStack(), "Vanilla Ice Cream Mix");
         LanguageRegistry.addName(AgricultureItems.chocolateIceCream.getItemStack(), "Chocolate Ice Cream");
         LanguageRegistry.addName(AgricultureItems.strawberryIceCream.getItemStack(), "Strawberry Ice Cream");
         LanguageRegistry.addName(AgricultureItems.vanillaIceCream.getItemStack(), "Vanilla Ice Cream");
         LanguageRegistry.addName(AgricultureItems.appleCinnamonCookie.getItemStack(), "Apple Cinnamon Cookie");
         LanguageRegistry.addName(AgricultureItems.butterCookie.getItemStack(), "Butter Cookie");
         LanguageRegistry.addName(AgricultureItems.chocolateChipCookie.getItemStack(), "Chocolate Chip Cookie");
         LanguageRegistry.addName(AgricultureItems.chocolateSauce.getItemStack(), "Chocolate Sauce");
         LanguageRegistry.addName(AgricultureItems.peanutButterCookie.getItemStack(), "Peanut Butter Cookie");
         LanguageRegistry.addName(AgricultureItems.pumpkinCookie.getItemStack(), "Pumpkin Cookie");
         LanguageRegistry.addName(AgricultureItems.roastedMarshmellows.getItemStack(), "Roasted Marshmellows");
         LanguageRegistry.addName(AgricultureItems.snickerDoodle.getItemStack(), "Snickerdoodle");
         LanguageRegistry.addName(AgricultureItems.strawberryJellyCookie.getItemStack(), "Strawberry Jelly Cookie");
         LanguageRegistry.addName(AgricultureItems.sugarCookie.getItemStack(), "Sugar Cookie");
         LanguageRegistry.addName(AgricultureItems.toastedPBSandwich.getItemStack(), "Toasted PB Sandwich");
         LanguageRegistry.addName(AgricultureItems.vanilla.getItemStack(), "Vanilla");
 
         LanguageRegistry.addName(AgricultureItems.appleCinnamonCookieBurned.getItemStack(), "Apple Cinnamon Cooke (Burned)");
         LanguageRegistry.addName(AgricultureItems.applePieBurned.getItemStack(), "Apple Pie (Burned)");
         LanguageRegistry.addName(AgricultureItems.baconBurned.getItemStack(), "Bacon (Burned)");
         LanguageRegistry.addName(AgricultureItems.beefJerkeyBurned.getItemStack(), "Beef Jerkey (Burned)");
         LanguageRegistry.addName(AgricultureItems.butterCookieBurned.getItemStack(), "Butter Cookie (Burned)");
         LanguageRegistry.addName(AgricultureItems.caramelBurned.getItemStack(), "Caramel (Burned)");
         LanguageRegistry.addName(AgricultureItems.carrotCakeBurned.getItemStack(), "Carrot Cake (Burned)");
         LanguageRegistry.addName(AgricultureItems.chocolateChipCookieBurned.getItemStack(), "Chocolate Chip Cookie (Burned)");
         LanguageRegistry.addName(AgricultureItems.chocolateSauceBurned.getItemStack(), "Chocolate Sauce (Burned)");
         LanguageRegistry.addName(AgricultureItems.frenchToastBurned.getItemStack(), "French Toast (Burned)");
         LanguageRegistry.addName(AgricultureItems.grilledCheeseBurned.getItemStack(), "Grilled Cheese (Burned)");
         LanguageRegistry.addName(AgricultureItems.hamburgerPattyBurned.getItemStack(), "Hambuerger Patty (Burned)");
         LanguageRegistry.addName(AgricultureItems.loafOfBreadBurned.getItemStack(), "Loaf of Bread (Burned)");
         LanguageRegistry.addName(AgricultureItems.pastaBurned.getItemStack(), "Pasta (Burned)");
         LanguageRegistry.addName(AgricultureItems.peanutButterCookieBurned.getItemStack(), "Peanut Butter Cookie (Burned)");
         LanguageRegistry.addName(AgricultureItems.pumpkinCookieBurned.getItemStack(), "Pumpkin Cookie (Burned)");
         LanguageRegistry.addName(AgricultureItems.roastedMarshmellowsBurned.getItemStack(), "Roasted Marshmellows (Burned)");
         LanguageRegistry.addName(AgricultureItems.shortcakeBurned.getItemStack(), "Shortcake (Burned)");
         LanguageRegistry.addName(AgricultureItems.snickerDoodleBurned.getItemStack(), "Snickerdoodle (Burned)");
         LanguageRegistry.addName(AgricultureItems.strawberryJellyCookieBurned.getItemStack(), "Strawberry Jelly Cookie (Burned)");
         LanguageRegistry.addName(AgricultureItems.roastedPeanutsBurned.getItemStack(), "Roasted Peanuts (Burned)");
         LanguageRegistry.addName(AgricultureItems.strawberryPieBurned.getItemStack(), "Strawberry Pie (Burned)");
         LanguageRegistry.addName(AgricultureItems.sugarCookieBurned.getItemStack(), "Sugar Cookie (Burned)");
         LanguageRegistry.addName(AgricultureItems.toastBurned.getItemStack(), "Toast (Burned)");
         LanguageRegistry.addName(AgricultureItems.toastedPBSandwichBurned.getItemStack(), "Toasted PB Sandwich (Burned)");
         LanguageRegistry.addName(AgricultureItems.toastedPBJSandwichAppleBurned.getItemStack(), "Toasted PB&J Sandwich (Apple) (Burned)");
         LanguageRegistry.addName(AgricultureItems.toastedPBJSandwichStrawberryBurned.getItemStack(), "Toasted PB&J Sabdwich (Strawberry) (Burned)");
 
     }
 
     public static void addRecipes()
     {
         GameRegistry.addShapelessRecipe(AgricultureItems.sliceOfCheese.getItemStack(4), AgricultureItems.cheese.getItemStack());
         GameRegistry.addShapelessRecipe(AgricultureItems.sliceOfBread.getItemStack(4), AgricultureItems.loafOfBread.getItemStack());
 
         GameRegistry.addRecipe(AgricultureItems.clayBowl.getItemStack(12), "X X", " X ", 'X', Item.clay);
         GameRegistry.addRecipe(AgricultureItems.clayPlate.getItemStack(12), "XXX", 'X', Item.clay);
         GameRegistry.addRecipe(AgricultureItems.clayCup.getItemStack(12), "X", "X", 'X', Item.clay);
         GameRegistry.addRecipe(AgricultureItems.ovenRack.getItemStack(), "XXX", "XXX", "XXX", 'X', Block.fenceIron);
 
         GameRegistry.addShapelessRecipe(AgricultureItems.water.getItemStack(), AgricultureItems.ceramicCup.getItemStack(), Item.bucketWater);
         GameRegistry.addShapelessRecipe(AgricultureItems.milk.getItemStack(), AgricultureItems.ceramicCup.getItemStack(), Item.bucketMilk);
 
         GameRegistry.addShapelessRecipe(AgricultureItems.candiedApple.getItemStack(), Item.appleRed, Item.stick);
 
         FurnaceRecipes.smelting().addSmelting(AgricultureItems.clayBowl.getItemStack().itemID, AgricultureItems.clayBowl.getItemStack().getItemDamage(), AgricultureItems.ceramicBowl.getItemStack(), 0.1f);
         FurnaceRecipes.smelting().addSmelting(AgricultureItems.clayPlate.getItemStack().itemID, AgricultureItems.clayPlate.getItemStack().getItemDamage(), AgricultureItems.ceramicPlate.getItemStack(), 0.1f);
         FurnaceRecipes.smelting().addSmelting(AgricultureItems.clayCup.getItemStack().itemID, AgricultureItems.clayCup.getItemStack().getItemDamage(), AgricultureItems.ceramicCup.getItemStack(), 0.1f);
 
     }
 
     private static SubItem createSubFood(final int id, final int damage, final int j)
     {
         final Item item = Item.itemsList[id + 256];
 
         AgricultureItems.idTaken_do(id, item);
 
         return new SubItemFood(id, damage, j);
     }
 
     private static SubItem createSubFood(final int id, final int damage, final int j, final float f)
     {
         final Item item = Item.itemsList[id + 256];
 
         AgricultureItems.idTaken_do(id, item);
 
         return new SubItemFood(id, damage, j, f);
     }
 
     private static SubItem createSubItem(final int id, final int damage)
     {
         final Item item = Item.itemsList[id + 256];
 
         AgricultureItems.idTaken_do(id, item);
 
         return new SubItem(id, damage);
     }
 
     private static SubItem createSubItemSeed(final int id, final int damage, final int j)
     {
         final Item item = Item.itemsList[id + 256];
 
         AgricultureItems.idTaken_do(id, item);
 
         return new SubItemSeed(id, damage, j);
     }
 
     private static void idTaken_do(final int id, final Item item)
     {
         if (item != null && !(item instanceof SuperItem)) { throw new IllegalArgumentException("Slot " + id + " is already occupied by " + Item.itemsList[256 + id]); }
     }
 
     public static void init()
     {
         // ovenRack = new SubItem(dishID,
         // 6).setUnlocalizedName("OvenRack").setTextureName("OvenRack").setCreativeTab(Agriculture.tab);
 
         int damage = 0;
         AgricultureItems.clayBowl = AgricultureItems.createSubItem(AgricultureItems.dishID, damage++).setUnlocalizedName("ClayBowl").setTextureName("ClayBowl").setCreativeTab(Agriculture.tab);
         AgricultureItems.clayPlate = AgricultureItems.createSubItem(AgricultureItems.dishID, damage++).setUnlocalizedName("ClayPlate").setTextureName("ClayPlate").setCreativeTab(Agriculture.tab);
         AgricultureItems.clayCup = AgricultureItems.createSubItem(AgricultureItems.dishID, damage++).setUnlocalizedName("ClayCup").setTextureName("ClayCup").setCreativeTab(Agriculture.tab);
         AgricultureItems.ceramicBowl = AgricultureItems.createSubItem(AgricultureItems.dishID, damage++).setUnlocalizedName("CeramicBowl").setTextureName("CeramicBowl").setCreativeTab(Agriculture.tab);
         AgricultureItems.ceramicPlate = AgricultureItems.createSubItem(AgricultureItems.dishID, damage++).setUnlocalizedName("CeramicPlate").setTextureName("CeramicPlate").setCreativeTab(Agriculture.tab);
         AgricultureItems.ceramicCup = AgricultureItems.createSubItem(AgricultureItems.dishID, damage++).setUnlocalizedName("CeramicCup").setTextureName("CeramicCup").setCreativeTab(Agriculture.tab);
         AgricultureItems.ovenRack = AgricultureItems.createSubItem(AgricultureItems.dishID, damage++).setUnlocalizedName("OvenRack").setTextureName("OvenRack").setCreativeTab(Agriculture.tab);
 
         damage = 1;
         AgricultureItems.candiedApple = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 1).setUnlocalizedName("CandiedApple").setCreativeTab(Agriculture.tab);
         AgricultureItems.frenchFries = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 3).setUnlocalizedName("FrenchFries").setTextureName("FrenchFries").setCreativeTab(Agriculture.tab);
         AgricultureItems.water = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 1).setUnlocalizedName("Water").setTextureName("Water").setCreativeTab(Agriculture.tab);
         AgricultureItems.milk = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 1).setUnlocalizedName("Milk").setTextureName("Milk").setCreativeTab(Agriculture.tab);
         AgricultureItems.hotCocoa = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2, 0.1f).setUnlocalizedName("HotCocoa").setTextureName("HotCocoa").setCreativeTab(Agriculture.tab);
         AgricultureItems.vinegar = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Vinegar").setTextureName("Vinegar").setCreativeTab(Agriculture.tab);
         AgricultureItems.beer = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("Beer").setTextureName("Beer").setCreativeTab(Agriculture.tab);
         AgricultureItems.rawHamburgerPatty = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("RawHamburgerPatty").setTextureName("RawHamburgerPatty").setCreativeTab(Agriculture.tab);
         AgricultureItems.appleJellyToast = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 10).setUnlocalizedName("AppleJellyToast").setTextureName("AppleJellyToast").setCreativeTab(Agriculture.tab);
         AgricultureItems.cinnamonToast = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 9).setUnlocalizedName("CinnamonToast").setTextureName("CinnamonToast").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryJellyToast = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 10).setUnlocalizedName("StrawberryJellyToast").setTextureName("StrawberryJellyToast").setCreativeTab(Agriculture.tab);
         AgricultureItems.caramelAppleWithNuts = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 7).setUnlocalizedName("CaramelAppleWithNuts").setTextureName("CaramelAppleWithNuts").setCreativeTab(Agriculture.tab);
         AgricultureItems.appleJelly = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("AppleJelly").setTextureName("AppleJelly").setCreativeTab(Agriculture.tab);
         AgricultureItems.mashedPotatos = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 5).setUnlocalizedName("MashedPotatos").setTextureName("MashedPotatos").setCreativeTab(Agriculture.tab);
         AgricultureItems.carrotCakeBatter = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("CarrotCakeBatter").setTextureName("CarrotCakeBatter").setCreativeTab(Agriculture.tab);
         AgricultureItems.rawFrenchToast = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("RawFrenchToast").setTextureName("RawFrenchToast").setCreativeTab(Agriculture.tab);
         AgricultureItems.batter = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Batter").setTextureName("Batter").setCreativeTab(Agriculture.tab);
         AgricultureItems.macaroniAndCheese = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 8).setUnlocalizedName("MacaroniAndCheese").setTextureName("MacaroniAndCheese").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryJelly = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryJelly").setTextureName("StrawberryJelly").setCreativeTab(Agriculture.tab);
         AgricultureItems.rawApplePie = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("RawApplePie").setTextureName("RawApplePie").setCreativeTab(Agriculture.tab);
         AgricultureItems.rawStrawberryPie = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("RawStrawberryPie").setTextureName("RawStrawberryPie").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryShortcake = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 16).setUnlocalizedName("StrawberryShortcake").setTextureName("StrawberryShortcake").setCreativeTab(Agriculture.tab);
         AgricultureItems.hamburger = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 6).setUnlocalizedName("Hamburger").setTextureName("Hamburger").setCreativeTab(Agriculture.tab);
         AgricultureItems.pbjSandwichApple = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 12).setUnlocalizedName("PBJSandwichApple").setTextureName("PBJSandwichApple").setCreativeTab(Agriculture.tab);
         AgricultureItems.pbjSandwichStrawberry = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 12).setUnlocalizedName("PBJSandwichStrawberry").setTextureName("PBJSandwichStrawberry").setCreativeTab(Agriculture.tab);
         AgricultureItems.cheeseSandwich = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 5).setUnlocalizedName("CheeseSandwich").setTextureName("CheeseSandwich").setCreativeTab(Agriculture.tab);
         AgricultureItems.butteredToast = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 6).setUnlocalizedName("ButteredToast").setTextureName("ButteredToast").setCreativeTab(Agriculture.tab);
         AgricultureItems.baconCheeseFries = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 6).setUnlocalizedName("BaconCheeseFries").setTextureName("BaconCheeseFries").setCreativeTab(Agriculture.tab);
         AgricultureItems.baconCheeseburger = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 9).setUnlocalizedName("BaconCheeseburger").setTextureName("BaconCheeseburger").setCreativeTab(Agriculture.tab);
         AgricultureItems.peanutButter = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("PeanutButter").setTextureName("PeanutButter").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolate = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 3).setUnlocalizedName("Chocolate").setTextureName("Chocolate").setCreativeTab(Agriculture.tab);
         AgricultureItems.cheese = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Cheese").setTextureName("Cheese").setCreativeTab(Agriculture.tab);
         AgricultureItems.butter = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Butter").setTextureName("Butter").setCreativeTab(Agriculture.tab);
         AgricultureItems.whippedCream = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("WhippedCream").setTextureName("WhippedCream").setCreativeTab(Agriculture.tab);
         AgricultureItems.dough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Dough").setTextureName("Dough").setCreativeTab(Agriculture.tab);
         AgricultureItems.appleGelatin = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("AppleGelatin").setTextureName("Gelatin").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryGelatin = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryGelatin").setTextureName("StrawberryGelatin").setCreativeTab(Agriculture.tab);
         AgricultureItems.marshmellows = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 3).setUnlocalizedName("Marshmellows").setTextureName("Marshmellows").setCreativeTab(Agriculture.tab);
         AgricultureItems.pastaDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("PastaDough").setTextureName("PastaDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.cheeseFries = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 5).setUnlocalizedName("CheeseFries").setTextureName("CheeseFries").setCreativeTab(Agriculture.tab);
         AgricultureItems.rawChickenNuggets = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("RawChickenNuggets").setTextureName("RawChickenNuggets").setCreativeTab(Agriculture.tab);
         AgricultureItems.cheeseburger = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 8).setUnlocalizedName("Cheeseburger").setTextureName("Cheeseburger").setCreativeTab(Agriculture.tab);
         AgricultureItems.deluxeHotCocoa = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 11).setUnlocalizedName("DeluxeHotCocoa").setTextureName("DeluxeHotCocoa").setCreativeTab(Agriculture.tab);
         AgricultureItems.saltedBeef = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("SaltedBeef").setTextureName("SaltedBeef").setCreativeTab(Agriculture.tab);
         AgricultureItems.breadedChicken = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("BreadedChicken").setTextureName("BreadedChicken").setCreativeTab(Agriculture.tab);
         AgricultureItems.saltedPork = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("SaltedPork").setTextureName("SaltedPork").setCreativeTab(Agriculture.tab);
         AgricultureItems.caramelApple = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 4).setUnlocalizedName("CaramelApple").setTextureName("CaramelApple").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateCoveredStrawberries = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 4).setUnlocalizedName("ChocolateCoveredStrawberries").setTextureName("ChocolateCoveredStrawberries").setCreativeTab(Agriculture.tab);
         AgricultureItems.cinnamonAndSugar = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("CinnamonAndSugar").setTextureName("CinnamonAndSugar").setCreativeTab(Agriculture.tab);
         AgricultureItems.sliceOfCheese = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("SliceOfCheese").setTextureName("SliceOfCheese").setCreativeTab(Agriculture.tab);
         AgricultureItems.sliceOfBread = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 1).setUnlocalizedName("SliceOfBread").setTextureName("SliceOfBread").setCreativeTab(Agriculture.tab);
         AgricultureItems.friedChicken = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 3).setUnlocalizedName("FriedChicken").setTextureName("FriedChicken").setCreativeTab(Agriculture.tab);
         AgricultureItems.chickenNuggets = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 4).setUnlocalizedName("ChickenNuggets").setTextureName("ChickenNuggets").setCreativeTab(Agriculture.tab);
         AgricultureItems.shortcake = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 8).setUnlocalizedName("Shortcake").setTextureName("Shortcake").setCreativeTab(Agriculture.tab);
         AgricultureItems.carrotCake = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 10).setUnlocalizedName("CarrotCake").setTextureName("CarrotCake").setCreativeTab(Agriculture.tab);
         AgricultureItems.grilledCheese = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 6).setUnlocalizedName("GrilledCheese").setTextureName("GrilledCheese").setCreativeTab(Agriculture.tab);
         AgricultureItems.loafOfBread = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 4).setUnlocalizedName("LoafOfBread").setTextureName("LoafOfBread").setCreativeTab(Agriculture.tab);
         AgricultureItems.pasta = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Pasta").setTextureName("Pasta").setCreativeTab(Agriculture.tab);
         AgricultureItems.toastedPbjSandwichApple = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 13).setUnlocalizedName("ToastedPBJSandwichApple").setTextureName("ToastedPBJSandwichApple").setCreativeTab(Agriculture.tab);
         AgricultureItems.toastedPbjSandwichStrawberry = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 13).setUnlocalizedName("ToastedPBJSandwichStrawberry").setTextureName("ToastedPBJSandwichStrawberry").setCreativeTab(Agriculture.tab);
         AgricultureItems.roastedPeanuts = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("RoastedPeanuts").setTextureName("RoastedPeanuts").setCreativeTab(Agriculture.tab);
         AgricultureItems.applePie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 10).setUnlocalizedName("ApplePie").setTextureName("ApplePie").setCreativeTab(Agriculture.tab);
         AgricultureItems.frenchToast = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 8).setUnlocalizedName("FrenchToast").setTextureName("FrenchToast").setCreativeTab(Agriculture.tab);
         AgricultureItems.hamburgerPatty = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("HamburgerPatty").setTextureName("HamburgerPatty").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryPie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 7).setUnlocalizedName("StrawberryPie").setTextureName("StrawberryPie").setCreativeTab(Agriculture.tab);
         AgricultureItems.beefJerkey = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("BeefJerkey").setTextureName("BeefJerkey").setCreativeTab(Agriculture.tab);
         AgricultureItems.bacon = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("Bacon").setTextureName("Bacon").setCreativeTab(Agriculture.tab);
         AgricultureItems.toast = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("Toast").setTextureName("Toast").setCreativeTab(Agriculture.tab);
         AgricultureItems.caramel = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Caramel").setTextureName("Caramel").setCreativeTab(Agriculture.tab);
         AgricultureItems.appleMush = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("AppleMush").setTextureName("AppleMush").setCreativeTab(Agriculture.tab);
         AgricultureItems.gelatin = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("gelatin").setTextureName("gelatin").setCreativeTab(Agriculture.tab);
         AgricultureItems.breadCrumbs = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("BreadCrumbs").setTextureName("BreadCrumbs").setCreativeTab(Agriculture.tab);
         AgricultureItems.groundBeef = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("GroundBeef").setTextureName("GroundBeef").setCreativeTab(Agriculture.tab);
         AgricultureItems.groundChicken = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("GroundChicken").setTextureName("GroundChicken").setCreativeTab(Agriculture.tab);
         AgricultureItems.groundPork = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("GroundPork").setTextureName("GroundPork").setCreativeTab(Agriculture.tab);
         AgricultureItems.crushedPeanuts = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("CrushedPeanuts").setTextureName("CrushedPeanuts").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryMush = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryMush").setTextureName("Shortcake").setCreativeTab(Agriculture.tab);
         AgricultureItems.flour = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Flour").setTextureName("Flour").setCreativeTab(Agriculture.tab);
         AgricultureItems.cookingOil = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("CookingOil").setTextureName("CookingOil").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberry = AgricultureItems.createSubItemSeed(AgricultureItems.foodID, damage++, 1).setUnlocalizedName("Strawberry").setTextureName("Strawberry").setCreativeTab(Agriculture.tab);
         AgricultureItems.salt = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Salt").setTextureName("Salt").setCreativeTab(Agriculture.tab);
         AgricultureItems.cinnamon = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Cinnamon").setTextureName("Cinnamon").setCreativeTab(Agriculture.tab);
         AgricultureItems.groundCinnamon = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("GroundCinnamon").setTextureName("GroundCinnamon").setCreativeTab(Agriculture.tab);
         AgricultureItems.peanuts = AgricultureItems.createSubItemSeed(AgricultureItems.foodID, damage++, 1).setUnlocalizedName("Peanuts").setTextureName("Peanuts").setCreativeTab(Agriculture.tab);
 
         AgricultureItems.appleCinnamonCookieDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("AppleCinnamonCookieDough").setTextureName("AppleCinnamonCookieDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.bowlOfRawPasta = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("BowlOfRawPasta").setTextureName("BowlOfRawPasta").setCreativeTab(Agriculture.tab);
         AgricultureItems.butterCookieDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ButterCookieDough").setTextureName("ButterCookieDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.cheesyBaconPotatoes = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 9).setUnlocalizedName("BaconCheesyPotatoes").setTextureName("BaconCheesyPotatoes").setCreativeTab(Agriculture.tab);
         AgricultureItems.cheesyPotatoes = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 7).setUnlocalizedName("CheesyPotatoes").setTextureName("CheesyPotatoes").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateChipCookieDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ChocolateChipCookieDough").setTextureName("ChocolateChipCookieDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateIceCreamChocolateSauce = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ChocolateIceCreamChocolateSauce").setTextureName("ChocolateIceCreamChocolateSauce").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateIceCreamMix = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ChocolateIceCreamMix").setTextureName("ChocolateIceCreamMix").setCreativeTab(Agriculture.tab);
         AgricultureItems.dicedPotatoes = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("DicedPotatoes").setTextureName("DicedPotatoes").setCreativeTab(Agriculture.tab);
         AgricultureItems.doubleBaconCheeseburger = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 18).setUnlocalizedName("DoubleBaconCheeseburger").setTextureName("DoubleBaconCheeseburger").setCreativeTab(Agriculture.tab);
         AgricultureItems.iceCreamMix = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("IceCreamMix").setTextureName("IceCreamMix").setCreativeTab(Agriculture.tab);
         AgricultureItems.pbSandwich = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 8).setUnlocalizedName("PBSandwich").setTextureName("PBSandwich").setCreativeTab(Agriculture.tab);
         AgricultureItems.peanutButterCookieDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("PeanutButterCookieDough").setTextureName("PeanutButterCookieDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.pumpkinCookieDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("PumpkinCookieDough").setTextureName("PumpkinCookieDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.snickerDoodleDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("SnickerDoodleDough").setTextureName("SnickerDoodleDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryIceCreamChocolateSauce = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryIceCreamChocolateSauce").setTextureName("StrawberryIceCreamChocolateSauce").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryIceCreamMix = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryIceCreamMix").setTextureName("StrawberryIceCreamMix").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryJellyCookieDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryJellyCookieDough").setTextureName("StrawberryJellyCookieDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.sugarCookieDough = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("SugarCookieDough").setTextureName("SugarCookieDough").setCreativeTab(Agriculture.tab);
         AgricultureItems.vanillaIceCreamChocolateSauce = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("VanillaIceCreamChocolateSauce").setTextureName("VanillaIceCreamChocolateSauce").setCreativeTab(Agriculture.tab);
         AgricultureItems.vanillaIceCreamMix = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("VanillaIceCreamMix").setTextureName("VanillaIceCreamMix").setCreativeTab(Agriculture.tab);
         // IceBox
         AgricultureItems.chocolateIceCream = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 7).setUnlocalizedName("ChocolateIceCream").setTextureName("ChocolateIceCream").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryIceCream = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 5).setUnlocalizedName("StrawberryIceCream").setTextureName("StrawberryIceCream").setCreativeTab(Agriculture.tab);
         AgricultureItems.vanillaIceCream = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 5).setUnlocalizedName("VanillaIceCream").setTextureName("VanillaIceCream").setCreativeTab(Agriculture.tab);
         // Oven
         AgricultureItems.appleCinnamonCookie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("AppleCinnamonCookie").setTextureName("AppleCinnamonCookie").setCreativeTab(Agriculture.tab);
         AgricultureItems.butterCookie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("ButterCookie").setTextureName("ButterCookie").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateChipCookie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("ChocolateChipCookie").setTextureName("ChocolateChipCookie").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateSauce = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 4).setUnlocalizedName("ChocolateSauce").setTextureName("ChocolateSauce").setCreativeTab(Agriculture.tab);
         AgricultureItems.peanutButterCookie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("PeanutButterCookie").setTextureName("PeanutButterCookie").setCreativeTab(Agriculture.tab);
         AgricultureItems.pumpkinCookie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("PumpkinCookie").setTextureName("PumpkinCookie").setCreativeTab(Agriculture.tab);
         AgricultureItems.roastedMarshmellows = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 4).setUnlocalizedName("RoastedMarshmellows").setTextureName("RoastedMarshmellows").setCreativeTab(Agriculture.tab);
         AgricultureItems.snickerDoodle = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("SnickerDoodle").setTextureName("SnickerDoodle").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryJellyCookie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("StrawberryJellyCookie").setTextureName("StrawberryJellyCookie").setCreativeTab(Agriculture.tab);
         AgricultureItems.sugarCookie = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 2).setUnlocalizedName("SugarCookie").setTextureName("SugarCookie").setCreativeTab(Agriculture.tab);
         AgricultureItems.toastedPBSandwich = AgricultureItems.createSubFood(AgricultureItems.foodID, damage++, 11).setUnlocalizedName("ToastedPBSandwich").setTextureName("ToastedPBSandwich").setCreativeTab(Agriculture.tab);
         AgricultureItems.vanilla = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("Vanilla").setCreativeTab(Agriculture.tab);
 
         AgricultureItems.appleCinnamonCookieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("AppleCinnamonCookieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.applePieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ApplePieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.baconBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("BaconBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.beefJerkeyBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("BeefJerkeyBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.butterCookieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ButterCookieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.caramelBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("CaramelBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.carrotCakeBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("CarrotCakeBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateChipCookieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ChocolateChipCookieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.chocolateSauceBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ChocolateSauceBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.frenchToastBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("FrenchToastBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.grilledCheeseBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("GrilledCheeseBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.hamburgerPattyBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("HamburgerPattyBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.loafOfBreadBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("LoafOfBreadBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.pastaBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("PastaBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.peanutButterCookieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("PeanutButterCookieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.pumpkinCookieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("PumpkinCookieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.roastedMarshmellowsBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("RoastedMarshmellowsBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.roastedPeanutsBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("RoastedPeanutsBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.shortcakeBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ShortcakeBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.snickerDoodleBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("SnickerDoodleBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryJellyCookieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryJellyCookieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.strawberryPieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("StrawberryPieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.sugarCookieBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("SugarCookieBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.toastBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ToastBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.toastedPBJSandwichAppleBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ToastedPBJSandwichAppleBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.toastedPBJSandwichStrawberryBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ToastedPBJSandwichStrawberryBurned").setCreativeTab(Agriculture.tab);
         AgricultureItems.toastedPBSandwichBurned = AgricultureItems.createSubItem(AgricultureItems.foodID, damage++).setUnlocalizedName("ToastedPBSandwichBurned").setCreativeTab(Agriculture.tab);
 
         AgricultureItems.registerOreDictionary();
     }
 
     private static void notify(final ItemStack stack)
     {
         Agriculture.instance.getLogger().fine("Tweaked " + stack.getUnlocalizedName() + " recipe.");
     }
 
     public static void registerOreDictionary()
     {
         OreDictionary.registerOre("ovenRack", AgricultureItems.ovenRack.getItemStack());
         OreDictionary.registerOre("clayBowl", AgricultureItems.clayBowl.getItemStack());
         OreDictionary.registerOre("clayPlate", AgricultureItems.clayPlate.getItemStack());
         OreDictionary.registerOre("clayCup", AgricultureItems.clayCup.getItemStack());
         OreDictionary.registerOre("ceramicBowl", AgricultureItems.ceramicBowl.getItemStack());
         OreDictionary.registerOre("ceramicPlate", AgricultureItems.ceramicPlate.getItemStack());
         OreDictionary.registerOre("ceramicCup", AgricultureItems.ceramicCup.getItemStack());
 
         OreDictionary.registerOre("foodStrawberry", AgricultureItems.strawberry.getItemStack());
         OreDictionary.registerOre("foodCandiedApple", AgricultureItems.candiedApple.getItemStack());
         OreDictionary.registerOre("foodCupofWater", AgricultureItems.water.getItemStack());
         OreDictionary.registerOre("foodCupofMilk", AgricultureItems.milk.getItemStack());
         OreDictionary.registerOre("foodHotCocoa", AgricultureItems.hotCocoa.getItemStack());
         OreDictionary.registerOre("foodVinegar", AgricultureItems.vinegar.getItemStack());
         OreDictionary.registerOre("foodBeer", AgricultureItems.beer.getItemStack());
         OreDictionary.registerOre("foodRawHamburgerPatty", AgricultureItems.rawHamburgerPatty.getItemStack());
         OreDictionary.registerOre("foodAppleJellyToast", AgricultureItems.appleJellyToast.getItemStack());
         OreDictionary.registerOre("foodCinnamonToast", AgricultureItems.cinnamonToast.getItemStack());
         OreDictionary.registerOre("foodStrawberryJellyToast", AgricultureItems.strawberryJellyToast.getItemStack());
         OreDictionary.registerOre("foodCaramelApplewithNuts", AgricultureItems.caramelAppleWithNuts.getItemStack());
         OreDictionary.registerOre("foodAppleJelly", AgricultureItems.appleJelly.getItemStack());
         OreDictionary.registerOre("foodMashedPotatos", AgricultureItems.mashedPotatos.getItemStack());
         OreDictionary.registerOre("foodCarrotCakeBatter", AgricultureItems.carrotCakeBatter.getItemStack());
         OreDictionary.registerOre("foodRawFrenchToast", AgricultureItems.rawFrenchToast.getItemStack());
         OreDictionary.registerOre("foodBatter", AgricultureItems.batter.getItemStack());
         OreDictionary.registerOre("foodMacaroniandCheese", AgricultureItems.macaroniAndCheese.getItemStack());
         OreDictionary.registerOre("foodStrawberryJelly", AgricultureItems.strawberryJelly.getItemStack());
         OreDictionary.registerOre("foodRawApplePie", AgricultureItems.rawApplePie.getItemStack());
         OreDictionary.registerOre("foodRawStrawberryPie", AgricultureItems.rawStrawberryPie.getItemStack());
         OreDictionary.registerOre("foodStrawberryShortcake", AgricultureItems.strawberryShortcake.getItemStack());
         OreDictionary.registerOre("foodHamburger", AgricultureItems.hamburger.getItemStack());
         OreDictionary.registerOre("foodPB&JSandwichApple", AgricultureItems.pbjSandwichApple.getItemStack());
         OreDictionary.registerOre("foodPB&JSandwichStrawberry", AgricultureItems.pbjSandwichStrawberry.getItemStack());
         OreDictionary.registerOre("foodCheeseSandwich", AgricultureItems.cheeseSandwich.getItemStack());
         OreDictionary.registerOre("foodButteredToast", AgricultureItems.butteredToast.getItemStack());
         OreDictionary.registerOre("foodBaconCheeseFries", AgricultureItems.baconCheeseFries.getItemStack());
         OreDictionary.registerOre("foodBaconCheeseburger", AgricultureItems.baconCheeseburger.getItemStack());
         OreDictionary.registerOre("foodChocolate", AgricultureItems.chocolate.getItemStack());
         OreDictionary.registerOre("foodCheese", AgricultureItems.cheese.getItemStack());
         OreDictionary.registerOre("foodButter", AgricultureItems.butter.getItemStack());
         OreDictionary.registerOre("foodWhippedCream", AgricultureItems.whippedCream.getItemStack());
         OreDictionary.registerOre("foodDough", AgricultureItems.dough.getItemStack());
         OreDictionary.registerOre("foodAppleGelatin", AgricultureItems.appleGelatin.getItemStack());
         OreDictionary.registerOre("foodStrawberryGelatin", AgricultureItems.strawberryGelatin.getItemStack());
         OreDictionary.registerOre("foodMarshmellows", AgricultureItems.marshmellows.getItemStack());
         OreDictionary.registerOre("foodPastaDough", AgricultureItems.pastaDough.getItemStack());
         OreDictionary.registerOre("foodCheeseFries", AgricultureItems.cheeseFries.getItemStack());
         OreDictionary.registerOre("foodRawChickenNuggets", AgricultureItems.rawChickenNuggets.getItemStack());
         OreDictionary.registerOre("foodCheeseburger", AgricultureItems.cheeseburger.getItemStack());
         OreDictionary.registerOre("foodDeluxeHotCocoa", AgricultureItems.deluxeHotCocoa.getItemStack());
         OreDictionary.registerOre("foodSaltedBeef", AgricultureItems.saltedBeef.getItemStack());
         OreDictionary.registerOre("foodBreadedChicken", AgricultureItems.breadedChicken.getItemStack());
         OreDictionary.registerOre("foodSaltedPork", AgricultureItems.saltedPork.getItemStack());
         OreDictionary.registerOre("foodCaramelApple", AgricultureItems.caramelApple.getItemStack());
         OreDictionary.registerOre("foodChocolate-CoveredStrawberries", AgricultureItems.chocolateCoveredStrawberries.getItemStack());
         OreDictionary.registerOre("foodCinnamonandSugar", AgricultureItems.cinnamonAndSugar.getItemStack());
         OreDictionary.registerOre("foodSliceofCheese", AgricultureItems.sliceOfCheese.getItemStack());
         OreDictionary.registerOre("foodSliceofBread", AgricultureItems.sliceOfBread.getItemStack());
         OreDictionary.registerOre("foodFriedChicken", AgricultureItems.friedChicken.getItemStack());
         OreDictionary.registerOre("foodFrenchFries", AgricultureItems.frenchFries.getItemStack());
         OreDictionary.registerOre("foodChickenNuggets", AgricultureItems.chickenNuggets.getItemStack());
         OreDictionary.registerOre("foodShortcake", AgricultureItems.shortcake.getItemStack());
         OreDictionary.registerOre("foodCarrotCake", AgricultureItems.carrotCake.getItemStack());
         OreDictionary.registerOre("foodGrilledCheese", AgricultureItems.grilledCheese.getItemStack());
         OreDictionary.registerOre("foodLoafofBread", AgricultureItems.loafOfBread.getItemStack());
         OreDictionary.registerOre("foodPasta", AgricultureItems.pasta.getItemStack());
         OreDictionary.registerOre("foodToastedPB&JSandwichApple", AgricultureItems.toastedPbjSandwichApple.getItemStack());
         OreDictionary.registerOre("foodToastedPB&JSandwichStrawberry", AgricultureItems.toastedPbjSandwichStrawberry.getItemStack());
         OreDictionary.registerOre("foodRoastedPeanuts", AgricultureItems.roastedPeanuts.getItemStack());
         OreDictionary.registerOre("foodApplePie", AgricultureItems.applePie.getItemStack());
         OreDictionary.registerOre("foodFrenchToast", AgricultureItems.frenchToast.getItemStack());
         OreDictionary.registerOre("foodHamburgerPatty", AgricultureItems.hamburgerPatty.getItemStack());
         OreDictionary.registerOre("foodStrawberryPie", AgricultureItems.strawberryPie.getItemStack());
         OreDictionary.registerOre("foodBeefJerkey", AgricultureItems.beefJerkey.getItemStack());
         OreDictionary.registerOre("foodBacon", AgricultureItems.bacon.getItemStack());
         OreDictionary.registerOre("foodToast", AgricultureItems.toast.getItemStack());
         OreDictionary.registerOre("foodCaramel", AgricultureItems.caramel.getItemStack());
         OreDictionary.registerOre("foodAppleMush", AgricultureItems.appleMush.getItemStack());
         OreDictionary.registerOre("foodGelatin", AgricultureItems.gelatin.getItemStack());
         OreDictionary.registerOre("foodBreadCrumbs", AgricultureItems.breadCrumbs.getItemStack());
         OreDictionary.registerOre("foodGroundBeef", AgricultureItems.groundBeef.getItemStack());
         OreDictionary.registerOre("foodGroundChicken", AgricultureItems.groundChicken.getItemStack());
         OreDictionary.registerOre("foodGroundPork", AgricultureItems.groundPork.getItemStack());
         OreDictionary.registerOre("foodCrushedPeanuts", AgricultureItems.crushedPeanuts.getItemStack());
         OreDictionary.registerOre("foodStrawberryMush", AgricultureItems.strawberryMush.getItemStack());
         OreDictionary.registerOre("foodFlour", AgricultureItems.flour.getItemStack());
         OreDictionary.registerOre("foodPeanutButter", AgricultureItems.peanutButter.getItemStack());
         OreDictionary.registerOre("foodCookingOil", AgricultureItems.cookingOil.getItemStack());
         OreDictionary.registerOre("foodSalt", AgricultureItems.salt.getItemStack());
         OreDictionary.registerOre("foodCinnamon", AgricultureItems.cinnamon.getItemStack());
         OreDictionary.registerOre("cropCinnamon", AgricultureItems.cinnamon.getItemStack());
         OreDictionary.registerOre("foodGroundCinnamon", AgricultureItems.groundCinnamon.getItemStack());
         OreDictionary.registerOre("foodPeanuts", AgricultureItems.peanuts.getItemStack());
         OreDictionary.registerOre("foodAppleCinnamonCookieDough", AgricultureItems.appleCinnamonCookieDough.getItemStack());
         OreDictionary.registerOre("foodBowlofRawPasta", AgricultureItems.bowlOfRawPasta.getItemStack());
         OreDictionary.registerOre("foodButterCookieDough", AgricultureItems.butterCookieDough.getItemStack());
         OreDictionary.registerOre("foodCheesyBaconPotatoes", AgricultureItems.cheesyBaconPotatoes.getItemStack());
         OreDictionary.registerOre("foodCheesyPotatoes", AgricultureItems.cheesyPotatoes.getItemStack());
         OreDictionary.registerOre("foodChocolateChipCookieDough", AgricultureItems.chocolateChipCookieDough.getItemStack());
         OreDictionary.registerOre("foodChocolateIceCreamChocolateSauce", AgricultureItems.chocolateIceCreamChocolateSauce.getItemStack());
         OreDictionary.registerOre("foodChocolateIceCreamMix", AgricultureItems.chocolateIceCreamMix.getItemStack());
         OreDictionary.registerOre("foodDicedPotatoes", AgricultureItems.dicedPotatoes.getItemStack());
         OreDictionary.registerOre("foodDoubleBaconCheeseburger", AgricultureItems.doubleBaconCheeseburger.getItemStack());
         OreDictionary.registerOre("foodIceCreamMix", AgricultureItems.iceCreamMix.getItemStack());
         OreDictionary.registerOre("foodPBSandwich", AgricultureItems.pbSandwich.getItemStack());
         OreDictionary.registerOre("foodPeanutButterCookeDough", AgricultureItems.peanutButterCookieDough.getItemStack());
         OreDictionary.registerOre("foodPumpkinCookieDough", AgricultureItems.pumpkinCookieDough.getItemStack());
         OreDictionary.registerOre("foodSnickerdoodleDough", AgricultureItems.snickerDoodleDough.getItemStack());
         OreDictionary.registerOre("foodStrawberryIceCreamChocolateSauce", AgricultureItems.strawberryIceCreamChocolateSauce.getItemStack());
         OreDictionary.registerOre("foodStrawberryIceCreamMix", AgricultureItems.strawberryIceCreamMix.getItemStack());
         OreDictionary.registerOre("foodStrawberryJellyCookieDough", AgricultureItems.strawberryJellyCookieDough.getItemStack());
         OreDictionary.registerOre("foodSugarCookieDough", AgricultureItems.sugarCookieDough.getItemStack());
         OreDictionary.registerOre("foodVanillaIceCreamChocolateSauce", AgricultureItems.vanillaIceCreamChocolateSauce.getItemStack());
         OreDictionary.registerOre("foodVanillaIceCreamMix", AgricultureItems.vanillaIceCreamMix.getItemStack());
         OreDictionary.registerOre("foodChocolateIceCream", AgricultureItems.chocolateIceCream.getItemStack());
         OreDictionary.registerOre("foodStrawberryIceCream", AgricultureItems.strawberryIceCream.getItemStack());
         OreDictionary.registerOre("foodVanillaIceCream", AgricultureItems.vanillaIceCream.getItemStack());
         OreDictionary.registerOre("foodAppleCinnamonCookie", AgricultureItems.appleCinnamonCookie.getItemStack());
         OreDictionary.registerOre("foodButterCookie", AgricultureItems.butterCookie.getItemStack());
         OreDictionary.registerOre("foodChocolateChipCookie", AgricultureItems.chocolateChipCookie.getItemStack());
         OreDictionary.registerOre("foodChocolateSauce", AgricultureItems.chocolateSauce.getItemStack());
         OreDictionary.registerOre("foodPeanutButterCookie", AgricultureItems.peanutButterCookie.getItemStack());
         OreDictionary.registerOre("foodPumpkinCookie", AgricultureItems.pumpkinCookie.getItemStack());
         OreDictionary.registerOre("foodRoastedMarshmellows", AgricultureItems.roastedMarshmellows.getItemStack());
         OreDictionary.registerOre("foodSnickerdoodle", AgricultureItems.snickerDoodle.getItemStack());
         OreDictionary.registerOre("foodStrawberryJellyCookie", AgricultureItems.strawberryJellyCookie.getItemStack());
         OreDictionary.registerOre("foodSugarCookie", AgricultureItems.sugarCookie.getItemStack());
         OreDictionary.registerOre("foodToastedPBSandwich", AgricultureItems.toastedPBSandwich.getItemStack());
         OreDictionary.registerOre("foodVanilla", AgricultureItems.vanilla.getItemStack());
 
         OreDictionary.registerOre("foodAppleCinnamonCookeBurned", AgricultureItems.appleCinnamonCookieBurned.getItemStack());
         OreDictionary.registerOre("foodApplePieBurned", AgricultureItems.applePieBurned.getItemStack());
         OreDictionary.registerOre("foodBaconBurned", AgricultureItems.baconBurned.getItemStack());
         OreDictionary.registerOre("foodBeefJerkeyBurned", AgricultureItems.beefJerkeyBurned.getItemStack());
         OreDictionary.registerOre("foodButterCookieBurned", AgricultureItems.butterCookieBurned.getItemStack());
         OreDictionary.registerOre("foodCaramelBurned", AgricultureItems.caramelBurned.getItemStack());
         OreDictionary.registerOre("foodCarrotCakeBurned", AgricultureItems.carrotCakeBurned.getItemStack());
         OreDictionary.registerOre("foodChocolateChipCookieBurned", AgricultureItems.chocolateChipCookieBurned.getItemStack());
         OreDictionary.registerOre("foodChocolateSauceBurned", AgricultureItems.chocolateSauceBurned.getItemStack());
         OreDictionary.registerOre("foodFrenchToastBurned", AgricultureItems.frenchToastBurned.getItemStack());
         OreDictionary.registerOre("foodGrilledCheeseBurned", AgricultureItems.grilledCheeseBurned.getItemStack());
         OreDictionary.registerOre("foodHambuergerPattyBurned", AgricultureItems.hamburgerPattyBurned.getItemStack());
         OreDictionary.registerOre("foodLoafofBreadBurned", AgricultureItems.loafOfBreadBurned.getItemStack());
         OreDictionary.registerOre("foodPastaBurned", AgricultureItems.pastaBurned.getItemStack());
         OreDictionary.registerOre("foodPeanutButterCookieBurned", AgricultureItems.peanutButterCookieBurned.getItemStack());
         OreDictionary.registerOre("foodPumpkinCookieBurned", AgricultureItems.pumpkinCookieBurned.getItemStack());
         OreDictionary.registerOre("foodRoastedMarshmellowsBurned", AgricultureItems.roastedMarshmellowsBurned.getItemStack());
         OreDictionary.registerOre("foodShortcakeBurned", AgricultureItems.shortcakeBurned.getItemStack());
         OreDictionary.registerOre("foodSnickerdoodleBurned", AgricultureItems.snickerDoodleBurned.getItemStack());
         OreDictionary.registerOre("foodStrawberryJellyCookieBurned", AgricultureItems.strawberryJellyCookieBurned.getItemStack());
         OreDictionary.registerOre("foodRoastedPeanutsBurned", AgricultureItems.roastedPeanutsBurned.getItemStack());
         OreDictionary.registerOre("foodStrawberryPieBurned", AgricultureItems.strawberryPieBurned.getItemStack());
         OreDictionary.registerOre("foodSugarCookieBurned", AgricultureItems.sugarCookieBurned.getItemStack());
         OreDictionary.registerOre("foodToastBurned", AgricultureItems.toastBurned.getItemStack());
         OreDictionary.registerOre("foodToastedPBSandwichBurned", AgricultureItems.toastedPBSandwichBurned.getItemStack());
         OreDictionary.registerOre("foodToastedPB&JSandwichAppleBurned", AgricultureItems.toastedPBJSandwichAppleBurned.getItemStack());
         OreDictionary.registerOre("foodToastedPB&JSabdwichStrawberryBurned", AgricultureItems.toastedPBJSandwichStrawberryBurned.getItemStack());
 
     }
 
     public static void setupItems()
     {
         ((SubItemSeed) AgricultureItems.peanuts).setPlantBlock(AgricultureBlocks.peanut);
         ((SubItemSeed) AgricultureItems.strawberry).setPlantBlock(AgricultureBlocks.strawberry);
 
        ChestGenHooks.addItem("dungeonChest", new WeightedRandomChestContent(AgricultureItems.strawberry.getItemStack(), 1, 3, 5));
        ChestGenHooks.addItem("villageBlacksmithChestContents", new WeightedRandomChestContent(AgricultureItems.strawberry.getItemStack(), 1, 3, 5));
        ChestGenHooks.addItem("dungeonChest", new WeightedRandomChestContent(AgricultureItems.peanuts.getItemStack(), 1, 3, 5));
        ChestGenHooks.addItem("villageBlacksmithChestContents", new WeightedRandomChestContent(AgricultureItems.peanuts.getItemStack(), 1, 3, 5));
 
         AgricultureItems.addRecipes();
         AgricultureItems.addNames();
     }
 
     public static void tweakRecipeIguana()
     {
         final ItemStack itemStack = AgricultureItems.clayBowl.getItemStack(12);
 
         final List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
 
         for (int i = 0; i < recipes.size(); i++)
         {
             final IRecipe tmpRecipe = recipes.get(i);
             final ItemStack recipeResult = tmpRecipe.getRecipeOutput();
             if (recipeResult != null && itemStack.isItemEqual(recipeResult))
             {
                 recipes.remove(i--);
             }
         }
 
         AgricultureItems.notify(itemStack);
         GameRegistry.addRecipe(AgricultureItems.clayBowl.getItemStack(20), "X X", "XXX", 'X', Item.clay);
     }
 }
