 package herbstJennrichLehmannRitter.engine.model.impl;
 
 import herbstJennrichLehmannRitter.engine.model.DefenceBuilding;
 
 public abstract class AbstractDefenceBuilding implements DefenceBuilding {
 
 	private int actualPoints;
 	
 	public AbstractDefenceBuilding(int actualPoints) {
 		this.actualPoints = actualPoints;
 	}
 	
 	@Override
 	public int getActualPoints() {
 		return this.actualPoints;
 	}
 
 	@Override
 	public void setActualPoints(int points) {
 		if (points < 0) {
 			this.actualPoints = 0;
 		} else {
 			this.actualPoints = points;
 		}
 	}
 	
 	@Override
 	public void addPoints(int points) {
 		setActualPoints(this.actualPoints + points);
 	}
 	
 	@Override
 	public int applyDamage(int damage) {
 		if (this.actualPoints < damage) {
			damage -= this.actualPoints; 
 			setActualPoints(0);
			return damage;
 		}
 		
 		setActualPoints(this.actualPoints - damage);
 		return 0;
 	}
 
 }
