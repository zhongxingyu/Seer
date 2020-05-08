 package it.chalmers.tendu.gamemodel;
 
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 
 import java.util.Map;
 
 //TODO make none dependent of internal clock
 
 public abstract class MiniGame {
 	private Difficulty difficulty;
 	private final GameId gameId;
 	private long gameTime;
 	private SimpleTimer timer;
 
 	/**
 	 * Creates a new minigame.
 	 * 
 	 * @param addTime
 	 *            > 0 if extra time should be added
 	 * @param difficulty
 	 *            the game's difficulty
 	 * @param gameId
 	 */
 	public MiniGame(Difficulty difficulty, GameId gameId,
 			Map<String, Integer> players) {
 		this.difficulty = difficulty;
 		this.gameId = gameId;
 		this.players = players;
 		timer = new SimpleTimer();
 	}
 
 	/** No args constructor for reflection use */
 	protected MiniGame() {
 		gameId = null;
 	}
 
 	public void setGameTime(long gameTime, long extraTime) {
 		this.gameTime = gameTime + extraTime;
 	}
 
 	/**
 	 * Gets the remaining time
 	 * 
 	 * @return time left in millis seconds
 	 */
 	public long getRemainingTime() {
 		return timer.getRemainingTime();
 	}
 
 	/**
 	 * Call if host pushed new model
 	 */
 	public void reInit() {
 		timer.restart(timer.getRemainingTime());
 	}
 
 	/**
 	 * @param time
 	 *            the change in milliseconds, can be positive or negative;
 	 */
 	public void changeTime(long time) {
 		timer.change(time);
 	}
 
 	public long getGameTime() {
 		return gameTime;
 	}
 
 	/**
 	 * Integer = player id String = player MacAddress
 	 */
 	private Map<String, Integer> players;
 
 	/**
 	 * Gets the difficulty of the game
 	 * 
 	 * @return the difficulty
 	 */
 	public Difficulty getDifficulty() {
 		return difficulty;
 	}
 
 	/**
 	 * Sets the difficulty
 	 * 
 	 * @param difficulty
 	 *            the difficulty
 	 */
 	public void setDifficulty(Difficulty difficulty) {
 		this.difficulty = difficulty;
 	}
 
 
 	/**
 	 * Gets the game id.
 	 * 
 	 * @return the game's id
 	 */
 	public GameId getGameId() {
 		return gameId;
 	}
 
 	public void startGameTimer() {
 		timer.start(gameTime);
 	}
 
 	/**
 	 * Get the player number corresponding to your own macAddress.
 	 * 
 	 * @return
 	 */
 	public int getplayerNbr() {
 		String myMac = Player.getInstance().getMac();
 		int playerNbr = players.get(myMac);
 		return playerNbr;
 	}
 
 	/**
 	 * Get the number of players currently playing the game
 	 * 
 	 * @return
 	 */
 	public int getNumberOfPlayers() {
 		return players.size();
 	}
 	public boolean timerIsDone() {
 		
 		return timer.isDone();
 	}
 
 	/**
 	 * Returns the results of the game
 	 */
 	public abstract GameResult getGameResult();
 
 
	//Override this method
 	public abstract GameState checkGameState(); 
 //		if(timer.isDone()) {
 //			return GameState.LOST;
 //		} 
 //		
 //		return GameState.RUNNING;
 //	}
 
 	public void pauseTimer() {
 		timer.pause();
 	}
 }
