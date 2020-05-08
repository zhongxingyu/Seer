 /*
  * State
  * 
  * Version 0.1
  * 
  * Represents the Sokoban State.
  * 
  * Should not be confused with the class Map. 
  */
 
 import java.util.Arrays;
 import java.util.Vector;
 
 public class State implements Cloneable {
 
 	private Vector<Box> boxes;
 	private Vector<Cell> reachableCells; //should be HashMap?
 	private Vector<Cell> unReachableCells; //should be HashMap?
 
 	private int playerRow; // Player position in this state (after move that led to this state)
 	private int playerCol;
 
     private int lastBoxMovedIndex; // Index of the box moved that led to this state
 	private char lastMoveDir; // Move direction of box according to above that led to this state
 
 	private int parentKey; //reference to make it possible to find parent state - for final path building.
 
 
     /**
 	 * Constructs the internal representation of the State
 	 *
 	 * USED ONLY WHEN CREATING INITIAL STATE
 	 * @param
 	 */
 	public State(Vector<Box> pBoxes, int pPlayerRow, int pPlayerCol) {
 		boxes = pBoxes;
 		playerRow = pPlayerRow;
 		playerCol= pPlayerCol;
 		lastBoxMovedIndex = -1; //No box is last moved in initial state.
 		//Should maybe be set to the box the player is moved to?
 		lastMoveDir = 'I'; // Initial state 'I', no last move direction.
 
 		/*
 		 * TODO move player to one box...
 		 * 
 		 *
 		 */
 
 		for(Box box : pBoxes){
 			if(isConnected(playerRow, playerCol, box.getRow(),box.getCol())){
 				/*
 				 * Move player there
 				 * break
 				 */
 			}
 		}
 
 	} // End constructor State
 
 	/**
 	 * Constructs a state which is the result of applying one move.
 	 * "final" in arguments to avoid applying changes to parent state
 	 *
 	 * @param pParentState
 	 * @param pBoxIndex    index if the boxes vector
 	 * @param pMoveDir     the direction to move the box
 	 */
 	public State(final State pParentState, int pBoxIndex, char pMoveDir) {
 		// set boxes
 		this.boxes = (Vector<Box>) pParentState.boxes.clone();
 
 		// set player position before moving the box
 		playerRow = this.boxes.get(pBoxIndex).getRow();
 		playerCol = this.boxes.get(pBoxIndex).getCol();
 		lastBoxMovedIndex = pBoxIndex;
 		lastMoveDir = pMoveDir;
 
 		// move the box
 		this.boxes.get(pBoxIndex).move(pMoveDir);
 
         // if the box position is on goal - add to the field boxesOnGoal
         if (Board.isGoal(boxes.get(pBoxIndex).getRow(), boxes.get(pBoxIndex).getCol())){
         	boxes.get(pBoxIndex).setIsOnGoal(true);
         }
         else{
         	boxes.get(pBoxIndex).setIsOnGoal(false);
         }
 
 	} // End constructor State
 
 
 	/**
 	 * Creates a string representation for each state which is
 	 * only dependent on which cells are occupied with boxes
 	 * (irrespective of which box is where)
 	 * @return
 	 */
 	public String hashString(){
 		String[] lStringArray = new String[boxes.size()];
 
 		for (int i = 0; i < boxes.size(); i++){
 			lStringArray[i] = boxes.get(i).hashString();
 		}
 
 		Arrays.sort(lStringArray);
 
 		StringBuilder builder = new StringBuilder();
 		for(String element : lStringArray) {
 			builder.append(element);
 		}
 		return builder.toString();
 	}
 
 	public int getPlayerRow(){
 		return playerRow;
 	}
 
 	public int getPlayerCol(){
 		return playerCol;
 	}
 
     public int getLastBoxMovedIndex() {
         return lastBoxMovedIndex;
     }
 
 	/**
 	 * Does all necessary checks to see if a box is movable to the position.
 	 *
 	 * Checks are:
 	 * 1) no wall in that position
 	 * 2) no box in that position
 	 * 3) no dead lock type 0 in that position (i.e. a corner)
 	 * 4) can player reach the opposite position.
 	 *
 	 * @return
 	 */
 	public boolean tryMove(Box pBox, char pDir){
 
 		int lR = pBox.getRow();
 		int lC = pBox.getCol();
 
 		int lMoveToRow = 0;
 		int lMoveToCol = 0;
 		int lPlayerRow = 0;
 		int lPlayerCol = 0;
 
 		boolean lCorrectInput=false;
 
 		switch (pDir) {
 		case 'U':
 			lMoveToRow = lR - 1;
 			lMoveToCol = lC;
 			lPlayerRow = lR + 1;
 			lPlayerCol = lC;
 			lCorrectInput = true;
 			break;
 		case 'D':
 			lMoveToRow = lR + 1;
 			lMoveToCol = lC;
 			lPlayerRow = lR - 1;
 			lPlayerCol = lC;
 			lCorrectInput = true;
 			break;
 		case 'R':
 			lMoveToRow = lR;
 			lMoveToCol = lC + 1;
 			lPlayerRow = lR;
 			lPlayerCol = lC - 1;
 			lCorrectInput = true;
 			break;
 		case 'L':
 			lMoveToRow = lR;
 			lMoveToCol = lC - 1;
 			lPlayerRow = lR;
 			lPlayerCol = lC + 1;
 			lCorrectInput = true;
 			break;
 		}
 
 		if(!lCorrectInput){
 			if (Main.debugMode) System.out.println("StateError: TryMove: wrong direction input");
 		}
 
 		return (!Board.isWall(lMoveToRow, lMoveToCol) &&
 				!this.isBox(lMoveToRow, lMoveToCol) &&
 				!Board.isDeadLockT0(lMoveToRow, lMoveToCol) &&
 				this.isConnected(playerRow, playerCol, lPlayerRow, lPlayerCol));
 	}
 
 	public boolean isBox( int pRow, int pCol){
 		/*
 		 * TODO
 		 * 
 		 * Very inefficient way of checking must be rewritten later.
 		 */
 
 		for(Box box : boxes){
 			if (box.getRow() == pRow && box.getCol() == pCol){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isConnected(Cell pCell1, Cell pCell){
 
 		//skrivs av Adam...
 
 		return true;
 	}
 
 	private boolean isConnected(int pRow1, int pCol1,  int pRow2 , int pCol2){
 
 		//skrivs av Adam...
 
 		return true;
 	}
 	/**
 	 * Returnerar den cell som expanderades senast innan
 	 *
 	 * @param pRow1
 	 * @param pCol1
 	 * @param pRTarget
 	 * @param pCTarget
 	 * @return
 	 */
 	private Cell cellNeighbour(int pRow1, int pCol1,  int pRTarget , int pCTarget){
 
 
 		//skrivs av Adam...
 
 		return new Cell(0, 0);
 	}
 
 	/**
 	 * Operates on an (empty) Vector with States
 	 * and "fills" with all successor states
 	 *
 	 * @param pStates
 	 */
 	public void allSuccessors(Vector<State> pStates) {
 		/*
 		 * TODO
 		 * 
 		 * Idea taken from HW1
 		 * I guess that the reason for sending in an empty vector that this function
 		 * "operates" on is to avoid incorrect references...
 		 * 
 		 */
 
 		pStates.clear();
 
 		int boxIndex = 0;
 		for (Box box : boxes) {
 			/* If a move is possible, then add the new state in the pStates vector */
 			if (tryMove(box, 'U'))
 				pStates.add(new State(this, boxIndex, 'U'));
 			if (tryMove(box, 'D'))
 				pStates.add(new State(this, boxIndex, 'D'));
 			if (tryMove(box, 'R'))
 				pStates.add(new State(this, boxIndex, 'R'));
 			if (tryMove(box, 'L'))
 				pStates.add(new State(this, boxIndex, 'L'));
 			boxIndex++;
 		} // End for boxes
 	} // End allSuccessors
 
 	/**
 	 * Operates on an (empty) Vector with States
 	 * and "fills" with all successor states
 	 *
 	 * @param pStates
 	 */
 	public void selectiveSuccessors(Vector<State> pStates, int[] pBoxIndexes) {
 
 		pStates.clear();
 
 		for (int index : pBoxIndexes) {
 			/* If a move is possible, then add the new state in the pStates vector */
 			if (tryMove(boxes.get(index), 'U'))
 				pStates.add(new State(this, index, 'U'));
 			if (tryMove(boxes.get(index), 'D'))
 				pStates.add(new State(this, index, 'D'));
 			if (tryMove(boxes.get(index), 'R'))
 				pStates.add(new State(this, index, 'R'));
 			if (tryMove(boxes.get(index), 'L'))
 				pStates.add(new State(this, index, 'L'));
 		} // End for boxes
 	} // End allSuccessors
 
 
     public boolean isFinalState() {
     	
     	boolean allOnGoal = true;
     	
     	for (Box box : boxes){
     		if(!box.isOnGoal()){
     			return false;
     		}
     	}
     	return allOnGoal;
     }
 
 } // End Class State
