 package ui.isometric.mock;
 
 import ui.isometric.IsoInterface;
 import util.*;
 import game.*;
 
 public class UITestMain {
 	private static GameWorld sgm = new GameWorld();
 
 	public static void main(String[] args) {
 		game.GameThing tile = new game.things.GroundTile(sgm);
 		sgm.level(0).location(new Position(5, 0), Direction.NORTH).put(tile);
 		sgm.level(0).location(new Position(5, 1), Direction.NORTH).put(new game.things.GroundTile(sgm, "ground_grey_water_two_sides"));
		//sgm.level(0).put(new Position(5, 2), new game.things.Wall(sgm, "wallx"));
//		Position pos51 = new Position(5,1);
//		sgm.level(0).put(pos51, new game.things.Player(sgm, pos51, Direction.SOUTH, "character_cordi_empty"));
		IsoInterface view = new IsoInterface("IsoTest", sgm, new ClientMessageHandlerMock());
 		view.show();
 	}
 }
