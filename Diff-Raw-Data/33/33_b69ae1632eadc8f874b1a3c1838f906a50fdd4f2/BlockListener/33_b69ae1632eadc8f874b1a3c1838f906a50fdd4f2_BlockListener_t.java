 package com.minecarts.verrier.lockdown.listener;
 
 import com.minecarts.verrier.lockdown.*;
 
 import org.bukkit.Material;
 import org.bukkit.event.block.*;
 import org.bukkit.entity.Player;
 
 public class BlockListener extends org.bukkit.event.block.BlockListener{
     private Lockdown plugin;
     public BlockListener(Lockdown plugin){
         this.plugin = plugin;
     }
     
    @Override
     public void onBlockBreak(BlockBreakEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
         plugin.informPlayer(event.getPlayer());
     }
     
     //Also handled by player Interact
    @Override
     public void onBlockPlace(BlockPlaceEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
         plugin.informPlayer(event.getPlayer());
     }
     
     //Also handled by player Interact
     /*
     public void onBlockInteract(BlockInteractEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
         if(event.getEntity() instanceof Player){ 
             plugin.informPlayer((Player) event.getEntity());
         }
     }
     */
     
    @Override
     public void onBlockIgnite(BlockIgniteEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
     }
     
    @Override
     public void onBlockBurn(BlockBurnEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
     }
     
    @Override
     public void onBlockFlow(BlockFromToEvent event){
         plugin.log("EVENT: " + event.getEventName());
         if(event.isCancelled() || !plugin.isLocked()){
             return;
         }
         event.setCancelled(true);
     }
 }
