 package sokoban;
 /*
  * Solver
  * 
  * Version 0.1
  * 
  * 2013.09.19
  *
  */
 
 
 import java.util.*;
 
 
 
 
 public class Solver {
 	private State processState;
 	
 	/*
 	 * The key-String represents the box configuration only.
 	 * 
 	 * If this configuration has "been visited" before a vector is returned
 	 * containing the DIFFERNT states visited whith this config. (but whith
 	 * player in different subspaces...)
 	 */
 	HashSet<State> visitedStates = new HashSet<State>();
 	Comparator comparator = new StatePriorityComparator();
 	PriorityQueue<State> simpleQueue = new PriorityQueue<State>(10, comparator);
 
 	
 	public Solver(){
 		
 		/*
 		 *TODO
 		 *
 		 * in the Board-object is everything needed; constant parameters from the
 		 * Board object it self and the initial state which should be expanded;
 		 *  
 		 */
 	}
 	
 	/* Must check if this hash function is a good one!*/ 
 	public static String getHashString(int row,int col){
 		return  String.valueOf(row) + String.valueOf(col);
 	}
 
 	// Tried to improve old getFinalState() with a* implementation (did not work(because of longer time)). 
 	public State aStar(){
 		
 		simpleQueue.offer(Board.getInitialState());
 		
 		while(simpleQueue.peek().isFinalState() == false){
 			
 			processState = simpleQueue.remove();
 			visitedStates.add(processState);
 			
 			Vector<State> childsOfCurState = new Vector<State>();
 			processState.allSuccessors(childsOfCurState); //fills with all children
 			
 			for (State child : childsOfCurState){
 				
 				int cost = child.getG() + 1; 
 				
 				if(simpleQueue.contains(child) == true && cost < child.getG()){
 					simpleQueue.remove(child);
 				}
 				
 				if(simpleQueue.contains(child) == false && visitedStates.contains(child) == false){
 					simpleQueue.add(child);
 				}
 			}	
 		}
 		return simpleQueue.remove();
 	}
 
 
 	public State greedyBFS(){
 		
 		int lIterations = 0;
 		
 		simpleQueue.add(Board.getInitialState());
 		visitedStates.add(Board.getInitialState());
 		
 		boolean lfoundFinalState = false;
 
 		while(!lfoundFinalState && lIterations<500000 && !simpleQueue.isEmpty() ){
 			
 			lIterations++;
 			
 			State lCurState = simpleQueue.poll();
 
 			//Visualizer.printState(lCurState, "--- State explored in iteration: #" + lIterations + " ---");
 			
 			if (lCurState.isFinalState()){
 				if (Sokoban.debugMode) Visualizer.printState(lCurState, "THE FINAL STATE IS FOUND! See below:");
 				lfoundFinalState = true;
 				return lCurState;
 			}
 			else{
 				Vector<State> childrenOfCurState = new Vector<State>();
 				lCurState.allSuccessors(childrenOfCurState); //fills with all children
 				for (State child : childrenOfCurState){
 					if(!visitedStates.contains(child)){
 						visitedStates.add(child);
 						simpleQueue.add(child);
 						//Visualizer.printState(child, "accepted child in iteration: #" + lIterations);
 					}
 					else{
 						//Visualizer.printState(child, "Rejected child in iteration: #" + lIterations);
 					}
 				}
 			}
 			
 			//System.out.println("States in queue: " + simpleQueue.size());
 
 
 		}//while (searching for final state in queue)
         if(Sokoban.debugMode)
 		    System.out.println("Solver line 77: No final sate was found, returned initial state.");
 
 		return Board.getInitialState();
 	}
 	
 	
 	/**
 	 * Returns move necessary from cell to first parent cell.
 	 * @param aCell
 	 * @return
 	 */
 	public static String strChecker(Cell pCell){
 		
 		if(pCell.getRow() == pCell.getParent().getRow()){
 			if(pCell.getCol() > pCell.getParent().getCol()){
 				return "l";
 			}
 			else{
 				return "r";
 			}
 		}
 		else{
 			if(pCell.getRow() > pCell.getParent().getRow()){
 				return "u";
 			}
 			else{
 				return "d";
 			}
 		}
 	}
 
 	/**
 	 * Returns moves necessary to this cell from all parent cells.
 	 * @param pCell
 	 * @return
 	 */
 	public static String strPath(Cell pCell){
 		
 		String strMoves = "";
 		while(pCell.getParent() != null){
 			strMoves += strChecker(pCell); //+ strMoves;
 			pCell = pCell.getParent();
 		}
 		//strMoves += strChecker(pCell);
 		return strMoves;
 	}
 	
 	/**
 	 * Returns cell with position next to box.
 	 * @param state
 	 * @param pRow
 	 * @param pCol
 	 * @param pRowBox
 	 * @param pColBox
 	 * @return
 	 */
 	public static Cell cellNeighborToBox(State state,
                                          int pRow, int pCol, int pRowBox, int pColBox) {
 		
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowBox,pColBox);
 		
 		boolean bolFinished = false;
 		Cell lCellNeighbor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChildren = new PriorityQueue<Cell>(10,comparator);
         
         lQueChildren.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChildren.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChildren.remove();
 			int lRow = lCellChild.getRow();
 			int lCol = lCellChild.getCol();
 			int incInt = -1;
 
 			while(incInt <= 1){
 				//Make row child (path).
 				int lRowChild = lRow + incInt;
 				String hashIndex1 = getHashString(lRowChild,lCol);
 				
 				if(!lSetPrPaths.contains(hashIndex1)){
 					//Add child to previous paths.
 					lSetPrPaths.add(hashIndex1);
 			
 					if(!Board.isWall(lRowChild, lCol)){
 						
 						if(!state.isBox(lRowChild,lCol)){
 							//If we are not finished add child to queue.
 							lQueChildren.add(new Cell(lRowChild,lCol));
 						}
 						else if(lRowChild == pEndCell.getRow() &&
 								lCol == pEndCell.getCol()){
 							//If we have found path return cell which is next to searched position.
 							lCellNeighbor = lCellChild;
 							bolFinished = true;
 							break;
 						}
 					}
 				}
 				
 				//Repeat for column child (paths).
 				int lColChild = lCol + incInt;
 				String hashIndex2 = getHashString(lRow,lColChild);
 				
 				if(!lSetPrPaths.contains(hashIndex2)){
 					lSetPrPaths.add(hashIndex2);	
 					
 					if(!Board.isWall(lRow, lColChild)){
 						
 						if(!state.isBox(lRow,lColChild)){
 							lQueChildren.add(new Cell(lRow,lColChild));
 						}
 						else if(lRow == pEndCell.getRow() &&
 								lColChild == pEndCell.getCol()){
 							lCellNeighbor = lCellChild;
 							bolFinished = true;
 							break;
 						}
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		return lCellNeighbor;
 	}
 	
 	/**
 	 * Returns linked cell with position next to box.
 	 * @param state
 	 * @param pRow
 	 * @param pCol
 	 * @param pRowBox
 	 * @param pColBox
 	 * @return
 	 */
 	public static Cell cellLinkedNeighborToBox(State state,
                                                int pRow, int pCol, int pRowBox, int pColBox) {
 
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowBox,pColBox);
 		boolean bolFinished = false;
 		Cell lCellNeighbor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChildren = new PriorityQueue<Cell>(10,comparator);
         
         lQueChildren.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChildren.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChildren.remove();
 			int lRow = lCellChild.getRow();
 			int lCol = lCellChild.getCol();
 			int incInt = -1;
 
 			while(incInt <= 1){
 				int lRowChild = lRow + incInt;
 				String hashIndex1 = getHashString(lRowChild,lCol);
 				
 				if(!lSetPrPaths.contains(hashIndex1)){
 					lSetPrPaths.add(hashIndex1);
 			
 					if(!Board.isWall(lRowChild, lCol)){
 						
 						if(!state.isBox(lRowChild,lCol)){
 							lQueChildren.add(new Cell(lCellChild,lRowChild,lCol));
 						}
 						else if(lRowChild == pEndCell.getRow() &&
 								lCol == pEndCell.getCol()){
 							lCellNeighbor = new Cell(lCellChild,lRowChild,lCol);
 							bolFinished = true;
 							break;
 						}
 					}
 				}
 				int lColChild = lCol + incInt;
 				String hashIndex2 = getHashString(lRow,lColChild);
 				
 				if(!lSetPrPaths.contains(hashIndex2)){
 					lSetPrPaths.add(hashIndex2);	
 					
 					if(!Board.isWall(lRow, lColChild)){
 						
 						if(!state.isBox(lRow,lColChild)){
 							lQueChildren.add(new Cell(lCellChild,lRow,lColChild));
 						}
 						else if(lRow == pEndCell.getRow() &&
 								lColChild == pEndCell.getCol()){
 							lCellNeighbor = new Cell(lCellChild,lRow,lColChild);
 							bolFinished = true;
 							break;
 						}
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		if(lCellNeighbor != null){
 			return lCellNeighbor.getParent();
 		}
 		else{
 			return lCellNeighbor;
 		}
 	}
 	/**
 	 * Returns linked cell with position next to some position.
 	 * @param state
 	 * @param pRow
 	 * @param pCol
 	 * @param pRowBox
 	 * @param pColBox
 	 * @return
 	 */
 	public static Cell cellLinkedNeighborToPath(State pState,
                                                 int pRow, int pCol, int pRowPath, int pColPath) {
 
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowPath,pColPath);
 		boolean bolFinished = false;
 		Cell lCellNeighbor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChildren = new PriorityQueue<Cell>(10,comparator);
         
         lQueChildren.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChildren.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChildren.remove();
 			int lRow = lCellChild.getRow();
 			int lCol = lCellChild.getCol();
 			int incInt = -1;
 
 			while(incInt <= 1){
 				int lRowChild = lRow + incInt;
 				String hashIndex1 = getHashString(lRowChild,lCol);
 				
 				if(!lSetPrPaths.contains(hashIndex1)){
 					lSetPrPaths.add(hashIndex1);
 			
 					if(!Board.isFree(pState,lRowChild, lCol)){
 						
 						if(lRowChild == pEndCell.getRow() &&
 								lCol == pEndCell.getCol()){
 							lCellNeighbor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChildren.add(new Cell(lCellChild,lRowChild,lCol));
 						}	
 					}
 				}
 				int lColChild = lCol + incInt;
 				String hashIndex2 = getHashString(lRow,lColChild);
 				
 				if(!lSetPrPaths.contains(hashIndex2)){
 					lSetPrPaths.add(hashIndex2);
 			
 					if(!Board.isFree(pState,lRow, lColChild)){
 						
 						if(lRow == pEndCell.getRow() &&
 								lColChild == pEndCell.getCol()){
 							lCellNeighbor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChildren.add(new Cell(lCellChild,lRow,lColChild));
 						}	
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		return lCellNeighbor;
 	}
 	
 	/**
 	 * Returns linked cell from some position to another position.
 	 * @param pState
 	 * @param pRow
 	 * @param pCol
 	 * @param pRowPath
 	 * @param pColPath
 	 * @return
 	 */
 	public static Cell cellLinkedToPath(State pState,
                                         int pRow, int pCol, int pRowPath, int pColPath) {
 
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowPath,pColPath);
 		boolean bolFinished = false;
 		Cell lCellNeighbor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChildren = new PriorityQueue<Cell>(10,comparator);
         
         lQueChildren.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 //		if(pRow == pRowPath && pCol == pColPath){
 	//		lCellNeighbor = new Cell()
 		//}
 		
 		while(!lQueChildren.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChildren.remove();
 			int lRow = lCellChild.getRow();
 			int lCol = lCellChild.getCol();
 			int incInt = -1;
 
 			while(incInt <= 1){
 				int lRowChild = lRow + incInt;
 				String hashIndex1 = getHashString(lRowChild,lCol);
 				
 				if(!lSetPrPaths.contains(hashIndex1)){
 					lSetPrPaths.add(hashIndex1);
 			
 					if(Board.isFree(pState,lRowChild, lCol)){
 						
 						if(lRowChild == pEndCell.getRow() &&
 								lCol == pEndCell.getCol()){
 							lCellNeighbor = new Cell(lCellChild,lRowChild,lCol);
 							bolFinished = true;
 						}
 						else{
 							lQueChildren.add(new Cell(lCellChild,lRowChild,lCol));
 						}	
 					}
 				}
 				int lColChild = lCol + incInt;
 				String hashIndex2 = getHashString(lRow,lColChild);
 				
 				if(!lSetPrPaths.contains(hashIndex2)){
 					lSetPrPaths.add(hashIndex2);
 			
 					if(Board.isFree(pState,lRow, lColChild)){
 						
 						if(lRow == pEndCell.getRow() &&
 								lColChild == pEndCell.getCol()){
 							lCellNeighbor = new Cell(lCellChild,lRow,lColChild);
 							bolFinished = true;
 						}
 						else{
 							lQueChildren.add(new Cell(lCellChild,lRow,lColChild));
 						}	
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		return lCellNeighbor;
 	}
 	
 	public Cell cellNeighborToPath2(State pState, int pRow, int pCol, int pRowPath, int pColPath){
 		return null;
 	}
 	
 	/**
 	 * Returns cell next to some position.
 	 * @param pState
 	 * @param pRow
 	 * @param pCol
 	 * @param pRowPath
 	 * @param pColPath
 	 * @return
 	 */
 	public static Cell cellNeighborToPath(State pState,
                                           int pRow, int pCol, int pRowPath, int pColPath) {
 
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowPath,pColPath);
 		boolean bolFinished = false;
 		Cell lCellNeighbor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
         
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChildren = new PriorityQueue<Cell>(10,comparator);
         
         lQueChildren.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChildren.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChildren.remove();
 			int lRow = lCellChild.getRow();
 			int lCol = lCellChild.getCol();
 			int incInt = -1;
 
 			while(incInt <= 1){
 				int lRowChild = lRow + incInt;
 				String hashIndex1 = getHashString(lRowChild,lCol);
 				
 				if(!lSetPrPaths.contains(hashIndex1)){
 					lSetPrPaths.add(hashIndex1);
 			
 					if(Board.isFree(pState,lRowChild, lCol)){
 						
 						if(lRowChild == pEndCell.getRow() &&
 								lCol == pEndCell.getCol()){
 							lCellNeighbor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChildren.add(new Cell(lRowChild,lCol));
 						}	
 					}
 				}
 				int lColChild = lCol + incInt;
 				String hashIndex2 = getHashString(lRow,lColChild);
 				
 				if(!lSetPrPaths.contains(hashIndex2)){
 					lSetPrPaths.add(hashIndex2);
 			
 					if(Board.isFree(pState,lRow, lColChild)){
 						
 						if(lRow == pEndCell.getRow() &&
 								lColChild == pEndCell.getCol()){
 							lCellNeighbor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChildren.add(new Cell(lRow,lColChild));
 						}	
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		return lCellNeighbor;
 	}
 
 	
 	/**
 	 * Returns true if there it is possible to move next to box.
 	 * @param state
 	 * @param pStartCell
 	 * @param pEndCell
 	 * @return
 	 */
 	public static boolean isPathToBox(State pState ,int pRow,int pCol,int pRowBox,
 			int pColBox){
 		Cell lCell = cellNeighborToBox(pState, pRow, pCol, pRowBox, pColBox);
 		return (lCell != null);
 	}
 	
 	/**
 	 * Returns true if there is path to some position.
 	 * @param pState
 	 * @param pRow
 	 * @param pCol
 	 * @param pRowPath
 	 * @param pColPath
 	 * @return
 	 */
 	public static boolean isPathToPath(State pState,int pRow,int pCol,int pRowPath,
 			int pColPath){
 		if(pRow==pRowPath && pCol==pColPath) //for some reason the function otherwise return false...
 			return true;
 		
 		Cell lCell = cellNeighborToPath(pState, pRow, pCol, pRowPath, pColPath);
 		return (lCell != null);
 	}
 	
 	/**
 	 * Returns cell with needed player position to perform this state.
 	 * @param pState
 	 * @return
 	 */
 	public static Cell cellLinkedToState(State pState){
 		char lMove = pState.getCharLastMove();
 		
 		Cell lCellNeededPlayerPos = null;
 		
 		if(lMove == 'U'){
 			lCellNeededPlayerPos = new Cell(pState.getPlayerRow()+1,pState.getPlayerCol());
 		}
 		else if(lMove == 'D'){
 			lCellNeededPlayerPos = new Cell(pState.getPlayerRow()-1,pState.getPlayerCol());
 		}
 		else if(lMove == 'L'){
 			lCellNeededPlayerPos = new Cell(pState.getPlayerRow(),pState.getPlayerCol()+1);
 		}
 		else if(lMove == 'R'){
 			lCellNeededPlayerPos = new Cell(pState.getPlayerRow(),pState.getPlayerCol()-1);
 		}
 		return lCellNeededPlayerPos;
 	}
 	
 	/**
 	 * Returns solution string from goal state endState.
 	 * @param pEndState
 	 * @return
 	 */
 	public static String getStrToGoal(State pEndState){
 
 		String goalString = "";
 		
 		while(pEndState.getParent().getParent()!=null){
 			//goalString += pEndState.getCharLastMove();
 			goalString = pEndState.getCharLastMove() + goalString;
 
 			//If not same box is moving we have to player path between the successive states.			
 			Cell currentPos = cellLinkedToState(pEndState);
 			Cell nextPos = cellLinkedToPath(pEndState.getParent(), currentPos.getRow(),
                     currentPos.getCol(), pEndState.getParent().getPlayerRow(),
                     pEndState.getParent().getPlayerCol());
			if(nextPos!=null){
 				goalString = strPath(nextPos) + goalString;
 			}
			//else{
				//System.out.println("No Path!");
				//break;
			//}
 				
 			pEndState = pEndState.getParent();
 			}
 		
 
 		//If we are two steps from parent == null, we have that state.parent is 
 		//initial state where player pos is start pos.
 		//goalString += pEndState.getCharLastMove();
 		goalString = pEndState.getCharLastMove() + goalString;
 		Cell currentPos = cellLinkedToState(pEndState);
 		if(currentPos.getRow()==pEndState.getParent().getPlayerRow() &&
 				currentPos.getCol()==pEndState.getParent().getPlayerCol()){
 			return goalString;
 		}
 		else{
 			Cell nextPos = cellLinkedToPath(pEndState.getParent(),currentPos.getRow(),currentPos.getCol(),
 					pEndState.getParent().getPlayerRow(),
 					pEndState.getParent().getPlayerCol());
 			goalString = strPath(nextPos) + goalString;
 		}
 		return goalString;
 	}
 }
