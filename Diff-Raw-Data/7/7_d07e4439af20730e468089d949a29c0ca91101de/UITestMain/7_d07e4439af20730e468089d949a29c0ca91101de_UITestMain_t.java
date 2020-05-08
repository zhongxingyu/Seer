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
		sgm.level(0).location(new Position(5, -2), Direction.NORTH).put(new game.things.SpawnPoint(sgm));
		sgm.getPlayer("Cordi").login();
		IsoInterface view = new IsoInterface("IsoTest", sgm, new ClientMessageHandlerMock(), sgm.getPlayer("Cordi").gid());
 		view.show();
 	}
 }
