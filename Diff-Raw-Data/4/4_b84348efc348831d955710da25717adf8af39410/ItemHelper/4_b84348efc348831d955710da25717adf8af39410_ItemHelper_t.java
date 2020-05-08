 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.utils;
 
 import net.minecraft.block.Block;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.oredict.OreDictionary;
 
 import java.util.List;
 
 public class ItemHelper {
     /**
      * Damages item.
      *
      * @param item
      * @param amount
      * @return If item is destroyed.
      */
     public static boolean damageItem(ItemStack item, int amount) {
         //this.itemDamage > this.getMaxDamage()
         if (item == null) return false;
         if (amount <= 0) return false;
 
         int newItemDamage = item.getItemDamage() + amount;
         item.setItemDamage(newItemDamage);
         if (newItemDamage > item.getMaxDamage()) {
             return true;
         }
 
         return false;
     }
 
     public static void insertStackMultipleTimes(List<ItemStack> list, ItemStack item, int count) {
         for (int i = 0; i < count; i++) {
             list.add(item.copy());
         }
     }
 
     public static ItemStack getItemStackAnyDamage(Item item) {
         return new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
     }
 
     public static ItemStack getItemStackAnyDamage(Block block) {
         return new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
     }
 
     public static boolean haveStacksSameIdAndDamage(ItemStack template, ItemStack tested) {
         if (template == null || tested == null) return false;
         if (template.itemID != tested.itemID) return false;
         if (template.getItemDamage() == OreDictionary.WILDCARD_VALUE) return true;
         return template.getItemDamage() == tested.getItemDamage();
     }
 
     public static boolean haveStacksSameIdDamageAndProperSize(ItemStack template, ItemStack tested) {
         if (!haveStacksSameIdAndDamage(template, tested)) return false;
         return tested.stackSize >= template.stackSize;
     }
 
     public static int findItemIdByName(String name) {
         if (name == null || name.isEmpty()) return 0;
        String prefixedName = "item." + name;
 
         for (int i = 0; i < Item.itemsList.length; i++) {
             Item item = Item.itemsList[i];
             if (item != null) {
                if (prefixedName.equals(item.getUnlocalizedName())) {
                     return item.itemID;
                 }
             }
         }
 
         return 0;
     }
 
     public static ItemStack[] copyStackArray(ItemStack[] inv) {
         ItemStack[] ret = new ItemStack[inv.length];
         for (int i = 0; i < inv.length; i++) {
             ItemStack input = inv[i];
             ret[i] = input == null ? null : input.copy();
         }
         return ret;
     }
 
     public static boolean isOutputSlotFreeFor(ItemStack output, int slotNumber, IInventory inv) {
         ItemStack outputSlotStack = inv.getStackInSlot(slotNumber);
         if (outputSlotStack == null) return true;
         if (!haveStacksSameIdAndDamage(outputSlotStack, output)) return false;
         if (outputSlotStack.stackSize + output.stackSize > outputSlotStack.getMaxStackSize()) return false;
         return true;
     }
 }
