 /**
  * Buffs the Character's defense for a bit.
  */
 
 package yuuki.action;
 
 import yuuki.buff.DefenseBuff;
 import yuuki.entity.Character;
 
 public class BasicDefense extends Action implements Cloneable {
 
 	/**
 	 * Creates a new BasicDefense.
 	 *
 	 * @param turns Number of turns it lasts.
 	 */
 	public BasicDefense(int turns) {
 		super("defend", 0.0, 0.0, null,
 				new DefenseBuff("defending", 1.5, turns));
 	}
 	
 	/**
 	 * Creates a BasicDefense from an existing one.
 	 * 
 	 * @param args Must contain a single int that is the duration.
 	 */
	public BasicAttack createInstance(String[] args) {
 		int d = Integer.parseInt(args[0]);
		return new BasicAttack(d);
 	}
 	
 	/**
 	 * Creates a clone of this BasicDefense.
 	 *
 	 * @return The clone.
 	 */
 	public BasicDefense clone() {
 		return (BasicDefense) super.clone();
 	}
 	
 	/**
 	 * Applies the cost to the origin. The origin does not need to spend
 	 * anything to use this Action, so this method always returns true.
 	 *
 	 * @return True unconditionally.
 	 */
 	protected boolean applyCost() {
 		return true;
 	}
 	
 	/**
 	 * Has no effect.
 	 */
 	protected void applyEffect() {}
 	
 	/**
 	 * Applies the defense buff to the origin.
 	 */
 	protected void applyBuffs() {
 		origin.addBuff(originBuff);
 	}
 	
 	/**
 	 * Sets the performer of this Action. This also sets the target.
 	 *
 	 * @param performer The Character performing the action.
 	 */
 	@Override
 	public void setOrigin(Character performer) {
 		super.setOrigin(performer);
 		super.addTarget(performer);
 	}
 	
 	/**
 	 * Has no effect, as adding a target makes no sense.
 	 */
 	@Override
 	public void addTarget(Character t) {}
 	
 	/**
 	 * Has no effect, as BasicDefense does not cost anything.
 	 */
 	@Override
 	protected void setCostStat(Character c) {}
 	
 	/**
 	 * Has no effect, as BasicDefense does not have any stat effects.
 	 */
 	@Override
 	protected void setEffectStat(Character c) {}
 
 }
