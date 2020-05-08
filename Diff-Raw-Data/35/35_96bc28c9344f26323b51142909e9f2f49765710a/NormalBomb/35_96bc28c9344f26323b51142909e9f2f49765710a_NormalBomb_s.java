 package com.github.joakimpersson.tda367.model.tiles.bombs;
 
 import static com.github.joakimpersson.tda367.model.constants.Direction.SOUTH;
 import static com.github.joakimpersson.tda367.model.constants.Direction.WEST;
 import static com.github.joakimpersson.tda367.model.constants.Direction.NONE;
 import static com.github.joakimpersson.tda367.model.constants.Direction.EAST;
 import static com.github.joakimpersson.tda367.model.constants.Direction.NORTH;
 
 import java.util.Map;
 import java.util.Timer;
 
 import com.github.joakimpersson.tda367.model.constants.Direction;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Destroyable;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 /**
  * Definition of a regular bomb.
  * 
  * @author rekoil
  * @modified Viktor Anderling
  * 
  */
 public class NormalBomb extends Bomb {
 	private Tile[][] map;
	private String image;
 
 	/**
 	 * Creates a regular bomb.
 	 * 
 	 * @param player
 	 *            Defines the owner of the bomb.
 	 * @param timer
 	 *            The timer which will detonate the bomb.
 	 */
 	public NormalBomb(Player player, Timer timer) {
 		super(player, timer);
		this.image = "bomb";
 	}
 
 	@Override
 	public Map<Position, Direction> explode(Tile[][] m) {
 		map = m;
 
 		directedFire(NORTH);
 		directedFire(SOUTH);
 		directedFire(EAST);
 		directedFire(WEST);
 
 		// dont forget to add itself
 		fireList.put(pos, NONE);
 
 		removeFromPlayer();
 		return fireList;
 	}
 
 	/**
 	 * Creates a column of fire.
 	 * 
 	 * @param dir
 	 *            Direction of fire-column.
 	 */
 	private void directedFire(Direction dir) {
 		int x = pos.getX();
 		int y = pos.getY();
 
 		int firePower = power;
 
 		for (int i = 1; i <= range; i++) { // TODO should the range be regarded
 											// as including the position of the
 											// bomb? i.e. range of 2 means
 											// player can hit only directly
 											// adjacent tiles
 			if (firePower > 0) {
 				Position firePos = new Position(x + ((int)dir.getX() * i), y + ((int)dir.getY() * i));
 				if (validPos(firePos)) {
 					Tile tile = map[firePos.getY()][firePos.getX()]; // inverted
 																		// >.<
 					if (tryBreak(tile, firePower)) {
 						Destroyable destroyableTile = (Destroyable) tile;
 						firePower -= destroyableTile.getToughness();
 						fireList.put(firePos, dir);
 					} else {
 						break; // fire stops directly
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public String getTileType() {
		return this.image;
 	}
 }
