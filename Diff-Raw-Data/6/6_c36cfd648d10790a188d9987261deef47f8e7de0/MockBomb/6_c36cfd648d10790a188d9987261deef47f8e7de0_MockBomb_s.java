 package com.github.joakimpersson.tda367.model.tiles.bombs;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 
 import com.github.joakimpersson.tda367.model.constants.Direction;
 import com.github.joakimpersson.tda367.model.player.Player;
 import com.github.joakimpersson.tda367.model.tiles.Tile;
 import com.github.joakimpersson.tda367.model.tiles.bombs.Bomb;
 import com.github.joakimpersson.tda367.model.utils.Position;
 
 /**
  * 
  * @author joakimpersson
  *
  */
 public class MockBomb extends Bomb {
 
 	public MockBomb(Player p, Timer t) {
 		super(p, t);
 	}
 
 	// TODO Fix the test for the new returntype: Map<Position, Direction>.
 	@Override
 	public Map<Position, Direction> explode(Tile[][] map) {
 		throw new UnsupportedOperationException();
 	}
 
 }
