 package edu.berkeley.gamesman.game;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.*;
 import edu.berkeley.gamesman.game.util.BitSetBoard;
 import edu.berkeley.gamesman.game.util.Connect4ReducerBoard;
 import edu.berkeley.gamesman.game.util.ItergameState;
 import edu.berkeley.gamesman.game.util.PieceRearranger;
 import edu.berkeley.gamesman.util.ExpCoefs;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Implementation of Connect 4 using the general IterArrangerHasher
  * 
  * @author DNSpies
  */
 public final class Connect4 extends TieredIterGame {
 	private int[][] indices;
 
 	private int[] colHeights;
 
 	private int piecesToWin;
 
 	private int gameHeight, gameWidth, gameSize;
 
 	private ArrayList<Place> pieces;
 
 	private long[] multiplier;
 
 	private long[] moveArrangement;
 
 	/**
 	 * A list of the columns which are not full
 	 */
 	public int[] openColumn;
 
 	private long[] children;
 
 	private BitSetBoard bsb;
 
 	private ExpCoefs ec;
 
 	private long pieceArrangement;
 
 	private boolean hasNextPieceArrangement = false;
 
 	private PieceRearranger iah;
 
 	private int[] groupSizes;
 
 	private int numMoves;
 
 	private static final class Place {
 		private Place(int row, int col) {
 			this.row = row;
 			this.col = col;
 		}
 
 		private int row, col;
 	}
 
 	/**
 	 * @param conf
 	 *            The configuration object
 	 */
 	public void initialize(Configuration conf) {
 		super.initialize(conf);
 		gameWidth = conf.getInteger("gamesman.game.width", 7);
 		gameHeight = conf.getInteger("gamesman.game.height", 6);
 		piecesToWin = conf.getInteger("gamesman.game.pieces", 4);
 		indices = new int[gameHeight][gameWidth];
 		for (int row = 0; row < gameHeight; row++)
 			for (int col = 0; col < gameWidth; col++)
 				indices[row][col] = -1;
 		gameSize = gameWidth * gameHeight;
 		pieces = new ArrayList<Place>(gameSize);
 		moveArrangement = new long[gameWidth];
 		colHeights = new int[gameWidth];
 		if (conf.getBoolean("gamesman.game.reduceWins", false))
 			bsb = new Connect4ReducerBoard(gameHeight, gameWidth);
 		else
 			bsb = new BitSetBoard(gameHeight, gameWidth);
 		ec = new ExpCoefs(gameHeight, gameWidth + 1);
 		multiplier = new long[gameSize + 1];
 		multiplier[0] = 1;
 		for (int i = 1; i <= gameSize; i++)
 			multiplier[i] = multiplier[i - 1] * i / ((i + 1) / 2);
 		children = new long[gameWidth];
 		openColumn = new int[gameWidth];
 		groupSizes = new int[gameWidth];
 	}
 
 	@Override
 	public String displayState() {
 		String s = stateToString();
 		StringBuffer str = new StringBuffer((gameWidth + 3) * gameHeight);
 		for (int row = gameHeight - 1; row >= 0; row--)
 			str.append("|"
 					+ s.substring(row * gameWidth, (row + 1) * gameWidth)
 					+ "|\n");
 		return str.toString();
 	}
 
 	@Override
 	public ItergameState getState() {
 		return new ItergameState(pieces.size(), pieceArrangement
 				* iah.colorArrangements + iah.getHash());
 	}
 
 	@Override
 	public int getTier() {
 		return pieces.size();
 	}
 
 	@Override
 	public boolean hasNextHashInTier() {
 		return iah.hasNext() || hasNextPieceArrangement;
 	}
 
 	@Override
 	public void nextHashInTier() {
 		if (iah.hasNext())
 			changeBitSet(iah.next());
 		else
 			nextPieceArrangement();
 	}
 
 	private void nextPieceArrangement() {
 		pieceArrangement++;
 		int col = 0, row;
 		Place rowCol;
 		while (colHeights[col] == 0) {
 			col++;
 		}
 		int pieceCount = 0;
 		do {
 			pieceCount += colHeights[col];
 			col++;
 		} while (colHeights[col] == gameHeight);
 		int numPieces = pieceCount - 1;
 		colHeights[col]++;
 		pieceCount = 0;
 		for (int i = 0; i < col; i++) {
 			if (numPieces - pieceCount > gameHeight) {
 				colHeights[i] = gameHeight;
 			} else {
 				colHeights[i] = numPieces - pieceCount;
 			}
 			for (row = 0; row < colHeights[i]; row++) {
 				indices[row][i] = pieceCount;
 				rowCol = pieces.get(pieceCount);
 				rowCol.row = row;
 				rowCol.col = i;
 				pieceCount++;
 			}
 			for (; row < gameHeight; row++)
 				indices[row][i] = -1;
 		}
 		for (row = 0; row < colHeights[col]; row++) {
 			indices[row][col] = pieceCount;
 			rowCol = pieces.get(pieceCount);
 			rowCol.row = row;
 			rowCol.col = col;
 			pieceCount++;
 		}
 		for (; row < gameHeight; row++)
 			indices[row][col] = -1;
 		if (numPieces == 0) {
 			for (col++; col < gameWidth; col++)
 				if (colHeights[col] < gameHeight)
 					break;
 			if (col == gameWidth)
 				hasNextPieceArrangement = false;
 		}
 		numMoves = 0;
 		int totSize = 0;
 		for (int i = 0; i < gameWidth; i++) {
 			if (colHeights[i] == gameHeight)
 				totSize += colHeights[i];
 			else {
 				openColumn[numMoves] = i;
 				groupSizes[numMoves] = (totSize + colHeights[i]);
 				numMoves++;
 				totSize = 0;
 			}
 		}
 		iah.setGroupSizes(groupSizes, numMoves);
 		iah.reset();
 		setMoveArrangements();
 		setBSBfromIAH();
 	}
 
 	private void changeBitSet(PieceRearranger.ChangedPieces cp) {
 		Place rowCol;
 		while (cp.hasNext()) {
 			rowCol = pieces.get(cp.next());
 			bsb.flipPiece(rowCol.row, rowCol.col);
 		}
 	}
 
 	@Override
 	public long numHashesForTier() {
 		return ec.getCoef(gameWidth, pieces.size()) * iah.colorArrangements;
 	}
 
 	@Override
 	public int numStartingPositions() {
 		return 1;
 	}
 
 	@Override
 	public PrimitiveValue primitiveValue() {
 		char lastTurn = (pieces.size() % 2 == 1 ? 'X' : 'O');
 		int result = bsb.xInALine(piecesToWin, lastTurn);
 		if (result > 0)
 			return PrimitiveValue.LOSE;
 		else if (result < 0)
 			return PrimitiveValue.IMPOSSIBLE;
 		else if (pieces.size() == gameSize)
 			return PrimitiveValue.TIE;
 		else
 			return PrimitiveValue.UNDECIDED;
 	}
 
 	private char get(int row, int col) {
 		if (indices[row][col] == -1)
 			return ' ';
 		else
 			return iah.get(indices[row][col]);
 	}
 
 	@Override
 	public void setStartingPosition(int n) {
 		setTier(0);
 	}
 
 	@Override
 	public void setState(ItergameState pos) {
 		setTier(pos.tier);
 		long mult = iah.colorArrangements;
 		long hash = pos.hash;
 		setArrangement(hash / mult);
 		iah.setFromHash(hash % mult);
 		setBSBfromIAH();
 	}
 
 	private void setArrangement(long arrange) {
 		int pieceCount = pieces.size();
 		for (int col = gameWidth - 1; col >= 0; col--) {
 			colHeights[col] = 0;
 			long tryHash = ec.getCoef(col, pieceCount);
 			while (arrange >= tryHash) {
 				arrange -= tryHash;
 				pieceCount--;
 				colHeights[col]++;
 				tryHash = ec.getCoef(col, pieceCount);
 			}
 		}
 		setToColHeights(pieces.size());
 	}
 
 	@Override
 	public void setTier(int tier) {
 		int pieceCount = 0;
 		for (int col = 0; col < gameWidth; col++) {
 			if (tier - pieceCount > gameHeight) {
 				pieceCount += gameHeight;
 				colHeights[col] = gameHeight;
 			} else {
 				colHeights[col] = tier - pieceCount;
 				pieceCount = tier;
 			}
 		}
 		setToColHeights(tier);
 	}
 
 	private void setToColHeights(int numPieces) {
 		int col = 0, row = 0;
 		StringBuilder rearrangeString = new StringBuilder(numPieces + gameWidth);
 		pieceArrangement = 0;
 		numMoves = 0;
 		groupSizes[0] = 0;
 		hasNextPieceArrangement = false;
 		int os = numPieces / 2;
 		pieces.clear();
 		for (int i = 0; i < numPieces; i++) {
 			while (row >= colHeights[col]) {
 				if (colHeights[col] < gameHeight) {
 					if (i > colHeights[col])
 						hasNextPieceArrangement = true;
 					rearrangeString.append(' ');
 					for (; row < gameHeight; row++)
 						indices[row][col] = -1;
 					openColumn[numMoves] = col;
 					groupSizes[numMoves] += colHeights[col];
 					numMoves++;
 					if (numMoves < gameWidth)
 						groupSizes[numMoves] = 0;
 				} else
 					groupSizes[numMoves] += gameHeight;
 				col++;
 				row = 0;
 			}
 			indices[row][col] = i;
 			pieces.add(new Place(row, col));
 			rearrangeString.append('T');
 			pieceArrangement += ec.getCoef(col, i + 1);
 			row++;
 		}
 		if (colHeights[col] < gameHeight) {
 			rearrangeString.append(' ');
 			openColumn[numMoves] = col;
 			groupSizes[numMoves] += colHeights[col];
 			numMoves++;
 		}
 		for (col++; col < gameWidth; col++) {
 			rearrangeString.append(' ');
 			openColumn[numMoves] = col;
 			groupSizes[numMoves] = 0;
 			numMoves++;
 		}
 		setMoveArrangements();
 		iah = new PieceRearranger(rearrangeString.toString(), os, numPieces
 				- os);
 		setBSBfromIAH();
 	}
 
 	private void setMoveArrangements() {
 		int i = pieces.size();
 		long addValue = 0;
 		for (int col = gameWidth - 1; col >= 0; col--) {
 			addValue += ec.getCoef(col, i + 1);
 			moveArrangement[col] = pieceArrangement + addValue;
 			i -= colHeights[col];
 			addValue -= ec.getCoef(col, i + 1);
 		}
 	}
 
 	@Override
 	public long numHashesForTier(int numPieces) {
 		return ec.getCoef(gameWidth, numPieces) * multiplier[numPieces];
 	}
 
 	@Override
 	public void setFromString(String pos) {
 		int numPieces = 0;
 		StringBuilder iahString = new StringBuilder(gameSize);
 		for (int col = 0; col < gameWidth; col++) {
 			colHeights[col] = 0;
 			for (int row = 0; row < gameHeight; row++) {
 				char c = pos.charAt(row * gameWidth + col);
 				if (c == ' ') {
 					iahString.append(' ');
 					break;
 				} else {
 					iahString.append(c);
 					colHeights[col]++;
 					numPieces++;
 				}
 			}
 		}
 		setToColHeights(numPieces);
 		iah = new PieceRearranger(iahString.toString());
 		setBSBfromIAH();
 	}
 
 	private void setBSBfromIAH() {
 		bsb.clear();
 		for (int i = 0; i < pieces.size(); i++) {
 			Place rowCol = pieces.get(i);
 			bsb.addPiece(rowCol.row, rowCol.col, iah.get(i));
 		}
 	}
 
 	@Override
 	public String stateToString() {
 		StringBuilder board = new StringBuilder(gameSize);
 		for (int row = 0; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 				board.append(get(row, col));
 			}
 		}
 		return board.toString();
 	}
 
 	@Override
 	public Collection<Pair<String, ItergameState>> validMoves() {
 		int lenChildren = iah.getChildren(pieces.size() % 2 == 1 ? 'O' : 'X',
 				children);
 		int nextNumPieces = pieces.size() + 1;
 		ArrayList<Pair<String, ItergameState>> moves = new ArrayList<Pair<String, ItergameState>>(
 				lenChildren);
 		int col = 0;
 		for (int i = 0; i < lenChildren; i++) {
 			while (colHeights[col] == gameHeight)
 				col++;
 			moves.add(new Pair<String, ItergameState>(String.valueOf(col),
 					new ItergameState(nextNumPieces, moveArrangement[col]
 							* multiplier[nextNumPieces] + children[i])));
 			col++;
 		}
 		return moves;
 	}
 
 	@Override
 	public int validMoves(ItergameState[] moves) {
 		int lenChildren = iah.getChildren(pieces.size() % 2 == 1 ? 'O' : 'X',
 				children);
 		int nextNumPieces = pieces.size() + 1;
 		int col = 0;
 		for (int i = 0; i < lenChildren; i++) {
 			while (colHeights[col] == gameHeight)
 				col++;
 			moves[i].tier = nextNumPieces;
 			moves[i].hash = moveArrangement[col] * multiplier[nextNumPieces]
 					+ children[i];
 			col++;
 		}
 		return lenChildren;
 	}
 
 	/**
 	 * @param moves
 	 *            Returns the value of the last time of move was possible in
 	 *            each column
 	 */
 	public void lastMoves(ItergameState[] moves) {
 		char nextPiece = pieces.size() % 2 == 1 ? 'O' : 'X';
 		int easyChildren = iah.getChildren(nextPiece, children);
 		int nextNumPieces = pieces.size() + 1;
 		int col;
 		int[] oldHeights = new int[gameWidth];
 		for (col = 0; col < gameWidth; col++)
 			oldHeights[col] = colHeights[col];
 		PieceRearranger oldArranger = iah;
 		for (col = 0; col < gameWidth && colHeights[col] >= gameHeight; ++col) {
 			int group = setToLastWithOpen(col);
 			if (group >= 0) {
 				moves[col].tier = nextNumPieces;
 				moves[col].hash = moveArrangement[col]
 						* multiplier[nextNumPieces]
 						+ iah.getChild(nextPiece, group);
 				setColHeights(pieces.size(), oldHeights);
 			} else {
 				moves[col].tier = 0;
 				moves[col].hash = 0L;
 			}
 		}
 		for (int i = 0; i < easyChildren; i++) {
 			moves[col].tier = nextNumPieces;
 			moves[col].hash = moveArrangement[col] * multiplier[nextNumPieces]
 					+ children[i];
 			for (++col; col < gameWidth && colHeights[col] >= gameHeight; ++col) {
 				int group = setToLastWithOpen(col);
 				if (group >= 0) {
 					moves[col].tier = nextNumPieces;
 					moves[col].hash = moveArrangement[col]
 							* multiplier[nextNumPieces]
 							+ iah.getChild(nextPiece, group);
 					setColHeights(pieces.size(), oldHeights);
 				} else {
 					moves[col].tier = 0;
 					moves[col].hash = 0L;
 				}
 			}
 		}
 		iah = oldArranger;
 		setBSBfromIAH();
 	}
 
 	private int setToLastWithOpen(int col) {
 		int i, piecesCount = 0;
 		int[] newHeights = new int[gameWidth];
 		for (i = 0; i < gameWidth; i++)
 			newHeights[i] = colHeights[i];
 		for (i = 0; i < col; ++i)
 			piecesCount += colHeights[i];
 		if (piecesCount >= col * gameHeight) {
 			piecesCount += gameHeight;
 			++i;
 			int tooMuch = piecesCount - 1;
 			while (i < gameWidth && piecesCount >= tooMuch) {
 				piecesCount += colHeights[i++];
 				tooMuch += gameHeight;
 			}
 			if (i >= gameWidth)
 				return -1;
 			while (i < gameWidth && colHeights[i] <= 0)
 				++i;
 			if (i >= gameWidth)
 				return -1;
 		}
 		++piecesCount;
 		--newHeights[i];
 		for (--i; i >= 0; --i) {
 			newHeights[i] = Math.min(piecesCount, gameHeight);
 			piecesCount -= newHeights[i];
 		}
 		if (newHeights[col] >= gameHeight) {
 			for (i = 0; i < col; i++)
 				piecesCount += newHeights[i];
 			if (piecesCount >= col * gameHeight) {
 				Util.fatalError("There should be space");
 				return -1;
 			}
 			++piecesCount;
 			--newHeights[col];
 			for (--i; i >= 0; --i) {
 				newHeights[i] = Math.min(piecesCount, gameHeight);
 				piecesCount -= newHeights[i];
 			}
 		}
 		setColHeights(pieces.size(), newHeights);
 		iah.setToEnd();
 		setBSBfromIAH();
 		for (i = 0; i < numMoves; i++) {
 			if (openColumn[i] == col)
 				break;
 		}
 		return i;
 	}
 
 	private void setColHeights(int numPieces, int[] newHeights) {
 		for (int col = 0; col < gameWidth; col++)
 			colHeights[col] = newHeights[col];
 		setToColHeights(numPieces);
 	}
 
 	@Override
 	public String describe() {
 		return toString();
 	}
 
 	@Override
 	public int numberOfTiers() {
 		return gameSize + 1;
 	}
 
 	@Override
 	public int maxChildren() {
 		return gameWidth;
 	}
 
 	public String toString() {
 		return gameWidth + "x" + gameHeight + " Connect " + piecesToWin;
 	}
 
 	private class C4Record extends Record {
 		protected C4Record() {
 			super(conf);
 		}
 
 		protected C4Record(long state) {
 			super(conf);
 			set(state);
 		}
 
 		protected C4Record(PrimitiveValue pVal) {
 			super(conf, pVal);
 		}
 
 		@Override
 		public long getState() {
 			if (conf.remotenessStates > 0) {
 				PrimitiveValue val = value;
 				if (val.equals(PrimitiveValue.TIE)) {
 					return gameSize + 1;
 				} else {
 					return remoteness;
 				}
 			} else {
 				return value.value;
 			}
 		}
 
 		@Override
 		public void set(long state) {
 			if (conf.remotenessStates > 0) {
 				if (state == gameSize + 1) {
 					value = PrimitiveValue.TIE;
 					remoteness = gameSize - pieces.size();
 				} else if ((state & 1L) > 0) {
 					value = PrimitiveValue.WIN;
 					remoteness = (int) state;
 				} else {
 					value = PrimitiveValue.LOSE;
 					remoteness = (int) state;
 				}
 			} else {
 				value = PrimitiveValue.values[(int) state];
 			}
 		}
 	}
 
 	@Override
 	public Record newRecord(PrimitiveValue pv) {
 		return new C4Record(pv);
 	}
 
 	@Override
 	public Record newRecord() {
 		return new C4Record();
 	}
 
 	@Override
 	public Record newRecord(long val) {
 		return new C4Record(val);
 	}
 
 	@Override
 	public long recordStates() {
 		if (conf.remotenessStates > 0)
 			return gameSize + 2;
 		else
 			return 3;
 	}
 }
