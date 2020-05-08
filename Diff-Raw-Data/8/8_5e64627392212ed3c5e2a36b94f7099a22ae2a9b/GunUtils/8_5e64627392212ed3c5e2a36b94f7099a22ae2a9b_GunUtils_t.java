 package team.GunsPlus.Util;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.Packet42RemoveMobEffect;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.SmallFireball;
 import org.bukkit.entity.Snowball;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.BlockIterator;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 
 import team.GunsPlus.API.Event.*;
 import team.GunsPlus.Enum.Effect;
 import team.GunsPlus.Enum.Projectile;
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.GunsPlusPlayer;
 import team.GunsPlus.Item.Addition;
 import team.GunsPlus.Item.Ammo;
 import team.GunsPlus.Item.Gun;
 
 public class GunUtils {
 
 	public static Gun getGun(String name) {
 		for (Gun g : GunsPlus.allGuns)
 			if (g.getName().equalsIgnoreCase(name))
 				return g;
 		return null;
 	}
 
 	public static String getFullGunName(Gun g, Addition... adds) {
 		String name = g.getName();
 		for (Addition a : adds)
 			name += "+" + a.getName();
 		return name;
 	}
 
 	public static String getRawGunName(Gun g) {
 		String name = g.getName();
 		return name.split("\\+")[0];
 	}
 
 	public static boolean holdsGun(SpoutPlayer p) {
 		ItemStack is = p.getItemInHand();
 		for (Gun g : GunsPlus.allGuns) {
 			SpoutItemStack sis = new SpoutItemStack(g);
 			if (is.getTypeId() == sis.getTypeId()
 					&& is.getDurability() == sis.getDurability()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static void shootProjectile(Location from, Location to, Projectile pro) {
 		float speed = (float) pro.getSpeed();
 		if (pro.equals(Projectile.ARROW)) {
 			Util.launchProjectile(Arrow.class, from, to, speed);
 		} else if (pro.equals(Projectile.FIREBALL)) {
 			Util.launchProjectile(Fireball.class, from, to, speed);
 		} else if (pro.equals(Projectile.SNOWBALL)) {
 			Util.launchProjectile(Snowball.class, from, to, speed);
 		} else if (pro.equals(Projectile.EGG)) {
 			Util.launchProjectile(Egg.class, from, to, speed);
 		} else if (pro.equals(Projectile.ENDERPEARL)) {
 			Util.launchProjectile(EnderPearl.class, from, to, speed);
 		} else if(pro.equals(Projectile.FIRECHARGE)) {
 			Util.launchProjectile(SmallFireball.class, from, to, speed); 
 		}
 	}
 
 	public static HashMap<LivingEntity, Integer> getTargets(Location l, Gun g,
 			boolean zoom) {
 		HashMap<LivingEntity, Integer> targets = new HashMap<LivingEntity, Integer>();
 		Location loc = l.clone();
 		HashMap<LivingEntity, Integer> e = null;
 		int acc = Util.getRandomInteger(0, 101) + 1;
 		int missing = (int) g.getValue(zoom ? "MISSING_IN" : "MISSING_OUT");
 		float randomfactor = (float) g.getValue("RANDOMFACTOR");
 		int spread = (int) (zoom ? (g.getValue("SPREAD_IN") / 2) + 1 : (g
 				.getValue("SPREAD_OUT") / 2 + 1));
 		if (acc >= missing) {
 			if (spread <= 0 && randomfactor <= 0) {
 				targets = getTargetEntities(l, g);
 			} else {
 				for (int i = 1; i <= spread; i += 4) {
 					loc = l.clone();
 					loc.setYaw(loc.getYaw()
 							+ Util.getRandomInteger(i,
 									Math.round(i * randomfactor)));
 					e = getTargetEntities(loc, g);
 					targets.putAll(e);
 					loc.setYaw(loc.getYaw()
 							- Util.getRandomInteger(i,
 									i + Math.round(i * randomfactor)));
 					e = getTargetEntities(loc, g);
 					targets.putAll(e);
 					loc.setPitch(loc.getPitch()
 							+ Util.getRandomInteger(i,
 									i + Math.round(i * randomfactor)));
 					e = getTargetEntities(loc, g);
 					targets.putAll(e);
 					loc.setPitch(loc.getPitch()
 							- Util.getRandomInteger(i,
 									i + Math.round(i * randomfactor)));
 					e = getTargetEntities(loc, g);
 					targets.putAll(e);
 				}
 			}
 		}
 		return targets;
 	}
 
 	public static HashMap<LivingEntity, Integer> getTargetEntities(
 			Location loc, Gun g) {
 		HashMap<LivingEntity, Integer> targets = new HashMap<LivingEntity, Integer>();
 		BlockIterator bitr = new BlockIterator(loc, 0d,
 				(int) g.getValue("RANGE"));
 		Block b;
 		Location l, el;
 		while (bitr.hasNext()) {
 			b = bitr.next();
 			Location blockcenter = Util.getMiddle(b.getLocation(), -0.5f);
 			Set<LivingEntity> entities = new HashSet<LivingEntity>();
 			if (!Util.isTransparent(b))
 				break;
 			for (Entity e : new ArrayList<Entity>(Util.getNearbyEntities(b.getLocation(), 0.4, 0.4,
 					0.4))) {
 				if (e instanceof LivingEntity) {
 					entities.add((LivingEntity) e);
 				}
 			}
 			for (LivingEntity e : entities) {
 				l = Util.getMiddle(e.getLocation(), -0.5f);
 				el = e.getEyeLocation();
 				double changedamage = (int) Math.ceil((float) g
 						.getValue("CHANGEDAMAGE")
 						* loc.toVector().distance(l.toVector()));
 				if (l.toVector().distance(blockcenter.toVector()) > el
 						.toVector().distance(blockcenter.toVector())) {
 					targets.put(
 							e,
 							(int) ((int) g.getValue("HEADSHOTDAMAGE") + changedamage<0?0:(int) g.getValue("HEADSHOTDAMAGE") + changedamage)
 									* -1);
 				} else {
 					targets.put(e,
 							(int) ((int) g.getValue("DAMAGE")<0?0:(int) g.getValue("DAMAGE") + changedamage));
 				}
 			}
 		}
 		return targets;
 	}
 
 	public static void removeAmmo(Inventory inv, ArrayList<ItemStack> ammo) {
 		if (ammo.isEmpty())
 			return;
 		HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 		ItemStack ammoStack = null;
 		for (ItemStack theStack : ammo) {
 			invAll = inv.all(theStack.getTypeId());
 			for (int j = 0; j < inv.getSize(); j++) {
 				if (invAll.containsKey(j)) {
 					ItemStack hi = invAll.get(j);
 					if (hi.getTypeId() == theStack.getTypeId()
 							&& hi.getDurability() == theStack.getDurability()) {
 						ammoStack = hi;
 						break;
 					}
 				}
 			}
 		}
 		if (ammoStack == null) {
 			return;
 		}
 		if (ammoStack.getAmount() > 1) {
 			ammoStack.setAmount(ammoStack.getAmount() - 1);
 		} else {
 			inv.remove(ammoStack);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public static void removeGunInHand(SpoutPlayer sp) {
 		if (isGun(sp.getItemInHand())) {
 			ItemStack remove = sp.getItemInHand();
 			if (remove == null)
 				return;
 			if (remove.getAmount() > 1) {
 				remove.setAmount(remove.getAmount() - 1);
 			} else {
 				remove = null;
 				sp.setItemInHand(null);
 			}
 			sp.updateInventory();
 		}
 	}
 
 	public static Ammo getFirstCustomAmmo(Inventory inv,
 			ArrayList<ItemStack> ammo) {
 		if (ammo.isEmpty())
 			return null;
 		HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 		ItemStack ammoStack = null;
 		for (ItemStack theStack : ammo) {
 			invAll = inv.all(theStack.getTypeId());
 			for (int j = 0; j < inv.getSize(); j++) {
 				if (invAll.containsKey(j)) {
 					ItemStack hi = invAll.get(j);
 					if (hi.getTypeId() == theStack.getTypeId()
 							&& hi.getDurability() == theStack.getDurability()) {
 						ammoStack = hi;
 						for (Ammo y : GunsPlus.allAmmo) {
 							if (new SpoutItemStack(y).getDurability() == ammoStack
 									.getDurability()
 									&& new SpoutItemStack(y).getTypeId() == ammoStack
 											.getTypeId())
 								return y;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public static boolean checkInvForAmmo(Inventory inv,
 			ArrayList<ItemStack> ammo) {
 		if (ammo.isEmpty())
 			return true;
 		HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 		for (ItemStack theStack : ammo) {
 			invAll = inv.all(theStack.getTypeId());
 			for (int j = 0; j < inv.getSize(); j++) {
 				if (invAll.containsKey(j)) {
 					ItemStack hi = invAll.get(j);
 					if (hi.getTypeId() == theStack.getTypeId()
 							&& hi.getDurability() == theStack.getDurability()) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public static boolean checkInvForGun(Inventory inv, Gun g) {
 		if (g == null)
 			return true;
 		HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 		SpoutItemStack theStack = new SpoutItemStack(g);
 		invAll = inv.all(theStack.getTypeId());
 		for (int j = 0; j < inv.getSize(); j++) {
 			if (invAll.containsKey(j)) {
 				ItemStack hi = invAll.get(j);
 				if (hi.getTypeId() == theStack.getTypeId()
 						&& hi.getDurability() == theStack.getDurability()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static int getAmmoCount(Inventory inv, ArrayList<ItemStack> ammo) {
 		HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 		int counter = 0;
 		if (ammo == null)
 			return counter;
 		for (ItemStack theStack : ammo) {
 			invAll = inv.all(theStack.getTypeId());
 			for (int j = 0; j < inv.getSize(); j++) {
 				if (invAll.containsKey(j)) {
 					ItemStack hi = invAll.get(j);
 					if (hi.getTypeId() == theStack.getTypeId()
 							&& hi.getDurability() == theStack.getDurability()) {
 						counter += hi.getAmount();
 					}
 				}
 			}
 		}
 		return counter;
 	}
 	
 	public static void performEffects(Shooter shooter, Location shooterLoc, HashSet<LivingEntity> targets, Gun gun){
 		LivingEntity shooterEntity = null;
 		if(shooter instanceof LivingShooter){
 			shooterEntity = ((LivingShooter)shooter).getLivingEntity();
 		}
 		ArrayList<Effect> effects = gun.getEffects();
 		for(Effect e : effects){
 			Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(shooter, gun, e));
 			for(LivingEntity le : targets){
				if(shooterEntity!=null&&shooterEntity.equals(le)) continue;
 				switch(e.getEffecttype()){
 					case BREAK:
 						EffectUtils.breakEffect(e, shooterLoc, le.getLocation());
 						break;
 					case PLACE:
 						EffectUtils.placeEffect(e, shooterLoc, le.getLocation());
 						break;
 					case POTION:
 						EffectUtils.potionEffect(e, shooterEntity,  le);
 						break;
 					case SMOKE:
 						EffectUtils.smokeEffect(e, shooterLoc, le.getLocation());
 						break;
 					case FIRE:
 						EffectUtils.fireEffect(e,shooterEntity, le);
 						break;
 					case SPAWN:
 						EffectUtils.spawnEffect(e, shooterLoc, le.getLocation());
 						break;
 					case LIGHTNING:
 						EffectUtils.lightningEffect(e, shooterLoc, le.getLocation());
 						break;
 					case EXPLOSION:
 						EffectUtils.explosionEffect(e, shooterLoc, le.getLocation());
 						break;
 					case PUSH:
 						EffectUtils.pushEffect(e, le, shooterEntity, shooterLoc.getDirection());
 						break;
 					case DRAW:
 						EffectUtils.drawEffect(e,le,shooterEntity, shooterLoc.getDirection());
 						break;
 				}
 			}
 		}
 	}
 
 	public static Gun getGunInHand(SpoutPlayer p) {
 		ItemStack is = p.getItemInHand();
 
 		if (holdsGun(p)) {
 			for (Gun g : GunsPlus.allGuns) {
 				SpoutItemStack sis = new SpoutItemStack(g);
 				if (is.getTypeId() == sis.getTypeId()
 						&& is.getDurability() == sis.getDurability()) {
 					return g;
 				}
 			}
 		}
 		return null;
 	}
 
 	public static Gun getGun(ItemStack item) {
 		for (Gun g : GunsPlus.allGuns) {
 			SpoutItemStack sis = new SpoutItemStack(g);
 			if (item.getTypeId() == sis.getTypeId()
 					&& item.getDurability() == sis.getDurability()) {
 				return g;
 			}
 		}
 		return null;
 	}
 
 	public static void zoomOut(GunsPlusPlayer gp) {
 		// PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, 0, 100);
 		// p.addPotionEffect(pe, true);
 		SpoutPlayer sp = (SpoutPlayer) gp.getPlayer();
 		CraftPlayer cp = (CraftPlayer) sp;
 
 		try {
 			Field field = EntityLiving.class.getDeclaredField("effects");
 			field.setAccessible(true);
 			@SuppressWarnings("rawtypes")
 			HashMap effects = (HashMap) field.get(cp.getHandle());
 			effects.remove(2);
 			EntityPlayer player = cp.getHandle();
 			player.netServerHandler.sendPacket(new Packet42RemoveMobEffect(
 					player.id, new MobEffect(2, 0, 0)));
 			cp.getHandle().getDataWatcher().watch(8, Integer.valueOf(0));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		sp.getMainScreen().removeWidget(
 				PlayerUtils.getPlayerBySpoutPlayer(sp).getZoomtexture());
 	}
 
 	public static void zoomIn(GunsPlus plugin, GunsPlusPlayer gp,
 			GenericTexture zTex, int zoomfactor) {
 		// SpoutPlayer sp = (SpoutPlayer) p;
 		// PotionEffect pe = new PotionEffect(PotionEffectType.SLOW, 24000,
 		// zoomfactor);
 		// p.addPotionEffect(pe, true);
 		SpoutPlayer sp = (SpoutPlayer) gp.getPlayer();
 		CraftPlayer cp = (CraftPlayer) sp;
 
 		cp.getHandle().addEffect(new MobEffect(2, 24000, zoomfactor));
 		try {
 			Field field;
 			field = EntityLiving.class.getDeclaredField("effects");
 			field.setAccessible(true);
 			@SuppressWarnings("rawtypes")
 			HashMap effects = (HashMap) field.get(cp.getHandle());
 			effects.remove(2);
 		} catch (NoSuchFieldException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e1) {
 			e1.printStackTrace();
 		}
 
 		if (!(zTex == null)) {
 			GenericTexture t = zTex;
 			t.setHeight(sp.getMainScreen().getHeight()).setWidth(
 					sp.getMainScreen().getWidth());
 			sp.getMainScreen().attachWidget(plugin, t);
 			gp.setZoomtexture(zTex);
 		}
 	}
 
 	public static boolean isHudEnabled(Gun g) {
 		return ((Boolean) g.getObject("HUDENABLED"));
 	}
 
 	public static boolean isMountable(Gun g) {
 		return ((Boolean) g.getObject("MOUNTABLE"));
 	}
 	
 	public static boolean isShootable(Gun g) {
 		return ((Boolean) g.getObject("SHOOTABLE"));
 	}
 
 	public static boolean isGun(ItemStack i) {
 		if (i != null)
 			for (Gun g : GunsPlus.allGuns) {
 				SpoutItemStack sis = new SpoutItemStack(g);
 				if (i.getTypeId() == sis.getTypeId()
 						&& i.getDurability() == sis.getDurability()) {
 					return true;
 				}
 			}
 		return false;
 	}
 	public static List<Block> getTargetBlocks(Location loc, Gun g) {
 		List<Block> targets = new ArrayList<Block>();
 		BlockIterator bitr = new BlockIterator(loc.add(0, 0, 0), 0d,
 				(int) g.getValue("RANGE"));
 		Block b;
 		while (bitr.hasNext()) {
 			b = bitr.next();
 			if (!b.getType().equals(Material.AIR)) targets.add(b);
 		}
 		return targets;
 	}
 }
