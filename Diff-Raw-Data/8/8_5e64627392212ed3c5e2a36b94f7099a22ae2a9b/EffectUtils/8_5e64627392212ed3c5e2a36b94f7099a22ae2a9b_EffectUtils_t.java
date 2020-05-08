 package team.GunsPlus.Util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.util.BlockIterator;
 import org.bukkit.util.Vector;
 import org.getspout.spoutapi.material.MaterialData;
 
 import team.GunsPlus.Enum.Effect;
 import team.GunsPlus.Enum.EffectType;
 import team.GunsPlus.Manager.ConfigParser;
 
 public class EffectUtils {
 	
 	private static Location switchLocation(Effect e, Location shooter, Location target){
 		Location ret = null;
 		switch (e.getEffectsection()) {
 			case TARGETLOCATION:
 				ret = target;
 				break;
 			case SHOOTERLOCATION:
 				ret = shooter;
 				break;
 			case TARGETENTITY:
 				ret = target;
 				break;
 			case SHOOTER:
 				ret = shooter;
 				break;
 			case FLIGHTPATH:
 				return null;
 		}
 		return ret;
 	}
 	
 	private static LivingEntity switchEntity(Effect e, LivingEntity shooter, LivingEntity target){
 		switch(e.getEffectsection()){
 			case TARGETENTITY:
 				return target;
 			case SHOOTER:
 				return shooter;
 			default:
 				return null;
 		}	
 	}
 	
 	private static List<Block> getTargetBlocks(Effect e, Location t){
 		List<Block> targetBlocks = new ArrayList<Block>();
 		if(!e.getEffectsection().getData().isEmpty()){
 			targetBlocks = Util.getSphere(t, (Integer)
 					e.getEffectsection().getData().get("RADIUS"));
 		}
 		if(targetBlocks.isEmpty()){
 			 targetBlocks.add(t.getBlock());
 		}
 		return targetBlocks;
 	}
 	
 	private static List<Block> getFlightPath(Location from, Location to){
 		List<Block> blockList = new ArrayList<Block>();
 		BlockIterator bitr = new BlockIterator(from.getWorld(), from.toVector(), to.toVector(), 0d, (int) Math.ceil(from.toVector().distance(to.toVector())));
 		while(bitr.hasNext()){
 			Block b = bitr.next();
 			if(Util.isTransparent(b)&&!Util.isTripod(b)){
 				blockList.add(b);
 			}else break;
 		}
 		return blockList;
 	}
 
 	public static void breakEffect(Effect e, Location shooter, Location location) {
 		Location target = switchLocation(e, shooter, location);
 		List<Block> targetBlocks = null;
 		if(target!=null)
 			targetBlocks = getTargetBlocks(e, target);
 		else
 			targetBlocks = getFlightPath(shooter, location);
 		for(Block b : targetBlocks)
 			breakBlock(b, e);
 	}
 	
 	private static void  breakBlock(Block b, Effect eff){
 		if(MaterialData.getBlock(b.getTypeId()).getHardness()<Float.valueOf(eff.getArgument("POTENCY").toString()).floatValue()&&!Util.isTripod(b)){
 			 b.breakNaturally();
 		}
 	}
 
 	public static void placeEffect(Effect e, Location shooter, Location location) {
 		Location target = switchLocation(e, shooter, location);
 		List<Block> targetBlocks = null;
 		if(target!=null)
 			targetBlocks = getTargetBlocks(e, target);
 		else
 			targetBlocks = getFlightPath(shooter, location);
 		for(Block b : targetBlocks)
 			placeBlock(b, e);
 	}
 	
 	private static void placeBlock(Block b , Effect eff) {
 		 if(Util.isTransparent(b)&&!Util.isTripod(b)){
 			 b.setTypeId(ConfigParser.parseItem((String)
 			 eff.getArgument("BLOCK")).getTypeId());
 		 }
 	}
 
 	public static void explosionEffect(Effect e, Location shooter,
 			Location location) {
 		Location target = switchLocation(e, shooter, location);
 		List<Block> targetBlocks = null;
 		if(target!=null)
 			targetBlocks = getTargetBlocks(e, target);
 		else
 			targetBlocks = getFlightPath(shooter, location);
 		for(Block b : targetBlocks)
 			explosion(b.getLocation(), e);
 	}
 	
 	private static void explosion(Location l, Effect eff){
 		if(Util.tntIsAllowedInRegion(l))
 			 l.getWorld().createExplosion(l, (Integer)
 			 eff.getArgument("SIZE"));
 	}
 
 	public static void lightningEffect(Effect e, Location shooter, Location location) {
 		Location target = switchLocation(e, shooter, location);
 		List<Block> targetBlocks = null;
 		if(target!=null)
 			targetBlocks = getTargetBlocks(e, target);
 		else
 			targetBlocks = getFlightPath(shooter, location);
 		for(Block b : targetBlocks)
 			lightning(b.getLocation(), e);
 	}
 	
 	private static void lightning(Location l, Effect eff){
 		l.getWorld().strikeLightning(l);
 	}
 
 	public static void spawnEffect(Effect e, Location shooter, Location location) {
 		Location target = switchLocation(e, shooter, location);
 		List<Block> targetBlocks = null;
 		if(target!=null)
 			targetBlocks = getTargetBlocks(e, target);
 		else
 			targetBlocks = getFlightPath(shooter, location);
 		for(Block b : targetBlocks)
 			spawn(b.getLocation(), e);
 	}
 	
 	private static void spawn(Location l, Effect eff){
 		Location l1 = l.clone();
 		 l1.setY(l.getY()+1);
 		 l1.getWorld().spawnCreature(l1, EntityType.valueOf((String)
 		 eff.getArgument("ENTITY")));
 	}
 	
 	public static void smokeEffect(Effect e, Location shooter, Location location) {
 		Location target = switchLocation(e, shooter, location);
 		List<Block> targetBlocks = null;
 		if(target!=null)
 			targetBlocks = getTargetBlocks(e, target);
 		else
 			targetBlocks = getFlightPath(shooter, location);
 		for(Block b : targetBlocks)
 			smoke(b.getLocation(), e);
 	}
 	
 	private static void smoke(Location l, Effect eff){
 		l.getWorld().playEffect(l,org.bukkit.Effect.SMOKE ,
 				 BlockFace.UP, (Integer) eff.getArgument("DENSITY"));
 	}
 
 	public static void potionEffect(Effect e, LivingEntity shooterEntity, LivingEntity le) {
 		potion(switchEntity(e, shooterEntity, le), e);
 	}
 	
 	private static void potion(LivingEntity le, Effect eff){
 		le.addPotionEffect(new
 		PotionEffect(PotionEffectType.getById(Integer.valueOf(eff.getArgument("ID").toString()).intValue()),
 		Integer.valueOf(eff.getArgument("DURATION").toString()).intValue(),
 		Integer.valueOf(eff.getArgument("STRENGTH").toString()).intValue()));	
 	}
 
 	public static void fireEffect(Effect e, LivingEntity shooterEntity, LivingEntity le) {
 		LivingEntity target = switchEntity(e, shooterEntity, le);
 		if(target!=null){
 			fire(target, e);
 		}else{
 			Location targetLoc = switchLocation(e, shooterEntity.getLocation(), le.getLocation());
 			List<Block> targetBlocks = null;
 			if(targetLoc!=null)
 				targetBlocks = getTargetBlocks(e, targetLoc);
 			else
 				targetBlocks = getFlightPath(shooterEntity.getLocation(), le.getLocation());
 			for(Block b : targetBlocks)
 				fire(b.getLocation(), e);
 		}
 	}
 		
 	private static void fire(LivingEntity le, Effect eff){
 		le.setFireTicks(Integer.valueOf(eff.getArgument("DURATION").toString()).intValue());
 	}
 	
 	private static void fire(Location l, Effect eff){
 		l.getWorld().playEffect(l,
 				 org.bukkit.Effect.MOBSPAWNER_FLAMES, null, (Integer)
 				 eff.getArgument("STRENGTH"));
 	}
 
 	public static void drawEffect(Effect e, LivingEntity le, LivingEntity shooterEntity, Vector v) {
 		move(v, switchEntity(e, shooterEntity, le), e);
 	}
 	
 	public static void pushEffect(Effect e, LivingEntity le, LivingEntity shooterEntity, Vector v) {
 		move(v, switchEntity(e, shooterEntity, le), e);
 	}
 	
 	private static void move(Vector dir, LivingEntity l, Effect eff){
		int factor = 1;
 		if(eff.getEffecttype().equals(EffectType.DRAW))
 			factor = -1;
 		dir.multiply(Double.valueOf(eff.getArgument("SPEED").toString()).doubleValue()*factor);
 		l.setVelocity(dir);
 	}
 }
