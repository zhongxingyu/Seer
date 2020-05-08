 package checkerssolitare;
 
 import java.util.LinkedList;
 
 import javax.swing.JFrame;
 
 
 import framework.*;
 
 /*
  * A RushHour puzzle is a 6x6 matrix of characters.
  * The RushHour puzzle implements the Puzzle interface methods:
  * 		void initialPosition() 
  * 		isGoal() 
  *      LinkedList legalMoves(Node node)
  *                   
  * The RushHour puzzle implements several helper methods
  *      RushHour move(Move m)
  *      boolean emptyMove(Move m, Node node)
  *      boolean hasVehicle(pos_x, pos_y, vehicle, length, direction)
  *      void placeVehicle(pos_x, pos_y, vehicle, length, direction)
  *      public LinkedList transform()
  *            
  * The RushHour puzzle implements equal and hashCode. The implementations override the 
  * default ones inherited from Object.                      
  * 	              
  */
 
 public class CheckersSolitare implements Puzzle {
 	char[][] matrix = new char[7][7];
 
 	
 	public CheckersSolitare() {
 		for (int i = 0; i<7; i++) {
 			for (int j = 0; j<7; j++) {
 				if((j<2 || j>4) && (i<2 || i>4)) {
 					matrix[i][j] = 'B';
 				} else {
 					matrix[i][j] = 'O';
 				}
 			}
 		}
 		
 	}
 	/*
 	 * Reads an initial position from a list of predefined configurations.
 	 * The initial position comes as a list of vehicle placements.
 	 * An iterator over the list of vehicle placements reads each 
 	 * vehicle placement (given in a VehicleNode object) and places
 	 * the vehicle on the matrix by calling helper placeVehicle(...). 
 	 * @see framework.Puzzle#initialPosition()
 	 */
 	public void initialPosition() {
 		for (int i = 0; i<7; i++) {
 			for (int j = 0; j<7; j++) {
 				if(((j<2 || j>4) && (i<2 || i>4)) || (j==3 && i==3))  {
 					continue;
 				} else {
 					matrix[i][j] = 'C';
 				}
 			}
 		}	
 	}
 	/*
 	 * Places vehicle "vehicle" of length "length" in direction "direction"
 	 * at row "pos_x", column "pos_y" 
 	 * 
 	 * direction (0,1): 0 means horizontal and 1 means vertical
 	 */
 	// TODO: direction should be coded with symbolic constants
 	
 	/*
 	 * Checks if there is a vehicle "vehicle" of length "length" placed at row "pos_x", column "pos_y"
 	 * in direction "direction" 
 	 */
 	
 	private boolean hasChecker(int pos_x, int pos_y) {
 		return matrix[pos_x][pos_y] == 'C';
 	}
 	
 	
 	/*
 	 * Checks if the puzzle is in a "solved" position.
 	 * Checks if the Ice Cream truck (vehicle 'X'), which occupies 
 	 * cells pos_X-1 and pos_X at row 2, is free to go --- 
 	 * i.e., cells 2,pos_X+1 through 2,5 are empty 
 	 */	
 	public boolean isGoal() {
 		if (this.hasChecker(3, 3)) {
 			for(int i=0; i<7; i++){
 				for(int j=0; j<7; j++){
 					if(((j<2 || j>4) && (i<2 || i>4)) || (i==3 && j==3)) {
 						continue;
 					}
 					if(this.hasChecker(i, j)) {
 						return false;
 					}
 				}
 			}
 			return true;
 		} 
 		return false;
 	}
 
 	/*
 	 * Transforms the current matrix into a linked list of VehicleNodes
 	 * Each VehicleNode contains the row, column, vehicle and direction 
 	 * of vehicle placement. Needed by RushHourPanel.  
 	 */
 	
 	private LinkedList transform() {
 		LinkedList theList = new LinkedList();
 		
 		for (int i=0; i<7; i++) {
 			for (int j=0; j<7; j++) {
 				if ((j<2 || j>4) && (i<2 || i>4)) {
 					continue;
 				}
 				CheckerNode cNode = new CheckerNode(i,j,matrix[i][j]);
 				theList.add(cNode);
 			}
 		}
 		
 		return theList;
 	}
 
 	
 	/*
 	 * drawSolution takes a linked list of Nodes representing the solution, 
 	 * crates a frame and draws the solution.
 	 */
 	
 	public void drawSolution(LinkedList theList) {
 		
 		JFrame frame = new JFrame("CheckersSolitare");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(600,600);
         
         frame.setVisible(true);
         
         for (Object o : theList) {
         	CheckersSolitare puzzle = (CheckersSolitare) o;
         	CheckersSolitarePanel checkersSolitarePanel = new CheckersSolitarePanel(puzzle.transform());
         	frame.getContentPane().add(checkersSolitarePanel);
         	frame.show();
         	        			
         	// an idle loop that delays the display of a position; makes moves visible
         	long t0,t1;
         	     
         	t0=System.currentTimeMillis();
         	     
         	do {
         		t1=System.currentTimeMillis();
             }
      	    while (t1-t0<500);
         	        
         }
 	}
 	
 	
 	/*
 	 * Returns a list of all positions that can be reached from the current position 
 	 * by taking one legal move. Parameter "node" is needed only for the purposes of
 	 * emptyMove (if a potential move is determined to be empty, then it is not 
 	 * placed on the list).
 	 */
 	
 	public LinkedList legalMoves(Node node) {
 		
 		
 		LinkedList theSet = new LinkedList();
 		for (int i=0; i<7; i++) {
 			for (int j=0; j<7; j++) { 
 				if (!this.hasChecker(i, j)){
 					continue;
 				} else {
 					//left move
 					if (matrix[i-1][j] == 'C' && matrix[i-2][j] == 'O') {
 						Move theMove = new Move(i,j,0);
 						CheckersSolitare position = move(theMove);
 						theSet.add(position);
 					}
 					//right move
 					if (matrix[i+1][j] == 'C' && matrix[i+2][j] == 'O') {
 						Move theMove = new Move(i,j,1);
 						CheckersSolitare position = move(theMove);
 						theSet.add(position);
 					}
 					//up move
 					if (matrix[i][j-1] == 'C' && matrix[i][j-2] == 'O') {
 						Move theMove = new Move(i,j,2);
 						CheckersSolitare position = move(theMove);
 						theSet.add(position);
 					}
 					//down move
 					if (matrix[i][j+1] == 'C' && matrix[i][j+2] == 'O') {
 						Move theMove = new Move(i,j,3);
 						CheckersSolitare position = move(theMove);
 						theSet.add(position);
 					}
 				}
 			}
 		}
 		return theSet;
 	}
 
 	
 	/*
 	 * Creates a new RushHour puzzle, which represents the new position resulting 
 	 * from taking move "theMove".
 	 * The returned RushHour puzzle (newPosition) is the same as the current one, except in 
 	 * exactly one vehicle which is moved left, right, up or down by one cell. 
 	 */
 	
 	private CheckersSolitare move(Move theMove) {
 		CheckersSolitare newPosition = this.clone();
 		
 		switch (theMove.direction) {
 		case 0: //left
 			newPosition.matrix[theMove.pos_x][theMove.pos_y] = 'O';
 			newPosition.matrix[theMove.pos_x-1][theMove.pos_y] = 'O';
 			newPosition.matrix[theMove.pos_x-2][theMove.pos_y] = 'C';
 		case 1: //right
 			newPosition.matrix[theMove.pos_x][theMove.pos_y] = 'O';
 			newPosition.matrix[theMove.pos_x+1][theMove.pos_y] = 'O';
 			newPosition.matrix[theMove.pos_x+2][theMove.pos_y] = 'C';
 		case 2: //up
 			newPosition.matrix[theMove.pos_y][theMove.pos_x] = 'O';
 			newPosition.matrix[theMove.pos_y-1][theMove.pos_x] = 'O';
 			newPosition.matrix[theMove.pos_y-2][theMove.pos_x] = 'C';
 		case 3: //down
 			newPosition.matrix[theMove.pos_y][theMove.pos_x] = 'O';
 			newPosition.matrix[theMove.pos_y+1][theMove.pos_x] = 'O';
 			newPosition.matrix[theMove.pos_y+2][theMove.pos_x] = 'C';
 		}
 		
 		return newPosition;
 	}
 	
 	public CheckersSolitare clone() {
 		
 		CheckersSolitare newPosition = new CheckersSolitare();
		for (int i=0; i<6; i++)
			for (int j=0; j<6; j++) 
 				newPosition.matrix[i][j] = matrix[i][j];
 		
 		return newPosition;
 	}
 	
 	/*
 	 * equals and hashCode implement value equality for RushHour objects.
 	 * Two objects are equal if their matrices are equal. The methods override
 	 * the default Object.equals and hashCode which implement reference equality.
 	 * Note: every class that overrides equal must override hashCode as well!!!
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	
 	public boolean equals(Object o) {
 	    
 		CheckersSolitare other = (CheckersSolitare) o;
 		for (int i=0; i<7; i++)
 			for (int j=0; j<7; j++) 
 				if (matrix[i][j] != other.matrix[i][j]) return false;
 		
 		return true;
 	}
 	
 	public int hashCode() {
 		int code=0;
 		for (int i=0; i<7; i++)
 			for (int j=0; j<7; j++) 
 				code = code+(matrix[i][j]*i+j);
 		return code;
 	}
 	
 
 	/*
 	 * The class encapsulates a move. Each move is defined as
 	 * row (pos_x), column (pos_y), direction (0 - left, 1 - right, 2 - up, 3 - down).
 	 * The vehicle at pos_x, pos_y is moved one block in direction direction.
 	 * TODO: direction should be coded with meaningful symbolic constants!
 	 */
 	
 	class Move {
 		int pos_x;
 		int pos_y;
 		int direction;
 		
 		Move(int pos_x, int pos_y, int direction) {
 			this.pos_x = pos_x;
 			this.pos_y = pos_y;
 			this.direction = direction;
 		}
 		
 	}
 
 	
 }
