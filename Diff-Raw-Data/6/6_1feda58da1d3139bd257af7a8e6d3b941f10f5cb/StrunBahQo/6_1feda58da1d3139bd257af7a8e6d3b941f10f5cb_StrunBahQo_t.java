 package com.reilaos.bukkit.TheThuum.shouts;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.reilaos.bukkit.TheThuum.Plugin;
 
 
 public class StrunBahQo implements Shout {
 	private String[] words = {"strun", "bah", "qo", "Storm Call", "Summons a storm that hits things with lightning."};
 	
 	Random RNG = new Random();
 
 	@Override
 	public String[] words() {
 		return words;
 	}
 
 	@Override
 	public void shout(Player dovahkiin, int level) {
 		int duration = 60 * 20 * level;
 		new lightningStorm(duration, dovahkiin, Plugin.thisOne, Plugin.thisOne.getServer().getScheduler());
 
 		dovahkiin.getWorld().setStorm(true);
 		dovahkiin.getWorld().setThundering(true);
 	}
 	
 	private class lightningStorm implements Runnable {
 		int duration;
 		Player dovahkiin;
 		JavaPlugin plugin;
 		BukkitScheduler scheduler;
 		
 		boolean stormBefore;
 		boolean thunderBefore;
 		
 		public lightningStorm (int duration, Player dovahkiin, JavaPlugin plugin, BukkitScheduler scheduler){
 			this.dovahkiin = dovahkiin;
 			this.duration = duration;
 			this.plugin = plugin;
 			this.scheduler = scheduler;
 			
 			stormBefore = dovahkiin.getWorld().hasStorm();
 			thunderBefore = dovahkiin.getWorld().isThundering();
 			
 			schedule();			
 		}
 		
 		@Override
 		public void run() {
 			// Pick something within 100 ft (~ 30.5m) of dovahkiin, strike with lightning
 			List<Entity> zapPool = dovahkiin.getNearbyEntities(30.5, 30.5, 30.5);
 			Entity zapMe;
 			if (zapPool.size() > 0) {
				int tries = 0;
 				do {
 					zapMe = zapPool.get(RNG.nextInt(zapPool.size()));
					tries++;
				} while (!(zapMe instanceof LivingEntity && !zapMe.isDead()) &&
						 tries < zapPool.size() * 2);
 				
 				zapMe.getWorld().strikeLightning(zapMe.getLocation());
 			}
 			
 			// Set yourself to 3-6 seconds later, unless you've already gone on too long			
 			if (duration > 0) {
 				schedule();
 			} else {
 				dovahkiin.getWorld().setStorm(stormBefore);
 				dovahkiin.getWorld().setThundering(thunderBefore);
 			}
 		}
 		
 		private void schedule() {
 			int delay = 60 + RNG.nextInt(60);
 			duration -= delay;
 			scheduler.scheduleSyncDelayedTask(plugin, this, delay);
 		}
 	}
 }
