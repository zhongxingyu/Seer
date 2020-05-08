 package tk.nekotech.war.listeners;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import tk.nekotech.war.War;
 
 public class ProjectileHit implements Listener {
 	private War war;
 	
 	public ProjectileHit(War war) {
 		this.war = war;
 	}
 	
 	@EventHandler
 	public void onProjectileHit(final ProjectileHitEvent event) {
 		if (event.getEntity() instanceof Arrow) {
 			war.arrows.add((Arrow) event.getEntity());
 		}
 		if (event.getEntity() instanceof Egg) {
 			Egg egg = (Egg) event.getEntity();
 			if (egg.getShooter() instanceof Player && war.medic.contains((Player) egg.getShooter())) {
 				egg.getLocation().getWorld().createExplosion(egg.getLocation(), 0);
				for (Entity entity : egg.getNearbyEntities(100D, 100D, 100D)) {
 					if (entity instanceof Player) {
						((Player) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 1));
 					}
 				}
 			}
 		}
 	}
 
 }
