 package battlechallenge.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Map.Entry;
 
 import com.google.gdata.util.ServiceException;
 
 import battlechallenge.ActionResult;
 import battlechallenge.ActionResult.ShotResult;
 import battlechallenge.CommunicationConstants;
 import battlechallenge.Coordinate;
 import battlechallenge.ShipAction;
 import battlechallenge.maps.BattleMap;
 import battlechallenge.maps.MapImporter;
 import battlechallenge.settings.Config;
 import battlechallenge.ship.Ship;
 import battlechallenge.ship.ShipCollection;
 import battlechallenge.structures.City;
 import battlechallenge.structures.Structure;
 import battlechallenge.visual.BCViz;
 import battlechallenge.visual.GenerateVideo;
 import battlechallenge.visual.YoutubeUploader;
 
 /**
  * The Class Game.
  */
 public class Game extends Thread {
 	
 	public static final boolean DEBUG = true;
 	
 	public static final int DEFAULT_WIDTH;
 	public static final int DEFAULT_HEIGHT;
 	public static final int DEFAULT_SPEED;
 	public static final int MAX_NUM_TURNS;
 	
 	static {
 		DEFAULT_WIDTH = 15;
 		DEFAULT_HEIGHT = 15;
 		DEFAULT_SPEED = 750; // number of milliseconds to sleep between turns
 		MAX_NUM_TURNS = 300; //300; // 5 minutes if 1 turn per second
 	}
 	
 	/** The players. */
 	private Map<Integer, ServerPlayer> players;
 	
 	private GameManager manager;
 	
 	private BCViz viz;
 	
 	private BattleMap map;
 	
 	private ShipCollection ships;
 	
 	private int minsPerShip = 10; // TODO: Make variable per game
 	
 	private String videoURL;
 	
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
 		for (int turnCount = 0; turnCount<MAX_NUM_TURNS && livePlayers > 1;turnCount++) {
 			doTurn(actionResults, turnCount);
 			viz.updateGraphics(turnCount);
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
 		
 		List<ServerPlayer> winnerList = getWinner();
 		List<String> rankList = new ArrayList<String>();
 		//ServerPlayer winner = null;
 		String url = "http://autonomousarmada.azurewebsites.net/Match/Record";
 		String charset = "UTF-8";
 		String param = "";
 		String query = "";
 		
 		
 		Boolean draw = false;
 			
 		if (winnerList != null) { 
 			// Generate video because the game returned a winner implying a successful game
 			GenerateVideo video = new GenerateVideo(viz.getCurrentFolderName());
 			try {
 				if (video.genVideo()) { // if video was generated successfully
 					YoutubeUploader uploader = new YoutubeUploader(viz.getCurrentFolderName() + Config.sep + viz.getiExp().getTimeStamp() + ".mp4");
 					uploader.uploadVideo(); 
 					videoURL = uploader.getVideoURL();
 					System.out.println(videoURL);
 					query += "link=" + videoURL + "&"; // add video link query string
 					viz.getiExp().deleteCurrDir(); // delete the game directory with screen shots of frames to make video;
 				}
 				else {
 					System.out.println("Video was unable to be created");
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			int count = 0;
 			//Contestant.1=first place player name
 			for (ServerPlayer p: winnerList) { // store params as "Contestant.#=playerName"
 				rankList.add("Rank: " + (p.getEndGameRank()-1) + " = " + p.getName());
 				param = "Contestant[" + (p.getEndGameRank()-1) + "]=" + p.getName();	
 				query += param;
 				if (count < winnerList.size() -1) {
 					query += "&"; // params must be seperated by '&'
 				}
 				count++;
 			}
 			
			query += "mapName=" + map.getName();
 			//System.out.println("Query: " + query);
 			
 			URLConnection connection = null;
 			
 			try {
 				connection = new URL(url).openConnection();
 			} catch (MalformedURLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			connection.setDoOutput(true); // Triggers POST.
 			connection.setRequestProperty("Accept-Charset", charset);
 			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
 			OutputStream output = null;
 			try {
 			     output = connection.getOutputStream();
 			     output.write(query.getBytes(charset));
 			} catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 			     if (output != null) try { output.close(); } catch (IOException logOrIgnore) {
 			    	 System.out.println("error on output");
 			     }
 			}
 			InputStream response = null;
 			try {
 				response = connection.getInputStream();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				System.out.println("Response Error");
 				e1.printStackTrace();
 			}
 			
 			//System.out.println("Response: " + response);
 		}
 		// Prints the final game rankings
 		for (String s: rankList) {
 			System.out.println(s);
 		}
 		
 		for(ServerPlayer p : winnerList) {
 			if (draw == true) {
 				p.endGame(CommunicationConstants.RESULT_DRAW);
 				continue;
 			}
 			p.endGame(CommunicationConstants.RESULT_RANKED + "You finished ranked: " + p.getEndGameRank() + "/" + winnerList.size());
 			
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
 	private void doTurn(Map<Integer, List<ActionResult>> actionResults, int turnCount) {
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
 			if (!p.requestTurn(allPlayersShips, actionResults, map, turnCount)) {
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
 			List<ShipAction> shipActions = p.getTurn(map.getNumCols(), map.getNumRows(), map.getStructures(), turnCount);
 			// reset action results for current user
 			actionResults.get(p.getId()).clear();
 			playerActions.put(p.getId(), shipActions);
 		}
 		handleCollisions(); // if ships are overlapping after moving
 		// Now that all ships are moved, evaluate shots for each player
 		Map<String, String> shipIDMap = new HashMap<String, String>(); // keep track of ships already given actions
 		for(ServerPlayer p : players.values()) {
 			if (playerActions.get(p.getId()) == null) // Player is dead no need to get actions when they cannot act
 				continue;
 			for(ShipAction sa : playerActions.get(p.getId())) {
 				// NOTE: moves are processed in: ServerPlayer.getTurn(...);
 				// check if shot is within game boundaries
 				if (sa.getShipIdentifier() == null) { // Player passed in null shipIdentifier
 					continue;
 				}
 				// Prevent same ship from getting multiple move commands
 				if (shipIDMap.get(sa.getShipIdentifier().toString()) != null) {
 					continue;
 				}
 				
 				shipIDMap.put(sa.getShipIdentifier().toString(), sa.getShipIdentifier().toString());
 				
 				Ship s = ships.getShip(sa.getShipIdentifier());
 				if (s == null || sa.getShotCoordList().isEmpty()) // Handle null ships and empty shot list
 					continue;
 				for (int k=0;k<s.getNumShots() && k<sa.getShotCoordList().size(); k++) {
 					Coordinate c = sa.getShotCoordList().get(k);
 					if (c == null || (s.distanceFromCoord(c) > s.getRange()) || 
 							!c.inBoundsInclusive(0, map.getNumRows()-1, 0, map.getNumCols()-1)) {// || 
 							//s.isSunken()) {
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
 									p.recordSunkenShip();
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
 		ships.removeSunkenShips();
 		ships.updateCoordinates();
 		spawnShips();
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
 	public List<ServerPlayer> getWinner() {
 //		List<ServerPlayer> validPlayers = new LinkedList<ServerPlayer>();
 		List<ServerPlayer> maxPlayerList = new LinkedList<ServerPlayer>();
 		PriorityQueue<ServerPlayer> playerList = new PriorityQueue<ServerPlayer>(8, comp);
 //		ServerPlayer maxPlayer = null;
 //		
 //		for (ServerPlayer player: players.values()) {{
 //				validPlayers.add(player);
 //		}	
 //		if (validPlayers.size() == 1)
 //			return validPlayers;
 //		if (validPlayers.size() == 0)
 //			validPlayers = new LinkedList<ServerPlayer>(players.values());
 		
 		int maxScore = -1;
 
 		for (ServerPlayer p : players.values()) {
 			playerList.offer(p); // Sorted by player Score
 		}
 		
 		int currRank = 1;
 		for (ServerPlayer p: playerList) { // assign player ranks
 			if (p.getScore() < maxScore) {
 				currRank++;
 			}
 			else {
 				maxScore = p.getScore();
 			}
 			p.setEndGameRank(currRank);
 			maxPlayerList.add(p); 
 		}
 		return maxPlayerList; // TODO: Change to return a priority queue
 	}
 	
 	public void allocateIncome() {
 		Map<Integer,Integer> incomes = new HashMap<Integer,Integer>();
 		for(Integer id : players.keySet())
 			incomes.put(id, 0);
 		for (Structure str: map.getStructures()) {
 			// there are no bases in map.getStructures
 			if (str instanceof City) {
 				City city = (City)str;
 				int ownerId = city.getOwnerId();
 				if (ownerId >= 0) { // all non-neutral cities
 					// increment players minerals by the amount the city generates
 					incomes.put(ownerId, incomes.get(ownerId) + city.getMineralGenerationSpeed());
 				}
 			}
 		}
 		for (Entry<Integer,Integer> e : incomes.entrySet())
 			players.get(e.getKey()).incrementMinerals(e.getValue());
 	}
 	
 	/**
 	 * Updates cities owners to players that have ships in the cities
 	 */
 	public void updateCities() {
 		Map<Coordinate, Ship> allShipCoords = new HashMap<Coordinate, Ship>(); // Stores coords of all players ships
 		Ship ship;
 		for(ServerPlayer p : players.values()) {
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
 	
 	
 	static Comparator<ServerPlayer> comp = new Comparator<ServerPlayer>() { // frames are created with IDs in order, sort them this way
 		  public int compare(ServerPlayer p1, ServerPlayer p2) {
 			Integer p1Score = p1.getScore();
 			Integer p2Score = p2.getScore();
 		    return p2Score.compareTo(p1Score);
 		  }
 	};
 }
