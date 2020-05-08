 package edu.berkeley.gamesman.hadoop.game.connect4;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import org.apache.hadoop.conf.Configuration;
 
 import edu.berkeley.gamesman.game.type.GameValue;
 import edu.berkeley.gamesman.hadoop.ranges.Range;
 import edu.berkeley.gamesman.hadoop.ranges.RangeTree;
 import edu.berkeley.gamesman.hasher.cachehasher.CacheMove;
 import edu.berkeley.gamesman.hasher.genhasher.Move;
 import edu.berkeley.gamesman.hasher.genhasher.Moves;
 import edu.berkeley.gamesman.solve.reader.SolveReader;
 import edu.berkeley.gamesman.util.Pair;
 
 public class Connect4 extends RangeTree<C4State> implements
 		SolveReader<C4State> {
 	private Move[] myMoves;
 	private Move[][] colMoves;
 	private C4Hasher myHasher;
 	private int width, height, inARow;
 	private int gameSize;
 	private int suffLen;
 
 	@Override
 	protected int suffixLength() {
 		return suffLen;
 	}
 
 	@Override
 	public Collection<C4State> getStartingPositions() {
 		C4State result = myHasher.newState();
 		return Collections.singleton(result);
 	}
 
 	@Override
 	public GameValue getValue(C4State state) {
 		int lastTurn = opposite(getTurn(state));
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				for (int dRow = 0; dRow <= 1; dRow++) {
					for (int dCol = -1; dCol <= 1; dCol++) {
						if (dRow == 0 && (dCol == 0 || dCol == -1))
 							continue;
 						int r = row, c = col;
 						boolean hasLine = true;
 						for (int i = 0; i < inARow; i++) {
 							if (!is(state, r, c, lastTurn)) {
 								hasLine = false;
 								break;
 							}
 							r += dRow;
 							c += dCol;
 						}
 						if (hasLine)
 							return GameValue.LOSE;
 					}
 				}
 			}
 		}
 		if (numPieces(state) == gameSize)
 			return GameValue.TIE;
 		else
 			return null;
 	}
 
 	int numPieces(C4State state) {
 		return state.get(gameSize);
 	}
 
 	private boolean is(C4State state, int row, int col, int val) {
 		return inBounds(row, col) && get(state, row, col) == val;
 	}
 
 	private boolean inBounds(int row, int col) {
 		return row >= 0 && row < height && col >= 0 && col < width;
 	}
 
 	int get(C4State state, int row, int col) {
 		return state.get(col * height + row);
 	}
 
 	private static int opposite(int turn) {
 		switch (turn) {
 		case 1:
 			return 2;
 		case 2:
 			return 1;
 		default:
 			throw new IllegalArgumentException(Integer.toString(turn));
 		}
 	}
 
 	int getTurn(C4State state) {
 		return getTurn(numPieces(state));
 	}
 
 	private static int getTurn(int numPieces) {
 		return (numPieces % 2) + 1;
 	}
 
 	@Override
 	protected C4Hasher getHasher() {
 		return myHasher;
 	}
 
 	@Override
 	protected Move[] getMoves() {
 		return myMoves;
 	}
 
 	private int getPlace(int row, int col) {
 		return col * height + row;
 	}
 
 	private boolean isBottom(int row, int col) {
 		return row == 0;
 	}
 
 	@Override
 	public void innerConfigure(Configuration conf) {
 		width = conf.getInt("gamesman.game.width", 5);
 		height = conf.getInt("gamesman.game.height", 4);
 		gameSize = width * height;
 		inARow = conf.getInt("gamemsan.game.pieces", 4);
 		myHasher = new C4Hasher(width, height);
 		ArrayList<Move>[] result = new ArrayList[width];
 		colMoves = new Move[width][];
 		for (int i = 0; i < width; i++) {
 			result[i] = new ArrayList<Move>();
 		}
 		for (int numPieces = 0; numPieces < gameSize; numPieces++) {
 			int turn = getTurn(numPieces);
 			for (int row = 0; row < height; row++) {
 				for (int col = 0; col < width; col++) {
 					int place = getPlace(row, col);
 					if (isBottom(row, col)) {
 						result[col].add(new CacheMove(gameSize, numPieces,
 								numPieces + 1, place, 0, turn));
 					} else {
 						result[col]
 								.add(new CacheMove(gameSize, numPieces,
 										numPieces + 1, place - 1, 1, 1, place,
 										0, turn));
 						result[col]
 								.add(new CacheMove(gameSize, numPieces,
 										numPieces + 1, place - 1, 2, 2, place,
 										0, turn));
 					}
 				}
 			}
 		}
 		ArrayList<Move> allMoves = new ArrayList<Move>();
 		for (int i = 0; i < width; i++) {
 			colMoves[i] = result[i].toArray(new Move[result[i].size()]);
 			allMoves.addAll(result[i]);
 		}
 		myMoves = allMoves.toArray(new Move[allMoves.size()]);
 		int varianceLength = conf.getInt("gamesman.game.variance.length", 10);
 		suffLen = Math.max(1, gameSize + 1 - varianceLength);
 	}
 
 	public C4State newState() {
 		return myHasher.newState();
 	}
 
 	public boolean playMove(C4State state, int col) {
 		boolean made = false;
 		for (Move m : colMoves[col]) {
 			if (Moves.matches(m, state) == -1) {
 				myHasher.makeMove(state, m);
 				made = true;
 				break;
 			}
 		}
 		return made;
 	}
 
 	@Override
 	public int getDivision(Range<C4State> range) {
 		assert range.length() == suffLen;
 		return range.get(suffLen - 1);
 	}
 
 	@Override
 	protected int getDivision(C4State state) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public C4State getPosition(String board) {
 		assert board.length() == gameSize;
 		int[] pos = new int[gameSize + 1];
 		int pieceCount = 0;
 		int i = 0;
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				char c = board.charAt(i++);
 				pos[col * height + row] = pieceFor(c);
 				if (c != ' ')
 					pieceCount++;
 			}
 		}
 		pos[gameSize] = pieceCount;
 		C4State s = newState();
 		getHasher().set(s, pos);
 		return s;
 	}
 
 	private static int pieceFor(char c) {
 		switch (c) {
 		case ' ':
 			return 0;
 		case 'X':
 			return 1;
 		case 'O':
 			return 2;
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	@Override
 	public Collection<Pair<String, C4State>> getChildren(C4State position) {
 		ArrayList<Pair<String, C4State>> children = new ArrayList<Pair<String, C4State>>();
 		for (int col = 0; col < width; col++) {
 			C4State s = newState();
 			getHasher().set(s, position);
 			if (playMove(s, col)) {
 				children.add(new Pair<String, C4State>(Integer.toString(col), s));
 			}
 		}
 		return children;
 	}
 
 	@Override
 	public String getString(C4State position) {
 		StringBuilder sb = new StringBuilder(gameSize);
 		for (int row = 0; row < height; row++) {
 			for (int col = 0; col < width; col++) {
 				sb.append(charFor(position.get(col * height + row)));
 			}
 		}
 		return sb.toString();
 	}
 
 	static char charFor(int piece) {
 		switch (piece) {
 		case 0:
 			return ' ';
 		case 1:
 			return 'X';
 		case 2:
 			return 'O';
 		default:
 			return '?';
 		}
 	}
 }
