 package MMC.neocraft.addons;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import MMC.neocraft.lib.handlers.LogHandler;
 
 public class AddonHandler
 {
 	public static void init()
 	{
 		initNEI();
 	}
 	
 	private static void initNEI()
 	{
 		try
 		{
 			Class<?> craftingRecipe = Class.forName("codechicken.nei.recipe.GuiCraftingRecipe");
 			Class<?> usageRecipe = Class.forName("codechicken.nei.recipe.GuiUsageRecipe");
 			if(craftingRecipe == null || usageRecipe == null) { throw new ClassNotFoundException(); }
 			
 			Class<?> craftingInterface = Class.forName("codechicken.nei.recipe.ICraftingHandler");
 			Class<?> usageInterface = Class.forName("codechicken.nei.recipe.IUsageHandler");
 			if(craftingInterface == null || usageInterface == null) { throw new ClassNotFoundException(); }
 			
 			Method registerRecipe = craftingRecipe.getMethod("registerRecipeHandler", craftingInterface);
 			Method registerUsage = usageRecipe.getMethod("registerUsageHandler", usageInterface);
 			if(registerRecipe == null || registerUsage == null) { throw new NoSuchMethodException(); }
 			
 			registerRecipe.setAccessible(true); registerUsage.setAccessible(true);

			KilnSmelteryNEI kilnSmelteryNEI = new KilnSmelteryNEI();
			KilnBakeryNEI kilnBakeryNEI = new KilnBakeryNEI();
			KilnFuelNEI kilnFuelNEI = new KilnFuelNEI();
 			
 			registerRecipe.invoke(null, kilnSmelteryNEI);
 			registerRecipe.invoke(null, kilnBakeryNEI);
 			registerRecipe.invoke(null, kilnFuelNEI);
 
 			registerUsage.invoke(null, kilnSmelteryNEI);
 			registerUsage.invoke(null, kilnBakeryNEI);
 			registerUsage.invoke(null, kilnFuelNEI);
 		}
 		catch(ClassNotFoundException cnfe) { LogHandler.info("NeoCraft could not find NEI, skipping NEI integration"); }
 		catch(NoSuchMethodException nsme) { LogHandler.info("NeoCraft found NEI, but couldn't find the register methods, skipping recipe registering"); }
 		catch(InvocationTargetException ite) { LogHandler.info("NeoCraft found NEI and required Methods, but couldn't invoke them, skipping recipe registering"); }
 		catch(IllegalAccessException iae) { LogHandler.info("NeoCraft found NEI and required Methods, but couldn't access them, skipping recipe registering"); }
 	}
 }
