 package com.github.Indiv0.BookDupe;
 
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.inventory.CraftingInventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ItemCraftListener implements Listener {
 
     // Create a method to handle/interact with crafting events.
     @EventHandler
     public void onItemCraft(CraftItemEvent event) {
         // Get the crafting inventory (3x3 matrix) used to craft the item.
         CraftingInventory inventory = event.getInventory();
 
         // Get the index of the first (and only) Material.WRITTEN_BOOK used in the recipe.
         int index = inventory.first(Material.WRITTEN_BOOK);
 
         if (index != -1)
         {
             // Get the item associated with that id from the inventory.
             ItemStack item = inventory.getItem(index);
 
             // Create a new ItemStack by cloning the previous one.
             ItemStack newItem = item.clone();
 
             // Set the amount of items in the new ItemStack to two.
             newItem.setAmount(2);
 
             // Add the new ItemStack to the player's inventory.
             event.getWhoClicked().getInventory().addItem(newItem);
         }
     }
    
 }
