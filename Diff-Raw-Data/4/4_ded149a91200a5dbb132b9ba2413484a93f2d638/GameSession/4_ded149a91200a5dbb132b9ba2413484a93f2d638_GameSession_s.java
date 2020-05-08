 /*-******THIS IS OUR GAME MODEL*********/
 package it.chalmers.tendu.gamemodel;
 
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 
 public class GameSession {
 
 	// public String hostMacAddress;
 	public MiniGame currentMiniGame = null;
 	private int completedLvls;
 	private Difficulty difficulty = Difficulty.ONE;
 	/**
 	 * Integer = player id String = player MacAddress
 	 */
 	private Map<String, Integer> players;
 	private Map<String, Boolean> playersWaitingToStart;
 	// private List<GameResult> gameResults;
 	private SessionResult sessionResult;
 
 	public List<String> playerReplayReady;
 
 	// public GameSession(Map<String, Integer> players, String hostMac) {
 	// this.players = players;
 	// hostMacAddress = hostMac;
 	// }
 	public GameSession(Map<String, Integer> players) {
 		completedLvls = 0;
 		this.players = players;
 		playersWaitingToStart = new HashMap<String, Boolean>();
 		currentMiniGame = getNextMiniGame();
 		// gameResults = new ArrayList<GameResult>();
 		sessionResult = new SessionResult();
 
 	}
 
 	// for reflection
 	@SuppressWarnings("unused")
 	private GameSession() {
 	}
 
 	private GameId getNextGameId() {
 		if (completedLvls < 2) {
 			difficulty = Difficulty.ONE;
 		} else if (completedLvls < 4) {
 			difficulty = Difficulty.TWO;
 		} else if (completedLvls < 6) {
 			difficulty = Difficulty.THREE;
 		} else if (completedLvls < 8) {
 			difficulty = Difficulty.FOUR;
 		} else {
 			difficulty = Difficulty.FIVE;
 		}
 		return MiniGameFactory.createGameId(difficulty);
 	}
 
 	private MiniGame getMiniGame(GameId gameId) {
 		long extraTime = 0;
 
 		if (sessionResult != null && sessionResult.gamesPlayed() > 0) {
 			extraTime = sessionResult.getLastResult().getRemainingTime();
 		}
 
 		return MiniGameFactory.createMiniGame(extraTime, gameId, difficulty,
 				players);
 
 	}
 
 	public MiniGame getNextMiniGame() {
 		return getMiniGame(getNextGameId());
 
 	}
 
 	public void setCurrentMiniGame(MiniGame miniGame) {
 		currentMiniGame = miniGame;
 	}
 
 	public Map<String, Integer> getPlayers() {
 		return players;
 	}
 
 	public void playerWaitingToStart(String macAddress) {
 		playersWaitingToStart.put(macAddress, true);
 	}
 
 	public boolean allWaiting() {
 		return (players.size() == playersWaitingToStart.size());
 	}
 
 	public void nextScreen() {
 		EventMessage message = new EventMessage(C.Tag.TO_SELF,
 				C.Msg.CREATE_SCREEN, currentMiniGame);
 		EventBus.INSTANCE.broadcast(message);
 	}
 
 	public void interimScreen() {
 		EventMessage message = new EventMessage(C.Tag.TO_SELF,
 				C.Msg.SHOW_INTERIM_SCREEN, sessionResult);
 		EventBus.INSTANCE.broadcast(message);
 	}
 
 	public void gameOverScreen() {
 		EventMessage message = new EventMessage(C.Tag.TO_SELF,
 				C.Msg.SHOW_GAME_OVER_SCREEN, sessionResult);
 		EventBus.INSTANCE.broadcast(message);
 	}
 
 	public void playerReplayReady(String player) {
 		if (!playerReplayReady.contains(player)) {
 			playerReplayReady.add(player);
 		}
 	}
 
 	public boolean arePlayersReady() {
 		return (players.size() == playerReplayReady.size());
 	}
 
 	public void miniGameEnded(GameResult gameResult) {
 		Gdx.app.log(this.getClass().getSimpleName(), " Time left = "
 				+ gameResult.getRemainingTime());
 		Gdx.app.log(this.getClass().getSimpleName(), " GameState = "
 				+ gameResult.getGameState());
 
 		if (gameResult.getGameState() == GameState.WON) {
 			sessionResult.addResult(gameResult);
 			interimScreen();
 		} else {
 			gameOverScreen();
 
 			// empty the results list
 			sessionResult.clear();
 		}
 
 		completedLvls = (sessionResult.gamesPlayed());
 	}
 }
