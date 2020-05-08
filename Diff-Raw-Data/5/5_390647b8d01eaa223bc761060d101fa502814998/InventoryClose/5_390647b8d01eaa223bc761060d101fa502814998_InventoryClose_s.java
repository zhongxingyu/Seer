 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.Color;
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.event.*;
 import org.bukkit.event.inventory.*;
 import org.bukkit.inventory.*;
 
 /**
  * Fired when a player closes an inventory.
  * 
  * @author UndeadScythes
  */
 public class InventoryClose extends ListenerWrapper implements Listener {
     @EventHandler
     public final void onEvent(final InventoryCloseEvent event) {
         final SaveablePlayer shopper = PlayerUtils.getOnlinePlayer(event.getPlayer().getName());
         if(shopper.isShopping()) {
             final ItemStack handItem = event.getView().getCursor();
             if(!handItem.getType().equals(Material.AIR)) {
                 event.getInventory().addItem(handItem);
                 event.getView().setCursor(new ItemStack(Material.AIR));
             }
             final Block block = ((BlockState)event.getInventory().getHolder()).getBlock();
             final ArrayList<ItemStack> shoppingList = ShopUtils.compareCarts(shopper);
             int totalDue = 0;
             for(final ItemStack item : shoppingList) {
                 totalDue += item.getAmount();
             }
             totalDue *= getPrice(block);
             if(totalDue > 0) {
                 if(shopper.canAfford(totalDue)) {
                     shopper.sendNormal("You spent " + totalDue + " " + Config.CURRENCIES + ".");
                     shopper.debit(totalDue);
                    findShopOwner(block.getLocation()).credit(totalDue);
                 } else {
                     shopper.sendError("You do not have enough money to buy these items.");
                     for(ItemStack item : shoppingList) {
                         if(item != null) {
                             event.getInventory().addItem(item);
                             shopper.getInventory().removeItem(item);
                         }
                     }
                 }
             }
             ShopUtils.removeShopper(shopper);
         }
     }
 
     private int getPrice(final Block block) {
         final Sign sign = (Sign)block.getRelative(BlockFace.UP).getState();
         if(sign.getLine(1).equals(Color.SIGN + "Shop")) {                               //
             return Integer.parseInt(sign.getLine(3).split(":")[0].replace("B ", ""));   // TODO: Update hack fix (fix me plox)
         }                                                                               //
         return Integer.parseInt(sign.getLine(1));
     }
 }
