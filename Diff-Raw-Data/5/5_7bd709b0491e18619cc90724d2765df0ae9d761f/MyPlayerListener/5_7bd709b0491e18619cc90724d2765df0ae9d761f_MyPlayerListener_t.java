 package me.ceramictitan.simplyjesus.Listeners;
 
 import me.ceramictitan.simplyjesus.User.User;
 
 import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 public class MyPlayerListener implements Listener {
 
 	@EventHandler
 	public void onEntityDamage(EntityDamageEvent event) {
 		if (event.getEntity() instanceof Player) {
 			final Player player = ((Player) event.getEntity());
 			if (User.godlist.contains(player.getName())) {
 				event.setCancelled(true);
 				player.playEffect(EntityEffect.HURT);
				if (player.getGameMode() == GameMode.CREATIVE) {
					return;
				}
 			}
 		}
 	}
 
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		Player p = event.getPlayer();
 		if (User.godlist.contains(p.getName())) {
 			User.godlist.remove(p.getName());
 		}
 	}
 }
