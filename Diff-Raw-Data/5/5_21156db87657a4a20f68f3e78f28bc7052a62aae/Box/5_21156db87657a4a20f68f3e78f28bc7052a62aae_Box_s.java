 package com.github.joakimpersson.tda367.model.tiles.nonwalkable;
 
 import java.util.Random;
 
 import com.github.joakimpersson.tda367.model.constants.Parameters;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.tiles.Destroyable;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.walkable.Floor;
 import com.github.joakimpersson.tda367.model.tiles.walkable.PowerUpFactory;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public class Box implements Tile, Destroyable {
 
 	private int toughness;
 	private String image;
 	private int boxImageNumber;
 
 	public Box() {
 		this.toughness = 1;
 		Random rand = new Random();
 		this.boxImageNumber = rand.nextInt(5) + 1;
		this.image = "box" + this.boxImageNumber;
 	}
 
 	@Override
 	public int getToughness() {
 		return toughness;
 	}
 
 	/**
 	 * When the box is destroyed either a floor or powerupitem is randomly
 	 * generated and returned. The probability for the PowerUpItem can be found
 	 * in the parameter class
 	 * 
 	 * @return Either a Floor or a PowerUpItem
 	 */
 	@Override
 	public Tile onFire() {
 		if (getRandomNbr()) {
 			PowerUpFactory facctory = new PowerUpFactory();
 			return facctory.createObject();
 		} else {
 			return new Floor();
 		}
 	}
 
 	/**
 	 * 
 	 * Get's a box's probability from the parameters class. Based on its
 	 * probability a boolean is generated randomly
 	 * 
 	 * @return Randomly chooses between true or false
 	 */
 	private boolean getRandomNbr() {
 		double a = Math.random();
 		double probability = Parameters.INSTANCE.getPowerUpProbabilityBox();
 
 		return probability >= a;
 
 	}
 
 	@Override
 	public boolean isWalkable() {
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return "Box";
 	}
 
 	@Override
 	public String getTileType() {
		return this.image;
 	}
 
 	@Override
 	public PointGiver getPointGiver() {
 		return PointGiver.Box;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj) {
 			return true;
 		}
 		return obj != null && getClass() == obj.getClass();
 	}
 
 	@Override
 	public int hashCode() {
 		int sum = 0;
 		sum += image.hashCode() * 7;
 		sum += toughness * 13;
 		return sum;
 	}
 }
