 package mods.storemore;
   
   
 import java.util.AbstractMap;
 import java.util.ArrayList;
   
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.registry.GameRegistry;
   
 import mods.storemore.common.storemoreMain;
 import mods.storemore.ic2.api.Ic2Recipes;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
   
 public class sm_ic2handler
 {
 
     public static void initIC2() {
 	}
     
     public static void compressedcoalIIRecipe()
     {
        ItemStack tOutput = new ItemStack(Item.diamond);
         ArrayList<ItemStack> tList = OreDictionary.getOres("packedcoalII");
         for (int i = 0; i < tList.size(); i++) {
             ItemStack tStack = tList.get(i);
             tStack = tStack.copy();
             tStack.stackSize = 1;
             Ic2Recipes.addMaceratorRecipe(tStack, tOutput);
         }
     }
         
     public static void compressedcoalIIIRecipe()
         {
        ItemStack tOutput = new ItemStack(Item.diamond);
        ArrayList<ItemStack> tList = OreDictionary.getOres("packedcoalIII");
         for (int i = 0; i < tList.size(); i++) {
             ItemStack tStack = tList.get(i);
             tStack = tStack.copy();
             tStack.stackSize = 9;
             Ic2Recipes.addMaceratorRecipe(tStack, tOutput);
         }
         
  
     }
     
 
     
 }
 
 	
