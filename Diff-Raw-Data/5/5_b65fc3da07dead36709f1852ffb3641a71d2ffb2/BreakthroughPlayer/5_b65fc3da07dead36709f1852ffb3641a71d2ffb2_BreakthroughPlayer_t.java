 package BreakthroughPlayer;
 
 import java.util.ArrayList;
 
 import game.*;
 import breakthrough.*;
 
 public class BreakthroughPlayer extends BaseBreakthroughPlayer {
 
 	protected ScoredBreakthroughMove[] mvStack;
 	public final int DEPTH_LIMIT = 5;
 
 	protected class ScoredBreakthroughMove extends BreakthroughMove {
 		public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double s)
 		{
 			super(r1, c1, r2, c2);
 			score = s;
 		}
 
 		public void set(int r1, int c1, int r2, int c2, double s)
 		{
 			startRow = r1;
 			startCol = c1;
 			endingRow = r2;
 			endingCol = c2;
 			score = s;
 		}
 		public double score;
 	}
 
 	public BreakthroughPlayer(String n) {
 		super(n, false);
 	}
 
	public ArrayList <BreakthroughMove> getMoves(BreakthroughState board, char who) {
 		ArrayList<BreakthroughMove> moves = new ArrayList<BreakthroughMove>();
 		for(int i = 0; i < board.N; i++){
 			for(int j = 0; i < board.N; i++){
 				// Home team moves from lower rows to higher rows
 				if(who == BreakthroughState.homeSym){
 					if(possibleMove(board, who, i, j, i+1, j-1)) moves.add(new BreakthroughMove(i,j,i+1,j+1));
 					if(possibleMove(board, who, i, j, i+1, j)) moves.add(new BreakthroughMove(i,j,i+1,j));
 					if(possibleMove(board, who, i, j, i+1, j+1)) moves.add(new BreakthroughMove(i,j, i+1, j+1));
 				}
 				// Away team moves from higher rows to lower rows
 				else{
 					if(possibleMove(board, who, i, j, i-1, j-1)) moves.add(new BreakthroughMove(i, j, i-1, j-1));
 					if(possibleMove(board, who, i, j, i-1, j)) moves.add(new BreakthroughMove(i, j, i-1, j));
 					if(possibleMove(board, who, i, j, i-1, j+1)) moves.add(new BreakthroughMove(i, j, i-1, j+1));
 					
 				}
 			}
 		}
		return moves;
 	}
 	
 	public boolean possibleMove(BreakthroughState board, char who, int r1, int c1, int r2, int c2){
 		// Not possible if any index is off the board
 		if(r1 < 0 || c1 < 0 || r2 < 0 || c2 < 0 || r1 >= board.N || 
 				c1 >= board.N || r2 >= board.N || c2 >= board.N) return false;
 		// No move can change row or column by more than 1
 		else if(Math.abs(r1-r2) > 1 || Math.abs(c1 - c2) > 1) return false;
 		// Not possible if the start position doesn't have a piece of the moving player's
 		else if(board.board[r1][c1] != who) return false;
 		// Not possible if the end position does have a piece of the moving player's (can't self-capture)
 		else if(board.board[r2][c2] == who) return false;
 		// Non-diagonal move can only be to an empty space
 		else if(c1 == c2 && board.board[r2][c2] != BreakthroughState.emptySym) return false;
 		else return true;
 	}
 
 	public BreakthroughState makeMove(BreakthroughState board, BreakthroughMove move) {
 		board.makeMove(move);
 		return board;
 	}
 
 	public boolean isTerminal(BreakthroughState board, ScoredBreakthroughMove move, int depth, int depthLimit) {
 		GameState.Status status = board.getStatus();
 		boolean isTerminal = false;
 		if(status == GameState.Status.HOME_WIN) {
 			move.set(0,0,0,0, Double.POSITIVE_INFINITY);
 			isTerminal = true;
 		} else if(status == GameState.Status.AWAY_WIN) {
 			move.set(0,0,0,0, Double.NEGATIVE_INFINITY);
 			isTerminal = true;
 		} else if(depth < depthLimit) {
 			move.set(0,0,0,0,0);
 			isTerminal = true;
 		}
 		return isTerminal;
 	}
 
 	public void minimax(BreakthroughState board, int depth, int depthLimit) {
 		boolean toMaximize = (board.getWho() == GameState.Who.HOME);		
 		boolean isTerminal = isTerminal(board, mvStack[depth], depth, depthLimit);
 		double bestValue;
 		if(isTerminal) {
 			;
 		} else if(depth == depthLimit) {
 			 mvStack[depth].set(0,0,0,0, evalBoard(board));
 		}
 		ScoredBreakthroughMove bestMove = mvStack[depth];
 		if(toMaximize) {
 			bestValue = Double.NEGATIVE_INFINITY;
 			bestMove.set(0,0,0,0,bestValue);
 			for(BreakthroughMove mv : getMoves(board, GameState.Who.HOME)) {
 				BreakthroughState temp = makeMove(board, mv);
 				minimax(temp, depth + 1, depthLimit);
 				if(mvStack[depth+1].score > bestMove.score) {
 					bestMove.set(mv.startRow, mv.startCol, mv.endingRow, mv.endingCol, mvStack[depth+1].score);
 				}
 			}
 		} else {
 			bestValue = Double.POSITIVE_INFINITY;
 			bestMove.set(0,0,0,0, bestValue);
 			for(BreakthroughMove mv : getMoves(board, )) {
 				BreakthroughState temp = makeMove(board, mv);
 				minimax(temp, depth + 1, depthLimit);
 				if (mvStack[depth+1].score < bestMove.score) {
 					bestMove.set(mv.startRow, mv.startCol, mv.endingRow, mv.endingCol, mvStack[depth+1].score);
 				}
 			}
 		}
 		mvStack[0] = bestMove;
 	}
 
 	public GameMove getMove(GameState state, String lastMove) {
 		minimax((BreakthroughState) state, 0, DEPTH_LIMIT);
 		return mvStack[0];
 	}
 
 	public static void main(String [] args) {
 		GamePlayer p = new RandomBreakthroughPlayer("Test Breakthrough");
 		p.compete(args);
 	}
 }
