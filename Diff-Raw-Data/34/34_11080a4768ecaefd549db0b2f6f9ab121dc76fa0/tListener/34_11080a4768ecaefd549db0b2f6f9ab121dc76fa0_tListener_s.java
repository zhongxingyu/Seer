 /**
  *
  * Copyright by Samuel Castro, 2011
  * All Rights Reserved
  *
  * NOTES:
  * Please do not redistribute this plugin without my permission
  */
 package com.SySammy.triggercmds;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
 
class tListener extends PlayerListener {
 
     tCmds plugin;
     private Methods Methods;
 
     public tListener(tCmds aThis) {
         this.plugin = aThis;
     }
 
    @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             if (event.getClickedBlock().getType().equals(Material.STONE_BUTTON) || event.getClickedBlock().getType().equals(Material.LEVER) || event.getClickedBlock().getType().equals(Material.STONE_PLATE) || event.getClickedBlock().getType().equals(Material.WOOD_PLATE)) {
                 Player p = event.getPlayer();
                 Location loc = event.getClickedBlock().getLocation();
                 Methods = new Methods(plugin);
                 if (plugin.iNames.containsKey(p)) {
                     Methods.RegLocation(p, loc);
                     return;
                 } else if (plugin.Cmds.containsKey(loc) && !event.getClickedBlock().getType().equals(Material.STONE_PLATE) && !event.getClickedBlock().getType().equals(Material.WOOD_PLATE)) {
                     Methods.ExecuteCmd(p, loc);
                 }
             }
         }
         if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
             if (event.getClickedBlock().getType().equals(Material.STONE_BUTTON) || event.getClickedBlock().getType().equals(Material.LEVER) || event.getClickedBlock().getType().equals(Material.STONE_PLATE) || event.getClickedBlock().getType().equals(Material.WOOD_PLATE)) {
                 Player p = event.getPlayer();
                 Location loc = event.getClickedBlock().getLocation();
                 Methods = new Methods(plugin);
                 if (plugin.iNames.containsKey(p)) {
                     Methods.delLink(p);
                     return;
                 } else if (plugin.Cmds.containsKey(loc) && !event.getClickedBlock().getType().equals(Material.STONE_PLATE) && !event.getClickedBlock().getType().equals(Material.WOOD_PLATE)) {
                     Methods.ExecuteCmd(p, loc);
                 }
             }
         }
         if (event.getAction().equals(Action.PHYSICAL)) {
             if (event.getClickedBlock().getType().equals(Material.STONE_PLATE) || event.getClickedBlock().getType().equals(Material.WOOD_PLATE)) {
                 Player p = event.getPlayer();
                 Location loc = event.getClickedBlock().getLocation();
                 Methods = new Methods(plugin);
                 if (!plugin.iNames.containsKey(p) && plugin.Cmds.containsKey(loc)) {
                     Methods.ExecuteCmd(p, loc);
                     return;
                 }
             }
         }
     }
 }
