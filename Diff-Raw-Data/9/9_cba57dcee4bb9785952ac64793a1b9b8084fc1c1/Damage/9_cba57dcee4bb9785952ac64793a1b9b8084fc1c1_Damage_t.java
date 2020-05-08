 package pro.homiecraft;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import pro.homiecraft.Commands.Resources.damageMap;
 
 public class Damage implements Listener {
 	@EventHandler
 	public void onDamage(EntityDamageByEntityEvent event){
 		Entity player = event.getEntity();
 		
 		if(damageMap.dmg.containsKey(player)){
 			if(event.getDamager() instanceof Entity){
 				event.setCancelled(true);
 			}
 			if(event.getDamager() instanceof Monster){
 				event.setCancelled(true);
 			}
 		}
 	}
 	@EventHandler
 	public void onBlockDamage(EntityDamageEvent event){
		Entity player = event.getEntity();
		
 		event.getCause();
		if(damageMap.dmg.containsKey(player)){
			if(event.getCause() == DamageCause.FALL){
				event.setCancelled(true);
			}
 		}
 	}
 }
