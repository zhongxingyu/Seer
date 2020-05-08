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
 
 	
 	/*
 	 * The key-String represents the box configuration only.
 	 * 
 	 * If this configuration has "been visited" before a vector is returned
 	 * containing the DIFFERNT states visited whith this config. (but whith
 	 * player in different subspaces...)
 	 */
 	HashMap<String, Vector<State>> visitedStates = new HashMap<String, Vector<State>>();
 	Queue<State> simpleQueue = new LinkedList<State>();
 	
 
 	
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
 	
 	public State getFinalState(){
 
 		int lAvoidEndlessLoop = 0;
 		
 		simpleQueue.add(Board.getInitialState());
 		boolean lfoundFinalState = false;
 
 		while(!lfoundFinalState && lAvoidEndlessLoop<1000 ){
 			
			System.out.println("Solver line 55: " + lAvoidEndlessLoop + " " + simpleQueue.size());

			
 			lAvoidEndlessLoop++;
 			
 			State lCurState = simpleQueue.poll();
 			if(isVisitedStateAndAdd(lCurState)){
 				/* 
 				 * If it is visited then do nothing!
 				 * If not visited than continue with els-checks below
 				 * THIS STATE IS AUTOMATICAL ADDED TO VISTED STATES
 				 * BY THE USE OF THE isVistedStateAndAdd method.
 				 */
 			}
 			else if (lCurState.isFinalState()){
 				lfoundFinalState = true;
 				return lCurState;
 			}
 			else{
 				Vector<State> childsOfCurState = new Vector<State>();
 				lCurState.allSuccessors(childsOfCurState); //fills with all children
				for (State child : childsOfCurState){
					simpleQueue.add(child);
				}
 			}
 		}//while (searching for final state in queue)
 		
 		System.out.println("Solver line 77: No final sate was found, returned initial state.");
 		return Board.getInitialState();
 	}
 
 	
 	
 	/**
 	 * Checks if the state is already visited.
 	 * 
 	 * First it is checked whether there exists a identical box configuration,
 	 * if not than the state is added to visitedStates in a new vector for that
 	 * box configuration and false is returned.
 	 * If there is an identical box configuration it is then checked if the player
 	 * shares the same subspace as the player in any of the earlier states with
 	 * identical box configuration.
 	 * 
 	 * @param pState
 	 * @return
 	 */
 	private boolean isVisitedStateAndAdd(State pThisState){
 		
 		String lStateStringKey = pThisState.hashString();
		System.out.println("Solver line 104: " + lStateStringKey);

		
 		
 		if(visitedStates.containsKey(lStateStringKey)){
 			Vector<State> lSameBoxConfig = visitedStates.get(lStateStringKey);
 			
 			boolean shareSubSpace = false;
 
 			for (State prevState : lSameBoxConfig){	
 				
 				/*
 				 * Could first check if players current position is marked as reachable
 				 * 
 				 * else check if connected...
 				 */
 
 				if(isPathToPath(pThisState, pThisState.getPlayerRow(),pThisState.getPlayerCol(),
 						prevState.getPlayerRow(), prevState.getPlayerCol())){
 					shareSubSpace = true;
 					/*
 					 * here is a possibility to add this player positions as "reachableCell"
 					 * for faster 
 					 */
 					break;
 				}
 			} //End for prevStates
 			
 			if( !shareSubSpace){
 				lSameBoxConfig.add(pThisState);
 			}	
 			return shareSubSpace;
 			
 		}//End if (contains same box configuration)
 		Vector<State> lNewBoxConfigVector = new Vector<State>();
 		lNewBoxConfigVector.add(pThisState);
 		return false;
 	}
 	
 	
 	public static String strPath(Cell pCell){
 		
 		if(pCell.getRow() == pCell.getParent().getRow()){
 			if(pCell.getCol() < pCell.getParent().getCol()){
 				return "U";
 			}
 			else{
 				return "D";
 			}
 		}
 		else{
 			if(pCell.getRow() < pCell.getParent().getRow()){
 				return "L";
 			}
 			else{
 				return "R";
 			}
 		}
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
 	public static Cell cellNeighboorToBox(State state ,
 			int pRow,int pCol,int pRowBox,int pColBox){
 		
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowBox,pColBox);
 		
 		boolean bolFinished = false;
 		Cell lCellNeighboor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChilds = new PriorityQueue<Cell>(10,comparator);
         
         lQueChilds.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChilds.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChilds.remove();
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
 							lQueChilds.add(new Cell(lRowChild,lCol));
 						}
 						else if(lRowChild == pEndCell.getRow() &&
 								lCol == pEndCell.getCol()){
 							lCellNeighboor = lCellChild;
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
 							lQueChilds.add(new Cell(lRow,lColChild));
 						}
 						else if(lRow == pEndCell.getRow() &&
 								lColChild == pEndCell.getCol()){
 							lCellNeighboor = lCellChild;
 							bolFinished = true;
 							break;
 						}
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		return lCellNeighboor;
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
 	public static Cell cellLinkedNeighboorToBox(State state ,
 			int pRow,int pCol,int pRowBox,int pColBox){
 
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowBox,pColBox);
 		boolean bolFinished = false;
 		Cell lCellNeighboor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChilds = new PriorityQueue<Cell>(10,comparator);
         
         lQueChilds.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChilds.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChilds.remove();
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
 							lQueChilds.add(new Cell(lCellChild,lRowChild,lCol));
 						}
 						else if(lRowChild == pEndCell.getRow() &&
 								lCol == pEndCell.getCol()){
 							lCellNeighboor = new Cell(lCellChild,lRowChild,lCol);
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
 							lQueChilds.add(new Cell(lCellChild,lRow,lColChild));
 						}
 						else if(lRow == pEndCell.getRow() &&
 								lColChild == pEndCell.getCol()){
 							lCellNeighboor = new Cell(lCellChild,lRow,lColChild);
 							bolFinished = true;
 							break;
 						}
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		if(lCellNeighboor != null){
 			return lCellNeighboor.getParent();
 		}
 		else{
 			return lCellNeighboor;
 		}
 	}
 	/**
 	 * Returns cell with position next to some position.
 	 * @param state
 	 * @param pRow
 	 * @param pCol
 	 * @param pRowBox
 	 * @param pColBox
 	 * @return
 	 */
 	public static Cell cellLinkedNeighboorToPath(State pState ,
 			int pRow,int pCol,int pRowPath,int pColPath){
 
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowPath,pColPath);
 		boolean bolFinished = false;
 		Cell lCellNeighboor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChilds = new PriorityQueue<Cell>(10,comparator);
         
         lQueChilds.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChilds.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChilds.remove();
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
 							lCellNeighboor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChilds.add(new Cell(lCellChild,lRowChild,lCol));
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
 							lCellNeighboor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChilds.add(new Cell(lCellChild,lRow,lColChild));
 						}	
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		return lCellNeighboor;
 	}
 	
 	public static Cell cellNeighboorToPath(State pState ,
 			int pRow,int pCol,int pRowPath,int pColPath){
 
 		Cell pStartCell = new Cell(pRow,pCol);
 		Cell pEndCell = new Cell(pRowPath,pColPath);
 		boolean bolFinished = false;
 		Cell lCellNeighboor = null;
 		
         Comparator<Cell> comparator = new Cell.NormComparator(
         		pEndCell.getRow(),
         		pEndCell.getCol());
 		HashSet<String> lSetPrPaths = new HashSet<String>();
         PriorityQueue<Cell> lQueChilds = new PriorityQueue<Cell>(10,comparator);
         
         lQueChilds.add(pStartCell);
 		lSetPrPaths.add(getHashString(pStartCell.getRow(),pStartCell.getCol()));
 		
 		while(!lQueChilds.isEmpty() &! bolFinished){
 			Cell lCellChild = lQueChilds.remove();
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
 							lCellNeighboor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChilds.add(new Cell(lRowChild,lCol));
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
 							lCellNeighboor = lCellChild;
 							bolFinished = true;
 						}
 						else{
 							lQueChilds.add(new Cell(lRow,lColChild));
 						}	
 					}
 				}
 			incInt = incInt + 2;	
 			}
 		}
 		return lCellNeighboor;
 	}
 
 	
 	/**
 	 * Returns true if there is path to box
 	 * @param state
 	 * @param pStartCell
 	 * @param pEndCell
 	 * @return
 	 */
 	public static boolean isPathToBox(State pState ,int pRow,int pCol,int pRowBox,
 			int pColBox){
 		Cell lCell = cellNeighboorToBox(pState,pRow,pCol,pRowBox,pColBox);
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
 		Cell lCell = cellNeighboorToPath(pState,pRow,pCol,pRowPath,pColPath);
 		return (lCell != null);
 	}
 	
 	public String solutionPath(){
 	
 		Vector<State> successorStates= new Vector<State>();
 		Board.getInitialState().allSuccessors(successorStates); // the vector should now include all successor states...
 		
  		/*
 		 * TODO for sure...	
 		 */
 		return "test: UuullL";
 	}
 	
 	private void heuristic(){
 		/*
 		 * 
 		 * TODO
 		 * 
 		 * Might of course not be a void function...
 		 */
 	}
 	
 }
