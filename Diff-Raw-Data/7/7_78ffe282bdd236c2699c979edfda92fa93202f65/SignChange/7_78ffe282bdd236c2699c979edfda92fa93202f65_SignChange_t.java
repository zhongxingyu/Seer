 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.*;
 import com.undeadscythes.udsplugin.utilities.*;
 import org.bukkit.block.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 
 /**
  * When a player changes the text of a sign.
  * @author UndeadScythes
  */
 public class SignChange extends ListenerWrapper implements Listener {
     private SaveablePlayer player;
     private Block block;
 
     @EventHandler
     public final void onEvent(final SignChangeEvent event) {
         final String line1 = event.getLine(0);
         final String line2 = event.getLine(1);
        final String line3 = event.getLine(2);
         player = PlayerUtils.getOnlinePlayer(event.getPlayer().getName());
         block = event.getBlock();
         if(line1.equalsIgnoreCase("[shop]") && checkPerm(Perm.SHOP_SIGN)) {
             if(line2.matches(UDSPlugin.INT_REGEX)) {
                 event.setLine(0, Color.SIGN + "[SHOP]");
             } else {
                 event.setCancelled(true);
                 player.sendError("The second line must contain a number.");
                 block.breakNaturally();
             }
         } else if(line1.equalsIgnoreCase("[checkpoint]") && checkPerm(Perm.SIGN_CHECKPOINT)) {
             event.setLine(0, Color.SIGN + "[CHECKPOINT]");
         } else if(line1.equalsIgnoreCase("[minecart]") && checkPerm(Perm.SIGN_MINECART)) {
             event.setLine(0, Color.SIGN + "[MINECART]");
         } else if(line1.equalsIgnoreCase("[prize]") && checkPerm(Perm.SIGN_PRIZE)) {
            if(findItem(line2) != null && line3.matches(UDSPlugin.INT_REGEX)) {
                 event.setLine(0, Color.SIGN + "[PRIZE]");
             } else {
                 badFormat();
             }
         } else if(line1.equalsIgnoreCase("[item]") && checkPerm(Perm.SIGN_ITEM)) {
            if(findItem(line2) != null && line3.matches(UDSPlugin.INT_REGEX)) {
                 event.setLine(0, Color.SIGN + "[ITEM]");
             } else {
                 badFormat();
             }
         } else if(line1.equalsIgnoreCase("[warp]") && checkPerm(Perm.SIGN_WARP)) {
             event.setLine(0, Color.SIGN + "[WARP]");
         } else if(line1.equalsIgnoreCase("[spleef]") && checkPerm(Perm.SIGN_SPLEEF)) {
             event.setLine(0, Color.SIGN + "[SPLEEF]");
         }
     }
 
     private boolean checkPerm(final Perm perm) {
         if(!player.hasPermission(perm)) {
             block.breakNaturally();
             player.sendError("You do not have permission to place this sign.");
             return false;
         }
         return true;
     }
 
     private void badFormat() {
         block.breakNaturally();
         player.sendError("You have not written this sign correctly.");
     }
 }
