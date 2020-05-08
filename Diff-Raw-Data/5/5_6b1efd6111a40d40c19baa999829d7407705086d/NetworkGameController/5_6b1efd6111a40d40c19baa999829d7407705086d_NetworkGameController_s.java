 package src.net;
 
 import java.util.Collection;
 
 import src.GameController;
 import src.ui.IDrawableCreep;
 import src.ui.IDrawableTower;
 
 /**
  * Manages the representation of a remote game.  NetworkGameController
  * is used to populate the MapComponent that represents the oppponent's
  * map.
  */
 public class NetworkGameController extends GameController {
 	private NetworkGame game;
 	
 	public NetworkGameController(NetworkGame g) {
 		game = g;
 	}
 	
 	public Collection<? extends IDrawableCreep> getDrawableCreeps() {
		return game.getCreeps();
 	}
 	
 	public Collection<? extends IDrawableTower> getDrawableTowers() {
		return game.getTowers();
 	}
 }
