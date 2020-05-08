 package com.minecraftserver.baseraider;
 
 import java.util.List;
 import java.util.UUID;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
 
import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.entity.EntityShootBowEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BaseRaider extends JavaPlugin implements Listener {
     // explodes and destroyes obby
     public static void explode(Block block) {
         Location loc = block.getLocation();
         int radius = 1;
         // 1F=0.25 TNT explosion; 4F=normal TNT strength
         loc.getWorld().createExplosion(loc, 1.5F);
         // removal of obby, radius of 3
         for (int x = -radius; x <= radius; x++)
             for (int y = -radius; y <= radius; y++)
                 for (int z = -radius; z <= radius; z++) {
                     Location targetLoc = new Location(loc.getWorld(),
                             loc.getX() + x, loc.getY() + y, loc.getZ() + z);
                     if (loc.distance(targetLoc) <= 3.0D
                             && targetLoc.getBlock().getType() == Material.OBSIDIAN) {
                         if (!PreciousStones.API().isPStone(targetLoc))
                             targetLoc.getBlock().setTypeId(0);
                     }
                 }
     }
 
     // removes floating liquid
     private static void removeFloatingLiquid(Block block) {
         Location loc = block.getLocation();
         int radius = 8;
         // removal of flowing water, radius of 4
         for (int x = -radius; x <= radius; x++)
             for (int y = -radius; y <= radius; y++)
                 for (int z = -radius; z <= radius; z++) {
                     Location targetLoc = new Location(loc.getWorld(),
                             loc.getX() + x, loc.getY() + y, loc.getZ() + z);
                     if (loc.distance(targetLoc) <= 3.0D
                             && ((targetLoc.getBlock().getType() == Material.STATIONARY_WATER
                                     || targetLoc.getBlock().getType() == Material.STATIONARY_LAVA
                                     || targetLoc.getBlock().getType() == Material.WATER || targetLoc
                                     .getBlock().getType() == Material.LAVA) && targetLoc
                                     .getBlock().getData() > 0)) {
                         targetLoc.getBlock().setTypeId(0);
                     }
                 }
     }
 
     // removes stationary liquid
     private static void removeStationaryLiquid(Block block) {
         Location loc = block.getLocation();
         int radius = 6;
         // removal of source water, radius of 3
         for (int x = -radius; x <= radius; x++)
             for (int y = -radius; y <= radius; y++)
                 for (int z = -radius; z <= radius; z++) {
                     Location targetLoc = new Location(loc.getWorld(),
                             loc.getX() + x, loc.getY() + y, loc.getZ() + z);
                     if (loc.distance(targetLoc) <= 3.0D
                             && ((targetLoc.getBlock().getType() == Material.STATIONARY_WATER
                                     || targetLoc.getBlock().getType() == Material.STATIONARY_LAVA
                                     || targetLoc.getBlock().getType() == Material.WATER || targetLoc
                                     .getBlock().getType() == Material.LAVA) && targetLoc
                                     .getBlock().getData() == 0)) {
                         targetLoc.getBlock().setTypeId(0);
                     }
                 }
     }
 
     Logger      log            = Logger.getLogger("Minecraft");
 
     Recipes     recipes        = new Recipes();
 
     List<UUID>  lvlOneArrows   = new Vector<>();
 
     List<UUID>  lvlTwoArrows   = new Vector<>();
 
     private int fireballflying = 0;
 
     @EventHandler
     public void onArrowHit(ProjectileHitEvent event) {
         if (event.getEntityType() == EntityType.ARROW) {
             Arrow arrow = (Arrow) event.getEntity();
             if (this.lvlOneArrows.contains(arrow.getUniqueId())) {
                 Block hitBlock = arrow.getLocation().getBlock();
                 hitBlock.getWorld().createExplosion(arrow.getLocation(), 0.1F);
                 removeFloatingLiquid(hitBlock);
                 this.lvlOneArrows.remove(arrow.getUniqueId());
             } else if (this.lvlTwoArrows.contains(arrow.getUniqueId())) {
                 Block hitBlock = arrow.getLocation().getBlock();
                 hitBlock.getWorld().createExplosion(arrow.getLocation(), 0.1F);
                 removeStationaryLiquid(hitBlock);
                 this.lvlTwoArrows.remove(arrow.getUniqueId());
             }
         }
     }
 
     @Override
     public void onDisable() {
         this.log.info("[BaseRaider] System Disabled.");
     }
 
     @EventHandler
     public void onDispenceFireball(BlockDispenseEvent event) {
         if (event.isCancelled()) return;
         if (event.getItem().getType() == Material.FIREBALL
                && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.RESET + "Obby killer")) {
            event.getItem().getItemMeta().getDisplayName().equals(ChatColor.RESET + "Obby killer");
             this.fireballflying++;
         }
     }
 
     @Override
     public void onEnable() {
         Recipes.addRecipes();
         com.minecraftserver.baseraider.Listener listener = new com.minecraftserver.baseraider.Listener(
                 this);
         getServer().getPluginManager().registerEvents(this, this);
         getServer().getPluginManager().registerEvents(listener, this);
         this.log.info("[BaseRaider] System Enabled.");
     }
 
     @EventHandler
     public void onFireBallIgnites(BlockIgniteEvent event) {
         if (event.isCancelled()) return;
         if (event.getPlayer() == null && this.fireballflying > 0) {
             this.fireballflying--;
             explode(event.getBlock());
         }
     }
 
     @EventHandler
     public void shootArrow(EntityShootBowEvent event) {
         if (event.isCancelled()) return;
         if (event.getBow().getEnchantmentLevel(Enchantment.WATER_WORKER) == 1) {
             if (event.getBow() != null) {
                 Player player = (Player) event.getEntity();
 
                 if (player.getItemInHand().getType() == Material.BOW) {
                     if (!player.hasPermission("baseraider.bow.cankeepbow1")) {
                         player.getInventory().remove(player.getItemInHand());
                     }
                     this.lvlOneArrows.add(event.getProjectile().getUniqueId());
                 }
             }
         } else if (event.getBow().getEnchantmentLevel(Enchantment.WATER_WORKER) == 2) {
             if (event.getBow() != null) {
                 Player player = (Player) event.getEntity();
                 if (player.getItemInHand().getType() == Material.BOW) {
                     if (!player.hasPermission("baseraider.bow.cankeepbow2")) {
                         player.getInventory().remove(player.getItemInHand());
                     }
                     this.lvlTwoArrows.add(event.getProjectile().getUniqueId());
                 }
             }
         }
     }
 
 }
