 package net.milkycraft.Listeners;
 
 import java.util.List;
 
 import net.milkycraft.Spawnegg;
 
import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.EnderCrystal;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class ThrowListener implements Listener {
 	Spawnegg plugin;	
 	public ThrowListener(Spawnegg instance) {
 		plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.HIGH)
 	public void OnThrow(PlayerInteractEvent e) {
 		final Player player = e.getPlayer();
 		ChatColor green = ChatColor.GREEN;
 		ChatColor red = ChatColor.RED;
 		if (e.getItem() == null) {
 			return;
 		}							
 		List<String> worldz = plugin.getConfig().getStringList(
 				"World.Worldname");
 		for (String worldname : worldz) {
 			if (e.getPlayer().getWorld().getName().equals(worldname)) {
 				if (e.getAction() == Action.RIGHT_CLICK_BLOCK || 
 						e.getAction() == Action.RIGHT_CLICK_AIR) {									
 					 if (e.getItem().getTypeId() == 384) {
 							if (plugin.getConfig().getBoolean(
 									"block.Throw.XpBottles")
 									&& !player
 											.hasPermission("entitymanager.xpbottles")) {
 								e.setCancelled(true);
 								alert(player, e);
 								player.sendMessage(green
 										+ "[EM]"
 										+ red
 										+ " You dont have permission to throw xp bottles");
 								return;
 							}
 						}
 					else if (e.getItem().getTypeId() == 385) {
 							if (plugin.getConfig().getBoolean(
 									"block.Throw.FireCharges")
 									&& !player
 											.hasPermission("entitymanager.firecharges")) {
 								e.setCancelled(true);
 								alert(player, e);
 								player.sendMessage(green
 										+ "[EM]"
 										+ red
 										+ " You dont have permission to throw fire charges");
 								return;
 							}
 						}
 					else if (e.getItem().getTypeId() == 344) {
 							if (plugin.getConfig().getBoolean(
 									"block.Throw.ChickenEggs")
 									&& !player
 											.hasPermission("entitymanager.chickeneggs")) {
 								e.setCancelled(true);
 								alert(player, e);
 								player.sendMessage(green
 										+ "[EM]"
 										+ red
 										+ " You dont have permission to throw eggs");
 								return;
 							}
 						}
 					else if (e.getItem().getTypeId() == 368) {
 							if (plugin.getConfig().getBoolean(
 									"block.Throw.EnderPearls")
 									&& !player
 											.hasPermission("entitymanager.enderpearl")) {
 								e.setCancelled(true);
 								alert(player, e);
 								player.sendMessage(green
 										+ "[EM]"
 										+ red
 										+ " You dont have permission to use Ender Pearls");
 								return;
 							}
 						}
 					else if (e.getItem().getTypeId() == 381) {
 							if (plugin.getConfig().getBoolean(
 									"block.Throw.EnderEyes")
 									&& !player
 											.hasPermission("entitymanager.endereye")) {
 								e.setCancelled(true);
 								alert(player, e);
 								player.sendMessage(green
 										+ "[EM]"
 										+ red
 										+ " You dont have permission to use Ender Eyes");
 								return;
 							}
 						}
 					else if (e.getItem().getTypeId() == 333) {
 							if (plugin.getConfig().getBoolean(
 									"block.Entities.Boats")
 									&& !player
 											.hasPermission("entitymanager.boat")) {
 								e.setCancelled(true);
 								alert(player, e);
 								player.sendMessage(green
 										+ "[EM]"
 										+ red
 										+ " You dont have permission to use Boats");
 								return;
 							}
 						}
 					else if (e.getItem().getTypeId() == 328) {
 							if (plugin.getConfig().getBoolean(
 									"block.Entities.Minecarts")
 									&& !player
 											.hasPermission("entitymanager.minecart")) {
 								e.setCancelled(true);
 								alert(player, e);
 								player.sendMessage(green
 										+ "[EM]"
 										+ red
 										+ " You dont have permission to use Minecarts");
 								return;
 							}
 					}
 					 if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
 						 Location loc = e.getClickedBlock().getLocation();	
 					if (e.getItem().getTypeId() == 383) {
 							if (e.getItem().getDurability() == 200 && e.getPlayer().hasPermission("entitymanager.crystal")) {							
 								e.getClickedBlock().getWorld()
 										.spawn(loc, EnderCrystal.class);
 								return;
 							}
 							}
 						}
 				}
 			}
 		}
 	}
 						public void alert(Player player, PlayerInteractEvent e) {
 								Bukkit.broadcast(ChatColor.GREEN + "[EM] " + ChatColor.DARK_RED
 										+ e.getPlayer().getDisplayName() + " Tryed to throw a "
 										+ ChatColor.GOLD + e.getItem().getType() + ".",
										"entitymanager.admin");										
 								return;
 							}
 						}
