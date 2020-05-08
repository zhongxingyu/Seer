 package com.undeadscythes.udsplugin;
 
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 
 /**
  * A minecart owned by a player.
 *
  * @author UndeadScythes
  */
 public class OwnedMinecart {
     private SaveablePlayer owner;
     private final Minecart minecart;
 
     public OwnedMinecart(final Minecart minecart, final SaveablePlayer owner) {
         this.owner = owner;
         this.minecart = minecart;
     }
 
     public final void setOwner(final SaveablePlayer player) {
         owner = player;
     }
 
     public final boolean near(final Location location) {
        return minecart.getWorld().equals(location.getWorld()) && minecart.getLocation().distance(location) < 2;
     }
 
     public final int age(final int ticks) {
         minecart.setTicksLived(minecart.getTicksLived() + ticks);
         return minecart.getTicksLived();
     }
 
     public final boolean isEmpty() {
         return minecart.isEmpty();
     }
 
     public final void remove() {
         this.minecart.remove();
         if(owner != null) {
             owner.sendNormal("You picked up your minecart.");
             owner.giveAndDrop(new ItemStack(Material.MINECART));
         }
     }
 
     public final UUID getUUID() {
         return minecart.getUniqueId();
     }
 }
