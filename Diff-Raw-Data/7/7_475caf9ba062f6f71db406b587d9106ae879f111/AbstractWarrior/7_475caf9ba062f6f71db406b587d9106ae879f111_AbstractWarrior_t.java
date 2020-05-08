 package BattleBases;
 
 public abstract class AbstractWarrior {
 	int healthPoints;
 	int attackStrength;
 	int cost;
 	
 	public AbstractWarrior() {
 		super();
		this.spawn();
 	}
 
 	public abstract void spawn();
 
 
 	public void warriorFight(AbstractWarrior opponent, int clockTick) {
 		this.healthPoints -= opponent.getAttackStrength();
 		opponent.takeHit(this.attackStrength);
 	}
 
 	int takeHit(int attackStrength) {
 		this.healthPoints -= attackStrength;
 		return healthPoints;
 	}
 
 	public int getHealth() {
 		return this.healthPoints;
 	}
 
 	protected int getAttackStrength() {
 		return this.attackStrength;
 	}
 
 }
