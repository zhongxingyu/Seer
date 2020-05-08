 package edu.ycp.cs481.ycpgames;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by Brian on 11/25/13.
  */
 public class CheckersAI extends CheckersPlayer {
 	private int [] selectedPiece;
 	private int [] move;
 	private CheckersBoard board;
 
     public CheckersAI(CheckersVal player, CheckersBoard b) {
 		super(player);
 		this.board = b;
 		setHumanPlayer(false);
 		selectedPiece = null;
 		move = null;
 	}
 
 
 	@Override
 	public void findMove(CheckersBoard b){
         this.board = b;
 		int[] bestMove;
 		selectedPiece = null;
 		move = null;
 		switch (settings.getDifficulty()){
 			case 0:
 				bestMove = minimax(2,super.getPlayerNum());
 				break;
 			case 1:
 				bestMove = minimax(4,super.getPlayerNum());
 				break;
 			case 2:
 				bestMove = minimax(8,super.getPlayerNum());
 				break;
 			default:
 				return;
 		}
 		selectedPiece[0] = bestMove[6];
 		selectedPiece[1] = bestMove[7];
 		move[0] = bestMove[0];
 		move[1] = bestMove[1];
 		move[2] = bestMove[2];
 		move[3] = bestMove[3];
 		move[4] = bestMove[4];
 	}
 	@Override
 	public int[] getSelectedPiece(){
 		return selectedPiece;
 	}
 	@Override
 	public int[] getMove(){
 		return move;
 	}
 	@Override
 	public void setMyTurn(Boolean turn){
 		super.setMyTurn(turn);
 		selectedPiece = null;
 		move = null;
 	}
 
 	//helper methods
 
 	/**
 	 *
 	 * @param depth
 	 * @param player
 	 * @return array containing best move and score for it
 	 * 				[0] = x coordinate
 	 * 				[1] = y coordinate
 	 * 				[2] = is a jump or not
 	 * 						0 means no 1 means yes
 	 * 				[3] = x of jumped piece
 	 * 				[4] = y of jumped piece
 	 * 				[5] = score
 	 * 				[6] = best piece x
 	 * 				[7] = best piece y
 	 */
 	private int[] minimax(int depth, CheckersVal player){
 		//find all pieces
 		List<int[]> pieces = findPieces();
 		CheckersPiece jumpedPiece;
 		//set goal
 		int bestScore;
 		if(super.getPlayerNum() == player){
 			bestScore = Integer.MIN_VALUE;
 		}else{
 			bestScore= Integer.MAX_VALUE;
 		}
 		int currentScore;
 		int [] bestMove = new int[7];
 		if(pieces.isEmpty() || depth == 0){
 			//either game is over or depth is reached
 			bestScore = evaluate();
 		}else{
 			//evaluate branches
 			for(int [] piece : pieces){
 				//get possible moves for current piece
 				List<int[]> moves = board.getValidMoves(piece[0],piece[1]);
 				for(int[] move : moves){
 					//try move
 					jumpedPiece = board.makeMove(piece[0],piece[1],move);
 					if(player == super.getPlayerNum()){
 						if(player == CheckersVal.PLAYER_ONE){
 							currentScore = minimax(depth-1, CheckersVal.PLAYER_TWO)[5];
 						}else{
 							currentScore = minimax(depth-1, CheckersVal.PLAYER_ONE)[5];
 						}
 						if(currentScore > bestScore){
 							bestScore = currentScore;
 							bestMove[0] = move[0];
 							bestMove[1] = move[1];
 							bestMove[2] = move[2];
 							bestMove[3] = move[3];
 							bestMove[4] = move[4];
 							bestMove[5] = bestScore;
 							bestMove[6] = piece[0];
 							bestMove[7] = piece[1];
 						}
 					}else{
 						if(player == CheckersVal.PLAYER_ONE){
 							currentScore = minimax(depth-1, CheckersVal.PLAYER_TWO)[5];
 						}else{
 							currentScore = minimax(depth-1, CheckersVal.PLAYER_ONE)[5];
 						}
 						if(currentScore < bestScore){
 							bestScore = currentScore;
 							bestMove[0] = move[0];
 							bestMove[1] = move[1];
 							bestMove[2] = move[2];
 							bestMove[3] = move[3];
 							bestMove[4] = move[4];
 							bestMove[5] = bestScore;
 							bestMove[6] = piece[0];
 							bestMove[7] = piece[1];
 						}
 					}
 					//undo move
 					board.undoMove(piece[0],piece[1],move, jumpedPiece);
 					jumpedPiece = null;
 				}
 			}
 		}
 		return bestMove;
 	}
 
 	/**
 	 *
 	 * @param x x value of space to check
 	 * @param y y value of space to check
 	 * @return boolean indicating if move is valid
 	 */
 	private boolean isMoveValid(int x, int y){
         return board.getPieceAt(x, y).getPlayer() == CheckersVal.EMPTY;
     }
 
 	/**
 	 *
 	 * @return a list of all of AIs pieces
 	 */
 	private List<int[]> findPieces(){
 		List<int[]> moves = new ArrayList<int[]>();
 
 		for (int x = 0; x <= 8; x++) {
 			for (int y = 0; y <= 8; y++) {
 				if(board.getPieceAt(x,y).getPlayer() == super.getPlayerNum()){
 					moves.add(new int[]{x, y});
 				}
 			}
 		}
 
 		return moves;
 	}
 
 	/**
 	 * evaluate the board
 	 * score += (AIpieces - OppPieces)^3
 	 * score += 10*AIkings
 	 * score += 20*AIjumps
 	 * score -= 10*OppKings
 	 * score -= 20*OppJumps
 	 * @return score
 	 */
 	private int evaluate(){
 		int score = (int)Math.pow(board.getPlayerTwoPieces()-board.getPlayerOnePieces(),3);
 		List<int[]> moves;
 		CheckersPiece temp;
 		for (int x = 0; x <= 8; x++) {
 			for (int y = 0; y <= 8; y++) {
 				temp = board.getPieceAt(x,y);
 				if(temp.getRank() == CheckersVal.KING){
 					if(temp.getPlayer() == super.getPlayerNum()){
 						//if piece is your king +10 points
 						score += 10;
 					}else{
 						//if piece is enemy king -10 points
 						score -= 10;
 					}
 
 				}
 				//evaluate moves
 				moves = board.getValidMoves(x,y);
 				for(int [] move : moves){
 					if(move[2] == 1){
 						//if move is a jump
 						if(temp.getPlayer() == super.getPlayerNum()){
 							score += 20;
 						}else{
 							score -=20;
 						}
 					}
 				}
 			}
 		}
 		return score;
 	}
 }
