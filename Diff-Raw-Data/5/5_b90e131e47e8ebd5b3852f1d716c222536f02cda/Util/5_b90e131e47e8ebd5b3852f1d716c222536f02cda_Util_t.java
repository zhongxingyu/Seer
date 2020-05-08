 package team.GunsPlus.Util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.getspout.spoutapi.sound.SoundManager;
 
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.Block.Tripod;
 import team.GunsPlus.Block.TripodData;
 import team.GunsPlus.Enum.EffectSection;
 import team.GunsPlus.Enum.EffectType;
 import team.GunsPlus.Item.Addition;
 import team.GunsPlus.Item.Ammo;
 import team.GunsPlus.Item.Gun;
 import team.GunsPlus.Manager.ConfigLoader;
 
 public class Util {
 	
 	public static boolean containsCustomItems(List<ItemStack> items){
 		for(ItemStack i : items){
 			if(isCustomItem(i)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static boolean isCustomItem(ItemStack item){
 		return new SpoutItemStack(item).isCustomItem();
 	}
 	
 	public static Block getBlockInSight(Location l, int blockIndex, int maxradius){
 		BlockIterator bi = new BlockIterator(l.getWorld(), l.toVector(), l.getDirection(), 0d, maxradius);
 		Block b = null;
 		for(int i = 0; i<blockIndex; i++){
 			if(bi.hasNext()){
 				b =  bi.next(); 
 			}else break;
 		}
 		return b;
 	}
 
 	public static void warn(String msg) {
 		if (GunsPlus.warnings) {
 			GunsPlus.log.warning(GunsPlus.PRE + " " + msg);
 		}
 	}
 
 	public static void info(String msg) {
 		GunsPlus.log.info(GunsPlus.PRE + " " + msg);
 	}
 	
 	public static void debug(Exception e){
 		if(GunsPlus.debug){
 			GunsPlus.log.info(GunsPlus.PRE + "[Debug] " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public static Projectile launchProjectile(Class<? extends Projectile> c,
 			Location from, Location to, float speed) {
 		Projectile e = from.getWorld().spawn(from, c);
 		e.setVelocity(to.toVector().multiply(speed));
 		Bukkit.getPluginManager().callEvent(new ProjectileLaunchEvent(e));
 		return e;
 	}
 
 	public static boolean enteredTripod(SpoutPlayer sp) {
 		for (TripodData td : GunsPlus.allTripodBlocks) {
 			if (td.getOwner() == null)
 				continue;
 			if (td.getOwner().getPlayer().equals(sp) && td.isEntered()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static TripodData getTripodDataOfEntered(SpoutPlayer sp) {
 		for (TripodData td : GunsPlus.allTripodBlocks) {
 			if (td.getOwner() == null)
 				continue;
 			if (td.getOwner().getPlayer().equals(sp) && td.isEntered()) {
 				return td;
 			}
 		}
 		return null;
 	}
 
 	public static TripodData loadTripodData(Block b) {
 		for (TripodData td : GunsPlus.allTripodBlocks)
 			if (td.getLocation().getBlock().equals(b))
 				return td;
 		return null;
 	}
 
 	public static TripodData loadTripodData(Location location) {
 		return loadTripodData(location.getBlock());
 	}
 
 	public static boolean isTripod(Location l) {
 		for (TripodData td : GunsPlus.allTripodBlocks)
 			if (td.getLocation().toVector().equals(l.toVector()))
 				return true;
 		return false;
 	}
 
 	public static boolean isTripod(Block b) {
 		return isTripod(b.getLocation());
 	}
 
 	public static boolean canSee(final Location observer, final Location observed, int range) {
 		Location o = observer.clone();
 		Location w = observed.clone();
 		if(o.toVector().distance(w.toVector())>range) return false;
 		BlockIterator bitr = new BlockIterator(setLookingAt(o, w), 0, range);
 		while (bitr.hasNext()) {
 			Block b = bitr.next();
 			if(b.equals(w.getBlock())) return true;
 			if (!Util.isTransparent(b)) {
 				break;
 			}
 		}
 		return false;
 	}
 
 	public static Location getMiddle(Location l, float YShift) {
  		Location loc = l;
 		loc = loc.getBlock().getLocation();
 		Vector vec = loc.toVector();
 		vec.add(new Vector(0.5, YShift, 0.5));
 		loc = vec.toLocation(loc.getWorld());
 		return loc;
 	}
 
 	public static boolean isGunsPlusMaterial(String name) {
 		for (int j = 0; j < GunsPlus.allGuns.size(); j++) {
 			if (GunsPlus.allGuns.get(j).getName().equalsIgnoreCase(name))
 				return true;
 		}
 		for (int j = 0; j < GunsPlus.allAmmo.size(); j++) {
 			if (GunsPlus.allAmmo.get(j).getName().equalsIgnoreCase(name))
 				return true;
 		}
 		for (int j = 0; j < GunsPlus.allAdditions.size(); j++) {
 			if (GunsPlus.allAdditions.get(j).getName().equalsIgnoreCase(name))
 				return true;
 		}
		if(Tripod.tripodenabled&&GunsPlus.tripod.getName().equals(name))
 			return true;
 		return false;
 	}
 
 	public static Object getGunsPlusMaterial(String name) {
 		Object cm = null;
 		if(!isGunsPlusMaterial(name)) return cm;
 		for (int i = 0; i < GunsPlus.allGuns.size(); i++) {
 			if (GunsPlus.allGuns.get(i).getName().equalsIgnoreCase(name)) {
 				cm = GunsPlus.allGuns.get(i);
 				return cm;
 			}
 		}
 		for (int j = 0; j < GunsPlus.allAmmo.size(); j++) {
 			if (GunsPlus.allAmmo.get(j).getName().equalsIgnoreCase(name)) {
 				cm = GunsPlus.allAmmo.get(j);
 				return cm;
 			}
 		}
 		for (int j = 0; j < GunsPlus.allAdditions.size(); j++) {
 			if (GunsPlus.allAdditions.get(j).getName().equalsIgnoreCase(name)) {
 				cm = GunsPlus.allAdditions.get(j);
 				return cm;
 			}
 		}
		if(Tripod.tripodenabled&&name.equals(GunsPlus.tripod.getName()))
 			cm = GunsPlus.tripod;
 		return cm;
 	}
 	
 	public static void playCustomSound(GunsPlus plugin, Location l, String url,
 			int volume) {
 		SoundManager SM = SpoutManager.getSoundManager();
 		SM.playGlobalCustomSoundEffect(plugin, url, false, l, 40, volume);
 	}
 
 	public static boolean isTransparent(Block block) {
 		Material m = block.getType();
 		if (GunsPlus.transparentMaterials.contains(m)||isTripod(block)) {
 			return true;
 		}
 		return false;
 	}
 
 	public static List<Entity> getNearbyEntities(Location loc, double radiusX,
 			double radiusY, double radiusZ) {
 		Entity e = loc.getWorld().spawn(loc, ExperienceOrb.class);
 		@SuppressWarnings("unchecked")
 		List<Entity> entities = (List<Entity>) ((ArrayList<Entity>) e.getNearbyEntities(radiusX, radiusY, radiusZ)).clone();
 		e.remove();
 		return entities;
 	}
 
 	public static int getRandomInteger(int start, int end) {
 		Random rand = new Random();
 		return start + rand.nextInt(end + 1);
 	}
 
 	public static void printCustomIDs() {
 		if (ConfigLoader.generalConfig.getBoolean("id-info-guns", true)) {
 			GunsPlus.log.log(Level.INFO, GunsPlus.PRE
 					+ " ------------  ID's of the guns: -----------------");
 			if (GunsPlus.allGuns.isEmpty())
 				GunsPlus.log.log(Level.INFO, "EMPTY");
 			for (Gun gun : GunsPlus.allGuns) {
 				GunsPlus.log.log(Level.INFO, "ID of " + gun.getName() + ":"
 						+ new SpoutItemStack(gun).getTypeId() + ":"
 						+ new SpoutItemStack(gun).getDurability());
 			}
 		}
 		if (ConfigLoader.generalConfig.getBoolean("id-info-ammo", true)) {
 			GunsPlus.log.log(Level.INFO, GunsPlus.PRE
 					+ " ------------  ID's of the ammo: -----------------");
 			if (GunsPlus.allAmmo.isEmpty())
 				GunsPlus.log.log(Level.INFO, "EMPTY");
 			for (Ammo ammo : GunsPlus.allAmmo) {
 				GunsPlus.log.log(Level.INFO, "ID of " + ammo.getName() + ":"
 						+ new SpoutItemStack(ammo).getTypeId() + ":"
 						+ new SpoutItemStack(ammo).getDurability());
 			}
 		}
 		if (ConfigLoader.generalConfig.getBoolean("id-info-additions", true)) {
 			GunsPlus.log
 					.log(Level.INFO,
 							GunsPlus.PRE
 									+ " ------------  ID's of the additions: -----------------");
 			if (GunsPlus.allAdditions.isEmpty())
 				GunsPlus.log.log(Level.INFO, "EMPTY");
 			for (Addition add : GunsPlus.allAdditions) {
 				GunsPlus.log.log(Level.INFO, "ID of " + add.getName() + ":"
 						+ new SpoutItemStack(add).getTypeId() + ":"
 						+ new SpoutItemStack(add).getDurability());
 			}
 		}
 		if(Tripod.tripodenabled){
 			info(" ------------ loaded the tripod block --------------");
 			info(" ID: "+new SpoutItemStack(GunsPlus.tripod).getTypeId()+":"+new SpoutItemStack(GunsPlus.tripod).getDurability());
 		}
 	}
 
 	public static boolean isAllowedInEffectSection(EffectType efftyp,
 			EffectSection effsec) {
 		switch (effsec) {
 		case SHOOTER:
 			switch (efftyp) {
 			case EXPLOSION:
 				return false;
 			case LIGHTNING:
 				return false;
 			case SMOKE:
 				return false;
 			case FIRE:
 				return true;
 			case PUSH:
 				return true;
 			case DRAW:
 				return true;
 			case SPAWN:
 				return false;
 			case POTION:
 				return true;
 			case PLACE:
 				return false;
 			case BREAK:
 				return false;
 			}
 			break;
 		case SHOOTERLOCATION:
 			switch (efftyp) {
 			case EXPLOSION:
 				return true;
 			case LIGHTNING:
 				return true;
 			case SMOKE:
 				return true;
 			case FIRE:
 				return true;
 			case PUSH:
 				return false;
 			case DRAW:
 				return false;
 			case SPAWN:
 				return true;
 			case POTION:
 				return false;
 			case PLACE:
 				return true;
 			case BREAK:
 				return true;
 			}
 			break;
 		case TARGETLOCATION:
 			switch (efftyp) {
 			case EXPLOSION:
 				return true;
 			case LIGHTNING:
 				return true;
 			case SMOKE:
 				return true;
 			case FIRE:
 				return true;
 			case PUSH:
 				return false;
 			case DRAW:
 				return false;
 			case SPAWN:
 				return true;
 			case POTION:
 				return false;
 			case PLACE:
 				return true;
 			case BREAK:
 				return true;
 			}
 			break;
 		case TARGETENTITY:
 			switch (efftyp) {
 			case EXPLOSION:
 				return false;
 			case LIGHTNING:
 				return false;
 			case SMOKE:
 				return false;
 			case FIRE:
 				return true;
 			case PUSH:
 				return true;
 			case DRAW:
 				return true;
 			case SPAWN:
 				return false;
 			case POTION:
 				return true;
 			case PLACE:
 				return false;
 			case BREAK:
 				return false;
 			}
 			break;
 		case FLIGHTPATH:
 			switch (efftyp) {
 			case EXPLOSION:
 				return true;
 			case LIGHTNING:
 				return true;
 			case SMOKE:
 				return true;
 			case FIRE:
 				return true;
 			case PUSH:
 				return false;
 			case DRAW:
 				return false;
 			case SPAWN:
 				return true;
 			case POTION:
 				return false;
 			case PLACE:
 				return true;
 			case BREAK:
 				return true;
 			}
 			break;
 		case UNDEFINED:
 			return false;
 		}
 		return false;
 	}
 
 	public static Vector getDirection(Location l) {
 		Vector vector = new Vector();
 
 		double rotX = l.getYaw();
 		double rotY = l.getPitch();
 
 		vector.setY(-Math.sin(Math.toRadians(rotY)));
 
 		double h = Math.cos(Math.toRadians(rotY));
 
 		vector.setX(-h * Math.sin(Math.toRadians(rotX)));
 		vector.setZ(h * Math.cos(Math.toRadians(rotX)));
 
 		return vector;
 	}
 
 	public static Location setLookingAt(Location loc, Location lookat) {
 		loc = loc.clone();
 		double dx = lookat.getX() - loc.getX();
 		double dy = lookat.getY() - loc.getY();
 		double dz = lookat.getZ() - loc.getZ();
 
 		if (dx != 0) {
 			if (dx < 0) {
 				loc.setYaw((float) (1.5 * Math.PI));
 			} else {
 				loc.setYaw((float) (0.5 * Math.PI));
 			}
 			loc.setYaw((float) loc.getYaw() - (float) Math.atan(dz / dx));
 		} else if (dz < 0) {
 			loc.setYaw((float) Math.PI);
 		}
 		double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
 		loc.setPitch((float) -Math.atan(dy / dxz));
 		loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI);
 		loc.setPitch(loc.getPitch() * 180f / (float) Math.PI);
 
 		return loc;
 	}
 
 	public static Location getHandLocation(Player p) {
 		Location loc = p.getLocation().clone();
 
 		double a = loc.getYaw() / 180D * Math.PI + Math.PI / 2;
 		double l = Math.sqrt(0.8D * 0.8D + 0.4D * 0.4D);
 
 		loc.setX(loc.getX() + l * Math.cos(a) - 0.8D * Math.sin(a));
 		loc.setY(loc.getY() + p.getEyeHeight() - 0.2D);
 		loc.setZ(loc.getZ() + l * Math.sin(a) + 0.8D * Math.cos(a));
 		return loc;
 	}
 	
 	public static List<Block> getSphere(Location center, double radius) {
 		List<Block> blockList = new ArrayList<Block>();
 	    radius += 0.5;
 	    final double radSquare = Math.pow(2, radius);
 	    final int radCeil = (int) Math.ceil(radius);
 	    final double centerX = center.getX();
 	    final double centerY = center.getY();
 	    final double centerZ = center.getZ();
 	 
 	    for(double x = centerX - radCeil; x <= centerX + radCeil; x++) {
 	        for(double y = centerY - radCeil; y <= centerY + radCeil; y++) {
 	            for(double z = centerZ - radCeil; z <= centerZ + radCeil; z++) {
 	                double distSquare = Math.pow(2, x - centerX) + Math.pow(2,y - centerY) + Math.pow(2,z - centerZ);
 	                if (distSquare > radSquare)
 	                    continue;
 	                Location currPoint = new Location(center.getWorld(), x, y, z);
 	                blockList.add(currPoint.getBlock());
 	            }
 	        }
 	    }
 	    return blockList;
 	}
 
 	public static boolean isBlockAction(Action a) {
 		switch (a) {
 		case RIGHT_CLICK_BLOCK:
 			return true;
 		case LEFT_CLICK_BLOCK:
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean tntIsAllowedInRegion(Location loc) {
 		if (GunsPlus.wg != null) {
 			if (!GunsPlus.wg.getGlobalRegionManager().allows(DefaultFlag.TNT,
 					loc)) {
 				return false;
 			} else
 				return true;
 		} else
 			return true;
 	}
 }
