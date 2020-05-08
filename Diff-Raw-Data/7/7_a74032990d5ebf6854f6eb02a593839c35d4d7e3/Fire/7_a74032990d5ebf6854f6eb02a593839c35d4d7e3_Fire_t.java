 package com.github.joakimpersson.tda367.model.tiles.walkable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.github.joakimpersson.tda367.model.constants.Direction;
 import com.github.joakimpersson.tda367.model.constants.PointGiver;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.WalkableTile;
 
 /**
  * 
  * @author joakimpersson
  * @modified Viktor Anderling
  * 
  */
 public class Fire implements WalkableTile {
 	private Direction direction;
 	private String image;
 	private Player fireOwner;
 
 	public Fire(Player fireOwner, Direction direction) {
 		this.fireOwner = fireOwner;
 		this.direction = direction;
		if (this.direction == null) {
			this.image = "fire-area";
		} else if (this.direction == Direction.NONE) {
 			this.image = "fire-mid";
 		} else if (this.direction.isHorizontal()) {
 			this.image = "fire-row";
 		} else if (this.direction.isVertical()) {
 			this.image = "fire-column";
 		}
 	}
 
 	/**
 	 * If a player enters a fire tile it should loose one hp. Since the tile's
 	 * state is not changed it returns itself when the method is invoked
 	 * 
 	 * @param The
 	 *            player who entered the tile
 	 * @return Itself
 	 */
 	@Override
 	public Tile playerEnter(Player player) {
 		player.playerHit();
 		distributePlayerPoints(player);
 		return this;
 	}
 
 	/**
 	 * Responsible for distributing the points the player that placed the fire
 	 * deserves when another player walks into his fire
 	 * 
 	 * @param targetPlayer
 	 *            The player that walked into the fire
 	 */
 	private void distributePlayerPoints(Player targetPlayer) {
 		List<PointGiver> pg = new ArrayList<PointGiver>();
 		if (!targetPlayer.isImmortal() && targetPlayer.isAlive()) {
 			targetPlayer.playerHit();
 			if (!fireOwner.equals(targetPlayer)) {
 				pg.add(PointGiver.PlayerHit);
 				if (!targetPlayer.isAlive()) {
 					pg.add(PointGiver.KillPlayer);
 				}
 				fireOwner.updatePlayerPoints(pg);
 			}
 		}
 	}
 
 	@Override
 	public boolean isWalkable() {
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Fire";
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 
 		Fire other = (Fire) obj;
 		return this.direction.equals(other.direction)
 				&& this.fireOwner.equals(other.fireOwner);
 	}
 
 	@Override
 	public int hashCode() {
 		int sum = 0;
 
 		sum += direction.hashCode() * 13;
 		sum += fireOwner.hashCode() * 17;
 
 		return sum;
 	}
 
 	@Override
 	public String getTileType() {
 		return this.image;
 	}
 }
