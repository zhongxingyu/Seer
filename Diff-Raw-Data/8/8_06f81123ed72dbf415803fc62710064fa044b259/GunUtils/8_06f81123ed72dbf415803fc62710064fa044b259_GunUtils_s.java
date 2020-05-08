 package team.GunsPlus.Util;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import net.minecraft.server.EntityLiving;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.Packet42RemoveMobEffect;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.EnderPearl;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Fireball;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.SmallFireball;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.material.MaterialData;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 
 import team.GunsPlus.API.Event.*;
 import team.GunsPlus.API.Event.Gun.*;
 import team.GunsPlus.Enum.Effect;
 import team.GunsPlus.Enum.EffectType;
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
 
 	public static String getGunNameWithAdditions(Gun g, Addition... adds) {
 		String name = g.getName();
 		for (Addition a : adds)
 			name += "+" + a.getName();
 		return name;
 	}
 
 	public static String getGunNameWITHOUTAdditions(Gun g) {
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
 
 	public static void shootProjectile(Location from, Location to,
 			Projectile pro) {
 		float speed = (float) pro.getSpeed();
 		if (pro.equals(Projectile.ARROW)) {
 			Arrow a = (Arrow) Util.launchProjectile(Arrow.class, from, to,
 					speed);
 			Bukkit.getServer().getPluginManager().callEvent(new GunProjectileEvent(a));
 		} else if (pro.equals(Projectile.FIREBALL)) {
 			Fireball fb = (Fireball) Util.launchProjectile(Fireball.class,
 					from, to, speed);
 			Bukkit.getServer().getPluginManager().callEvent(new GunProjectileEvent(fb));
 		} else if (pro.equals(Projectile.SNOWBALL)) {
 			Snowball sb = (Snowball) Util.launchProjectile(Snowball.class,
 					from, to, speed);
 			Bukkit.getServer().getPluginManager().callEvent(new GunProjectileEvent(sb));
 		} else if (pro.equals(Projectile.EGG)) {
 			Egg egg = (Egg) Util.launchProjectile(Egg.class, from, to, speed);
 			Bukkit.getServer().getPluginManager().callEvent(new GunProjectileEvent(egg));
 		} else if (pro.equals(Projectile.ENDERPEARL)) {
 			EnderPearl ep = (EnderPearl) Util.launchProjectile(
 					EnderPearl.class, from, to, speed);
 			Bukkit.getServer().getPluginManager().callEvent(new GunProjectileEvent(ep));
 		} else if(pro.equals(Projectile.FIRECHARGE)) {
 			SmallFireball sfb = (SmallFireball) Util.launchProjectile(
 					SmallFireball.class, from, to, speed); 
 			Bukkit.getServer().getPluginManager().callEvent(new GunProjectileEvent(sfb));
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
 			Location blockcenter = b.getLocation().add(0.5f, -0.5f, 0.5f);
 			Set<LivingEntity> entities = new HashSet<LivingEntity>();
 			if (!Util.isTransparent(b))
 				break;
 			for (Entity e : Util.getNearbyEntities(b.getLocation(), 0.4, 0.4,
 					0.4)) {
 				if (e instanceof LivingEntity) {
 					entities.add((LivingEntity) e);
 				}
 			}
 			for (LivingEntity e : entities) {
 				l = e.getLocation();
 				el = e.getEyeLocation();
 				double changedamage = (int) Math.ceil((float) g
 						.getValue("CHANGEDAMAGE")
 						* loc.toVector().distance(l.toVector()));
 				if (l.toVector().distance(blockcenter.toVector()) > el
 						.toVector().distance(blockcenter.toVector())) {
 					targets.put(
 							e,
 							(int) ((int) g.getValue("HEADSHOTDAMAGE") + changedamage)
 									* -1);
 				} else {
 					targets.put(e,
 							(int) ((int) g.getValue("DAMAGE") + changedamage));
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
 
 	public static int getAmmoCount(SpoutPlayer p, ArrayList<ItemStack> ammo) {
 		HashMap<Integer, ? extends ItemStack> invAll = new HashMap<Integer, SpoutItemStack>();
 		int counter = 0;
 		if (ammo == null)
 			return counter;
 		Inventory inv = p.getInventory();
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
 
 
 
 	public static void performEffects(HashSet<LivingEntity> targets, SpoutPlayer player, Gun gun){
 		ArrayList<Effect> effects = gun.getEffects();
 		Location loc_tar, loc_sp;
 		if(targets.isEmpty()) targets.add(player);
 		for(LivingEntity tar : targets){
 			loc_tar = (tar==player)?player.getTargetBlock(null, (int) gun.getValue("RANGE")).getLocation():tar.getEyeLocation();
 			loc_sp = player.getEyeLocation();
 			for(Effect eff : effects){
 				EffectType et = eff.getEffecttype();
 				switch(eff.getEffectsection()){
 					case TARGETLOCATION:
 						switch(et){
 							case EXPLOSION:
 								if(Util.tntIsAllowedInRegion(loc_tar)) loc_tar.getWorld().createExplosion(loc_tar, (Integer) eff.getArgument("SIZE"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case LIGHTNING:
 								loc_tar.getWorld().strikeLightning(loc_tar);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case SMOKE:
 								loc_tar.getWorld().playEffect(loc_tar,org.bukkit.Effect.SMOKE , BlockFace.UP, (Integer) eff.getArgument("DENSITY"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case SPAWN:
 								Location l1 = loc_tar;
 								l1.setY(loc_tar.getY()+1);
 								loc_tar.getWorld().spawnCreature(l1, EntityType.valueOf((String) eff.getArgument("ENTITY")));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case FIRE:
 								loc_tar.getWorld().playEffect(loc_tar, org.bukkit.Effect.MOBSPAWNER_FLAMES, null, (Integer) eff.getArgument("STRENGTH"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case PLACE:
 								loc_tar.getBlock().setTypeId((Integer) eff.getArgument("BLOCK"));
 								BlockIterator bi = new BlockIterator(player.getWorld(), loc_sp.toVector(), loc_tar.toVector(), 0, (int) gun.getValue("RANGE"));
 								Block last = null, b = null;
 								boolean loop=true;
 								while(bi.hasNext()&&loop){
 									last = b;
 									b = bi.next();
 									if(!Util.isTransparent(b)){
 										last.setTypeId((Integer) eff.getArgument("BLOCK"));
 										loop=false;
 										Bukkit.getServer().getPluginManager().callEvent(new GunBlockPlaceEvent(player, gun, last));
 									}
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case BREAK:
								if(MaterialData.getBlock(loc_tar.getBlock().getTypeId()).getHardness()<(Integer) eff.getArgument("POTENCY")){
 									loc_tar.getBlock().setTypeId(0);
 									Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(loc_tar.getBlock(), player));
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							}
 						break;
 					case SHOOTERLOCATION:
 						switch(et){
 							case EXPLOSION:
 								if(Util.tntIsAllowedInRegion(loc_tar)) loc_sp.getWorld().createExplosion(loc_sp, (Integer) eff.getArgument("SIZE"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case LIGHTNING:
 								loc_sp.getWorld().strikeLightning(loc_sp);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case SMOKE:
 								loc_sp.getWorld().playEffect(loc_sp, org.bukkit.Effect.SMOKE,  BlockFace.UP, (Integer) eff.getArgument("DENSITY"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case SPAWN:
 								Location l1 = loc_sp;
 								l1.setY(loc_tar.getY()+1);
 								loc_sp.getWorld().spawnCreature(l1, EntityType.valueOf((String) eff.getArgument("ENTITY")));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case FIRE:
 								loc_sp.getWorld().playEffect(loc_sp, org.bukkit.Effect.MOBSPAWNER_FLAMES, null, (Integer) eff.getArgument("STRENGTH"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case PLACE:
 								loc_sp.getBlock().setTypeId((Integer) eff.getArgument("BLOCK"));
 								BlockIterator bi = new BlockIterator(player.getWorld(), loc_sp.toVector(), loc_tar.toVector(), 0, (int) gun.getValue("RANGE"));
 								Block last = null, b = null;
 								boolean loop=true;
 								while(bi.hasNext()&&loop){
 									last = b;
 									b = bi.next();
 									if(!Util.isTransparent(b)){
 										last.setTypeId((Integer) eff.getArgument("BLOCK"));
 										loop=false;
 										Bukkit.getServer().getPluginManager().callEvent(new GunBlockPlaceEvent(player, gun, last));
 									}
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case BREAK:
								if(MaterialData.getBlock(loc_tar.getBlock().getTypeId()).getHardness()<(Integer) eff.getArgument("POTENCY")){
 									loc_tar.getBlock().setTypeId(0);
 									Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(loc_tar.getBlock(), player));
 								}
 								break;
 							}
 						break;
 					case TARGETENTITY:
 						if(tar==player) break;
 						switch(et){
 							case FIRE:
 								tar.setFireTicks((Integer) eff.getArgument("DURATION"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case PUSH:
 								Vector v1 = loc_sp.getDirection();
 								v1.multiply((Double)eff.getArgument("SPEED"));
 								tar.setVelocity(v1);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case DRAW:
 								Vector v2 = loc_sp.getDirection();
 								v2.multiply((Double)eff.getArgument("SPEED")*-1);
 								tar.setVelocity(v2);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case POTION:
 								tar.addPotionEffect(new PotionEffect(PotionEffectType.getById((Integer) eff.getArgument("ID")), (Integer)eff.getArgument("DURATION"), (Integer) eff.getArgument("STRENGTH")),true);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							}
 						break;
 					case SHOOTER:
 						switch(et){
 							case FIRE:
 								player.setFireTicks((Integer) eff.getArgument("DURATION"));
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case PUSH:
 								Vector v1 = loc_sp.getDirection();
 								v1.multiply((Double)eff.getArgument("SPEED"));
 								player.setVelocity(v1);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case DRAW:
 								Vector v2 = loc_sp.getDirection();
 								v2.multiply((Double)eff.getArgument("SPEED")*-1);
 								player.setVelocity(v2);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case POTION:
 								player.addPotionEffect(new PotionEffect(PotionEffectType.getById((Integer) eff.getArgument("ID")), (Integer)eff.getArgument("DURATION"), (Integer) eff.getArgument("STRENGTH")),true);
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 						}
 						break;
 					case FLIGHTPATH:
 						BlockIterator bi = new BlockIterator(player, Math.round(gun.getValue("RANGE")));
 						boolean loop = true;
 						switch(et){
 							case FIRE:
 								while(bi.hasNext()){
 									Block b = bi.next();
 									b.getWorld().playEffect(b.getLocation(), org.bukkit.Effect.MOBSPAWNER_FLAMES, null, (Integer) eff.getArgument("STRENGTH"));
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case EXPLOSION:
 								loop = true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									if(Util.isTransparent(b))
 										if(Util.tntIsAllowedInRegion(loc_tar)) b.getWorld().createExplosion(b.getLocation(), (Integer) eff.getArgument("SIZE"));
 									else loop=false;
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case LIGHTNING:
 								loop=true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									if(Util.isTransparent(b))
 									b.getWorld().strikeLightning(b.getLocation());
 									else loop=false;
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case SMOKE:
 								while(bi.hasNext()){
 									Block b = bi.next();
 									b.getWorld().playEffect(b.getLocation(), org.bukkit.Effect.SMOKE, BlockFace.UP, (Integer) eff.getArgument("DENSITY"));
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case SPAWN:
 								loop=true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									Location l1 = b.getLocation();
 									l1.setY(loc_tar.getY()+1);
 									if(Util.isTransparent(b))
 									b.getWorld().spawnCreature(l1, EntityType.valueOf((String) eff.getArgument("ENTITY")));
 									else loop=false;
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case PLACE:
 								loop = true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
 									if(Util.isTransparent(b)) {
 										b.setTypeId((Integer)eff.getArgument("BLOCK"));
 										Bukkit.getServer().getPluginManager().callEvent(new GunBlockPlaceEvent(player, gun, b));
 									} else loop=false;
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 							case BREAK:
 								loop = true;
 								while(bi.hasNext()&&loop){
 									Block b = bi.next();
									if(MaterialData.getBlock(b.getTypeId()).getHardness()<(Integer)eff.getArgument("POTENCY")){
 										b.setTypeId(0);
 										Bukkit.getServer().getPluginManager().callEvent(new BlockBreakEvent(loc_tar.getBlock(), player));
 									}else{
 										loop=false;
 									}
 								}
 								Bukkit.getServer().getPluginManager().callEvent(new GunEffectEvent(player, gun, eff));
 								break;
 						}
 						break;
 					case UNDEFINED:
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
 }
