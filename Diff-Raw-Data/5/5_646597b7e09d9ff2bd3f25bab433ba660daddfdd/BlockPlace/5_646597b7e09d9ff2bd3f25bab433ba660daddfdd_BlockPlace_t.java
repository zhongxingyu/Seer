 package tk.nekotech.war.events;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
import org.bukkit.entity.TNTPrimed;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import tk.nekotech.war.War;
 
 public class BlockPlace implements Listener {
 	private War war;
 	
 	public BlockPlace(War war) {
 		this.war = war;
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onBlockPlace(BlockPlaceEvent event) {
 		if (war.getConfig().getBoolean("ready-to-go")) {
 			if (event.getBlockPlaced().getType().equals(Material.TNT)) {
 				if (!event.getBlockAgainst().getType().equals(Material.OBSIDIAN)) {
 					event.setCancelled(true);
 					war.sendMessage(event.getPlayer(), ChatColor.RED + "TNT can only be placed on OBSIDIAN!");
 				} else {
 					event.setCancelled(true);
					event.getBlock().getLocation().getWorld().spawn(event.getBlock().getLocation(), TNTPrimed.class);
 				}
 			} else {
 				event.setCancelled(true);
 				war.sendMessage(event.getPlayer(), ChatColor.RED + "You can't place blocks!");
 			}
 		}
 	}
 
 }
