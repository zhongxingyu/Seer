 package me.menexia.dynafish;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class DFEntityListener implements Listener {
 	private final DynaFish plugin;
 	private static Random random = new Random();
 	public DFEntityListener(DynaFish instance) {
 		plugin = instance;
 	}
 
	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
 	public void on46_Explosion(final EntityExplodeEvent event) {
 			if (DynaFish.ENABLED_FOR_ALL == true) {
 	Entity ent = event.getEntity();
 	if (ent instanceof org.bukkit.entity.TNTPrimed && (ent.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.UP).isLiquid())) {
 		if (DynaFish.OVERALL_CHANCE > 0) {
 			Location sym = ent.getLocation();
 			World inull = sym.getWorld();
 			// overall chance of dropping something is true
 			if (random.nextInt(100) + 1 <= DynaFish.OVERALL_CHANCE) {
 				double x = sym.getX() + random.nextInt(5) - 2;
 				double y = sym.getY() + 1;
 				double z = sym.getZ() + random.nextInt(5) - 2;
 				for (int a=0; a<DynaFish.AMOUNT_TO_DROP; a++) {
 					if (random.nextInt(100) + 1 >= (DynaFish.CHANCE_PER_DROP)) continue;
 					inull.dropItemNaturally(new Location(inull, x, y, z), new ItemStack(349, 1));
 				}
 			}
 		}
 	}
 	return;
 			} else if (DynaFish.ENABLED_FOR_ALL == false) {
 			for(Player pl: plugin.user) {
 				if (plugin.user.contains(pl)) {
 Entity ent = event.getEntity();
 if (ent instanceof org.bukkit.entity.TNTPrimed && (ent.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.UP).isLiquid())) {
 	if (DynaFish.OVERALL_CHANCE > 0) {
 		Location sym = ent.getLocation();
 		World inull = sym.getWorld();
 		// overall chance of dropping something is true
 		if (random.nextInt(100) + 1 <= DynaFish.OVERALL_CHANCE) {
 			double x = sym.getX() + random.nextInt(5) - 2;
 			double y = sym.getY() + 1;
 			double z = sym.getZ() + random.nextInt(5) - 2;
 			for (int a=0; a<DynaFish.AMOUNT_TO_DROP; a++) {
 				if (random.nextInt(100) + 1 <= DynaFish.CHANCE_PER_DROP) continue;
 				inull.dropItemNaturally(new Location(inull, x, y, z), new ItemStack(349, 1));
 			}
 		}
 	}
 }
 				}
 			}
 			return;
 	}
 }
 }
