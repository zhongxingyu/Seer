 package edu.ycp.cs481.ycpgames;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by brian on 11/18/13.
  */
 public class CheckersGame {
 	private List<int[]> validMoves = new ArrayList<int[]>();
 	private int selectedX = -1, selectedY = -1;
 	private CheckersPlayer playerOne = new CheckersPlayer(CheckersVal.PLAYER_ONE);
 	private CheckersPlayer playerTwo;
 	private CheckersBoard board = new CheckersBoard();
 	public CheckersGame(){
 		super();
         this.reset();
         if(Settings.getInstance().isSinglePlayer()){
             playerTwo = new CheckersAI(CheckersVal.PLAYER_TWO,board);
         }else
             playerTwo = new CheckersPlayer(CheckersVal.PLAYER_TWO);
 	}
 
 	/**
 	 * resets everything
 	 */
 	public void reset() {
 		board.reset();
 	}
 
 	/**
 	 * used to select a piece, fills valid moves array
 	 *
 	 * @param x x value of piece to select
 	 * @param y y value of piece to select
 	 * @return true if valid piece to select false otherwise
 	 */
 	public boolean selectPiece(int x, int y){
 		if(board.getPieceAt(x,y).getPlayer() == whoseTurn()){
 			selectedX = x;
 			selectedY = y;
 			validMoves = board.getValidMoves(x,y);
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 * @return an array of valid moves for selected piece
 	 */
 	public List<int[]> getValidMoves() {
 		return validMoves;
 	}
 
 	/**
 	 *
 	 * @param x    x value of move
 	 * @param y    y value of move
 	 * @return code
 	 */
 	public void makeMove(int x, int y){
 		int[] selectedMove = null;
 		for(int[] move : validMoves){
 			if((move[0] == x) && (move[1] == y)){
 				selectedMove = move;
 				break;
 			}
 		}
 		//return if invalid move
 		if (selectedMove == null){
 			return;
 		}
        Log.d("CheckersGame", "X,Y: " + selectedMove[0] + "," + selectedMove[1]);
 		board.makeMove(selectedX, selectedY, selectedMove);
 		endTurn();
 
         if(Settings.getInstance().isSinglePlayer()){
             playerTwo.findMove(this.getBoard());
 
         }
 		//ai move here
 	}
 
 	/**
 	 *
 	 * @return CheckersVal of whose turn it is
 	 */
 	public CheckersVal whoseTurn(){
 		if(playerOne.isMyTurn()){
 			return CheckersVal.PLAYER_ONE;
 		}else{
 			return CheckersVal.PLAYER_TWO;
 		}
 	}
 	/**
 	 * Switches who's turn it is
 	 */
 	private void endTurn() {
 		playerOne.setMyTurn(!playerOne.isMyTurn());
 		playerTwo.setMyTurn(!playerTwo.isMyTurn());
 		selectedX = -1;
 		selectedY = -1;
 		validMoves = new ArrayList<int[]>();
 	}
 
     public CheckersBoard getBoard() {
         return this.board;
     }
 }
