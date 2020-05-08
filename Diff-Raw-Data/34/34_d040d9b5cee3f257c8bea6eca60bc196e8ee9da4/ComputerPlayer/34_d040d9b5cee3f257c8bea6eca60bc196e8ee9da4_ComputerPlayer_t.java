 
 package battleship;
 
 import java.util.HashSet;
 import battleship.Player.FleetType;
 
 /**
  * Computer player. Should mainly be concerned with generating coordinates for where to put ships and where to shoot. 
  * @author Victor, Wiktor
  *
  */
 public class ComputerPlayer extends Player {
 	
 	boolean isPlaced;
 
 	public ComputerPlayer(int size,String alias,String source) {
 		super(size,alias,source);
 	}
 
 	@Override
 	public void placeShips(Map map) {
 		for (Integer[] ship : fleet.keySet()) {
 			isPlaced=false;
 			while(isPlaced == false){
 				int[] start = new int[2];
 				
 				// Random placement
 				for (int i =0; i< 2; i++) {
 					// Generate a random number in the range [0,map.getSize()-1]
 					
 					start[i] = 0 + (int)(Math.random()*map.getSize()-1);
 				}
 				Coordinate startCoordinate = new Coordinate(
 						Integer.valueOf(start[0]), Integer.valueOf(start[1]));
 				
 				// Random orientation (vertical/horizontal)
 				int orientation = 0 + (int)(Math.random()*2);
 				
				isPlaced=checkAndPlace(map, ship, startCoordinate, orientation);
 			}
 		}
 		map.drawSetupMap();
 	}
 
 
 	@Override
 	public Coordinate generateMove(Map map) {
 		map.drawGameMap();
 		int[] start = new int[2];
 		Boolean isValidMove = false;
 		
 		Coordinate c = null;
 		
 		do{
 			for (int i =0; i< 2; i++) {
 				// Generate a random number in the range [0,map.getSize()-1]
 				start[i] = 0 + (int)(Math.random()*map.getSize());
 			}
 			//Create the coordinate
 			c = new Coordinate(
 					Integer.valueOf(start[0]), Integer.valueOf(start[1]));
 		
 			
 			
 			isValidMove = map.isSquareNotHit(c);			
 			}while(!isValidMove);
 		System.out.println(c.getX()+","+c.getY());
 		
 		return c;
 	}
 
 }
