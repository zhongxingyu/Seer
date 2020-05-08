 package fr.frozen.iron.common.skills;
 
 import java.util.List;
 
 import fr.frozen.iron.common.IronWorld;
 import fr.frozen.iron.common.entities.IronUnit;
 import fr.frozen.iron.common.entities.particles.DamageParticle;
 import fr.frozen.iron.util.IronConst;
 
 public abstract class Skill {
 	
 	public static final int MELEE_ATTACK = 0;
 	public static final int RANGED_ATTACK = 1;
 	public static final int HEAL = 2;
 	public static final int BLIND_SHOT = 3;
 	public static final int SHIELD_BLOCK = 4;
 	
 	public static Skill getSkill(int type) {
 		switch (type) {
 		case MELEE_ATTACK : 
 			return MeleeAttack.getInstance();
 		case RANGED_ATTACK :
 			return RangedAttack.getInstance();
 		case HEAL :
 			return Heal.getInstance();
 		case BLIND_SHOT :
 			return BlindShot.getInstance();
 		}
 		
 		return null;
 	}
 	
 	protected String name;
 	protected int type;
 	
 	protected Skill(String name, int type) {
 		this.name = name;
 		this.type = type;
 	}
 	
 	public abstract boolean canDo(IronWorld world, int srcId, int x, int y);
 	public abstract List<int[]> executeSkill(IronWorld world, int srcId, int x, int y);//returns list of couple [unitId, value]
 	//value being damage or heal or whatever
 
 	public String getSkillName() {
 		return name;
 	}
 
 	public int getSkillType() {
 		return type;
 	}
 	
 	protected void executeCommon(IronWorld world, int srcId, int x, int y,
 			List<int[]> values) {
 
 		IronUnit src = world.getUnitFromId(srcId);
 		IronUnit target; 
 		
 		if (src == null) {
 			return;
 		}
 		
 		src.setPlayed(true);
 
 		for (int [] couple : values) {
 			target = world.getUnitFromId(couple[0]);
 			if (target == null || target.isDead()) continue;
 			target.setHp(target.getHp() + couple[1]);
 		}
 	}
 	
 	public void executeClientSide(IronWorld world, int srcId, int x, int y,
 			List<int[]> values) {
 		if (values == null) return;
 		executeCommon(world, srcId, x, y, values);
 		
 		IronUnit target;
 		DamageParticle damage;
 		
 		for (int [] couple : values) {
 			target = world.getUnitFromId(couple[0]);
			if (target == null) {
 				continue;
 			}
 			if (target.isDead()) {
 				target.setCorpseSprite();
 			}
 			damage = new DamageParticle(world, target.getX() * IronConst.TILE_WIDTH,
 											   target.getY() * IronConst.TILE_WIDTH,
 											   couple[1]);
 			
 			world.addGameObject(damage, "gfx");
 		}
 	}
 }
