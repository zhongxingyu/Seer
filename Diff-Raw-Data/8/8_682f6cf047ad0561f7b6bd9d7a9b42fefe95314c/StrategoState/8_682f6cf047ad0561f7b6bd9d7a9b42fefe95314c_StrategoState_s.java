 package stratego.model;
 
 import stratego.common.StrategoException;
 import stratego.util.MoveResult;
 import stratego.util.StrategoCoordinate;
 import stratego.util.StrategoPieceType;
 import stratego.util.StrategoPlayerColor;
 
 
 public class StrategoState {
 
 	protected StrategoBoard board;
 	protected MoveResult gameStatus = MoveResult.OK;
 	protected StrategoPlayerColor playerTurn = StrategoPlayerColor.RED; //goes first
 	
 	public StrategoState(){
 		board = new StrategoBoard();
 	}	
 	/*
 	 * Checks to see if the game is over
 	 */
 	public boolean IsGameOver() throws StrategoException{
 		if(gameStatus == MoveResult.OK){
 			return true;
 		}
 		else{
 			throw new StrategoException("The game is over");
 			//return false; //Game ended
 		}
 		
 	}
 	/*
 	 * A function to easily make sure that the location is not empty, the coords are in bounds, and the piece type is at the specifed location
 	 */
 	public void canMove(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException {
 		//Make sure to is empty, make sure the piece at from is correct color and piece type
 		//if(!board.GetBoard()[to.getX()][to.getY()].isEmpty()){
 		//	throw new StrategoException("Location to is not empty");//TODO Put game logic here
 		//}
		if(board.GetBoard()[to.getX()][to.getY()].isPlaceable()){
			throw new StrategoException("Trying to move to non-placeable location");
		}
 		if(board.GetBoard()[from.getX()][from.getY()] == null){
 			throw new StrategoException("Location moving from is empty!");//Make sure that the location moving form is not empty		
 		}		
 		if(to.getX() > 9 || to.getX() < 0 || to.getY() > 9 || to.getY() < 0){//make sure not out of bounds
 			throw new StrategoException("Location out of bounds");
 		}
 		if(pieceType != board.GetBoard()[from.getX()][from.getY()].getPieceType()){//make sure piece is in the location specified
 			System.out.println(pieceType.toString());
 			System.out.println(board.GetBoard()[from.getX()][from.getY()].getX());
 			System.out.println(from.getY());
 			System.out.println(board.GetBoard()[from.getX()][from.getY()].getPieceType().toString());			
 			throw new StrategoException("Piece type specified is not at specified location");
 		}
 		
 	}
 	
 	
 	/*
 	 * Moves piece to location
 	 */
 	public void doMove(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to, StrategoPlayerColor playerColor) throws StrategoException{
 		if(playerColor == null){
 			if(board.GetBoard()[to.getX()][to.getY()].getPieceType() == null){ //If the move to location is empty, go there
 				board.GetBoard()[from.getX()][from.getY()].DePopulate();
 				board.GetBoard()[to.getX()][to.getY()].Populate(playerTurn, pieceType);	
 			}
 			else{
 				
 			}
 			//TODO Fighting logic
 			//Game logic for fighting and moving. 
 			InvertPlayerTurn();	
 		}
 		else{//For initial placement
 			board.GetBoard()[to.getX()][to.getY()].Populate(playerColor, pieceType);
 		}
 	}
 	
 	/*
 	 * Makes sure that the specified piece is able to move that distance A LOT of game logic here. Assume pieceType is valid for moving
 	 */
 	public void checkValidMove(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException{		
 		//If the difference in X xor Y is > 1 throw error
 		//XOR to make sure no diagonals
 		//CHecks everything besides scout
 		if(pieceType == StrategoPieceType.ONE || pieceType == StrategoPieceType.TWO ||
 				pieceType == StrategoPieceType.THREE || pieceType == StrategoPieceType.FOUR||
 				pieceType == StrategoPieceType.FIVE || pieceType == StrategoPieceType.SIX ||
 				pieceType == StrategoPieceType.SEVEN || pieceType == StrategoPieceType.SPY){
 			if(!(Math.abs(to.getX() - from.getX()) != 1 ^ Math.abs(to.getY() - from.getY()) != 1)){
 				//System.out.println("X = " + Math.abs(to.getX() - from.getX()));
 				//System.out.println("Y = " + Math.abs(to.getY() - from.getY()));
 				throw new StrategoException(pieceType + ": Is moving more/less than allowed");
 			}
 		}
 		//Check scout
 		else if(pieceType == StrategoPieceType.EIGHT){//FOr Scout, remember to check that everything inbetween is null
 			if(Math.abs(to.getX() - from.getX()) == 0 || Math.abs(to.getY() - from.getY()) == 0){ //Make sure only moving on 1 plane
 				//check to make sure no pieces inbetween, remember not to count the destination as a point
 				boolean moveX = false;
 				boolean moveY = false;
 				int smallnum;
 				int dist = Math.abs(to.getX() - from.getX()) + Math.abs(to.getY() - from.getY());//One will = 0 so add to find distance moving
 				//check which plane moving in
 				if(Math.abs(to.getX() - from.getX()) > 0){
 					moveX = true;
 				}
 				if(Math.abs(to.getY() - from.getY()) > 0){
 					moveY = true;
 				}
 				if(moveX){
 					//check for smaller number also Y stays same
 					if(to.getX() > from.getX()){ //destination is further
 						smallnum = from.getX();
 
 					}
 					else{	//starting location (from) is further
 						smallnum = to.getX();
 ;
 					}
 					for(int i = 1; i < dist; i++){ //check every location inbetween
 						if(board.GetBoard()[smallnum + i][to.getY()] != null){ //increase X and check every value inbetween
 							throw new StrategoException("Scout trying to move through pieces on X axis (Left right)");
 						}
 						if(board.GetBoard()[smallnum + i][to.getY()].isPlaceable() == false){ //increase X and check every value inbetween
 							throw new StrategoException("Scout trying to move through an unplaceable location");
 						}
 					}
 				}
 				if(moveY){
 					//check for smaller number also X stays same
 					if(to.getY() > from.getY()){ //destination is further
 						smallnum = from.getY();
 
 					}
 					else{	//starting location (from) is further
 						smallnum = to.getY();
 					}
 					for(int i = 1; i <= dist; i++){ //check every location inbetween
 						if(board.GetBoard()[to.getX()][smallnum + i].getPieceType() != null){ //increase X and check every value inbetween
 							throw new StrategoException("Scout trying to move through pieces on Y axis (Up down)");
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	
 	
 	/*
 	 * Switches player's turn
 	 */
 	private void InvertPlayerTurn(){ //Done
 		if(playerTurn == StrategoPlayerColor.RED){
 			playerTurn = StrategoPlayerColor.BLUE;
 		}
 		else{
 			playerTurn = StrategoPlayerColor.RED;
 		}
 	}
 }
