 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.celestiusmc.plasmamachinegun;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 /**
  * Plasma Machine Gun Listener.
  */
 public class PMGListener implements Listener {
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         
         if (!player.hasPermission("plasmamachinegun.use")) {
             return;
         }
         
         ItemStack item = event.getItem();
         
         if (!item.getType().equals(Material.BLAZE_ROD)) {
             return;
         }
         
         double speed = 20.0;
        Vector direction = player.getEyeLocation().getDirection().normalize();
         
         Fireball fb = player.launchProjectile(Fireball.class);
        fb.teleport(player.getEyeLocation().add(direction));
         fb.setYield(0f);
         fb.setIsIncendiary(false);
         
         fb.setDirection(direction);
         fb.setVelocity(direction.multiply(speed));
     }
 
 }
