 package net.gamesketch.bukkit.bot;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class playerListener extends PlayerListener {
 	
 	public void onPlayerMove(PlayerMoveEvent event) {
 		/*
 		 * Time freezer.
 		 */
 		if (GSGeneral.isTimeFrozen) {
 			if (GSGeneral.enablefreezetime) { 
 				if (event.getPlayer().getWorld().getTime() >= 4000 || event.getPlayer().getWorld().getTime() < 3000)  {
 					event.getPlayer().getWorld().setTime(3000);
 				}
 			}
 		}
 		/*
 		 * Underwater breathing.
 		 */
 		if (GSGeneral.enablewaterhelmet) { 
 			int air = event.getPlayer().getRemainingAir();
 			int maxair = event.getPlayer().getMaximumAir();
 			if (air < maxair) {
 				ItemStack helmet = event.getPlayer().getInventory().getHelmet();
 				ItemStack glass = new ItemStack(20);
 				if (helmet.getType().equals(glass.getType())) {
 					event.getPlayer().setRemainingAir(maxair);
 				}
 			}
 		}
 	}
 	/*
 	 * Non-ops infinite item prevention.
 	 */
 	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
 		if (event.isCancelled()) { return; }
 		if (event.getItem().getItemStack().getAmount() < 0) {
 			if (!event.getPlayer().isOp()) {
 				event.setCancelled(true);
 				
 			}
 		}
 	}
 	
 	/*
 	 * 
 	 * Halting the timefrozen timer if everybody left of the server.
 	 */
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		if (event.getPlayer().getServer().getOnlinePlayers().length < 1) {
 			if (GSGeneral.isTimeFrozen) {
 				GSGeneral.isTimeFrozen = !GSGeneral.isTimeFrozen;
 			}
 		}
 		
 	}
 	
 	/*
 	 * GS Motd
 	 */
    public void onPlayerJoin(PlayerJoinEvent event) {
         final Player player = event.getPlayer();
         final Timer timer = new Timer();
         timer.schedule(
         	new TimerTask() {
         		public void run() {
         			player.performCommand("motd");
         			timer.cancel();
         		}
         	}
         , 2500);
 
     }
 }
