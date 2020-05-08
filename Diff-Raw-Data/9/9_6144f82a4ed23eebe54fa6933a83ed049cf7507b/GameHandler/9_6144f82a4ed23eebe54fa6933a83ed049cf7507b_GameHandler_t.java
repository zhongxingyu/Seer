 package server;
 
 import static org.webbitserver.WebServers.createWebServer;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import org.webbitserver.WebServer;
 import org.webbitserver.handler.StaticFileHandler;
 
 import client.ClientDirUtil;
 
 public class GameHandler implements Runnable {
 
 	final int GAME_RATE = 30;
 	final int SYNC_RATE = 30;
	final static int WEB_SERVER_PORT = 9877;
 	final static boolean USE_EXTERNAL_WEB_ROOT = true;
 	final static String EXTERNAL_WEB_ROOT = "/var/www/Ape_On_Tape/";
 
 	private final String DEFAULT_ROOMNAME = "soup";
 
 	private GameServer gameServer;
 	private WebServer webServer;
 	private Map<String, Game> games;
 	private Map<Integer, List<Integer>> keysPressed;
 
 	private Map<Integer, String> playerNames;
 	private Map<Integer, String> playerRooms;
 
 	public GameHandler(int port, File webRoot) throws InterruptedException,
 			ExecutionException {
 		gameServer = new GameServer(this);
 		webServer = createWebServer(port)
 		/*
 		 * .add(new LoggingHandler( new SimpleLogSink(Chatroom.USERNAME_KEY)))
 		 */
 		.add("/chatsocket", gameServer).add(new StaticFileHandler(webRoot))
 				.start().get();
 
 		this.games = new HashMap<String, Game>();
 		games.put(DEFAULT_ROOMNAME, new Game(800, 400));
 		this.keysPressed = new HashMap<Integer, List<Integer>>();
 
 		System.out.println("Game Server running on: " + webServer.getUri());
 
 		this.playerNames = new HashMap<Integer, String>();
 		this.playerRooms = new HashMap<Integer, String>();
 	}
 
 	@Override
 	public void run() {
 		Date gameUpdate = new Date();
 		Date syncUpdate = new Date();
 		while (true) {
 			if (new Date().getTime() - gameUpdate.getTime() > 1000 / GAME_RATE) {
 				gameLoop();
 				gameUpdate = new Date();
 			}
 			if (new Date().getTime() - syncUpdate.getTime() > 1000 / SYNC_RATE) {
 				syncLoop();
 				syncUpdate = new Date();
 			}
 			try {
 				// sleep a little, in order for the process not to take 100%
 				// cpu.
 				Thread.sleep(100 / Math.max(GAME_RATE, SYNC_RATE));
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public static void main(String[] args) throws InterruptedException,
 			ExecutionException {
 		int port = WEB_SERVER_PORT;
 		File webRoot;
 		if (args.length > 0)
 			port = Integer.parseInt(args[0]);
 		if (args.length > 1)
 			webRoot = new File(args[1]);
 		else
 			webRoot = ClientDirUtil.getClientDirectory();
 
 		GameHandler gameHandler = new GameHandler(port, webRoot);
 		Thread gameThread = new Thread(gameHandler);
 		gameThread.run();
 	}
 
 	public void joinRoom(int id, String roomName) {
 		String user = playerNames.get(id);
 		playerRooms.put(id, roomName);
 		this.joinPlayer(id, roomName);
 		gameServer.sendJoinMessage(id, user, roomName, playersInRoomWith(id));
 		gameServer.sendNewRoomInfo(roomName, asList(id));
 	}
 
 	private void joinPlayer(int playerId, String roomName) {
 		if (!games.containsKey(roomName))
 			createRoom(roomName);
 
 		this.games.get(roomName).addPlayer(playerId, playerNames.get(playerId));
 	}
 
 	private void createRoom(String roomName) {
 		Game newRoom = new Game(800, 400);
 		synchronized (this.games) {
 			this.games.put(roomName, newRoom);
 		}
 		newRoom.addCollisionListener(new RealCollisionListener(this));
 		roomListUpdated();
 		createBot(newRoom);
 	}
 
 	private void createBot(Game room) {
 		room.addBot(IdFactory.getNextId(), "uncleverbot");
 	}
 
 	public void leavePlayer(int playerId) {
 		this.leaveCurrentRoom(playerId);
 		this.keysPressed.remove(playerId);
 	}
 
 	private void destroyRoom(String roomName) {
 		this.games.remove(roomName);
 		roomListUpdated();
 	}
 
 	private void gameLoop() {
 		for (Game game : games.values()) {
 			for (int id : new LinkedList<Integer>(keysPressed.keySet())) {
 				List<Integer> keys = keysPressed.get(id);
 				if (game.hasPlayerWithId(id))
 					game.setPlayerKeys(id, keys);
 				game.update();
 			}
 		}
 	}
 
 	private void syncLoop() {
 		synchronized (this.games) {
 			for (Game game : games.values()) {
 				this.gameServer.update(game.getEntitiesAndPlayersMap());
 			}
 		}
 	}
 
 	public void setKeysPressed(int id, List<Integer> keysPressed) {
 		this.keysPressed.put(id, keysPressed);
 	}
 
 	public int[][] getGameMap(String roomName) {
 		return Util.getArrayFromMap(games.get(roomName).getMap());
 	}
 
 	public void playerDisconnected(int id) {
 		gameServer.sendDisconnectMessage(id, playerNames.get(id),
 				playersInRoomWith(id));
 		this.leavePlayer(id);
 		gameServer.disconnect(id);
 		playerRooms.remove(id);
 	}
 
 	public void playerLogin(int id, String username) {
 		playerNames.put(id, username);
 		gameServer.sendRoomList(allRooms(), asList(id));
 	}
 
 	private List<Integer> asList(int id) {
 		List<Integer> list = new ArrayList<Integer>(1);
 		list.add(id);
 		return list;
 	}
 
 	private void roomListUpdated() {
 		gameServer.sendRoomList(allRooms(), this.allPlayers());
 	}
 
 	public void leaveCurrentRoom(int id) {
 		// check if player is in some room
 		if (playerRooms.containsKey(id)) {
 			gameServer.sendDisconnectMessage(id, playerNames.get(id),
 					playersInRoomWith(id));
 			String roomName = playerRooms.get(id);
 			Game room = games.get(roomName);
 			room.removePlayer(id);
 			playerRooms.remove(id);
 			if (room.noPlayers())
 				destroyRoom(roomName);
 		}
 	}
 
 	public void collision(Game game, Entity e) {
 		gameServer.sendCollisionAlert(game.getPlayers().keySet());
 	}
 
 	private Collection<String> allRooms() {
 		Set<String> result = new HashSet<String>();
 		result.addAll(this.playerRooms.values());
 		return result;
 	}
 
 	private Collection<Integer> allPlayers() {
 		Collection<Integer> result = this.playerNames.keySet();
 		return result;
 	}
 
 	private List<Integer> playersInRoomWith(int id) {
 		return new LinkedList<Integer>(games.get(playerRooms.get(id))
 				.getPlayers().keySet());
 	}
 
 	public List<Integer> idsFromPlayers(List<Player> players) {
 		List<Integer> ids = new LinkedList<Integer>();
 		for (Player p : players)
 			ids.add(p.id);
 		return ids;
 	}
 }
