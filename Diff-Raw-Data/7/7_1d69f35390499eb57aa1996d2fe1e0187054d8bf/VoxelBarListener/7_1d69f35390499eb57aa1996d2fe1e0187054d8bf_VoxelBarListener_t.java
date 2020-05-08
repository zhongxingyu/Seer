 package com.thevoxelbox.voxelbar;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerItemHeldEvent;
 
 public class VoxelBarListener implements Listener {
     private VoxelBar vb = new VoxelBar();
     
     @EventHandler
     public void onItemHeldChange(PlayerItemHeldEvent event) {
         int difference = event.getPreviousSlot() - event.getNewSlot();
         Player p = event.getPlayer();
         if ((p.isSneaking())) {
             if ((difference == 1)) {
                 if (p.isOp() || p.hasPermission("voxelbar.use")) {
                    if (vb.tm.isEnabled(p.getName())) {
                         VoxelBarFunctions.moveInventory(p, 1);
                         p.sendMessage(ChatColor.GREEN + "You moved your inventory " + ChatColor.RESET + ChatColor.UNDERLINE + ChatColor.DARK_AQUA +"backwards");
                     }
                 }
             } else if ((difference == -1)) {
                 if (p.isOp() || p.hasPermission("voxelbar.use")) {
                    if (vb.tm.isEnabled(p.getName())) {
                         VoxelBarFunctions.moveInventory(p, 3);
                         p.sendMessage(ChatColor.GREEN + "You moved your inventory " + ChatColor.RESET + ChatColor.UNDERLINE + ChatColor.GOLD +"forward");   
                     }
                 }
             }
         }
     }
 }
