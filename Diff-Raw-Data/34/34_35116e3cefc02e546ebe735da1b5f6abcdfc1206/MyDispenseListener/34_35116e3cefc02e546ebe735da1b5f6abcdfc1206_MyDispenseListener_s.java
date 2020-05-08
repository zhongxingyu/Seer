 package net.milkycraft.Listeners;
 
 import java.util.List;
 
 import net.milkycraft.Spawnegg;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class MyDispenseListener implements Listener {
 	Spawnegg plugin;
 
 	public MyDispenseListener(Spawnegg instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onDispense(BlockDispenseEvent event) {
 		ItemStack item = event.getItem();
 		List<String> worldz = plugin.getConfig().getStringList(
 				"World.Worldname");
 		for (String worldname : worldz) {
 			if (event.getBlock().getWorld().getName().equals(worldname)) {
 				if (event.getItem().getTypeId() == 383) {
 					if (plugin.getConfig().getBoolean(
 							"block.Dispense.SpawnerEggs")) {
 						alert(event);
 						event.setCancelled(true);
 						return;
 					}
 				}
				if (item.getTypeId() == 344) {
 					if (plugin.getConfig().getBoolean(
 							"block.Dispense.ChickenEggs")) {
 						alert(event);
 						event.setCancelled(true);
 						return;
 					}
 				}
				if (item.getTypeId() == 385) {
 					if (plugin.getConfig().getBoolean(
 							"block.Dispense.FireBalls")) {
 						alert(event);
 						event.setCancelled(true);
 						return;
 					}
 				}
				if (item.getTypeId() == 384) {
 					if (plugin.getConfig().getBoolean(
 							"block.Dispense.XpBottles")) {
 						alert(event);
 						event.setCancelled(true);
 						return;
 					}				
 				}
 			}
 		}
 	}
	@EventHandler(priority = EventPriority.HIGH)
 	public void alert(BlockDispenseEvent event) {
		/*boolean alertr = plugin.getConfig().getBoolean("send.alerts");
 		double x = event.getBlock().getLocation().getX();
 		double y = event.getBlock().getLocation().getY();
 		double z = event.getBlock().getLocation().getZ();
 		int xx = (int) x;
 		int yy = (int) y;
 		int zz = (int) z;
		if (alertr) {
			Bukkit.broadcast(ChatColor.GREEN + "[EM] " + ChatColor.DARK_RED
 					+ "Failed Dispense of: " + ChatColor.GOLD
 					+ event.getItem().getType() + ChatColor.DARK_RED + " in: "
 					+ ChatColor.YELLOW + event.getBlock().getWorld().getName()
					+ " at: " + xx + " , " + yy + " , " + zz + ".",
					"entitymanager.admin");
					*/
			return;
		}
 	}
 
