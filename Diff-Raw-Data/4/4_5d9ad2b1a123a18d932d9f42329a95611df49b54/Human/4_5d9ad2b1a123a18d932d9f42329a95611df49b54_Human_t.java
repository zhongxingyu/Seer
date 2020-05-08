 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Human extends Player {
 	
 	private Scanner in;
 
 	//public Human(String startColor, boolean startsOnZeroSide) {
 		//super(startColor, startsOnZeroSide);
 	//}
 	
 	public Human (String startColor, boolean startsOnZeroSide, Robot startGameRobot) {
 		super(startColor, startsOnZeroSide, startGameRobot);
 	}
 
 	public Human (String startXO, boolean startOnZeroSide) {
 		super(startXO, startOnZeroSide);
 		this.in = new Scanner(System.in);
 	}
 
 	public void takeTurn(Game g) {
 		super.performMove(this.inputMove(g), g.getGameBoard());
 	}
 
 	public Move inputMove(Game g) {
 		if (this.getRobot()!=null) {
 			//creates dictionary to hold scanned values
 			ArrayList<int[]> scannedLocations = new ArrayList<int[]>();
 			ArrayList<String> locationValues = new ArrayList<String>();
 			//gets list of moves from best to worst
 			Move[] possibleMoves = this.rankBestMoves(g, 1);
 			
 			//iterates over all possible moves
 			for (Move m : possibleMoves) {
 				//gets all waypoints of the move
 				int[][] waypoints = m.getWaypoints();
 				//declares pointColor variable
 				String pointColor;
 				
 				//declares variable to determine if the loop needs to continue
 				boolean shouldContinue = false;
 				//iterates over all waypoints which should be empty
 				for (int[] waypoint : ArraysHelper.copyOfRange(waypoints, 0, waypoints.length-1)) {
 					if (scannedLocations.contains(waypoint)) {
 						pointColor = locationValues.get(scannedLocations.indexOf(waypoint));
 					} else {
 						pointColor = this.getRobot().examineLocation(waypoint);
 						scannedLocations.add(waypoint);
 						locationValues.add(pointColor);
 					}	
 					//checks if the square is not empty
 					if (pointColor!=Board.color) {
 						shouldContinue = true;
 						break;
 					}
 				}
 				//continue if the move failed
 				if (shouldContinue) {
 					continue;
 				}
 				
 				//sets last waypoint
 				int[] waypoint = waypoints[waypoints.length-1];
 				//checks the color of the last waypoint
 				if (scannedLocations.contains(waypoint)) {
 					pointColor = locationValues.get(scannedLocations.indexOf(waypoint));
 				} else {
 					pointColor = this.getRobot().examineLocation(waypoints[waypoints.length-1]);
 					scannedLocations.add(waypoint);
 					locationValues.add(pointColor);
 				}
 				//checks if the correct piece is not on the square
 				if (pointColor!=this.getColor()) {
 					continue;
 				} else {
 					//this must be the right move, so return it
 					return m;
 				}
 			}
 			
 			//failed to find the right move
 			return null;
 		
 		} else {
 			System.out.println();
 			for (int y : new int[] {7,6,5,4,3,2,1,0}) {
 				String[] theLine = new String[8];
 				for (int x : new int[] {0,1,2,3,4,5,6,7}) {
 					if (super.getBoard().getPieceAtLocation(new int[] {x,y}) != null) {
 						theLine[x] = super.getBoard().getPieceAtLocation(new int[] {x,y}).getPlayer().getXO();
 					} else {
 						theLine[x] = "-";
 					}
 				}
 				for (String s : theLine) {
 					System.out.print(s+" ");
 				}
 				System.out.println();
 			}
 			System.out.println();
 			
 			boolean moveEntered = false;
 			Move inputtedMove = null;
 			while (!moveEntered) {
 				System.out.println("Enter Move:");
 				String inputLine = this.in.nextLine();
 				if ((inputLine.length()+1)%3==0) {
 					inputLine = inputLine + " ";
 				}
 				if (inputLine.length()%3==0) {
 
 					int numberOfWaypoints = inputLine.length()/3;
 					String[] waypointStrings = new String[numberOfWaypoints];
 					int[][] allWaypoints = new int[numberOfWaypoints][];
 					for (int i=0; i<numberOfWaypoints; i++) {
 						waypointStrings[i] = inputLine.substring(3*i,3*(i+1));
 					}
 					boolean validMove = true;
 					//CORBIN IS WORKING HERE
 					//for (String waypointString : waypointStrings) {
 						////could also test if first two characters are numbers
 						//if (waypointString.substring(2) != " ") {
 							//System.out.println("test");
 							//validMove = false; 
 						//}
 					//}
 					if (validMove) {
 						System.out.println("gothere");
 						for (int i=0; i<numberOfWaypoints; i++) {
 							allWaypoints[i] = new int[] {Integer.parseInt(inputLine.substring(3*i, 3*i+1)),Integer.parseInt(inputLine.substring(3*i+1, 3*i+2))};
 						}
						inputtedMove = new Move(this.getBoard().getPieceAtLocation(allWaypoints[0]), allWaypoints);
						if (inputtedMove.getMovePiece()!=null && inputtedMove.getMovePiece().getPlayer()==this && inputtedMove.calculateIsValid()) {
 							moveEntered = true;
 						}
 					}
 				}
 			}
 			return inputtedMove;
 		}
 	}
 }
