 package com.ayan4m1.multiarrow;
 
 import java.util.List;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 
 import com.ayan4m1.multiarrow.arrows.ArrowType;
 
 /**
  * MultiArrow entity listener
  * @author ayan4m1
  */
 public class MultiArrowEntityListener extends EntityListener {
 	private MultiArrow plugin;
 
 	public MultiArrowEntityListener(MultiArrow instance) {
 		plugin = instance;
 	}
 
 	public void onProjectileHit(ProjectileHitEvent event) {
 		if (event.getEntity() instanceof Arrow) {
 			Arrow arrow = (Arrow)event.getEntity();
 			ArrowType arrowType = plugin.activeArrowType.get(((Player)arrow.getShooter()).getName());
 			List<Entity> entities = arrow.getNearbyEntities(1D, 1D, 1D);
 			if (entities.size() == 0) {
 				if (plugin.activeArrowEffect.containsKey(arrow)) {
 					if (plugin.chargeFee((Player)arrow.getShooter(), arrowType)) {
 						plugin.activeArrowEffect.get(arrow).hitGround(arrow);
 						plugin.activeArrowEffect.remove(arrow);
 					}
 					if (plugin.config.getArrowRemove(arrowType)) {
 						arrow.remove();
 					}
 				}
 			}
 		}
 	}
 
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (event instanceof EntityDamageByProjectileEvent) {
 			EntityDamageByProjectileEvent dpe = ((EntityDamageByProjectileEvent)event);
 			if (dpe.getProjectile() instanceof Arrow) {
 				Arrow arrow = (Arrow)dpe.getProjectile();
 				ArrowType arrowType = plugin.activeArrowType.get(((Player)arrow.getShooter()).getName());
 				if (plugin.activeArrowEffect.containsKey(arrow)) {
 					event.setCancelled(true);
 					if (plugin.chargeFee((Player)arrow.getShooter(), arrowType)) {
 						plugin.activeArrowEffect.get(arrow).hitEntity(arrow, event.getEntity());
 						plugin.activeArrowEffect.remove(arrow);
 					}
 					if (plugin.config.getArrowRemove(arrowType)) {
 						arrow.remove();
 					}
 				}
 			}
 		}
 	}
 }
