 package no.tjnome.phfms;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 
 public class mainListner implements Listener {
 
	private final main plugin;

 	public mainListner(main plugin) {
		this.plugin = plugin;
 	}
 	
 	@EventHandler(priority=EventPriority.NORMAL, ignoreCancelled=true)
 	public void onCreatureSpawn(CreatureSpawnEvent event) {
 		boolean spawn = false;
 		
 		if (event.getSpawnReason() == SpawnReason.NATURAL) {
 			for (Player player : Bukkit.getOnlinePlayers()) {
 				if (event.getEntity().getLocation().distance(player.getLocation()) < 128) {
 					
 					// Here you have you check for if player should effect spawning of mobs. 
 					// I'm using sneaking as example.
 					
 					if (!player.isSneaking()) {
 						spawn = true;
 						break;
 					}
 					
 				}
 			}
 		} else if (event.getSpawnReason() == SpawnReason.NATURAL) {
 			for (Player player : Bukkit.getOnlinePlayers()) {
 				if (event.getEntity().getLocation().distance(player.getLocation()) < 16) {
 					
 					// Here you have you check for if player should effect spawning of mobs. 
 					// I'm using sneaking as example.
 					
 					if (!player.isSneaking()) {
 						spawn = true;
 						break;
 					}
 					
 				}
 			}
 		}
 		
 		if (!spawn) {
 			event.setCancelled(true);
 		}
 	}
 }
