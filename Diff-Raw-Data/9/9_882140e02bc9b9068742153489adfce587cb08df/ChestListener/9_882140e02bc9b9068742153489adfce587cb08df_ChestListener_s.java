 package com.coffeejawa.LootChests;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 
 public class ChestListener implements Listener {
     private LootChestsMain plugin;
     
     private final int chestID = Material.CHEST.getId();
     
     ChestListener(LootChestsMain plugin){
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteract(PlayerInteractEvent event){
         Block block = event.getClickedBlock();
         // if its not a chest, we don't care
         if((block == null) || (block.getTypeId() != chestID)){
             return;
         }
         
         // if its not in our map, we don't care
         if(!plugin.hasChest(block.getLocation())){
             return;
         }
         
         Player chestOwner = plugin.getChestOwner(block.getLocation());
        if(!chestOwner.equals(event.getPlayer().getName())){
             // block access
             event.setCancelled(true);
         }
         
         // remove chest if empty
         // TODO : this needs to be done after Player removes an item, not when first interacting
         Chest chest = (Chest) block.getState();
         if(chest.getBlockInventory().getSize() == 0){
             chest.setType(Material.AIR);
             event.setCancelled(true);
         }
         
     }
     @EventHandler
     public void onInventoryEvent(InventoryClickEvent event)
     {   
         // Check the inventory of the chest
         InventoryView view = event.getView();
         
         // Chest non-empty, ignore.
         for( ItemStack item : view.getTopInventory().getContents()){
             if(item != null){
                 return;
             }
             
         }
         
         Player player = (Player) event.getWhoClicked();
         Chest c = (Chest) view.getTopInventory().getHolder();
         if( c != null ){
             Block b = c.getBlock();
             
             // Chest not in our cache, ignore
             if(!plugin.hasChest(b.getLocation())){
                 return;
             }
             
             // Chest owned by another player, ignore
             if(plugin.getChestOwner(b.getLocation()) != player){
                 return;
             }
             
             b.setType(Material.AIR);
         }
         
     }
     
     
     
 }
