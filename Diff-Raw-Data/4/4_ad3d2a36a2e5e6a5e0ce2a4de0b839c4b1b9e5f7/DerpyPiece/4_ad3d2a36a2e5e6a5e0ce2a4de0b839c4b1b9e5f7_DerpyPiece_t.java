 package DerpyAI;
 
 import java.awt.Point;
 import sharedfiles.Piece;
 
 public class DerpyPiece extends Piece {
 
 	protected Point currentLocation;
 	
 	public DerpyPiece(boolean b, String id) {
 		super(b, id);
 	}

	public void setLocation(Point p) {
		currentLocation = p; 
	}
 	
 	public Point getLocation(){
 		return currentLocation; 
 	}
 
 }
