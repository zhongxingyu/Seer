 package game.gameserver;
 
 import game.common.IGameClient;
 import game.common.IGameDescription;
 import game.common.IGameInstanceDescription;
 import game.common.IGameServer;
 import game.communication.action.ControlActionType;
 import game.communication.action.IAction;
 import game.communication.action.IControlAction;
 import game.communication.action.IGameAction;
 import game.communication.action.IGameCreationAction;
 import game.communication.action.IGameCtrlAction;
 import game.communication.action.InconsistentActionTypeException;
 import game.communication.action.control.AskServerStateCtrlAction;
 import game.communication.action.control.CreateGameCtrlAction;
 import game.communication.action.control.IControlActionHandler;
 import game.communication.event.InconsistentEventTypeException;
 import game.communication.event.control.GameCreationStartedCtrlEvent;
 import game.communication.event.control.ServerStateCtrlEvent;
 
 import java.net.InetSocketAddress;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentSkipListMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The object describing the local GameServer. It receives all Action sent by
  * local and distant client, and is responsible for filtering them and
  * forwarding them to the appropriate Game.
  * 
  * @author benobiwan
  * 
  */
 public final class LocalGameServer implements IGameServer,
 		IControlActionHandler
 {
 	/**
 	 * Logger object.
 	 */
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(LocalGameServer.class);
 
 	/**
 	 * Map off all current games.
 	 */
 	private final ConcurrentSkipListMap<Integer, IServerSideGame<?, ?, ?, ?>> _gameList = new ConcurrentSkipListMap<Integer, IServerSideGame<?, ?, ?, ?>>();
 
 	/**
 	 * Map off all game currently in creation.
 	 */
 	private final ConcurrentSkipListMap<Integer, IServerGameCreator<?, ?, ?, ?, ?>> _gameInCreationList = new ConcurrentSkipListMap<Integer, IServerGameCreator<?, ?, ?, ?, ?>>();
 
 	/**
 	 * Name of the local server, always equal to "local".
 	 */
 	private final String _strName = "local";
 
 	/**
 	 * Id which will be used for the next game.
 	 */
 	private int _iNextGameId = 1;
 
 	/**
 	 * Lock to protect the use of _iNextGameId.
 	 */
 	private final Object _lockNextGame = new Object();
 
 	/**
 	 * Server timer used to schedule timeout tasks.
 	 */
 	private final Timer _timeoutTimer = new Timer("Timeout timer", true);
 
 	/**
 	 * Executor service to execute the timeout tasks.
 	 */
 	private final ExecutorService _timeOutExecutor;
 
 	/**
 	 * List of {@link IGameDescription} available on the server.
 	 */
 	private final Set<IGameDescription> _gameListDescription;
 
 	/**
 	 * Creates a new LocalGameServer.
 	 * 
 	 * @param gameListDescription
 	 *            list of {@link IGameDescription} available on the server.
 	 */
 	public LocalGameServer(final Set<IGameDescription> gameListDescription)
 	{
 		_gameListDescription = gameListDescription;
 		_timeOutExecutor = Executors.newCachedThreadPool();
 	}
 
 	@Override
 	public void handleAction(final IGameClient client, final IAction act)
 			throws InconsistentActionTypeException
 	{
 		if (LOGGER.isDebugEnabled())
 		{
 			LOGGER.debug(act.getClass().toString());
 		}
 		if (act instanceof IControlAction)
 		{
 			handleControlAction(client, (IControlAction) act);
 		}
 		else if (act instanceof IGameCtrlAction)
 		{
 			final IGameCtrlAction action = (IGameCtrlAction) act;
 			final IServerGameCreator<?, ?, ?, ?, ?> gameCreator = _gameInCreationList
 					.get(Integer.valueOf(action.getGameId()));
 			final IServerSideGame<?, ?, ?, ?> game = _gameList.get(Integer
 					.valueOf(action.getGameId()));
 			// don't check for null player because of join game action
 			final IServerSidePlayer<?> player = client
 					.getServerSidePlayer(action.getPlayerId());
 			if (gameCreator == null && game == null)
 			{
 				// TODO error in handleAction
 			}
 			else if (gameCreator != null)
 			{
 				gameCreator.handleGameCtrlAction(client, player, action);
 			}
 			else
 			{
 				// TODO error in handleAction
 			}
 		}
 		else if (act instanceof IGameCreationAction)
 		{
 			final IGameCreationAction action = (IGameCreationAction) act;
 			final IServerGameCreator<?, ?, ?, ?, ?> gameCreator = _gameInCreationList
 					.get(Integer.valueOf(action.getGameId()));
 			final IServerSidePlayer<?> player = client
 					.getServerSidePlayer(action.getPlayerId());
 			if (gameCreator == null)
 			{
 				LOGGER.error("Received a message from Client '"
 						+ client.getName()
 						+ "' concerning a game in creation id '"
 						+ action.getGameId()
 						+ "' which doesn't exits or is no longer in creation.");
 			}
 			else if (player == null)
 			{
				LOGGER.error("Received a message from Client '"
						+ client.getName()
						+ "' concerning a game in creation id '"
						+ action.getGameId() + "' and the player id '"
						+ action.getPlayerId() + "' which doesn't exists.");
 			}
 			else
 			{
 				gameCreator.handleGameCreationAction(player, action);
 			}
 		}
 		else if (act instanceof IGameAction)
 		{
 			final IGameAction action = (IGameAction) act;
 			final IServerSideGame<?, ?, ?, ?> game = _gameList.get(Integer
 					.valueOf(action.getGameId()));
 			final IServerSidePlayer<?> player = client
 					.getServerSidePlayer(action.getPlayerId());
 			if (game == null)
 			{
 				LOGGER.error("Received a message from Client '"
 						+ client.getName() + "' concerning a game id '"
 						+ action.getGameId() + "' which doesn't exits.");
 			}
 			else if (player == null)
 			{
 				LOGGER.error("Received a message from Client '"
 						+ client.getName() + "' concerning player id '"
 						+ action.getPlayerId() + "' which isn't registered.");
 			}
 			else if (!game.isInThisGame(player))
 			{
 				LOGGER.error("Received a message from Client '"
 						+ client.getName() + "' concerning a game id '"
 						+ action.getGameId() + "' in which he isn't playing.");
 			}
 			else
 			{
 				game.handleGameAction(player, action);
 			}
 		}
 		else
 		{
 			LOGGER.error("Unknown action type.");
 		}
 	}
 
 	@Override
 	public void registerGameClient(final IGameClient client)
 	{
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Unregister a Game.
 	 * 
 	 * @param g
 	 *            the Game to remove.
 	 */
 	public void unregisterGame(final IServerSideGame<?, ?, ?, ?> g)
 	{
 		_gameList.remove(Integer.valueOf(g.getGameId()));
 	}
 
 	@Override
 	public int compareTo(final IGameServer o)
 	{
 		return _strName.compareTo(o.getName());
 	}
 
 	@Override
 	public int hashCode()
 	{
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((_strName == null) ? 0 : _strName.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj)
 	{
 		if (this == obj)
 		{
 			return true;
 		}
 		if (obj == null)
 		{
 			return false;
 		}
 		if (getClass() != obj.getClass())
 		{
 			return false;
 		}
 		final LocalGameServer other = (LocalGameServer) obj;
 		if (_strName == null)
 		{
 			if (other._strName != null)
 			{
 				return false;
 			}
 		}
 		else if (!_strName.equals(other._strName))
 		{
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String getName()
 	{
 		return _strName;
 	}
 
 	@Override
 	public String toString()
 	{
 		return _strName;
 	}
 
 	@Override
 	public InetSocketAddress getRemoteAddress()
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getClientName()
 	{
 		return "ADMIN";
 	}
 
 	@Override
 	public boolean isRegistered()
 	{
 		// local client is always registered on the local GameServer.
 		return true;
 	}
 
 	@Override
 	public void handleControlAction(final IGameClient client,
 			final IControlAction act) throws InconsistentActionTypeException
 	{
 		switch (act.getType())
 		{
 		case CREATE_GAME:
 			if (act instanceof CreateGameCtrlAction)
 			{
 				handleCreateGameCtrlAction(client, (CreateGameCtrlAction) act);
 			}
 			else
 			{
 				throw new InconsistentActionTypeException(
 						ControlActionType.CREATE_GAME, act.getClass());
 			}
 			break;
 		case ASK_SERVER_STATE:
 			if (act instanceof AskServerStateCtrlAction)
 			{
 				handleAskServerStateCtrlActionAction(client,
 						(AskServerStateCtrlAction) act);
 			}
 			else
 			{
 				throw new InconsistentActionTypeException(
 						ControlActionType.ASK_SERVER_STATE, act.getClass());
 			}
 			break;
 		}
 	}
 
 	@Override
 	public void handleCreateGameCtrlAction(final IGameClient client,
 			final CreateGameCtrlAction act)
 	{
 		// TODO check whether the user has the right to create a game.
 		// TODO check if max game has been reached.
 		synchronized (_lockNextGame)
 		{
 			final IServerGameCreator<?, ?, ?, ?, ?> gameCreator = act
 					.getGameCreator();
 			gameCreator.initialize(this, _iNextGameId, client,
 					act.getCreatorPlayerId());
 			_gameInCreationList.put(Integer.valueOf(gameCreator.getGameId()),
 					gameCreator);
 			final GameCreationStartedCtrlEvent event = new GameCreationStartedCtrlEvent(
 					gameCreator.getGameId(), act.getCreatorPlayerId(),
 					gameCreator.getClientGameCreator());
 			_iNextGameId++;
 			try
 			{
 				client.handleEvent(this, event);
 			}
 			catch (final InconsistentEventTypeException e)
 			{
 				LOGGER.error(e.getLocalizedMessage(), e);
 			}
 		}
 	}
 
 	@Override
 	public void handleAskServerStateCtrlActionAction(final IGameClient client,
 			final AskServerStateCtrlAction act)
 	{
 		// listing the game as asked
 		final TreeSet<IGameInstanceDescription> setDescription = new TreeSet<IGameInstanceDescription>();
 		if (act.isOnlyJoinableGames())
 		{
 			for (final IServerSideGame<?, ?, ?, ?> game : _gameList.values())
 			{
 				if (game.isJoinable())
 				{
 					setDescription.add(game.getDescription());
 				}
 			}
 		}
 		else
 		{
 			for (final IServerSideGame<?, ?, ?, ?> game : _gameList.values())
 			{
 				setDescription.add(game.getDescription());
 			}
 		}
 		// adding games in creation
 		for (final IServerGameCreator<?, ?, ?, ?, ?> game : _gameInCreationList
 				.values())
 		{
 			setDescription.add(game.getDescription());
 		}
 
 		final ServerStateCtrlEvent evt = new ServerStateCtrlEvent(
 				getServerState(), setDescription);
 		try
 		{
 			client.handleEvent(this, evt);
 		}
 		catch (final InconsistentEventTypeException e)
 		{
 			LOGGER.error(e.getLocalizedMessage(), e);
 		}
 	}
 
 	/**
 	 * Schedule a {@link TimeoutTask} for execution.
 	 * 
 	 * @param iDelay
 	 *            the delay before the execution of the task.
 	 * @param game
 	 *            the game concerned by the task.
 	 * @param player
 	 *            the player concerned by the task.
 	 * @return the newly created and scheduled task.
 	 */
 	public TimerTask scheduleTask(final int iDelay,
 			final IServerSideGame<?, ?, ?, ?> game,
 			final IServerSidePlayer<?> player)
 	{
 		final TimerTask task = new TimeoutTask(_timeOutExecutor, iDelay, game,
 				player);
 		_timeoutTimer.schedule(task, iDelay);
 		return task;
 	}
 
 	@Override
 	public boolean isConnected()
 	{
 		// we are always connected to the local game server.
 		return true;
 	}
 
 	@Override
 	public boolean isConnecting()
 	{
 		// we are never connecting to the local game server.
 		return false;
 	}
 
 	@Override
 	public void updateServerState(final IServerState serverState)
 	{
 		// nothing to do.
 	}
 
 	@Override
 	public IServerState getServerState()
 	{
 		// TODO a revoir
 		synchronized (_lockNextGame)
 		{
 			return new ServerStateImpl(0, 0, _gameList.size()
 					+ _gameInCreationList.size(), 0);
 		}
 	}
 
 	@Override
 	public boolean isGameCreationAllowed()
 	{
 		// the local client has always the right to create a game on the local
 		// server.
 		return true;
 	}
 
 	@Override
 	public Set<IGameDescription> getAvailableGames()
 	{
 		return _gameListDescription;
 	}
 }
