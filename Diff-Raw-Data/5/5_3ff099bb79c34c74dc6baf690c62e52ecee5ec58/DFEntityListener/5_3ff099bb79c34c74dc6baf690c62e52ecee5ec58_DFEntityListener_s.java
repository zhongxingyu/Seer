 package me.menexia.dynafish;
 
 import java.util.Random;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.inventory.ItemStack;
 
 public class DFEntityListener extends EntityListener {
 	public static DynaFish plugin;
 	public DFEntityListener(DynaFish instance) {
 		plugin = instance;
 	}
 	
 	public void onEntityExplode(EntityExplodeEvent event) {
 			if (event.isCancelled()) return;
 			for(Player pl: plugin.user) {
 				if (plugin.user.contains(pl)) {
 Entity ent = event.getEntity();
 if (ent instanceof org.bukkit.entity.TNTPrimed && (ent.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.UP).isLiquid())) {
 	Random value = new Random();
 	int chance = value.nextInt(10);
 	if (chance < 1) {} else {
 Location sym = ent.getLocation();
 World shu = sym.getWorld();
 Random random = new Random();
 double x = sym.getX() + random.nextInt(5) - 2;
 double y = sym.getY() + 1;
 double z = sym.getZ() + random.nextInt(5) - 2;
 for (int a=0; a<33; a++) {
 	int p = random.nextInt(2);
 	if (p < 1) continue;
 	shu.dropItemNaturally(new Location(shu, x, y, z), new ItemStack(349, 1));
 } // end of for loop
 } // end of else statement
} // end of checks if in HashSet
 } // end of if TNTPrimed
} 
 } // end of onEntityExplode method
 }
