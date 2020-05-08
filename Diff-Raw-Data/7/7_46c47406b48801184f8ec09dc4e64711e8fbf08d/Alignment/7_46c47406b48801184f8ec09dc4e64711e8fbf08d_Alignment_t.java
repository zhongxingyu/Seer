 package edu.berkeley.gamesman.game;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Value;
 import edu.berkeley.gamesman.hasher.AlignmentHasher;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * @author Aloni, Brent, and DNSpies
  * 
  */
 public class Alignment extends Game<AlignmentState> {
 	private final int gameWidth, gameHeight, gameSize;
 	public final int piecesToWin;
 	private final AlignmentVariant variant; // should be an enum?
 	public final ArrayList<Pair<Integer, Integer>> openCells;
 	private final AlignmentHasher myHasher;
 
 	public Alignment(Configuration conf) {
 		super(conf);
 		gameWidth = conf.getInteger("gamesman.game.width", 4);
 		gameHeight = conf.getInteger("gamesman.game.height", 4);
 		gameSize = gameWidth * gameHeight;
 		variant = AlignmentVariant.variants[conf.getInteger(
 				"gamesman.game.variant", 1)];
		if (variant == AlignmentVariant.SUDDEN_DEATH)
			piecesToWin = 1;
		else
			piecesToWin = conf.getInteger("gamesman.game.pieces", 5);
 		// Removing corners
 		/*
 		 * Not compatible with AlignmentState and this removal is incorrect. if
 		 * (gameWidth > 4 && gameHeight > 4) { openCells.remove(0);
 		 * openCells.remove(1); openCells.remove(gameWidth);
 		 * openCells.remove(gameWidth-1); openCells.remove(gameWidth-2);
 		 * openCells.remove(2*gameWidth - 1);
 		 * openCells.remove((gameHeight-1)*gameWidth);
 		 * openCells.remove((gameHeight-2)*gameWidth);
 		 * openCells.remove((gameHeight-1)*gameWidth + 1);
 		 * openCells.remove((gameHeight-1)*gameWidth - 1);
 		 * openCells.remove((gameHeight)*gameWidth - 1);
 		 * openCells.remove((gameHeight)*gameWidth - 2); }
 		 */
 
 		openCells = new ArrayList<Pair<Integer, Integer>>();
 
 		for (int row = 0; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 
 				openCells.add(new Pair<Integer, Integer>(row, col));
 			}
 		}
 
 		myHasher = new AlignmentHasher(this);
 	}
 
 	@Override
 	public String describe() {
 		return "Alignment: " + gameWidth + "x" + gameHeight + " " + piecesToWin
 				+ " captures " + variant;
 	}
 
 	@Override
 	public String displayState(AlignmentState pos) {
 		StringBuilder board = new StringBuilder(2 * (gameWidth + 2)
 				* gameHeight);
 		int row = 0;
 		char nextSquare;
 		for (; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 				nextSquare = pos.get(row, col);
 				if (nextSquare == ' ') {
 					board.append('_' + " ");
 				} else {
 					board.append(pos.get(row, col) + " ");
 				}
 
 			}
 		}
 		for (row = 0; row < gameHeight; row++) {
 			board.replace((2 * gameWidth * (row + 1) - 1),
 					(2 * gameWidth * (row + 1)), "\n"); // is this correct?
 		}
 		board.append("xDead: " + pos.xDead + " oDead: " + pos.oDead + " "
 				+ opposite(pos.lastMove) + "\'s turn");
 		return board.toString();
 	}
 
 	@Override
 	public void hashToState(long hash, AlignmentState s) {
 		myHasher.unhash(hash, s);
 		assert Util.debug(DebugFacility.GAME, "The newest state is "
 				+ stateToString(s));
 	}
 
 	@Override
 	public int maxChildren() {
 		if (variant == AlignmentVariant.NO_SLIDE
 				|| variant == AlignmentVariant.SUDDEN_DEATH) {
 			return gameWidth * gameHeight;
 		} else {
 			return gameHeight * 65;
 		}
 
 	}
 
 	@Override
 	public AlignmentState newState() {
 		char[][] board = new char[gameHeight][gameWidth];
 		for (int row = 0; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 				board[row][col] = ' ';
 			}
 		}
 		return new AlignmentState(board, 0, 0, 'O');
 	}
 
 	@Override
 	public long numHashes() {
 		return myHasher.numHashes();
 	}
 
 	@Override
 	public Value primitiveValue(AlignmentState pos) {
 		if (pos.lastMove == 'X') {
 			if (pos.oDead >= piecesToWin) {
 				return Value.LOSE;
 			}
 			if (pos.xDead >= piecesToWin) {
 				return Value.WIN;
 			}
 			if (pos.full()) {
 				return Value.TIE;
 			} else {
 				return Value.UNDECIDED;
 			}
 		}
 		if (pos.lastMove == 'O') {
 			if (pos.xDead >= piecesToWin) {
 				return Value.LOSE;
 			}
 			if (pos.oDead >= piecesToWin) {
 				return Value.WIN;
 			}
 			if (pos.full()) {
 				return Value.TIE;
 			} else {
 				return Value.UNDECIDED;
 			}
 		} else {
 			throw new IllegalArgumentException("Last move cannot be "
 					+ pos.lastMove);
 		}
 	}
 
 	@Override
 	public Collection<AlignmentState> startingPositions() {
 		AlignmentState as = newState();
 		for (Pair<Integer, Integer> place : openCells)
 			as.put(place.car, place.cdr, ' ');
 		ArrayList<AlignmentState> retVal = new ArrayList<AlignmentState>(1);
 		retVal.add(as);
 		return retVal;
 	}
 
 	@Override
 	public long stateToHash(AlignmentState pos) {
 		return myHasher.hash(pos);
 	}
 
 	@Override
 	public String stateToString(AlignmentState pos) {
 		StringBuilder board = new StringBuilder(2 * (gameWidth + 2)
 				* gameHeight);
 		for (int row = 0; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 				board.append(pos.get(row, col));
 			}
 		}
 		board.append(pos.xDead + ":" + pos.lastMove + ":" + pos.oDead);
 		return board.toString();
 	}
 
 	@Override
 	public AlignmentState stringToState(String pos) {
 		char[][] board = new char[gameHeight][gameWidth];
 		int xDead, oDead;
 		char lastMove;
 		int square = 0;
 		for (int row = 0; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 				board[row][col] = pos.charAt(square);
 				square++;
 			}
 		}
 		String[] auxData = pos.substring(gameWidth * gameHeight).split(":");
 		xDead = Integer.parseInt(auxData[0]);
 		oDead = Integer.parseInt(auxData[2]);
 		lastMove = auxData[1].charAt(0);
 		return new AlignmentState(board, xDead, oDead, lastMove);
 	}
 
 	@Override
 	public Collection<Pair<String, AlignmentState>> validMoves(
 			AlignmentState pos) {
 		AlignmentState s = new AlignmentState(pos);
 		Collection<String> strings = new ArrayList<String>();
 		Collection<AlignmentState> states = new ArrayList<AlignmentState>();
 		if (variant == AlignmentVariant.STANDARD) {
 			throw new UnsupportedOperationException(
 					"STANDARD variant not complete");
 		} else if (variant == AlignmentVariant.NO_SLIDE
 				|| variant == AlignmentVariant.SUDDEN_DEATH) {
 			for (int row = 0; row < gameHeight; row++) {
 				for (int col = 0; col < gameWidth; col++) {
 					if (' ' == pos.get(row, col)) {
 						s.put(row, col, opposite(pos.lastMove));
 						strings.add("row: " + row + " col: " + col);
 						states.add(new AlignmentState(s));
 					}
 				}
 			}
 			return Pair.zip(strings, states);
 		} else if (variant == AlignmentVariant.DEAD_SQUARES) {
 			throw new UnsupportedOperationException(
 					"DEAD_SQUARES variant not complete");
 		}
 
 		return null;
 	}
 
 	@Override
 	public int validMoves(AlignmentState pos, AlignmentState[] children) {
 		int moves = 0;
 		if (variant == AlignmentVariant.STANDARD) {
 			throw new UnsupportedOperationException(
 					"STANDARD variant not complete");
 		} else if (variant == AlignmentVariant.NO_SLIDE
 				|| variant == AlignmentVariant.SUDDEN_DEATH) {
 			for (int row = 0; row < gameHeight; row++) {
 				for (int col = 0; col < gameWidth; col++) {
 					if (' ' == pos.get(row, col)) {
 						children[moves].set(pos);
 						children[moves].put(row, col, opposite(pos.lastMove));
 						children[moves].fireGuns(piecesToWin, variant);
 						children[moves].setLastMove(opposite(pos.lastMove));
 						moves++;
 					}
 				}
 			}
 
 		} else if (variant == AlignmentVariant.DEAD_SQUARES) {
 			throw new UnsupportedOperationException(
 					"DEAD_SQUARES variant not complete");
 		}
 		assert Util.debug(DebugFacility.GAME, (opposite(pos.lastMove)
 				+ " just moved\n" + moves + " moves possible"));
 		return moves;
 
 	}
 
 	public static char opposite(char player) {
 		switch (player) {
 		case ('X'):
 			return 'O';
 		case ('O'):
 			return 'X';
 		default:
 			return player;
 		}
 	}
 
 	int getWidth() {
 		return gameWidth;
 	}
 
 	int getHeight() {
 		return gameHeight;
 	}
 
 	@Override
 	public long getRecord(AlignmentState recordState, Record fromRecord) {
 		if (fromRecord.value == Value.UNDECIDED)
 			return gameSize + 2;
 		else if (fromRecord.value == Value.TIE)
 			return gameSize + 1;
 		else
 			return fromRecord.remoteness;
 	}
 
 	@Override
 	public void recordFromLong(AlignmentState recordState, long record,
 			Record toStore) {
 		if (record == gameSize + 2) {
 			toStore.value = Value.UNDECIDED;
 		} else if (record == gameSize + 1) {
 			toStore.value = Value.TIE;
 			toStore.remoteness = gameSize - recordState.numPieces;
 		} else {
 			toStore.value = (record & 1) == 1 ? Value.WIN : Value.LOSE;
 			toStore.remoteness = (int) record;
 		}
 	}
 
 	@Override
 	public long recordStates() {
 		return gameSize + 3;
 	}
 
 }
 
 enum AlignmentVariant {
 	STANDARD, NO_SLIDE, DEAD_SQUARES, SUDDEN_DEATH; // STANDARD = 1, NO_SLIDE =
 	// 2,
 	// DEAD_SQUARES = 3, SUDDEN_DEATH = 4;
 	static final AlignmentVariant[] variants = AlignmentVariant.values();
 }
