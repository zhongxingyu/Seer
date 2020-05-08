 package model.liarsDice.gameInfo;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import model.liarsDice.gameLogic.Bid;
 import model.liarsDice.gameLogic.Die;
 
 
 
 /**
  * Holds the relevant information for the current state of the game, including the current bid, the game history so far, 
  * the index of the player whose turn it is, and a PlayerInfo object for each player in the game.
  */
 public class GameInfo {
 	private Bid currentBid;
 	private GameHistory gameHistory;
 	private int myIndex;
 	private List<PlayerInfo> playersInfo;
 	
 	/**
 	 * Default constructor.
 	 */
 	public GameInfo(){
 		init(null, new GameHistory(), -1, new ArrayList<PlayerInfo>());
 	}
 	
 	/**
 	 * Constructor.
 	 * @param currentBid The current bid.
 	 * @param gameHistory The history of the game so far.
 	 * @param myIndex The index into the players list of the player for which this object was created.
 	 * @param players List of PlayerInfo objects (one for each player).
 	 */
 	public GameInfo(Bid currentBid, GameHistory gameHistory, int myIndex, List<PlayerInfo> players) {
 		init(currentBid, gameHistory, myIndex, players);
 	}
 	
 	/**
 	 * Copy constructor.  Creates a deep (unmodifiable) copy of everything in gi.
 	 * @param gi The GameInfo object to be copied.
 	 */
 	public GameInfo(GameInfo gi){
 		init(gi.getCurrentBid(), gi.getGameHistory(), gi.myIndex, gi.getAllPlayersInfo());
 	}
 
 	/**
 	 * Creates a deep (unmodifiable) copy of all parameters.
 	 * @param currentBid The current bid.
 	 * @param gameHistory The history of the game so far.
 	 * @param myIndex The index into playersInfo of the player whose turn it is.
 	 * @param playersInfo List of PlayerInfo objects (one for each player).
 	 */
 	public void init(Bid currentBid, GameHistory gameHistory, int myIndex, List<PlayerInfo> playersInfo) {
 		this.currentBid = currentBid;
 		this.gameHistory = new GameHistory(gameHistory);
 		this.myIndex = myIndex;
 		this.playersInfo = Collections.unmodifiableList(playersInfo);
 	}
 	
 	/**
 	 * @return The current bid or null if there are no bids yet (first turn of each round).
 	 */
 	public Bid getCurrentBid() {
 		return currentBid;
 	}
 
 	/**
 	 * @return The game history.
 	 */
 	public GameHistory getGameHistory() {
 		return gameHistory;
 	}
 
 	/**
 	 * @return The dice of the player whose turn it is.
 	 */
 	public List<Die> getMyDice() {
 		return playersInfo.get(myIndex).getDice();
 	}
 	
 	/**
 	 * @return The ID of the player whose turn it is.
 	 */
 	public int getMyPlayerID() {
 		return playersInfo.get(myIndex).getID();
 	}
 	
 	/**
 	 * @return List of PlayerInfo objects (one for each player including mine).
 	 */
 	public List<PlayerInfo> getAllPlayersInfo() {
 		return playersInfo;
 	}
 
 	/**
 	 * @return List of PlayerInfo objects (one for each player excluding mine).
 	 */
 	public List<PlayerInfo> getOtherPlayersInfo() {
 		List<PlayerInfo> otherPlayersInfo = new ArrayList<PlayerInfo>();
 		for (int i=0; i<playersInfo.size(); i++) {
 			if (i != myIndex)
 				otherPlayersInfo.add(playersInfo.get(i));
 		}
 		return otherPlayersInfo;
 	}
 	
 	/**
 	 * @return The ID of the winner of the game, or -1 if there is no winner (the game isn't over yet).
 	 */
 	public int getWinnerID()
 	{
		int winner = 0;
 		
 		for(PlayerInfo p : playersInfo){
 			if(p.getNumDice() > 0){
				if (winner != 0)
 					return -1;
 				winner = p.getID();
 			}
 		}
 		
 		return winner;
 	}
 	
 	/**
 	 * @return The total number of dice (between all players) remaining in the game.
 	 */
 	public int getTotalDice() {
 		int totalDice = 0;
 		for(PlayerInfo p : playersInfo){
 			totalDice += p.getNumDice();
 		}
 		return totalDice;
 	}
 	
 	/**
 	 * Checks to see if there is only one player with dice left.
 	 * @return true if game is over, false otherwise.
 	 */
 	public boolean isGameOver() {
 		return getWinnerID() >= 0;
 	}
 
 	/**
 	 * @return The index into getAllPlayersInfo() that will return my player info.
 	 */
 	public int getMyIndex() {
 		return myIndex;
 	}
 }
