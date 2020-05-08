 package server.model;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import server.listeners.CollisionListener;
 import server.listeners.PlayerMoveListener;
 import server.model.map.MapInfo;
 import server.model.map.PositionType;
 import server.model.map.TileMap;
 
 public class Game {
 
 	// store players separatly.
 	private volatile Map<Integer, Player> players;
 	private volatile Map<Integer, Entity> entities;
 	TileMap map;
 	int width, height; // TODO: unused? (also in constructor)
 	private Collection<CollisionListener> collisionListeners;
 	private Set<String> soundEvents;
 	/**
 	 * True if game has already started, false if waiting for
 	 * <code>start()</code> signal
 	 */
 	private boolean running;
 
 	public Game(int width, int height) {
 		this.players = new HashMap<Integer, Player>();
 		this.entities = new HashMap<Integer, Entity>();
 		this.collisionListeners = new LinkedList<CollisionListener>();
 		this.width = width;
 		this.height = height;
 		this.soundEvents = new HashSet<String>();
 		// TODO: replace map path (make dynamic choice)
		String mapPath = "src/client/maps/map.json"
				.replace("/", File.separator);
 		MapInfo mapInfo = MapInfo.fromJSON(mapPath);
 		this.map = new TileMap(mapInfo);
 		this.initEntities(mapInfo);
 	}
 
 	private void initEntities(MapInfo mapInfo) {
 		for (Entity e : mapInfo.createEntities(this.getMap()))
 			this.addEntity(e);
 	}
 
 	/**
 	 * Launch this game
 	 */
 	public synchronized void start() {
 		this.running = true;
 		for (Entity e : entities.values()) {
 			if (e instanceof Barrier)
 				((Barrier) e).open();
 		}
 	}
 
 	public void addPlayer(int playerId, String playerName) {
 		float[] start = map.getFirstTileXY(PositionType.PlayerStart);
 		start[0] += this.players.size();
 		Player player = new Player(playerId, start[0], start[1], playerName);
 		player.setId(playerId);
 		player.addMoveListener(new PlayerMoveListener(this, map));
 		this.players.put(player.getId(), player);
 	}
 
 	public void removePlayer(int playerId) {
 		this.players.remove(playerId);
 	}
 
 	public Map<Integer, Player> getPlayers() {
 		return this.players;
 	}
 
 	public List<Player> getPlayersList() {
 		return new LinkedList<Player>(this.getPlayers().values());
 	}
 
 	public void setPlayerKeys(int playerId, List<Integer> keys) {
 		this.players.get(playerId).setKeysPressed(keys);
 	}
 
 	public Map<Integer, Player> getPlayersAsMap() {
 		return this.players;
 	}
 
 	public TileMap getMap() {
 		return map;
 	}
 
 	public void update() {
 		for (Entity entity : this.getAllEntites())
 			entity.brain(this);
 	}
 
 	public boolean hasPlayerWithId(int id) {
 		return this.players.containsKey(id);
 	}
 
 	/**
 	 * Returns true if this game (room) is empty.
 	 */
 	public boolean noPlayers() {
 		return this.players.isEmpty();
 	}
 
 	public void addCollisionListener(CollisionListener listener) {
 		this.collisionListeners.add(listener);
 	}
 
 	public void collision(Entity e) {
 		if (e.collisionState())
 			return;
 
 		// TODO: save (and read) collision state dependent on side of collision
 		e.setCollisionState(true);
 		for (CollisionListener listener : collisionListeners)
 			listener.collisionOccured(this, e);
 		// this.soundEvents.add("wall-collision");
 	}
 
 	public void addEntity(Entity e) {
 		this.entities.put(e.getId(), e);
 	}
 
 	public void removeEntity(Entity e) {
 		this.entities.remove(e.getId());
 	}
 
 	/**
 	 * 
 	 * @return all entities of this game INCLUDING the players
 	 */
 	public List<Entity> getAllEntites() {
 		List<Entity> list = new LinkedList<Entity>(this.entities.values());
 		list.addAll(this.getPlayersList());
 		return list;
 	}
 
 	public List<Entity> getEntities() {
 		return new LinkedList<Entity>(this.entities.values());
 	}
 
 	public void noCollision(Entity e) {
 		e.setCollisionState(false);
 	}
 
 	public Map<Integer, Entity> getAllEntitiesMap() {
 		Map<Integer, Entity> e = new HashMap<Integer, Entity>(this.players);
 		e.putAll(this.entities);
 		return e;
 	}
 
 	public GameEvent[] popEvents() {
 		Collection<GameEvent> result = new ArrayList<GameEvent>();
 		for (String soundEvent : this.soundEvents) {
 			GameEvent event = new GameEvent(GameEvent.Type.SOUND);
 			event.content = soundEvent;
 			result.add(event);
 		}
 		this.soundEvents.clear();
 		return result.toArray(new GameEvent[0]);
 	}
 
 	public void playerHit(Player player) {
 		this.soundEvents.add("kill");
 	}
 
 	public boolean isRunning() {
 		return running;
 	}
 
 	public void playerFinished(Player p) {
 		p.win();
 		this.soundEvents.add("win");
 	}
 
 }
