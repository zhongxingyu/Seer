 package com.youzwak.connectfour;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.youzwak.connectfour.ConnectFourBoard.ColumnFullException;
 import com.youzwak.connectfour.ConnectFourBoard.OutOfRangeException;
 import com.youzwak.connectfour.ConnectFourBoard.Piece;
 
 public class ConnectFourAI {
 	
 	// Constants
 	public static final int MINMAX_DEPTH = 5;
 	
 	// Debug variable
	private boolean debugEnabled = true;
 	
 	// Member variables
 	private Piece computerPiece;
 	private Piece playerPiece;
 
 	// Inner class
 	private class Node {
 		private ConnectFourBoard board;
 		private Piece currentPlayer;
 		private int   column;
 		
 		public Node(ConnectFourBoard board, Piece currentPlayer) {
 			this.board    = new ConnectFourBoard(board);
 			this.currentPlayer = currentPlayer;
 		}
 		
 		public Node(ConnectFourBoard board, Piece currentPlayer, int column) {
 			this(board, currentPlayer);
 			this.column = column;
 		}
 
 		public void debug() {
 			this.board.printBoard();
 		}
 		
 		public boolean isTerminalNode() {
 			boolean terminal = false;
 			
 			if (this.board.isFull()) {
 				terminal = true;
 			} else if (this.board.isWinner(Piece.RED)) {
 				terminal = true;
 			} else if (this.board.isWinner(Piece.BLACK)) {
 				terminal = true;
 			}
 			return terminal;
 		}
 		private Piece getNextPlayer() {
 			if (this.currentPlayer == Piece.RED) {
 				return Piece.BLACK;
 			} else {
 				return Piece.RED;
 			}
 		}
 		
 		public int getColumn() {
 			return column;
 		}
 
 		/**
 		 * Determine the possible next moves and add to the tree
 		 */
 		public Set<Node> getChildren() {
 			Set<Node> children = new HashSet<Node>();
 			
 			for (int col = 0; col < ConnectFourBoard.COLUMNS; col++) {
 				// Skip moves where the space is already occupied
 				if (this.board.isFull(col)) {
 					continue;
 				}
 				// Create a new board containing this move possibility
 				ConnectFourBoard childBoard = new ConnectFourBoard(board);
 				try {
 					childBoard.addToColumn(col, currentPlayer);
 				} catch (OutOfRangeException e) {
 					continue;
 				} catch (ColumnFullException e) {
 					continue;
 				}
 				
 				// Store the child in the tree
 				Node child = new Node(childBoard, getNextPlayer(), col);
 				children.add(child);
 			}
 			return children;
 		}
 
 		public int getObjectiveValue() {
 			int objValue = 0;
 			
 			if (this.board.isWinner(playerPiece)) {
 				objValue = -10000;
 			} else if (this.board.isWinner(computerPiece)) {
 				objValue = 10000;
 			} else {
 				int playerThreats   = board.getThreats(playerPiece);
 				int computerThreats = board.getThreats(computerPiece);
 				
 				if (playerThreats > 0) {
 					objValue = -100 * playerThreats;
 				} else {
 					objValue = 100 * computerThreats;
 				}
 			}
 			return objValue;
 		}
 
 		private boolean hasThreat(Piece piece) {
 			boolean threat = false;
 						
 			return threat;
 		}
 		
 		public String toString() {
 			StringBuffer sb = new StringBuffer();
 			sb.append("[");
 			sb.append(column);
 			sb.append("]");
 			sb.append(" ");
 			return sb.toString();
 		}
 	} // End class Node
 	
 	public ConnectFourAI(Piece computerPiece) {
 		this.computerPiece = computerPiece;
 		
 		if (computerPiece == Piece.RED) {
 			this.playerPiece = Piece.BLACK;
 		} else {
 			this.playerPiece = Piece.RED;
 		}
 	}
 
 	public void enableDebug() {
 		this.debugEnabled = true;
 	}
 	
 	public void disableDebug() {
 		this.debugEnabled = false;
 	}
 	
 	private void debug(String s) {
 		if (this.debugEnabled) {
 			System.out.println(s);
 		}
 	}
 	
 	private int minimax(Node node, int depth, int alpha, int beta, boolean needMax) {
 		if (depth <= 0 || node.isTerminalNode()) {
 			return node.getObjectiveValue();
 		}
 
 		for (Node child : node.getChildren()) {
 			int score = minimax(child, depth-1, alpha, beta, !needMax);
 
 			if (needMax) {  // Do Max
 				if (score > alpha) {
 					alpha = score;
 				}
 				if (beta <= alpha) {
 					break;
 				}
 			} else { // Do Min
 				if (score < beta) {
 					beta = score;
 				}
 				if (beta <= alpha) {
 					break;
 				}
 			}
 		}
 		
 		return needMax ? alpha : beta;
 	}
 		
 	public int getMove(ConnectFourBoard other) {
 		int spaceNum = 0;
 		int score = 0;
 		int bestScore = Integer.MIN_VALUE;
 		Node bestMove = null;
 		Node node = new Node(other, computerPiece);
 		
 		Set<Node> children = node.getChildren();
 
 		for (Node child : children) {
 			// Use Minimax to get computer move
 			score = this.minimax(child, MINMAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
 			
 			// Display the move and score, for debugging
 			debug(child.getColumn() + ", score=" + score);
 			
 			// Chose the best move
 			if (score > bestScore) {
 				bestScore = score;
 				bestMove  = child;
 			}
 		}
 
 		debug("bestMove " + bestMove + ", bestScore = " + bestScore);
 		if (bestMove != null) {
 			spaceNum = bestMove.getColumn();
 		}
 		return spaceNum;
 	}
 	
 }
