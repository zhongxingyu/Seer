 package fr.frozen.iron.common.skills;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 
 import fr.frozen.game.ISpriteManager;
 import fr.frozen.iron.common.IronWorld;
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.common.entities.particles.Projectile;
 import fr.frozen.iron.common.equipment.RangedWeapon;
 import fr.frozen.iron.common.equipment.Weapon;
 import fr.frozen.iron.util.IronConst;
 import fr.frozen.iron.util.IronUtil;
 
 public class RangedAttack extends Skill {
 
 	private static RangedAttack instance = new RangedAttack();
 	
 	public static RangedAttack getInstance() {
 		return instance;
 	}
 	
 	protected RangedAttack() {
 		super("Ranged Attack", Skill.RANGED_ATTACK);
 	}
 	
 	protected boolean canDoAux(IronUnit src, IronUnit dst) {
 		if (src == null || dst == null) return false;
 		
 		Weapon rangedWeapon = src.getRangedWeapon();
 		if (rangedWeapon == null || !(rangedWeapon instanceof RangedWeapon)) return false;
 		if (rangedWeapon.getManaCost() > 0 && src.getStats().getMana() < rangedWeapon.getManaCost()) return false;
 		if (src.getId() == dst.getId()) return false;
 		if (src.getOwnerId() == dst.getOwnerId()) return false;
 		if (dst.isDead()) return false;
 
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
 	public boolean canDo(IronWorld world, int srcId, int x, int y) {
 		IronUnit src = world.getUnitFromId(srcId);
 		IronUnit dst = world.getUnitAtXY(x, y);
 		
 		if (!canDoAux(src, dst)) return false;
 		
 		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
 		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
 		
 		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
 		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
 		
 		return IronUtil.checkGrid(world, x1, y1, x2, y2);
 	}
 
 	
 
 	@Override
 	public List<int[]> executeSkill(IronWorld world, int srcId, int x, int y) {
 		if (!canDo(world, srcId, x, y)) return null;
 		IronUnit dst = world.getUnitAtXY(x, y);
 		IronUnit src = world.getUnitFromId(srcId);
 		
 		if (dst == null || src == null || src.getRangedWeapon() == null) return null;
 		
 		List<int[]> res = new ArrayList<int[]>();
 		int damage = IronUtil.getDamage(src, dst, false);
 		
		if (!dst.isDead())
			res.add(new int[]{dst.getId(), - (int)damage});
 		
 		executeCommon(world, srcId, x, y, res);
 		return res;
 	}
 
 	@Override
 	public void executeClientSide(IronWorld world, int srcId, int x, int y,
 			List<int[]> values) {
 		
 		super.executeClientSide(world, srcId, x, y, values);
 		if (values.size() <= 0) return;
 		
 		
 		IronUnit src = world.getUnitFromId(srcId);
 		IronUnit dst = world.getUnitFromId(values.get(0)[0]);
 		if (!(src.getRangedWeapon() instanceof RangedWeapon)) {
 			return;
 		}
 		RangedWeapon weapon = (RangedWeapon)src.getRangedWeapon();
 		if (!weapon.sendsProjectile()) return;
 		if (!ISpriteManager.getInstance().isSpriteLoaded(weapon.getProjectileName())) return;
 		
 
 		int x1 = (int)src.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
 		int y1 = (int)src.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
 		
 		int x2 = (int)dst.getX() * IronConst.TILE_WIDTH + IronConst.TILE_WIDTH / 2;
 		int y2 = (int)dst.getY() * IronConst.TILE_HEIGHT + IronConst.TILE_HEIGHT / 2;
 
 		Projectile arrow = new Projectile(world, x1 - IronConst.TILE_WIDTH / 2,
 									   y1 - IronConst.TILE_HEIGHT / 2,
 									   new Vector2f(x2 - x1, y2 - y1), weapon.getProjectileName());
 		world.addGameObject(arrow, "gfx");
 	}
 }
