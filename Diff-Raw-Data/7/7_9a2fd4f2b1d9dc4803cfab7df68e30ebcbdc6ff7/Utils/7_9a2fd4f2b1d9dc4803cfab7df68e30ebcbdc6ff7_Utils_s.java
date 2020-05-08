 package com.nullblock.vemacs.safedims;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 
 public class Utils {
 	public static Location getSafeY(Location location) {
 		Location safe = location;
 		for (double height = location.getY(); height >= 0; height--) {
 			safe.setY(height);
			if ((!safe.getBlock().getType().equals(Material.AIR))
					&& (new Location(safe.getWorld(), safe.getX(),
 							safe.getY() + 1.8, safe.getZ()).getBlock()
							.getType().isTransparent())) {
 				location = safe;
 				break;
 			}
 		}
 		return location;
 	}
 
 	public static Boolean isDangerous(String worldname) {
 		Boolean bool = Boolean.valueOf(false);
 		List dangerous = Bukkit.getServer().getPluginManager()
 				.getPlugin("SafeDims").getConfig().getStringList("dangerous");
 		Iterator itr = dangerous.iterator();
 		while (itr.hasNext()) {
 			if (worldname.toLowerCase().contains((String) itr.next())) {
 				bool = Boolean.valueOf(true);
 			}
 		}
 		return bool;
 	}
 }
