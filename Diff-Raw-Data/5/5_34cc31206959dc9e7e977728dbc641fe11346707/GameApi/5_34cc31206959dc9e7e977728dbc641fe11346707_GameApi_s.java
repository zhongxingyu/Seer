 package edu.rit.se.sse.rapdevx.api;
 
 import java.util.List;
 import edu.rit.se.sse.rapdevx.api.dataclasses.Game;
 import edu.rit.se.sse.rapdevx.api.dataclasses.Asset;
 import edu.rit.se.sse.rapdevx.api.dataclasses.Session;
 import edu.rit.se.sse.rapdevx.api.dataclasses.GameStatus;
 import edu.rit.se.sse.rapdevx.api.dataclasses.ShipLocation;
 
 /**
  * API access to the Game object on the server side
  *
  * Approximates the ActiveRecord pattern
  *
  * @author Ben Nichoals
  */
 public class GameApi {
 	public static List<Game> listGames() {
 		return null;
 	}
 
 	public static List<Asset> getAssets(Game inputGame, Session userSession) {
 		return null;
 	}
 
 	public static GameStatus getStatus(Game inputGame) {
 		return null;
 	}
 
 	public static boolean setShipPlacement(Game inputGame, Session userSession, List<ShipLocation> ships) {
 		return false;
 	}
 
 	// Unit Move contents
 	
 	public static boolean submitUnitMove(Game inputGame, Session userSession, int currentTurn, UnitMove move) {
 		return false;
 	}
 
 	public static List<UnitMove> getCurrentMoves(Game inputGame, Session userSession, int currentTurn) {
 		return null;
 	}
 
 	public static boolean removeUnitMove(Game inputGame, Session userSession, int currentTurn, UnitMove move) {
 		return false;
 	}
 
 	// Unit Attack contents
 	
 	public static boolean submitUnitAttack(Game inputGame, Session userSession, UnitAttack attack) {
 		return false;
 	}
 
 	public static List<UnitAttack> getCurrentAttacks(Game inputGame, Session userSession, int currentTurn) {
 		return null;
 	}
 
	public static boolean removeUnitMove(Game inputGame, Session userSession, int currentTurn, UnitMove move) {
 		return false;
 	}
 
 	public static boolean finishedWithTurn(Game inputGame, Session userSession, int currentTurn) {
 		return false;
 	}
 
 	public static List<UnitMove> getResultMoves(Game inputGame, Session userSession, int currentTurn) {
 		return null;
 	}
 
 	public static List<UnitAttack> getResultAttacks(Game inputGame, Session userSession, int currentTurn) {
 		return null;
 	}
 }
