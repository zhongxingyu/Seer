 package net.invisioncraft.plugins.salesmania.util;
 
 import net.invisioncraft.plugins.salesmania.Salesmania;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Owner: Justin
  * Date: 6/3/12
  * Time: 2:52 AM
  */
 public class ItemManager {
     private Salesmania plugin;
     private static Logger consoleLogger = Salesmania.consoleLogger;
     public ItemManager(Salesmania plugin) {
         this.plugin = plugin;
     }
 
     public static int getQuantity(Player player, ItemStack itemStack) {
         int quantity = 0;
         for(Map.Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(itemStack.getTypeId()).entrySet()) {
             // Check for data value + enchants
             if(!compareItem(entry.getValue(), itemStack)) continue;
             quantity += entry.getValue().getAmount();
         }
         return quantity;
     }
 
     public static boolean takeItem(Player player, ItemStack itemStack) {
         int remainingQuantity = itemStack.getAmount();
         for(Map.Entry<Integer, ? extends ItemStack> entry : player.getInventory().all(itemStack.getTypeId()).entrySet()) {
             if(!compareItem(entry.getValue(), itemStack)) continue;
 
             if(remainingQuantity == 0) break;
             ItemStack stack = entry.getValue();
 
             if(remainingQuantity >= stack.getAmount()) {
                 remainingQuantity -= stack.getAmount();
                 player.getInventory().removeItem(stack);
             }
 
             else if (remainingQuantity < stack.getAmount()) {
                 stack.setAmount(stack.getAmount()-remainingQuantity);
                 remainingQuantity -= remainingQuantity;
             }
         }
         if(remainingQuantity != 0) {
             consoleLogger.severe("Could not take expected quantity!");
             return false;
         }
         else return true;
     }
 
     public static boolean compareItem(ItemStack stack1, ItemStack stack2) {
         if(!stack1.getEnchantments().equals(stack2.getEnchantments())) return false;
         if(!stack1.getData().equals(stack2.getData())) return false;
         if(stack1.getDurability() != stack2.getDurability()) return false;
         return true;
     }
 
     public static String getName(ItemStack itemStack) {
         // Spawner names
         String itemName;
         if(itemStack.getTypeId() == Block.MOB_SPAWNER.id) {
             itemName = EntityType.fromId((int) itemStack.getData().getData()).getName()
                     + " Spawner";
         }
         else try {
             itemName = Items.itemByStack(itemStack).getName();
         } catch (NullPointerException ex) {
             itemName = itemStack.getType().name();
         }
 }
