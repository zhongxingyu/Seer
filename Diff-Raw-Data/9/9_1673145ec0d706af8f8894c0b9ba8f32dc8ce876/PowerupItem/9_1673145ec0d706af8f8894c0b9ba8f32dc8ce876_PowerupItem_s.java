 package com.github.joakimpersson.tda367.model.tiles.walkable;
 
 import com.github.joakimpersson.tda367.model.constants.Attribute;
 import com.github.joakimpersson.tda367.model.constants.GameModeType;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Destroyable;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.WalkableTile;
 
 /**
  * 
  * @author joakimpersson
  * 
  */
 public abstract class PowerupItem implements WalkableTile, Destroyable {
 
	private static int toughness = 0;
 
 	public PowerupItem() {

 	}
 
 	@Override
 	public Tile playerEnter(Player player) {
 		player.upgradeAttr(this.getAttr(), GameModeType.Round);
 		return new Floor();
 	}
 
 	@Override
 	public int getToughness() {
 		return toughness;
 	}
 
 	/**
 	 * If a powerup item is within the fire range the powerup will be destroyed
 	 * and replaced with an empty floor tile
 	 * 
 	 * @return A floor tile
 	 */
 	@Override
 	public Tile onFire() {
 		return new Floor();
 	}
 
 	@Override
 	public PointGiver getPointGiver() {
 		return PointGiver.PowerUpItem;
 	}
 
 	@Override
 	public boolean isWalkable() {
 		return true;
 	}
 
 	/**
 	 * 
 	 * @return A powerup attribute that will last the entire round
 	 */
 	public abstract Attribute getAttr();
 }
