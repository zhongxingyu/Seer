 package bitlegend.legendutils.Listeners;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.entity.Snowman;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 import bitlegend.legendutils.LegendUtils;
 
 public class LUEntityListener extends EntityListener {
 	private final LegendUtils plugin;
 
 	public LUEntityListener(LegendUtils instance) {
 		plugin = instance;
 	}
 	
 	public void onEntityDeath(EntityDeathEvent event) {
 		try {
 			//Entity killer = ((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager();
 			double modifier = plugin.config.readDouble("XP_Modifier");
 			int xp = 0;
 			if (event.getEntity() instanceof Player) {
 				Player p = (Player)event.getEntity();
 				int playerxp = p.getExperience();
 				xp = (int)(playerxp * 0.1);
 			} else {
 				xp = event.getDroppedExp();
 			}
 			event.setDroppedExp((int)(xp * modifier));
 		} catch (Exception e) {
 			//
 		}
 	}
 	
 	@Override
 	public void onEntityDamage(EntityDamageEvent event){
 		try {
 			if(event instanceof EntityDamageByEntityEvent){
 				Entity damagesource = ((EntityDamageByEntityEvent)event).getDamager();
 				Entity damagetarget = ((EntityDamageByEntityEvent)event).getEntity();
 				if(damagesource instanceof Snowball && damagetarget instanceof Monster && ((Snowball)damagesource).getShooter() instanceof Snowman){
 					event.setDamage(1);
 				}
 			}
 			
 		} catch (Exception e) {
			System.out.println("OnEntityDamage error! : " + e.getMessage());
 		}
 	}
 }
