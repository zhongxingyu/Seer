 package cc.co.vijfhoek.basics.listeners;
 
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.*;
 
 import cc.co.vijfhoek.basics.Basics;
 
 public class BasicsEntityListener extends EntityListener {
 	public void onEntityExplode(EntityExplodeEvent event) {
 		Basics basics = new Basics();
 		Entity entity = event.getEntity();
 		if(entity instanceof TNTPrimed) {
			if (basics.cConfig.getBoolean("prevent.block-damage.tnt", false)) {
 				event.setCancelled(true);
 			}
 		} else if(entity instanceof Creeper) {
			if (basics.cConfig.getBoolean("prevent.block-damage.creeper", false)) {
 				event.setCancelled(true);
 			}
 		}
 	}
 }
