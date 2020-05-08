 package com.emc.food;
 
 /*
  * EnhanceMeCraft Basic Modification
  * @Author: nextized
  * @Last changed: 2013-09-01
  * Licensed under nextized cross license - see license.txt for more information
  */
 
 import com.emc.conf.idManager;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 
 public class foodManager {
 
 		// @Defined Items (@nexized)
 		public static Item foodRawBacon;
 	    public static Item foodBacon;
 	    public static Item foodRawMoutton;
 	    public static Item foodMoutton;
 	    public static Item foodCucumber;
 	    public static Item foodBanana;
 	    public static Item foodTunaSalad;
 	    public static Item foodRawSardine;
 	    public static Item foodSardine;
 	    public static Item foodCheese;
 	    public static Item foodRye;
 	    public static Item foodBarley;
 	    
 	    // @Defined Items (@mod_attackz)  
 		public static Item foodCorn;
 		public static Item foodTomato;
 		public static Item foodStrawberry;
 		public static Item foodBlueberry;
 		public static Item foodGreenTomato;
 		public static Item foodOrange;
 		public static Item foodLettuce;
 		public static Item foodEggplant;
 		public static Item foodEggScramled;
 		
 		public static void addFood(idManager idm, CreativeTabs tab) {
	    	if(idm.ifEnabled("@food")) {
 				// Defined Items (@nexized)
 		    	foodRawBacon = (new emcFood(idm.getItemID("foodRawBacon"), 1, 0.1F, true, tab).setUnlocalizedName("foodRawBacon"));
 		    	foodBacon = (new emcFood(idm.getItemID("foodBacon"), 3, 0.3F, true, tab).setUnlocalizedName("foodBacon"));
 		    	foodRawMoutton = (new emcFood(idm.getItemID("foodRawMoutton"), 1, 0.1F, true, tab).setUnlocalizedName("foodRawMoutton"));
 		    	foodMoutton = (new emcFood(idm.getItemID("foodMoutton"), 4, 0.4F, true, tab).setUnlocalizedName("foodMoutton"));
 		    	foodCucumber = (new emcFood(idm.getItemID("foodCucumber"), 1, 0.1F, true, tab).setUnlocalizedName("foodCucumber"));
 		    	foodBanana = (new emcFood(idm.getItemID("foodBanana"), 1, 0.1F, true, tab).setUnlocalizedName("foodBanana"));
 		    	foodTunaSalad = (new emcFood(idm.getItemID("foodTunaSalad"), 3, 0.3F, true, tab).setUnlocalizedName("foodTunaSalad"));
 		    	foodRawSardine = (new emcFood(idm.getItemID("foodRawSardine"), 1, 0.1F, true, tab).setUnlocalizedName("foodRawSardine"));
 		    	foodSardine = (new emcFood(idm.getItemID("foodSardine"), 1, 0.1F, true, tab).setUnlocalizedName("foodSardine"));
 		    	foodCheese = (new emcFood(idm.getItemID("foodCheese"), 3, 0.3F, true, tab).setUnlocalizedName("foodCheese"));
 		    	foodRye = (new emcFood(idm.getItemID("foodRye"), 1, 0.1F, true, tab).setUnlocalizedName("foodRye"));
 		    	foodBarley = (new emcFood(idm.getItemID("foodBarley"), 1, 0.1F, true, tab).setUnlocalizedName("foodBarley"));
 		    	
 		    	foodCorn = (new emcFood(idm.getItemID("foodCorn"), 1, 0.1F, true, tab).setUnlocalizedName("foodCorn"));
 		    	foodTomato = (new emcFood(idm.getItemID("foodTomato"), 3, 0.3F, true, tab).setUnlocalizedName("foodTomato"));
 		    	foodStrawberry = (new emcFood(idm.getItemID("foodStrawberry"), 1, 0.1F, true, tab).setUnlocalizedName("foodStrawberry"));
 		    	foodBlueberry = (new emcFood(idm.getItemID("foodBlueberry"), 3, 0.3F, true, tab).setUnlocalizedName("foodBlueberry"));
 		    	foodGreenTomato = (new emcFood(idm.getItemID("foodTomato"), 1, 0.1F, true, tab).setUnlocalizedName("foodGreenTomato"));
 		    	foodOrange = (new emcFood(idm.getItemID("foodOrange"), 3, 0.3F, true, tab).setUnlocalizedName("foodOrange"));
 		    	foodLettuce = (new emcFood(idm.getItemID("foodLettuce"), 1, 0.1F, true, tab).setUnlocalizedName("foodLettuce"));
 		    	foodEggplant = (new emcFood(idm.getItemID("foodEggplant"), 3, 0.3F, true, tab).setUnlocalizedName("foodEggplant"));
 		    	foodEggScramled = (new emcFood(idm.getItemID("foodEggScramled"), 3, 0.3F, true, tab).setUnlocalizedName("foodEggScramled"));
 	    	}
 	    }
 }
