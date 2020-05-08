 package edu.berkeley.gamesman.core;
 
 import java.math.BigInteger;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Public interface that all Games must implement to be solvable
  * 
  * @author Steven Schlansker
  * @param <State> The object used to represent a Game State
  *
  */
 public abstract class Game<State> {
 	
 	protected final int gameWidth, gameHeight;
 	protected final Configuration conf;
 	
 	@SuppressWarnings("unused")
 	private Game(){
 		Util.fatalError("Do not call this constructor!");
 		gameWidth = 0;
 		gameHeight = 0;
 		conf = null;
 	}
 	
 	/**
 	 * Initialize game width/height
 	 * NB: when this constructor is called, the Configuration
 	 * is not required to have initialized the Hasher yet!
 	 * @param conf configuration
 	 */
 	public Game(Configuration conf){
 		String w = conf.getProperty("gamesman.game.width","<auto>");
 		String h = conf.getProperty("gamesman.game.height","<auto>");
 		if(w.equals("<auto>"))
 			gameWidth = getDefaultBoardWidth();
 		else
 			gameWidth = Integer.parseInt(w);
 		
 		if(h.equals("<auto>"))
 			gameHeight = getDefaultBoardHeight();
 		else
 			gameHeight = Integer.parseInt(h);
 		this.conf = conf;
 	}
 	
 	/**
 	 * Generates all the valid starting positions
 	 * @return a Collection of all valid starting positions
 	 */
 	public abstract Collection<State> startingPositions();
 	
 	/**
 	 * Given a board state, generates all valid board states one move away from the given state
 	 * @param pos The board state to start from
 	 * @return A <move,state> pair for all valid board states one move forward
 	 */
 	public abstract Collection<Pair<String,State>> validMoves(State pos);
 	
	/**
	 * @param pos
	 * @param move
	 * @return
	 */
 	public State doMove(State pos, String move) {
 		for(Pair<String, State> next : validMoves(pos)) {
 			if(next.car.equals(move))
 				return next.cdr;
 		}
 		return null;
 	}
 	
 	/**
 	 * Given a board state return its primitive "value".
 	 * Usually this value includes WIN, LOSE, and perhaps TIE
 	 * Return UNDECIDED if this is not a primitive state
 	 * @param pos The primitive State
 	 * @return the Record representing the state
 	 * @see edu.berkeley.gamesman.core.Record
 	 */
 	public abstract PrimitiveValue primitiveValue(State pos);
 	
 	/**
 	 * @param conf the Configuration that this game is played with
 	 */
 	//public abstract void initialize(Configuration conf);
 	
 	/**
 	 * Unhash a given hashed value and return the corresponding Board
 	 * @param hash The hash given
 	 * @return the State represented
 	 */
 	public abstract State hashToState(BigInteger hash);
 	/**
 	 * Hash a given state into a hashed value
 	 * @param pos The State given
 	 * @return The hash that represents that State
 	 */
 	public abstract BigInteger stateToHash(State pos);
 	
 	/**
 	 * @return the last valid hash possible in the current configuration
 	 */
 	public abstract BigInteger lastHash();
 	
 	/**
 	 * Produce a machine-parsable String representing the state.  This
 	 * function must be the exact opposite of stringToState
 	 * @param pos the State given
 	 * @return a String
 	 * @see Game#stringToState(String)
 	 */
 	public abstract String stateToString(State pos);
 	
 	/**
 	 * "Pretty-print" a State for display to the user
 	 * @param pos The state to display
 	 * @return a pretty-printed string
 	 */
 	public abstract String displayState(State pos);
 	/**
 	 * Given a String construct a State.
 	 * This <i>must</i> be compatible with stateToString as it is
 	 * used to send states over the network.
 	 * @param pos The String given
 	 * @return a State
 	 * @see Game#stateToString(Object)
 	 */
 	public abstract State stringToState(String pos);
 	
 	/**
 	 * @return the default board width for this game
 	 */
 	public abstract int getDefaultBoardWidth();
 	/**
 	 * @return the default board height for this game
 	 */
 	public abstract int getDefaultBoardHeight();
 	
 	/**
 	 * @return the width of the board this game is played on
 	 */
 	public int getGameWidth(){
 		return gameWidth;
 	}
 	
 	/**
 	 * @return the height of the board this game is played on
 	 */
 	public int getGameHeight(){
 		return gameHeight;
 	}
 	
 	/**
 	 * @return a String that uniquely describes the setup of this Game (including any variant information, game size, etc)
 	 */
 	public abstract String describe();
 	
 	/**
 	 * Called to notify the Game that the Configuration now has
 	 * a specified and initialized Hasher.
 	 * Make sure to call your superclass's method!
 	 */
 	public void prepare(){}
 	
 	/**
 	 * @return the pieces that a Hasher will have to deal with
 	 */
 	public abstract char[] pieces();
 	
 }
