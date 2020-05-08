 package me.furt.craftworlds.listeners;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.furt.craftworlds.CraftWorlds;
 import me.furt.craftworlds.Teleport;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 public class CWPlayerListener implements Listener {
 	CraftWorlds plugin;
 
 	public CWPlayerListener(CraftWorlds instance) {
 		this.plugin = instance;
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerChat(PlayerChatEvent event) {
 		Player player = event.getPlayer();
 		String format = event.getFormat();
 		String world = player.getWorld().getName();
 		event.setFormat("[" + ChatColor.GOLD + world + ChatColor.WHITE + "]"
 				+ format);
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerMove(PlayerMoveEvent event) {
 		Player player = event.getPlayer();
 		Location l = player.getLocation();
 		Block b = player.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(),
 				l.getBlockZ());
 		List<Sign> s = new ArrayList<Sign>();
 		if (b.getType().equals(Material.PORTAL)) {
 			for (int x = -2; x <= 2; x++) {
 				for (int y = -1; y <= 3; y++) {
 					for (int z = -2; z <= 2; z++) {
 						BlockState block = b.getRelative(x, y, z).getState();
 						if (block.getType() == Material.WALL_SIGN) {
 							s.add((Sign) block);
 						}
 					}
 				}
 			}
 
 			if (s.size() != 0) {
 				for (int i = 0; i < s.size(); i++) {
 					Sign sign = (Sign) s.get(i);
 					if (sign.getLine(1).length() <= 0)
 						continue;
 					World world = plugin.getServer().getWorld(
 							sign.getLine(1).toString());
 					if (world != null) {
 						if (sign.getLine(2).equalsIgnoreCase("spawn")) {
 							Location sl = new Teleport(plugin)
 									.getDestination(world.getSpawnLocation());
 							if (plugin.hasPerm(player, "craftworlds."
 									+ world.getName().toLowerCase(), false)) {
 								event.setTo(sl);
 								// player.teleport(sl);
 							} else {
 								player.sendMessage("You do not have permission to use that portal.");
 								return;
 							}
 						}
 
 					}
 				}
 			}
 		}
 	}
 }
