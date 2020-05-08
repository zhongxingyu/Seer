 package com.sparkedia.valrix.BackToBody;
 
 import org.bukkit.Location;
import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 public class BEntityListener extends EntityListener {
 	public BackToBody plugin;
 	
 	public BEntityListener(BackToBody plugin) {
 		this.plugin = plugin;
 	}
 
 	public void onEntityDeath(EntityDeathEvent e) {
 		Entity entity = e.getEntity();
 		if (entity instanceof Player) {
 			//get location and store it
			Location loc = entity.getLocation().getBlock().getFace(BlockFace.UP).getLocation();
 			String name = ((Player)entity).getName();
 			Property prop = new Property(plugin.getCanonFile(plugin.players+'/'+name+".loc"), plugin);
 			prop.setDouble("x", loc.getX());
 			prop.setDouble("y", loc.getY());
 			prop.setDouble("z", loc.getZ());
 			prop.setFloat("yaw", loc.getYaw());
 		}
 	}
 }
