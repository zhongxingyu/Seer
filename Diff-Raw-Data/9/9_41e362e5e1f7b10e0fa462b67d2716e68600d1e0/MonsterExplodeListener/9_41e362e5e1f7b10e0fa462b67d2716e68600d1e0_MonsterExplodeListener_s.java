 package org.monstercraft.explode.listeners;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.plugin.Plugin;
 import org.monstercraft.explode.MonsterExplode;
 import org.monstercraft.explode.util.Variables;
 
 import com.malikk.shield.Shield;
 import com.malikk.shield.ShieldAPI;
 
 public class MonsterExplodeListener extends MonsterExplode implements Listener {
 
 	private final Random ran = new Random();
 	private ShieldAPI api = null;
 	ArrayList<Location> locations = new ArrayList<Location>();
 
 	public MonsterExplodeListener(MonsterExplode plugin) {
 		Plugin x = plugin.getServer().getPluginManager().getPlugin("Shield");
 		if (x != null && x instanceof Shield) {
 			Shield shield = (Shield) x;
 			api = shield.getAPI();
 		}
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
 	public void onExplode(EntityExplodeEvent event) {
 		Location loc = event.getLocation();
 		if (Variables.prevent_block_damage) {
 			for (Location location : locations) {
 				if (location.getX() == loc.getX()
 						&& location.getY() == loc.getY()
 						&& loc.getZ() == location.getZ()
 						&& loc.getWorld() == location.getWorld()) {
 					event.blockList().clear();
 					locations.remove(location);
 					return;
 				}
 			}
 		}
 	}
 
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onEntityDeath(EntityDeathEvent event) {
 		int i = ran.nextInt(100);
 		Entity entity = event.getEntity();
 		if (!(entity instanceof Player)) {
 			return;
 		}
 		Player player = (Player) entity;
 		Location loc = player.getLocation();
 		if (!player.hasPermission("monsterexplode.explode")) {
 			return;
 		}
 		if (!Variables.enabled) {
 			player.sendMessage(ChatColor.RED
 					+ "Exploding on death is currently disabled.");
 			return;
 		}
		if (api != null) {
 			if (api.isInRegion(loc)) {
 				player.sendMessage(ChatColor.RED
 						+ "You could not explode withn the protection region!");
 				return;
 			}
 		}
 		if (i <= Variables.randomnessFactor) {
 			World world = player.getWorld();
 			if (Variables.prevent_block_damage) {
 				locations.add(loc);
 			}
 			world.createExplosion(loc, Variables.size, true);
 			player.sendMessage(ChatColor.RED + "You have exploded!");
		} else {

 		}
 	}
 }
