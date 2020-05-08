 package stratego.model;
 
 import stratego.common.StrategoException;
 import stratego.util.MoveResult;
 import stratego.util.StrategoCoordinate;
 import stratego.util.StrategoPieceType;
 import stratego.util.StrategoPlayerColor;
 
 
 public class StrategoState {
 
 	protected StrategoBoard board;	//The Stratego Board
 	protected MoveResult gameStatus = MoveResult.OK; //Used to check if the game is over or not
 	protected StrategoPlayerColor playerTurn = StrategoPlayerColor.RED; //goes first
 	private StrategoPieceBox redPieceBox, bluePieceBox;
 	
 	/*
 	 * Contsructor, creates board and pieces
 	 */
 	public StrategoState(){
 		board = new StrategoBoard();
 		redPieceBox = new StrategoPieceBox();
 		bluePieceBox = new StrategoPieceBox();
 	}	
 		
 	/**
 	 * This method returns 'gamestatus' useful for checking to see who won
 	 *
 	 * @return whether or not the game has ended (boolean)
 	 */
 	public MoveResult getGameStatus(){
 		return gameStatus;		
 	}
 	
 	/**
 	 * This method verifies that the piece given can move. It checks to verify the locations are correct and the piece is correct
 	 * 
 	 * @param pieceType
 	 *            the piece type that is being moved
 	 * @param from
 	 *            the coordinate where the piece begins.
 	 * @param to
 	 *            the coordinate where the piece is after the move has been made.
 	 * @throws StrategoException
 	 *            if there are any problems with the move that has been specified, such as placing it onto an unplaceable location,
 	 *            the from field is empty, location specified to is out of bounds, or you are specifying the wrong piece type
 	 */
 	public void canMove(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException {
 		//Make sure to is empty, make sure the piece at from is correct color and piece type
 		//if(!board.GetBoard()[to.getX()][to.getY()].isEmpty()){
 		//	throw new StrategoException("Location to is not empty");
 		//}
 		
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
 		if(!board.GetBoard()[to.getX()][to.getY()].isPlaceable()){
 			throw new StrategoException("Trying to move to non-placeable location");
 		}
 		if(board.GetBoard()[to.getX()][to.getY()].getPlayerColor() == board.GetBoard()[from.getX()][from.getY()].getPlayerColor()){	//Make sure not moving into own pieces
 			throw new StrategoException("Cannot attack your own pieces");
 		}
 		
 	}
 		
 	/**
 	 * This method executes a move in the game. It is called for every move that must be
 	 * made. Including placing pieces.
 	 * 
 	 * @param pieceType
 	 *            the piece type that is being moved
 	 * @param from
 	 *            the coordinate where the piece begins. If the coordinate is null, then
 	 *            the piece begins off the board (that is, it is placed on the board in
 	 *            this move).
 	 * @param to
 	 *            the coordinated where the piece is after the move has been made.
 	 * @param playerColor
 	 * 				the color of the player. Null during normal gameplay, playerColor when placing pieces
 	 * @throws StrategoException
 	 *             if there are any problems in making the move (such as specifying a
 	 *             coordinate that does not have the appropriate piece, or the color of
 	 *             the piece is the color of the player who is moving.
 	 */
 	public void doMove(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to, StrategoPlayerColor playerColor) throws StrategoException{
 		if(playerColor == null){ //Moving pieces around
 			if(board.GetBoard()[to.getX()][to.getY()].getPieceType() == null){ //If the move to location is empty, go there
 				board.GetBoard()[from.getX()][from.getY()].DePopulate();
 				board.GetBoard()[to.getX()][to.getY()].Populate(playerTurn, pieceType);	
 			}
 			else{ //Check any predefined conditions and then evaluate the results
 				if(checkIfBombAndEvaluate(pieceType, from, to));
 				else if(checkIfFlagAndEvaluate(pieceType, from, to));	
 				else if(checkIfSpyAttackingMarshall(pieceType, from, to));
 				else {
 					checkVictor(pieceType, from, to);
 				}				
 			}			 
 			InvertPlayerTurn();	 //Change players turn
 		}
 		else{//For initial placement
 			if(playerColor == StrategoPlayerColor.BLUE){
 				if(bluePieceBox.isAvailable(pieceType)){
 					board.GetBoard()[to.getX()][to.getY()].Populate(StrategoPlayerColor.BLUE, pieceType);
 				}
 				else{
 					throw new StrategoException("You cannot place anymore of those, BLUE");
 				}
 			}
 			else{//is red
 				if(redPieceBox.isAvailable(pieceType)){
 					board.GetBoard()[to.getX()][to.getY()].Populate(StrategoPlayerColor.RED, pieceType);
 				}
 				else{
 					throw new StrategoException("You cannot place anymore of those, RED");
 				}
 			}
 		}
 	}
 	
 	/**
	 * This method makes sure that a move specified is valid (IE, only moving 1 spot at a time, and nothing inbetween. Also checks to make sure 
 	 * not attacking own pieces
 	 * 
 	 * @param pieceType
 	 *            the piece type that is being moved
 	 * @param from
 	 *            the coordinate where the piece begins.
 	 * @param to
 	 *            the coordinated where the piece is after the move has been made.
 	 * @throws StrategoException
 	 *             if there are any issues making the move(You are moving more or less than allowed, or through pieces
 	 */
 	public void checkValidMove(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException{		
 		//If the difference in X xor Y is > 1 throw error
 		//XOR to make sure no diagonals
 		//CHecks everything besides scout
 		if(pieceType != StrategoPieceType.EIGHT){
 			if(!(Math.abs(to.getX() - from.getX()) != 1 ^ Math.abs(to.getY() - from.getY()) != 1)){
 				throw new StrategoException(pieceType + ": Is moving more/less than allowed");
 			}
 		}
 		//Check scout
 		else{//For Scout, remember to check that everything inbetween is null
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
 						smallnum = to.getX();;
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
 	
 	/**
 	 * This method executes a move in the game. It determines who won the fight and places the pieces in the correct locations
 	 * 
 	 * @param pieceType
 	 *            the piece type that is being moved
 	 * @param from
 	 *            the coordinate where the piece begins.
 	 * @param to
 	 *            the coordinated where the piece is after the move has been made.
 	 * @throws StrategoException
 	 *           if the piece specified is not in the right spot
 	 */
 	private void checkVictor(StrategoPieceType attackerPieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException{
 		StrategoPieceType defenderPieceType = board.GetBoard()[to.getX()][to.getY()].getPieceType(); //get defender piecetype
 		if (attackerPieceType.getValue() < defenderPieceType.getValue()){//Attacker won
 			board.GetBoard()[to.getX()][to.getY()].DePopulate();//depopulate defender
 			board.GetBoard()[to.getX()][to.getY()].Populate(playerTurn, attackerPieceType);//populate spot with attacker
 			board.GetBoard()[from.getX()][from.getY()].DePopulate(); //depopulate prev attacker
 		}
 		else if(attackerPieceType.getValue() == defenderPieceType.getValue()){ //Tie
 			board.GetBoard()[to.getX()][to.getY()].DePopulate();
 			board.GetBoard()[from.getX()][from.getY()].DePopulate();
 		}
 		else{//Defender won
 			board.GetBoard()[from.getX()][from.getY()].DePopulate(); //get rid of defender
 		}
 	}
 	
 	/*
 	 * Checks if the spy is attacking the Marshall (1)
 	 */
 	private boolean checkIfSpyAttackingMarshall(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException{
 		if(board.GetBoard()[to.getX()][to.getY()].getPieceType() == StrategoPieceType.ONE && 
 			board.GetBoard()[from.getX()][from.getY()].getPieceType() == StrategoPieceType.SPY){
 			board.GetBoard()[to.getX()][to.getY()].DePopulate();
 			board.GetBoard()[to.getX()][to.getY()].Populate(playerTurn, pieceType);
 			board.GetBoard()[from.getX()][from.getY()].DePopulate();
 			return true;
 		}
 		return false;
 		
 	}
 	
 	/*
 	 * Checks if the attacker is attacking bomb, if not engineer, destroy piece else destroy bomb
 	 */
 	private boolean checkIfBombAndEvaluate(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException{
 		if(board.GetBoard()[to.getX()][to.getY()].getPieceType() == StrategoPieceType.BOMB){//Check if attacking bomb
 			if(board.GetBoard()[from.getX()][from.getY()].getPieceType() == StrategoPieceType.EIGHT){//Check if Engineer
 				board.GetBoard()[to.getX()][to.getY()].DePopulate();
 				board.GetBoard()[to.getX()][to.getY()].Populate(playerTurn, pieceType);
 				board.GetBoard()[from.getX()][from.getY()].DePopulate();				
 			}	
 			else{
 				board.GetBoard()[from.getX()][from.getY()].DePopulate();
 			}
 			return true; //There was a bomb
 		}		
 		return false; //no bomb
 		
 	}
 	
 	/*
 	 * Checks if the attacker is attacking a flag, if so, change state and end game
 	 */
 	private boolean checkIfFlagAndEvaluate(StrategoPieceType pieceType, StrategoCoordinate from, StrategoCoordinate to) throws StrategoException{
 		if(board.GetBoard()[to.getX()][to.getY()].getPieceType() == StrategoPieceType.FLAG){
 			StrategoPlayerColor playcol = board.GetBoard()[from.getX()][from.getY()].getPlayerColor();
 			board.GetBoard()[to.getX()][to.getY()].DePopulate();
 			board.GetBoard()[from.getX()][from.getY()].DePopulate();
 			board.GetBoard()[to.getX()][to.getY()].Populate(playcol, pieceType);
 			if(board.GetBoard()[from.getX()][from.getY()].getPlayerColor() == StrategoPlayerColor.RED){
 				gameStatus = MoveResult.RED_WINS;
 			}else{
 				gameStatus = MoveResult.BLUE_WINS;				
 			}
 			System.out.print(gameStatus);
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	/*	 
 	 * Switches player's turn
 	 */
 	private void InvertPlayerTurn(){
 		playerTurn = (playerTurn == StrategoPlayerColor.RED) ? StrategoPlayerColor.BLUE : StrategoPlayerColor.RED; //Inline if
 	}
 }
