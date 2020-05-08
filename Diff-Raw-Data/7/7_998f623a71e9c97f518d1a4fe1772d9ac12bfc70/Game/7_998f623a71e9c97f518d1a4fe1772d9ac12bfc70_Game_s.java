 package battlechallenge.server;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import battlechallenge.ActionResult;
 import battlechallenge.ActionResult.ShotResult;
 import battlechallenge.CommunicationConstants;
 import battlechallenge.Coordinate;
 import battlechallenge.ShipAction;
 import battlechallenge.maps.BattleMap;
 import battlechallenge.maps.MapImporter;
 import battlechallenge.ship.Ship;
 import battlechallenge.ship.ShipCollection;
 import battlechallenge.structures.City;
 import battlechallenge.structures.Structure;
 import battlechallenge.visual.BCViz;
 
 /**
  * The Class Game.
  */
 public class Game extends Thread {
 	
 	public static final boolean DEBUG = true;
 	
 	public static final int DEFAULT_WIDTH;
 	public static final int DEFAULT_HEIGHT;
 	public static final int DEFAULT_SPEED;
 	
 	static {
 		DEFAULT_WIDTH = 15;
 		DEFAULT_HEIGHT = 15;
 		DEFAULT_SPEED = 750; // number of milliseconds to sleep between turns
 	}
 	
 	/** The players. */
 	private Map<Integer, ServerPlayer> players;
 	
 	private GameManager manager;
 	
 	private BCViz viz;
 	
 	private BattleMap map;
 	
 	private ShipCollection ships;
 	
 	private int minsPerShip = 10; // TODO: Make variable per game
 	
 	/**
 	 * Used to check how many players are in the game
 	 * @return The number of players in the game
 	 */
 	public int numberOfPlayers() {
 		return players.size();
 	}
 	
 	/**
 	 * Instantiates a new game.
 	 *  
 	 * @param players the player
 	 */
 	public Game(List<ServerPlayer> players, GameManager manager) {
 		this(players, DEFAULT_WIDTH, DEFAULT_HEIGHT, manager);
 	}
 	
 	/**
 	 * Constructor for a game that requires at least two players
 	 * @param players The players that make up the game
 	 * @param height The height of the game board
 	 * @param width  The width of the game board
 	 * @param manager The GameManager that creates the game
 	 */
 	public Game(List<ServerPlayer> players, int height, int width, GameManager manager) {
 		if (players == null || players.size() < 2)
 			throw new IllegalArgumentException();
 		// TODO: ensure no null entries in list
 		this.players = new HashMap<Integer, ServerPlayer>();
 		for (ServerPlayer p : players)
 			this.players.put(p.getId(), p);
 		this.manager = manager;
 		ships = new ShipCollection();
 		for (ServerPlayer p : players) {
 			ships.addPlayer(p);
 			p.setShipCollection(ships);
 		}
 		
 		// DEBUG INFO
 		StringBuilder sb = new StringBuilder("Starting new Game with players { ");
 		for(ServerPlayer p : players)
 			sb.append(p.getId()).append(", ");
 		sb.delete(sb.length()-2, sb.length()).append(" }");
 		System.out.println(sb.toString());
 		
 		this.start();
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Thread#run()
 	 */
 	public void run() {
 		// TODO: Start playing game
 		// FIXME: Do not allow other players to be added.
 		/*
 		 * 			[[ INITIALIZE GAME ]]
 		 */
 		map = MapImporter.getMap(); // get default map
 		setPlayerCredentials();
 		map.assignPlayersToBases(players.values(), getInitialShips()); // set bases
 		setGameConstants();
 		viz = new BCViz(players.values(), map.getStructures(), map.getNumCols(), map.getNumRows());
 		
 		/*
 		 * 			[[ PLAY GAME ]]
 		 */
 		// TODO: set structures to players and vice-versa
 		Map<Integer, List<ActionResult>> actionResults = new HashMap<Integer, List<ActionResult>>();
 		for(ServerPlayer p : players.values())
 			actionResults.put(p.getId(), new LinkedList<ActionResult>());
 		int livePlayers = 0;
 		for (ServerPlayer player: players.values()) {
 			if (player.isAlive()) {
 				livePlayers++;
 			}
 		}
 		while (livePlayers > 1) {
 			doTurn(actionResults);
 			viz.updateGraphics();
 			livePlayers = 0;
 			for (ServerPlayer player: players.values()) {
 				if (player.isAlive()) {
 					livePlayers++;
 				}
 			}
 		}
 		
 		
 		/*
 		 * 			[[ END GAME ]]
 		 */
 		ServerPlayer winner = getWinner();
 		if (winner != null)
 			System.out.println("Winner is " + winner.getName());
 		else
 			System.out.println("Error: Winner not found.");
 		
 		for(ServerPlayer p : players.values()) {
 			if (winner == p)
 				p.endGame(CommunicationConstants.RESULT_WIN);
 			else {
 				p.endGame(CommunicationConstants.RESULT_LOSE);
 			}
 		}
 		manager.removeGame(this);
 	}
 	
 	private void setPlayerCredentials() {
 		for(ServerPlayer p : players.values()) {
 			if (p.setCredentials(map.getNumCols(), map.getNumRows()))
 				System.out.println("Set player credentials: " + p.toString());
 			else {
 				// TODO: remove player and pause game if necessary
 			}
 		}
 	}
 	
 	public void setGameConstants() {
 		for(ServerPlayer p : players.values()) {
 			p.setMinsPerShip(minsPerShip);
 		}
 	}
 	
 	
 	/**
 	 * Will sink all ships that are overlapping after moving
 	 */
 	public void handleCollisions() {
 		Map<Coordinate, Ship> allShipCoords = new HashMap<Coordinate, Ship>(); // Stores coords of all players ships
 		HashSet<Ship> shipsToSink = new HashSet<Ship>();
 		for(ServerPlayer p : players.values()) {
 			for (Ship s: p.getUnsunkenShips(p)) {
 					Coordinate coord = s.getLocation();
 					if (allShipCoords.get(coord) != null) {
 						shipsToSink.add(allShipCoords.get(coord)); // sink ship that had coordinates in allShipCoords
 						shipsToSink.add(s); // sink ship colliding with another ship
 					}
 					allShipCoords.put(coord, s);
 				}
 			}
 		for (Ship s: shipsToSink) {
 			s.setHealth(0); // Sink ship as it has collided with another ship
 		}
 	}
 
 	
 	/**
 	 * 
 	 * @param actionResults The results of last turns actions
 	 */
 	private void doTurn(Map<Integer, List<ActionResult>> actionResults) {
 		if (DEBUG) {
 
 
 			// DEGBUG INFO: print each users guess
 			/*
 			StringBuilder sb = new StringBuilder();
 			for (Entry<Integer, List<ActionResult>> e : actionResults.entrySet()) {
 				sb.append("\t{ ").append(e.getKey()).append(" ");
 				for (ActionResult a : e.getValue())
 					sb.append(a.getResult().toString()).append(" ");
 				sb.append(" }\n");
 			}
 			System.out.println(sb.toString());
 			*/
 
 
 		}
 		// hash map of ships to  reveal entire map
 		// TODO: alter for fog of war
 		Map<Integer, List<Ship>> allPlayersShips = new HashMap<Integer, List<Ship>>();
 		for (ServerPlayer p : players.values()) {
 			allPlayersShips.put(p.getId(), p.getUnsunkenShips(p)); // List of all players ships to send to each player	
 		}
 		for(ServerPlayer p : players.values()) {
 			if (!p.isAlive()) // Player is dead, don't request their turn
 				continue;
 			if (!p.requestTurn(allPlayersShips, actionResults, map)) {
 				// TODO: handle lost socket connection
 			}
 		}
 		try {
 			// FIXME: Change speed depending on how fast players return results from calls
 			// to their doTurn method. Or game specific value (slower time, harder game)
 			Thread.sleep(DEFAULT_SPEED);
 		} catch (InterruptedException e) {
 			// TODO: handle thread failure
 		}
 		// Get all player actions and save them into a hash map by network id
 		// This is where all the ships are moved.
 		Map<Integer, List<ShipAction>> playerActions = new HashMap<Integer, List<ShipAction>>();
 		for(ServerPlayer p : players.values()) {
 			if (!p.isAlive()) // Player is dead no need to get actions when they cannot act
 				continue;
 			// shot coordinates and moves ships
 			List<ShipAction> shipActions = p.getTurn(map.getNumCols(), map.getNumRows());
 			// reset action results for current user
 			actionResults.get(p.getId()).clear();
 			playerActions.put(p.getId(), shipActions);
 		}
 		handleCollisions(); // if ships are overlapping after moving
 		// Now that all ships are moved, evaluate shots for each player
		for(int j=0;j<players.size();j++) {
			ServerPlayer p = players.get(j);
 			if (!p.isAlive()) // Player is dead no need to get actions when they cannot act
 				continue;
 			for(ShipAction sa : playerActions.get(p.getId())) {
 				// NOTE: moves are processed in: ServerPlayer.getTurn(...);
 				// check if shot is within game boundaries
 				Ship s = ships.getShip(sa.getShipIdentifier());
 				if (s == null)
 					continue;
 				for (int k=0;k<s.getNumShots(); k++) {
 					Coordinate c = sa.getShotCoordList().get(k);
 					if (c == null || (s.distanceFromCoord(c) > s.getRange()) || 
 							!c.inBoundsInclusive(0, map.getNumRows()-1, 0, map.getNumCols()-1) || 
 							s.isSunken()) {
 						// ignore shot out of bounds or invalid shot range
 						continue;
 					} else {
 						ActionResult hit = null;
 						ActionResult temp = null;
 						// valid shot location received
 						for(ServerPlayer opp : players.values()) {
 							// add all action results
 							// beware of friendly fire				
 							temp = (opp.isHit(c, s, s.getDamage()));
 							if (temp.getResult() == ShotResult.HIT)
 								hit = temp;
 							else if (temp.getResult() == ShotResult.SUNK) {
 								hit = temp;
 								if (opp != p) {
 									p.incrementScore();
 								}
 							}
 						}
 						actionResults.get(p.getId()).add(hit == null ? temp : hit);
 					}
 				}
 			}
 		}
 		updateCities();
 		allocateIncome();
 		spawnShips();
 		ships.removeSunkenShips();
 		ships.updateCoordinates();
 	}
 	
 	/**
 	 * Adds the player.
 	 *
 	 * @param player the player to add to the game
 	 * @return true, if player is successfully added to the game
 	 */
 	public boolean addPlayer(ServerPlayer player) {
 		// FIXME: handle null players
 		// TODO: how to add players
 		return false;
 	}
 	
 	/**
 	 * Gets the winner by checking each player to see if they
 	 * have any ships left
 	 *
 	 * @return the winner who is the only player with at least one ship left unsunk.
 	 */
 	public ServerPlayer getWinner() {
 		// TODO: get player with max score
 		for (ServerPlayer player: players.values()) {
 			if (player.isAlive()) {
 				return player;
 			}
 		}
 		return null;
 	}
 	
 	public List<City> updateStructures() {
 		return null;
 	}
 	
 	public void allocateIncome() {
 		for (Structure str: map.getStructures()) {
 			// there are no bases in map.getStructures
 			if (str instanceof City) {
 				City city = (City)str;
 				int ownerId = city.getOwnerId();
 				if (ownerId >= 0) { // all non-neutral cities
 					ServerPlayer p = players.get(ownerId);
 					 // increment players minerals by the amount the city generates
 					p.incrementMinerals(city.getMineralGenerationSpeed());
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Updates cities owners to players that have ships in the cities
 	 */
 	public void updateCities() {
 		Map<Coordinate, Ship> allShipCoords = new HashMap<Coordinate, Ship>(); // Stores coords of all players ships
 		Ship ship;
		for(int j=0;j<players.size();j++) {
			ServerPlayer p = players.get(j);
 			for (Ship s: p.getUnsunkenShips(p)) {
 					Coordinate coord = s.getLocation();
 					allShipCoords.put(coord, s);
 				}
 		}
 		for (Structure str: map.getStructures()) {
 			// there are no bases in map.getStructures
 			if (str instanceof City) {
 				City city = (City)str;
 				ship = allShipCoords.get(city.getLocation());
 				if (ship != null) // There is a ship on the city
 					city.setOwner(ship.getPlayerId());
 				else {
 					city.setOwner(-1); // Neutral City
 				}
 			}
 		}
 		
 	}
 	
 	public void spawnShips() {
 		for (ServerPlayer player: players.values()) {
 			player.spawnShip();
 		}
 	}
 	
 	public List<Ship> getInitialShips() {
 		List<Ship> ships = new LinkedList<Ship>();
 		ships.add(new Ship());
 		return ships;
 	}
 }
