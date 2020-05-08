 
 package com.quartercode.basicexpression.command;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Minecart;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.inventory.ItemStack;
 import com.quartercode.minecartrevolution.MinecartRevolution;
 import com.quartercode.minecartrevolution.util.expression.ExpressionCommand;
 import com.quartercode.minecartrevolution.util.expression.ExpressionCommandInfo;
 
 public class CollectCommand implements ExpressionCommand {
 
     private final MinecartRevolution minecartRevolution;
 
     public CollectCommand(final MinecartRevolution minecartRevolution) {
 
         this.minecartRevolution = minecartRevolution;
     }
 
     @Override
     public ExpressionCommandInfo getInfo() {
 
         return new ExpressionCommandInfo("co", "collect");
     }
 
     @Override
     public boolean canExecute(final Minecart minecart) {
 
         return minecart instanceof StorageMinecart;
     }
 
     @Override
     public void execute(final Minecart minecart, final Object parameter) {
 
         if (minecart instanceof StorageMinecart) {
             int radius = 5;
             final List<String> items = new ArrayList<String>();
 
             if (parameter != null) {
                 for (final String part : String.valueOf(parameter).split(",")) {
                     try {
                         if (part.startsWith("r")) {
                             radius = Integer.parseInt(part.replaceAll("r", ""));
                         } else {
                             items.add(part);
                         }
                     }
                     catch (final NumberFormatException e) {
                         MinecartRevolution.handleSilenceThrowable(e);
                     }
                 }
             }
 
            for (final String item : items) {
                collectItems((StorageMinecart) minecart, radius, minecartRevolution.getAliasConfig().getId(item));
             }
         }
     }
 
     private void collectItems(final StorageMinecart storageMinecart, final int radius, final int itemId) {
 
         if (storageMinecart.getInventory().firstEmpty() < 0) {
             return;
         }
 
         for (final Entity entity : storageMinecart.getNearbyEntities(radius, radius, radius)) {
             if (entity instanceof Item) {
                 final Item item = (Item) entity;
                 if (item.isDead()) {
                     continue;
                 } else if (itemId > 0 && item.getItemStack().getTypeId() != itemId) {
                     continue;
                 }
 
                 storageMinecart.getInventory().addItem(new ItemStack[] { item.getItemStack() });
                 item.remove();
             }
         }
     }
 
 }
