 package battlechallenge.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import battlechallenge.ActionResult;
 import battlechallenge.ActionResult.ShotResult;
 import battlechallenge.CommunicationConstants;
 import battlechallenge.Coordinate;
 import battlechallenge.ShipAction;
 import battlechallenge.ship.Ship;
 import battlechallenge.structures.Base;
 import battlechallenge.structures.City;
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
 	
 	/** The board width. */
 	private int boardWidth;
 	
 	/** The board height. */
 	private int boardHeight;
 	
 	/** The players. */
 	private List<ServerPlayer> players;
 	
 	private GameManager manager;
 	
 	private BCViz viz;
 	
 	private File gameMap = new File("C:/Users/Califax/workspace/battle-challenge/Maps/Map_01");
 	
 	private List<City> structures = new ArrayList<City>();
 	
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
 		this.players = players;
 		this.boardHeight = height;
 		this.boardWidth = width;
 		this.manager = manager;
 		
 		// DEBUG INFO
 		StringBuilder sb = new StringBuilder("Starting new Game with players { ");
 		for(ServerPlayer p : players)
 			sb.append(p.getId()).append(", ");
 		sb.delete(sb.length()-2, sb.length()).append(" }");
 		System.out.println(sb.toString());
 		
 		this.start();
 	}
 	
 	public void importMap(File gameMap) {
 		String gameMapName = gameMap.toString();
 		int playerNum = 0; // Start at 0 increase every time map finds a base
 		int row = 0;
 		try {
 			Scanner scanner = new Scanner(new FileReader(gameMapName));
 			while (scanner.hasNextLine()){
 		      playerNum = processChar(scanner.nextLine(), playerNum, row); // will have increased if a base is assigned to a player
 		      row++;
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public int processChar(String input, int playerNum, int row) {
 		
 		Base base;
 		for (int i = 0; i < input.length(); i++) { // Go through the column in the map
 			if (input.charAt(i) == ('C')) {
 				structures.add(new City(new Coordinate(row, i)));
 			}
 			if (input.charAt(i) == ('B') && playerNum < players.size()) {
 			    base = new Base(playerNum, new Coordinate(row, i));
 				structures.add(base); 
 				players.get(playerNum).setBase(base); // Tell server player where its base is
 				playerNum++;
 			}
 		}
 		return playerNum;
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
 		setPlayerCredentials();
 		
 		/*
 		 * 			[[ PLAY GAME ]]
 		 */
 		Map<Integer, List<ActionResult>> actionResults = new HashMap<Integer, List<ActionResult>>();
 		importMap(gameMap);
 		structures = getStructures();
 		placeOriginalShips();
 		// TODO: set structures to players and vice-versa
 		for(ServerPlayer p : players)
 			actionResults.put(p.getId(), new LinkedList<ActionResult>());
 		int livePlayers = 0;
 		for (ServerPlayer player: players) {
 			if (player.isAlive()) {
 				livePlayers++;
 			}
 		}
 		while (livePlayers > 1) {
 			doTurn(actionResults, structures);
 //			viz.updateGraphics();
 			livePlayers = 0;
 			for (ServerPlayer player: players) {
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
 		
 		for(ServerPlayer p : players) {
 			if (winner == p)
 				p.endGame(CommunicationConstants.RESULT_WIN);
 			else {
 				p.endGame(CommunicationConstants.RESULT_LOSE);
 			}
 		}
 		manager.removeGame(this);
 	}
 	
 	private void setPlayerCredentials() {
 		for(ServerPlayer p : players) {
 			if (p.setCredentials(boardWidth, boardHeight))
 				System.out.println("Set player credentials: " + p.toString());
 			else {
 				// TODO: remove player and pause game if necessary
 			}
 		}
 	}
 	
 	/**
 	 * Will sink all ships that are overlapping after moving
 	 */
 	public void handleCollisions() {
 		Map<Coordinate, Ship> allShipCoords = new HashMap<Coordinate, Ship>(); // Stores coords of all players ships
 		HashSet<Ship> shipsToSink = new HashSet<Ship>();
 		for(int j=0;j<players.size();j++) {
 			ServerPlayer p = players.get(j);
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
 	private void doTurn(Map<Integer, List<ActionResult>> actionResults, List<City> structures) {
 		if (DEBUG) {
 			// DEGBUG INFO: print each users guess
 			StringBuilder sb = new StringBuilder();
 			for (Entry<Integer, List<ActionResult>> e : actionResults.entrySet()) {
 				sb.append("\t{ ").append(e.getKey()).append(" ");
 				for (ActionResult a : e.getValue())
 					sb.append(a.getResult().toString()).append(" ");
 				sb.append(" }\n");
 			}
 			System.out.println(sb.toString());
 		}
 		// hash map of ships to  reveal entire map
 		// TODO: alter for fog of war
 		Map<Integer, List<Ship>> allPlayersShips = new HashMap<Integer, List<Ship>>();
 		for (ServerPlayer p : players) {
 			allPlayersShips.put(p.getId(), p.getUnsunkenShips(p)); // List of all players ships to send to each player	
 		}
 		for(ServerPlayer p : players) {
 			if (!p.isAlive()) // Player is dead, don't request their turn
 				continue;
 			if (!p.requestTurn(allPlayersShips, actionResults, structures)) {
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
 		for(int j=0;j<players.size();j++) {
 			ServerPlayer p = players.get(j);
 			if (!p.isAlive()) // Player is dead no need to get actions when they cannot act
 				continue;
 			// shot coordinates and moves ships
 			List<ShipAction> shipActions = p.getTurn(boardWidth, boardHeight);
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
 				for (int k=0;k<p.getShip(sa.getShipIdentifier()).getNumShots(); k++) {
 					Coordinate c = sa.getShotCoordList().get(k);
 					Ship s = p.getShip(sa.getShipIdentifier());
 					if ((s.distanceFromCenter(c) > s.getRange()) || 
 							!c.inBoundsInclusive(0, boardHeight-1, 0, boardWidth-1) || 
 							s.isSunken()) {
 						// ignore shot out of bounds or invalid shot range
 						continue;
 					} else {
 						ActionResult hit = null;
 						ActionResult temp = null;
 						// valid shot location received
 						for(ServerPlayer opp : players) {
 							// add all action results
 							// beware of friendly fire				
 							temp = (opp.isHit(c, s.getDamage()));
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
 		for (ServerPlayer player: players) {
 			if (player.isAlive()) {
 				return player;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Creates the default ships for this game.
 	 *
 	 * @return the ships
 	 */
 //	public static List<Ship> getShips() {
 //		List<Ship> ships = new ArrayList<Ship>();
 //		ships.add(new Ship());
 //		// Setting the original ship Ids
 //		for (int i = 0; i < ships.size(); i++) {
 //			ships.get(i).setShipId(i);
 //		}
 //		return ships;
 //	}
 	
 	/**
 	 * Places the original ships for each player
 	 */
 	public void placeOriginalShips() {
 		List<Ship> ships = new ArrayList<Ship>();
 		for (City base: structures) {
 			if (base instanceof Base) {
 				Ship ship = new Ship(base.getLocation());
 				ship.setPlayerId(base.getOwnerId());
 				ships.add(ship);
 				players.get(base.getOwnerId()).placeShip(ship);
 			}
 		}
 	}
 	
 	/**
 	 * Creates a base for each player then the default cities per number of players.
 	 *
 	 * @return the list of structures
 	 */
 	public List<City> getStructures() {
 		return structures;
 	}
 	
 	public List<City> updateStructures() {
 		return null;
 	}
 	
 	public void allocateIncome() {
 		for (City city: structures) {
 			int ownerId = city.getOwnerId();
 			if (ownerId == -1) { // neutral city
 				continue;
 			}
 			ServerPlayer p = players.get(ownerId);
 			 // increment players minerals by the amount the city generates
 			p.incrementMinerals(city.getMineralGenerationSpeed());
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
 		for (City city: structures) {
 			if (city instanceof Base) {
 				continue;
 			}
 			ship = allShipCoords.get(city.getLocation());
 			if (ship != null) // There is a ship on the city
				city.setOwner(ship.getPlayerId());
			else {
				city.setOwner(-1); // Neutral City
			}
 		}
 		
 	}
 	
 }
