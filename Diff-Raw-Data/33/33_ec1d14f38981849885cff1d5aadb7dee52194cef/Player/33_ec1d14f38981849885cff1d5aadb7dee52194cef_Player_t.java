 package net.castegaming.game.entities;
 
 import net.castegaming.game.enums.EntityType;
 
 public class Player extends Entity{
 	
 	private double fuelLevel;
 
 	public Player() {
 		super(EntityType.PLAYER);
 		setFuelLevel(100.0);
 		setFriction(0.05);
 	}
 
 	@Override
 	public void update() {
 		fuelLevel -= 0.01;
 		
		double direction = (getDirection() > 180) ? getDirection() - 0.1 : getDirection() + 0.1;
 		
 		setDirectionSpeed(direction, getSpeed() - 0.1);
 		
 	}
 
 	/**
 	 * Sets the fuel level
 	 * @param currentLevel The level to set
 	 */
 	public void setFuelLevel(double currentLevel) {
 		fuelLevel = currentLevel;
 	}
 	
 	/**
	 * Returns the fuel level 
	 * @return the current fuel level
 	 */
 	public double getFuelLevel(){
 		return fuelLevel;
 	}
 	
 	/**
 	 * Drains the fuel.
 	 * @param toDraing the amount to drain.
 	 */
 	public void drainFuel(double toDraing){
 		if (fuelLevel - toDraing <= 0){
 			die();
 		} else {
 			fuelLevel -= toDraing;
 		}
 	}
 }
