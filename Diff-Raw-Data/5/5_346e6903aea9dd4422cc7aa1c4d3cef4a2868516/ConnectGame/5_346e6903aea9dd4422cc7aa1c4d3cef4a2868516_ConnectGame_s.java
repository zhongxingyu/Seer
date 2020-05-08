 package edu.berkeley.gamesman.game;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.*;
 import edu.berkeley.gamesman.game.util.ItergameState;
 import edu.berkeley.gamesman.hasher.MMHasher;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A superclass for hex-style dartboard games in which the objective is to
  * connect across the board somehow
  * 
  * @author dnspies
  */
 public abstract class ConnectGame extends TieredIterGame {
 	private char turn;
 	protected MMHasher mmh;
 	protected final ItergameState myState = newState();
 
 	/**
 	 * @param conf
 	 *            The configuration object
 	 */
 	public void initialize(Configuration conf) {
 		super.initialize(conf);
 		mmh = new MMHasher();
 	}
 
 	protected abstract int getBoardSize();
 
 	@Override
 	public ItergameState getState() {
 		return myState;
 	}
 
 	@Override
 	public int getTier() {
 		return myState.tier;
 	}
 
 	@Override
 	public boolean hasNextHashInTier() {
 		return myState.hash < numHashesForTier() - 1;
 	}
 
 	@Override
 	public int maxChildren() {
 		return getBoardSize();
 	}
 
 	@Override
 	public void nextHashInTier() {
 		myState.hash++;
 		gameMatchState();
 	}
 
 	@Override
 	public long numHashesForTier() {
 		int tier = getTier();
 		return Util.nCr(getBoardSize(), tier) * Util.nCr(tier, tier / 2);
 	}
 
 	@Override
 	public long numHashesForTier(int tier) {
 		return Util.nCr(getBoardSize(), tier) * Util.nCr(tier, tier / 2);
 	}
 
 	@Override
 	public int numStartingPositions() {
 		return 1;
 	}
 
 	@Override
 	public int numberOfTiers() {
 		return getBoardSize() + 1;
 	}
 
 	@Override
 	public void setFromString(String pos) {
 		setToCharArray(convertInString(pos));
 		stateMatchGame();
 	}
 
 	private void stateMatchGame() {
 		char[] arr = getCharArray();
 		int tier = 0;
 		for (int i = 0; i < arr.length; i++) {
 			if (arr[i] != ' ')
 				tier++;
 		}
 		myState.tier = tier;
 		turn = ((tier & 1) == 1) ? 'O' : 'X';
 		myState.hash = mmh.hash(getCharArray());
 	}
 
 	private void gameMatchState() {
 		int tier = myState.tier;
 		mmh.unhash(myState.hash, getCharArray(), (tier + 1) / 2, tier / 2);
 	}
 
 	@Override
 	public void setStartingPosition(int n) {
 		char[] arr = getCharArray();
 		int size = getBoardSize();
 		for (int i = 0; i < size; i++)
 			arr[i] = ' ';
 		setToCharArray(arr);
 		myState.tier = 0;
 		turn = 'X';
 		myState.hash = 0;
 	}
 
 	@Override
 	public void setState(ItergameState pos) {
 		myState.set(pos);
 		gameMatchState();
 	}
 
 	@Override
 	public void setTier(int tier) {
 		myState.tier = tier;
		turn = ((tier & 1) == 1) ? 'O' : 'X';
 		myState.hash = 0;
 		gameMatchState();
 	}
 
 	@Override
 	public Collection<Pair<String, ItergameState>> validMoves() {
 		ItergameState[] moves = new ItergameState[maxChildren()];
 		int totalMoves = validMoves(moves);
 		ArrayList<Pair<String, ItergameState>> resultMoves = new ArrayList<Pair<String, ItergameState>>(
 				totalMoves);
 		for (int i = 0; i < totalMoves; i++) {
 			resultMoves.add(new Pair<String, ItergameState>(
 					Integer.toString(i), moves[i]));
 		}
 		return resultMoves;
 	}
 
 	@Override
 	public int validMoves(ItergameState[] moves) {
 		char[] pieces = getCharArray();
 		int c = 0;
 		for (int i = 0; i < pieces.length; i++) {
 			if (pieces[i] == ' ') {
 				pieces[i] = turn;
 				stateMatchGame();
 				moves[c].set(myState);
 				pieces[i] = ' ';
 				c++;
 			}
 		}
 		stateMatchGame();
 		return c;
 	}
 
 	private final class ConnectRecord extends Record {
 		protected ConnectRecord() {
 			super(conf);
 		}
 
 		protected ConnectRecord(long state) {
 			super(conf);
 			set(state);
 		}
 
 		protected ConnectRecord(PrimitiveValue pVal) {
 			super(conf, pVal);
 		}
 
 		@Override
 		public long getState() {
 			if (conf.remotenessStates > 0) {
 				return remoteness;
 			} else {
 				switch (value) {
 				case LOSE:
 					return 0L;
 				case WIN:
 					return 1L;
 				default:
 					return 0L;
 				}
 			}
 		}
 
 		@Override
 		public void set(long state) {
 			if ((state & 1) == 1)
 				value = PrimitiveValue.WIN;
 			else
 				value = PrimitiveValue.LOSE;
 			if (conf.remotenessStates > 0)
 				remoteness = (int) state;
 		}
 	}
 
 	@Override
 	public Record newRecord(PrimitiveValue pv) {
 		return new ConnectRecord(pv);
 	}
 
 	@Override
 	public Record newRecord() {
 		return new ConnectRecord();
 	}
 
 	@Override
 	public Record newRecord(long val) {
 		return new ConnectRecord(val);
 	}
 
 	@Override
 	public long recordStates() {
 		if (conf.remotenessStates > 0)
 			return getBoardSize() + 1;
 		else
 			return 2;
 	}
 
 	protected abstract char[] getCharArray();
 
 	@Override
 	public String stateToString() {
 		return convertOutString(getCharArray());
 	}
 
 	public abstract char[] convertInString(String s);
 
 	public abstract String convertOutString(char[] charArray);
 
 	protected abstract void setToCharArray(char[] myPieces);
 
 	@Override
 	public PrimitiveValue primitiveValue() {
 		PrimitiveValue result;
 		if ((myState.tier & 1) == 1)
 			result = isWin('X') ? PrimitiveValue.LOSE
 					: PrimitiveValue.UNDECIDED;
 		else
 			result = isWin('O') ? PrimitiveValue.LOSE
 					: PrimitiveValue.UNDECIDED;
 		assert Util.debug(DebugFacility.GAME, result.name() + "\n");
 		if (myState.tier == numberOfTiers() - 1
 				&& result == PrimitiveValue.UNDECIDED)
 			return PrimitiveValue.IMPOSSIBLE;
 		else
 			return result;
 	}
 
 	protected abstract boolean isWin(char c);
 }
