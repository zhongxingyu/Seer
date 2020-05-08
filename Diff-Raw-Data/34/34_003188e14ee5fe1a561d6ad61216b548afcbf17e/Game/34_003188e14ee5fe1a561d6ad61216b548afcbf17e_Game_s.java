 package theresistance.core;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import theresistance.core.state.GameState;
 import theresistance.core.state.ProposeState;
 import theresistance.core.util.ExtraInfoBag;
 
 /**
  * A game of The Resistance.
  */
 public class Game
 {
 	private String id;
 	private final GameConfig config;
 	private final List<Round> rounds = new LinkedList<>();
 	private List<Player> players = new LinkedList<>();
 	private GameState<?> gameState;
 
 	private final ExtraInfoBag extraInfo = new ExtraInfoBag();
 
 	private boolean isStarted = false;
 	private int curRound = 0;
 	private int curLeader = 0;
 	private Alignment winners = Alignment.NEITHER;
 
 	public Game(GameConfig config)
 	{
 		this.config = config;
 	}
 
 	/**
 	 * set id
 	 * 
 	 * @param id
 	 */
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	/**
 	 * @return id
 	 */
 	public String getId()
 	{
 		return id;
 	}
 
 	public void gotoNextLeader()
 	{
 		curLeader = (curLeader + 1) % players.size();
 	}
 
 	/**
 	 * @return config
 	 */
 	public GameConfig getConfig()
 	{
 		return config;
 	}
 
 	/**
 	 * Adds a new player to the game
 	 * 
 	 * @param player
 	 */
 	public void addPlayer(Player player)
 	{
 		players.add(player);
 	}
 
 	/**
 	 * Removes a player from the game.
 	 * 
 	 * @param player
 	 */
 	public void removePlayer(Player player)
 	{
 		players.remove(player);
 	}
 
 	/**
 	 * initializes the game and prepares for play
 	 */
 	public void start()
 	{
 		new RoleAssigner().assign(players, config.getRoles());
 
 		for (Mission mission : config.getMissions())
 		{
 			rounds.add(new Round(rounds.size(), mission));
 		}
 
 		for (PostRoundEventHandler handler : config.getHandlers())
 		{
 			handler.init(this);
 		}
 
 		// TODO: the fact that game needs to know about a specific state seems
 		// wrong but not sure how to fix right now
		gameState = new ProposeState(getCurrentLeader());
 		isStarted = true;
 	}
 
 	/**
 	 * @return true if the game has already started, false, otherwise
 	 */
 	public boolean isStarted()
 	{
 		return isStarted;
 	}
 
 	/**
 	 * progresses the game to the next round and calls the post round event
 	 * handlers.
 	 */
 	public void completeRound()
 	{
 		for (PostRoundEventHandler handler : config.getHandlers())
 		{
 			handler.roundFinished();
 		}
 
 		curRound++;
 		gotoNextLeader();
 	}
 
 	/**
 	 * @return true if the game is over, false, otherwise
 	 */
 	public boolean isOver()
 	{
 		return winners != Alignment.NEITHER;
 	}
 
 	/**
 	 * @return the winning side
 	 */
 	public Alignment getWinners()
 	{
 		return winners;
 	}
 
 	/**
 	 * set winning team
 	 * 
 	 * @param winners
 	 */
 	public void setWinners(Alignment winners)
 	{
 		this.winners = winners;
 	}
 
 	/**
 	 * @return extra info
 	 */
 	public ExtraInfoBag getExtraInfo()
 	{
 		return extraInfo;
 	}
 
 	/**
 	 * @return rounds
 	 */
 	public List<Round> getRounds()
 	{
 		return rounds;
 	}
 
 	/**
 	 * @return players
 	 */
 	public List<Player> getPlayers()
 	{
 		return players;
 	}
 
 	/**
 	 * get a player by their name
 	 * 
 	 * @param name
 	 * @return player
 	 */
 	public Player getPlayer(String name)
 	{
 		return players.get(players.indexOf(new Player(name)));
 	}
 
 	/**
 	 * @return number of players
 	 */
 	public int getNumPlayers()
 	{
 		return players.size();
 	}
 
 	/**
 	 * @return current round of play
 	 */
 	public Round getCurrentRound()
 	{
 		return rounds.get(curRound);
 	}
 
 	/**
 	 * @return round handlers
 	 */
 	public List<PostRoundEventHandler> getPostRoundEventHandlers()
 	{
 		return config.getHandlers();
 	}
 
 	/**
 	 * @return true if the game is ready to start, false, otherwise
 	 */
 	public boolean isReady()
 	{
 		return players.size() == config.getRoles().size();
 	}
 
 	/**
 	 * @return current mission leader
 	 */
 	public Player getCurrentLeader()
 	{
 		return players.get(curLeader);
 	}
 
 	public void setState(GameState<?> gameState)
 	{
 		this.gameState = gameState;
 	}
 
 	public GameState<?> getState()
 	{
 		return gameState;
 	}
 
 	public <T> T getState(Class<T> expectedState)
 	{
 		return expectedState.cast(gameState);
 	}
 
 	public Round getRound(int index)
 	{
 		return rounds.get(index);
 	}
 }
