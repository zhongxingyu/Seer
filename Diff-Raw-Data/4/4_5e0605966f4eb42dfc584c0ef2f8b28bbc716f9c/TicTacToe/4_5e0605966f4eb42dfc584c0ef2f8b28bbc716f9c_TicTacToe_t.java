 package edu.berkeley.gamesman.game;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.State;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.util.Pair;
 
 /**
  * A (relatively) simple implementation of Tic Tac Toe<br />
  * Created as a demonstration of GamesmanJava on Friday, September 10, 2010
  * 
  * @author dnspies
  */
 public final class TicTacToe extends Game<TicTacToeState> {
 	private final int width;
 	private final int height;
 	private final int boardSize;
 	private final int piecesToWin;
 
 	/**
 	 * Default Constructor
 	 * 
 	 * @param conf
 	 *            The Configuration object
 	 */
 	public TicTacToe(Configuration conf) {
 		super(conf);
 		width = conf.getInteger("gamesman.game.width", 3);
 		height = conf.getInteger("gamesman.game.height", 3);
 		boardSize = width * height;
 		piecesToWin = conf.getInteger("gamesman.game.pieces", 3);
 	}
 
 	@Override
 	public Collection<TicTacToeState> startingPositions() {
 		ArrayList<TicTacToeState> returnList = new ArrayList<TicTacToeState>(1);
 		TicTacToeState returnState = newState();
 		returnList.add(returnState);
 		return returnList;
 	}
 
 	@Override
 	public Collection<Pair<String, TicTacToeState>> validMoves(
 			TicTacToeState pos) {
 		ArrayList<Pair<String, TicTacToeState>> moves = new ArrayList<Pair<String, TicTacToeState>>(
 				boardSize - pos.numPieces);
 		TicTacToeState[] children = new TicTacToeState[boardSize
 				- pos.numPieces];
 		String[] childNames = new String[children.length];
 		for (int i = 0; i < children.length; i++) {
 			children[i] = newState();
 		}
 		validMoves(pos, children);
 		int moveCount = 0;
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				if (pos.getPiece(row, col) == ' ')
 					childNames[moveCount++] = String
 							.valueOf((char) ('A' + col))
 							+ Integer.toString(row + 1);
 			}
 		}
		for (int i = 0; i < children.length; i++) {
			moves.add(new Pair<String, TicTacToeState>(childNames[i],
					children[i]));
		}
 		return moves;
 	}
 
 	@Override
 	public int maxChildren() {
 		return boardSize;
 	}
 
 	@Override
 	public String stateToString(TicTacToeState pos) {
 		return pos.toString();
 	}
 
 	@Override
 	public String displayState(TicTacToeState pos) {
 		StringBuilder sb = new StringBuilder((width + 1) * 2 * (height + 1));
 		for (int row = height - 1; row >= 0; row--) {
 			sb.append(row + 1);
 			for (int col = 0; col < width; col++) {
 				sb.append(" ");
 				char piece = pos.getPiece(row, col);
 				if (piece == ' ')
 					sb.append('-');
 				else if (piece == 'X' || piece == 'O')
 					sb.append(piece);
 				else
 					throw new Error(piece + " is not a valid piece");
 			}
 			sb.append("\n");
 		}
 		sb.append(" ");
 		for (int col = 0; col < width; col++) {
 			sb.append(" ");
 			sb.append((char) ('A' + col));
 		}
 		sb.append("\n");
 		return sb.toString();
 	}
 
 	@Override
 	public TicTacToeState stringToState(String pos) {
 		return new TicTacToeState(width, pos.toCharArray());
 	}
 
 	@Override
 	public String describe() {
 		return width + "x" + height + " Tic Tac Toe with " + piecesToWin
 				+ " pieces";
 	}
 
 	@Override
 	public TicTacToeState newState() {
 		return new TicTacToeState(width, height);
 	}
 
 	@Override
 	public int validMoves(TicTacToeState pos, TicTacToeState[] children) {
 		int numMoves = 0;
 		char turn = pos.numPieces % 2 == 0 ? 'X' : 'O';
 		for (int i = 0; i < boardSize; i++) {
 			if (pos.getPiece(i) == ' ') {
 				children[numMoves].set(pos);
 				children[numMoves].setPiece(i, turn);
 				numMoves++;
 			}
 		}
 		return numMoves;
 	}
 
 	@Override
 	public Value primitiveValue(TicTacToeState pos) {
 		char lastTurn = pos.numPieces % 2 == 0 ? 'O' : 'X';
 		for (int row = 0; row < height; row++) {
 			int piecesInRow = 0;
 			for (int col = 0; col < width; col++) {
 				if (pos.getPiece(row, col) == lastTurn) {
 					piecesInRow++;
 					if (piecesInRow == piecesToWin)
 						return Value.LOSE;
 				} else
 					piecesInRow = 0;
 			}
 		}
 		for (int col = 0; col < width; col++) {
 			int piecesInCol = 0;
 			for (int row = 0; row < height; row++) {
 				if (pos.getPiece(row, col) == lastTurn) {
 					piecesInCol++;
 					if (piecesInCol == piecesToWin)
 						return Value.LOSE;
 				} else
 					piecesInCol = 0;
 			}
 		}
 		for (int row = 0; row <= height - piecesToWin; row++) {
 			for (int col = 0; col <= width - piecesToWin; col++) {
 				int pieces;
 				for (pieces = 0; pieces < piecesToWin; pieces++) {
 					if (pos.getPiece(row + pieces, col + pieces) != lastTurn)
 						break;
 				}
 				if (pieces == piecesToWin)
 					return Value.LOSE;
 			}
 		}
 		for (int row = 0; row <= height - piecesToWin; row++) {
 			for (int col = piecesToWin - 1; col < width; col++) {
 				int pieces;
 				for (pieces = 0; pieces < piecesToWin; pieces++) {
 					if (pos.getPiece(row + pieces, col - pieces) != lastTurn)
 						break;
 				}
 				if (pieces == piecesToWin)
 					return Value.LOSE;
 			}
 		}
 		if (pos.numPieces == boardSize)
 			return Value.TIE;
 		else
 			return Value.UNDECIDED;
 	}
 
 	@Override
 	public long stateToHash(TicTacToeState pos) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public long numHashes() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public long recordStates() {
 		return boardSize + 3;
 	}
 
 	@Override
 	public void hashToState(long hash, TicTacToeState s) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void longToRecord(TicTacToeState recordState, long record,
 			Record toStore) {
 		if (record == boardSize + 1) {
 			toStore.value = Value.TIE;
 			toStore.remoteness = boardSize - recordState.numPieces;
 		} else if (record == boardSize + 2)
 			toStore.value = Value.UNDECIDED;
 		else if (record >= 0 && record <= boardSize) {
 			toStore.value = record % 2 == 0 ? Value.LOSE : Value.WIN;
 			toStore.remoteness = (int) record;
 		}
 	}
 
 	@Override
 	public long recordToLong(TicTacToeState recordState, Record fromRecord) {
 		if (fromRecord.value == Value.WIN || fromRecord.value == Value.LOSE)
 			return fromRecord.remoteness;
 		else if (fromRecord.value == Value.TIE)
 			return boardSize + 1;
 		else if (fromRecord.value == Value.UNDECIDED)
 			return boardSize + 2;
 		else
 			throw new Error("Invalid Value");
 	}
 }
 
 class TicTacToeState implements State {
 	private final char[] board;
 	private final int width;
 	int numPieces = 0;
 
 	public TicTacToeState(int width, int height) {
 		this.width = width;
 		board = new char[width * height];
 		for (int i = 0; i < board.length; i++) {
 			board[i] = ' ';
 		}
 	}
 
 	public TicTacToeState(int width, char[] charArray) {
 		this.width = width;
 		board = charArray;
 	}
 
 	public void set(State s) {
 		TicTacToeState ttts = (TicTacToeState) s;
 		if (board.length != ttts.board.length)
 			throw new Error("Different Length Boards");
 		int boardLength = board.length;
 		System.arraycopy(ttts.board, 0, board, 0, boardLength);
 		numPieces = ttts.numPieces;
 	}
 
 	public void setPiece(int row, int col, char piece) {
 		setPiece(row * width + col, piece);
 	}
 
 	public void setPiece(int index, char piece) {
 		if (board[index] != ' ')
 			numPieces--;
 		board[index] = piece;
 		if (piece == 'X' || piece == 'O') {
 			numPieces++;
 		} else if (piece != ' ')
 			throw new Error("Invalid piece: " + piece);
 	}
 
 	public char getPiece(int row, int col) {
 		return getPiece(row * width + col);
 	}
 
 	public char getPiece(int index) {
 		return board[index];
 	}
 
 	public String toString() {
 		return Arrays.toString(board);
 	}
 }
