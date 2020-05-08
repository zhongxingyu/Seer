 /*
  * Copyright (C) 2013 daboross
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bukkitdev.removegoditems;
 
 import java.util.Map;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 /**
  *
  * @author daboross
  */
 public class GodItemChecker {
 
     private final RemoveGodItemsPlugin plugin;
 
     public GodItemChecker(RemoveGodItemsPlugin plugin) {
         this.plugin = plugin;
     }
 
     public void removeGodEnchants(HumanEntity player) {
         Inventory inv = player.getInventory();
         Location loc = player.getLocation();
         String name = player.getName();
         for (ItemStack it : player.getInventory().getArmorContents()) {
             removeGodEnchants(it, inv, loc, name);
         }
         for (ItemStack it : player.getInventory().getContents()) {
             removeGodEnchants(it, inv, loc, name);
         }
     }
 
     public void removeGodEnchants(ItemStack itemStack, HumanEntity p) {
         removeGodEnchants(itemStack, p.getInventory(), p.getLocation(), p.getName());
     }
 
     public void removeGodEnchants(ItemStack itemStack, Inventory inventory, Location location, String name) {
        if (itemStack != null && itemStack.getEnchantments().size() > 0 && itemStack.getType() != Material.AIR) {
             for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
                 Enchantment e = entry.getKey();
                 if (entry.getValue() > e.getMaxLevel() || !e.canEnchantItem(itemStack)) {
                     String message;
                     if (e.canEnchantItem(itemStack)) {
                         message = String.format("Changed level of enchantment %s from %s to %s on item %s in inventory of %s", e.getName(), entry.getValue(), e.getMaxLevel(), itemStack.getType().toString(), name);
                         itemStack.addEnchantment(e, e.getMaxLevel());
                     } else {
                         message = String.format("Removed enchantment %s level %s on item %s in inventory of %s", e.getName(), entry.getValue(), itemStack.getType().toString(), name);
                         itemStack.removeEnchantment(e);
                     }
                     plugin.getLogger().log(Level.INFO, message);
                 }
             }
             checkOverstack(itemStack, inventory, location, name);
         }
     }
 
     public void checkOverstack(ItemStack itemStack, Inventory inventory, Location location, String name) {
         int maxAmount = itemStack.getType().getMaxStackSize();
         int amount = itemStack.getAmount();
         if (amount > maxAmount) {
             int numStacks = amount / maxAmount;
             int left = amount % maxAmount;
             plugin.getLogger().log(Level.INFO, "Unstacked item {0} of size {1} to size {2} with {3} extra stacks in inventory of {4} size", new Object[]{itemStack.getType().name(), amount, left, numStacks, name});
             itemStack.setAmount(left);
             for (int i = 0; i < numStacks; i++) {
                 ItemStack newStack = itemStack.clone();
                 newStack.setAmount(maxAmount);
                 int slot = inventory.firstEmpty();
                 if (slot < 0) {
                     location.getWorld().dropItemNaturally(location, newStack);
                 } else {
                     inventory.setItem(slot, newStack);
                 }
             }
         }
     }
 
     public void runFullCheckNextSecond(Player p) {
         Bukkit.getScheduler().runTaskLater(plugin, new GodItemFixRunnable(p), 20);
     }
 
     public class GodItemFixRunnable implements Runnable {
 
         private final Player p;
 
         public GodItemFixRunnable(Player p) {
             this.p = p;
         }
 
         @Override
         public void run() {
             removeGodEnchants(p);
         }
     }
 }
