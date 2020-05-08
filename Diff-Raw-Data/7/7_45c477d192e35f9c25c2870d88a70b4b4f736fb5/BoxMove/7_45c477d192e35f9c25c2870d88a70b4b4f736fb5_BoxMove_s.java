 package model;
 
 public class BoxMove {
 	Position pos;
 	String pPath;
 	
 	public BoxMove(Position newPosition, String playerPath) {
 		pos = newPosition;
 		pPath = playerPath;
 	}
 
 	public Position getNewPosition() {
 		return pos;
 	}
 
 	public String getPlayerPath() {
 		return pPath;
 	}
 
 }
