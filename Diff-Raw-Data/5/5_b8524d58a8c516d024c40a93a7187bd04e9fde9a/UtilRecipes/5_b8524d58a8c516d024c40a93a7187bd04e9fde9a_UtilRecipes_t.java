 package playacem.allrondism.core.util;
 
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 /**
  * Allrondism
  * 
  * UtilRecipes
  * 
  * provides alternate ways for adding recipes
  * 
  * @author Playacem
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * 
  */
 public class UtilRecipes {
 
     public static void addVanillaRecipe(String type, boolean useOreDict, ItemStack output, Object... params) {
         if (type.toUpperCase().contains("SHAPED")) {
             if (useOreDict) {
                 GameRegistry.addRecipe(new ShapedOreRecipe(output, params));
             }
             else {
                 GameRegistry.addShapedRecipe(output, params);
             }
         } else if (type.toUpperCase().contains("SHAPELESS")) {
             if (useOreDict) {
                 GameRegistry.addRecipe(new ShapelessOreRecipe(output, params));
             }
             else {
                 GameRegistry.addShapelessRecipe(output, params);
             }
         } else {
             StringBuilder sB = new StringBuilder();
            sB.append(String.format("The crafting recipe for %s (%s) was not properly registered.", output.toString(), output.getDisplayName()));
             LogHelper.alert(sB.toString());
         }
     }
 
     /**
      * Adds a vanilla crafting recipe, supports OreDictionary
      * 
      * @param type
      *            - Shaped or Shapeless
      * @param output
      * @param params
      */
     public static void addVanillaRecipe(String type, ItemStack output, Object... params) {
         addVanillaRecipe(type, true, output, params);
     }
 
     public static void addVanillaSmelting(int input, ItemStack output, float xp) {
         addVanillaSmelting(input, 0, output, xp);
     }
 
     /**
      * Adds a Vanilla Smelting recipe Metadata compatible
      * 
      */
     public static void addVanillaSmelting(int id, int metadata, ItemStack output, float xp) {
         FurnaceRecipes.smelting().addSmelting(id, metadata, output, xp);
     }
 
     /**
      * Adds a 3x3 crafting recipe for the specified block and the matching
      * uncrafting recipe
      * 
      * @param storageBlock
      *            - result
      * @param component
      *            - a String or an ItemStack
      */
     public static void addStorageRecipe(ItemStack storageBlock, Object component) {
         if (!(component instanceof String || component instanceof ItemStack)) {
             StringBuilder sB = new StringBuilder();
            sB.append(String.format("Component is not valid! Block: %s Component: %s", storageBlock.getDisplayName(), component.toString()));
             LogHelper.alert(sB.toString());
             LogHelper.alert("The Recipe was not added.");
             return;
         }
         UtilRecipes.addVanillaRecipe("Shaped", storageBlock, "xxx", "xxx", "xxx",
                 Character.valueOf('x'), component);
 
         ItemStack componentStack = null;
         if (component instanceof ItemStack) {
             componentStack = (ItemStack) component;
         } else {
             componentStack = UtilOreDict.instance().getItemStack(component, 9);
         }
 
         UtilRecipes.addVanillaRecipe("Shapeless", componentStack, storageBlock);
     }
 
 }
