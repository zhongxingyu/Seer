 /**
  * User: YBogomolov
  * Date: 29.07.11
  * Time: 16:13
  */
 
 package com.github.doodlez.bukkit.globalquest.listeners;
 
 import com.github.doodlez.bukkit.globalquest.GlobalQuestPlugin;
 import net.minecraft.server.CraftingManager;
 import net.minecraft.server.CraftingRecipe;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.ShapedRecipes;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.server.ServerListener;
 
 import org.bukkit.plugin.Plugin;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 public class SpecialServerListener extends ServerListener {
     @Override
     public void onPluginEnable(PluginEnableEvent event) {
         Plugin plugin = event.getPlugin();
 
         if (plugin.getDescription().getName().equals("GlobalQuestPlugin")) {
             CraftingManager craftingManager = CraftingManager.getInstance();
             List<CraftingRecipe> craftList = craftingManager.b();
 
             List<CraftingRecipe> editedList = new ArrayList<CraftingRecipe>();
 
             for (CraftingRecipe recipe : craftList) {
                 if (recipe instanceof ShapedRecipes) {
                     ShapedRecipes shapedRecipe = (ShapedRecipes)recipe;
                     ItemStack recipeResult = shapedRecipe.b();
                     String resultName = recipeResult.getItem().j();
 
                     boolean addItem = true;
                     for (String blockedItem : GlobalQuestPlugin.blockedRecipes) {
                         if (resultName.equals(blockedItem)) {
                             System.out.print(resultName + " is blocked!");
                             addItem = false;
                             break;
                         }
                     }
                    if (addItem)
                         editedList.add(recipe);
                 }
             }
 
             try {
                 if (setPrivateValue(CraftingManager.class, craftingManager, "b", editedList))
                     System.out.print("All right, set edited list!");
                 else
                     System.out.print("Nope.");
             } catch (NoSuchFieldException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Sets private field to a new value. Use with caution!
      * @param instanceClass Class to which instance belongs.
      * @param instance Instance whose field needed to be edited.
      * @param fieldName Field name (self-explanatory, huh?).
      * @param newValue New field value.
      * @return True, if field was set successfully, and false otherwise.
      * @throws IllegalArgumentException Illegal argument exception
      * @throws SecurityException Security exception
      * @throws NoSuchFieldException No such field exception
      */
     public static boolean setPrivateValue(Class instanceClass, Object instance, String fieldName, Object newValue)
             throws IllegalArgumentException, SecurityException, NoSuchFieldException {
         try {
             Field f = instanceClass.getDeclaredField(fieldName);
             f.setAccessible(true);
             f.set(instance, newValue);
             return true;
         }
         catch(IllegalAccessException e) {
             System.out.print("Illegal access.");
             return false;
         }
         catch (IndexOutOfBoundsException e) {
             System.out.print("Index out of bounds.");
             return false;
         }
    }
 }
