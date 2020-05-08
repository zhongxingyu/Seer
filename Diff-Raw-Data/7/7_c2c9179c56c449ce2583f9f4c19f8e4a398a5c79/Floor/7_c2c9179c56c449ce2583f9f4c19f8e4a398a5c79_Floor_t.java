 package com.github.joakimpersson.tda367.model.tiles.walkable;
 
 import java.util.Random;
 
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Destroyable;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.WalkableTile;
 
 /**
  * An object representing a Floor tile.
  * 
  * @author joakimpersson
  * @modified adderollen
  * 
  */
 public class Floor implements WalkableTile, Destroyable {
 
 	private final int toughness;
 	private final String imageType;
	private final int floorNumber;
 
 	/**
 	 * Creating a Floor Tile.
 	 */
 	public Floor() {
 		// should be ignored by the fire and skipped
 		this.toughness = 0;
 		this.imageType = "floor";
		Random rand = new Random();
		this.floorNumber = rand.nextInt(5) + 1;
 	}
 
 	@Override
 	public int getToughness() {
 		return toughness;
 	}
 
 	/**
 	 * A floor is the simplest tile and is only meant to be walkable for the
 	 * player without any other actions. Therefore it returns itself (this) when
 	 * the method is called, since nothing has changed to the tile.
 	 * 
 	 * @return Itself
 	 */
 	@Override
 	public Tile onFire() {
 		return this;
 	}
 
 	/**
 	 * A floor is the simplest tile and is only meant to be walkable for the
 	 * player without any other actions. Therefore it returns itself (this) when
 	 * the method is called, since nothing has changed to the tile
 	 * 
 	 * @return Itself
 	 */
 	@Override
 	public Tile playerEnter(Player player) {
 		return this;
 	}
 
 	@Override
 	public boolean isWalkable() {
 		return true;
 	}
 
 	@Override
 	public String getTileType() {
 		return imageType + floorNumber;
 	}
 
 	@Override
 	public PointGiver getPointGiver() {
 		return PointGiver.Floor;
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
 		sum += imageType.hashCode() * 7;
 		sum += toughness * 13;
 		return sum;
 	}
 }
