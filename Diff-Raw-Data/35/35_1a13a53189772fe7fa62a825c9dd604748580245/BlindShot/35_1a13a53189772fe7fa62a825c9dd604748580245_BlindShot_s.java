 package fr.frozen.iron.common.skills;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 
 import fr.frozen.iron.common.IronWorld;
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.common.entities.particles.BlindShotProjectile;
 import fr.frozen.iron.common.entities.particles.Projectile;
 import fr.frozen.iron.common.equipment.RangedWeapon;
 import fr.frozen.iron.common.equipment.Weapon;
 import fr.frozen.iron.util.IronConst;
 import fr.frozen.iron.util.IronUtil;
 
 public class BlindShot extends Skill {
 	private static BlindShot instance = new BlindShot();
 	
 	public static BlindShot getInstance() {
 		return instance;
 	}
 	
 	protected BlindShot() {
 		super("Blind Shot", Skill.BLIND_SHOT);
 	}
 	
 	@Override
 	public boolean canDo(IronWorld world, int srcId, int x, int y) {
 		IronUnit src = world.getUnitFromId(srcId);
 		IronUnit dst = world.getUnitAtXY(x, y);
 		
 		if (src == null || dst == null) return false;
 		
 		Weapon rangedWeapon = src.getRangedWeapon();
 		if (rangedWeapon == null || !(rangedWeapon instanceof RangedWeapon)) return false;
 		if (rangedWeapon.getManaCost() > 0 && src.getStats().getMana() < rangedWeapon.getManaCost()) return false;
 		if (src.getId() == dst.getId()) return false;
 		if (src.getOwnerId() == dst.getOwnerId()) return false;
 		if (dst.isDead()) return false;
 		if (src.hasMoved()) return false;
 		//range check
 		int minRange = rangedWeapon.getMinRange();
 		int maxRange = rangedWeapon.getMaxRange();
 		double distance = IronUtil.distance((int)src.getX(), (int)src.getY(), (int)dst.getX(), (int)dst.getY());
 		
 		
 		if (distance < minRange || distance > maxRange) return false;
 		return true;
 	}
 
 	@Override
 	public void executeCommon(IronWorld world, int srcId, int x, int y,
 			List<int[]> values) {
 		super.executeCommon(world, srcId, x, y, values);
 		IronUnit src = world.getUnitFromId(srcId);
 		src.getStats().setMana(src.getStats().getMana() - src.getRangedWeapon().getManaCost());
 	}
 
 	@Override
 	public List<int[]> executeSkill(IronWorld world, int srcId, int x, int y) {
 		if (!canDo(world, srcId, x, y)) return null;
 		IronUnit src = world.getUnitFromId(srcId);
 		if (src == null || src.getRangedWeapon() == null) return null;
 
 		double random = Math.random();
 		if (random < IronConst.BLIND_SHOT_MISS_PROB) x -= 1;
 		else if (random >= 1 - IronConst.BLIND_SHOT_MISS_PROB) x += 1;
 		
 		random = Math.random();
 		if (random < IronConst.BLIND_SHOT_MISS_PROB) y -= 1;
 		else if (random >= 1 - IronConst.BLIND_SHOT_MISS_PROB) y += 1;
 		
 		x = Math.max(0, Math.min(IronConst.MAP_WIDTH - 1, x));
 		y = Math.max(0, Math.min(IronConst.MAP_HEIGHT - 1, y));
 		
 		
 		List<int[]> res = new ArrayList<int[]>();
 		IronUnit dst = world.getUnitAtXY(x, y);
		if (dst != null) {
 			float damage = IronUtil.getDamage(src, dst, false);
 			res.add(new int[]{dst.getId(), - (int)damage});
 		} else {
 			int pos = x * IronConst.MAP_HEIGHT;
 			pos += y;
 			res.add(new int[]{-2, pos});
 		}
 		executeCommon(world, srcId, x, y, res);
 		return res;
 	}
 
 	@Override
 	public void executeClientSide(IronWorld world, int srcId, int x, int y,
 			List<int[]> values) {
 		
 		if (values.size() <= 0) return;
 		IronUnit dst = world.getUnitFromId(values.get(0)[0]);
 		IronUnit src = world.getUnitFromId(srcId);
 		if (src == null || dst == null && values.get(0)[0] != -2) return;
 
 		super.executeClientSide(world, srcId, x, y, values);
 		
 		
 		if (src.getRangedWeapon() == null || !(src.getRangedWeapon() instanceof RangedWeapon)) return;
 		RangedWeapon weapon = (RangedWeapon)src.getRangedWeapon();
 		if (!weapon.sendsProjectile()) return;
 		
 		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
 		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
 		int x2, y2;
 		int xdst, ydst;
 		boolean missed = false;
 		
 		if (dst != null) {
 			x2 = (int)dst.getX() * IronConst.TILE_WIDTH;
 			y2 = (int)dst.getY() * IronConst.TILE_HEIGHT;
 		} else {
 			missed = true;
 			x2 = values.get(0)[1] / IronConst.MAP_HEIGHT;
 			y2 = values.get(0)[1] % IronConst.MAP_HEIGHT;
 
 			if (x2 < 0 || x2 >= IronConst.MAP_WIDTH || y2 < 0 || y2 >= IronConst.MAP_HEIGHT) {
 				System.out.println("OUT OF BOUNDS IN BLIND SHOT CLIENT SIDE !! x="+x2+" y="+y2);
 			}
 			
 			x2 *= IronConst.TILE_WIDTH;
 			y2 *= IronConst.TILE_HEIGHT;
 		}
 		
 		xdst = x2;
 		ydst = y2;
 		
 		x2 += IronConst.TILE_WIDTH / 2;
 		y2 += IronConst.TILE_HEIGHT / 2;
 		
 		Projectile arrow = new BlindShotProjectile(world, x1 - IronConst.TILE_WIDTH / 2,
 				   y1 - IronConst.TILE_HEIGHT / 2,
 				   new Vector2f(x2 - x1, y2 - y1), weapon.getProjectileName(), xdst, ydst, missed);
 		world.addGameObject(arrow, "gfx");
 	}
 }
