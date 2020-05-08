 package Server.gameSetupModule;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import Server.gameModule.Game;
 import Server.gameModule.Player;
 import Server.userModule.UserObject;
 
 /**
  * Wrapper Class for a poker table
  * 
  * @author mouhyi, Peter
  * 
  */
 public class GameTable implements Runnable {
 
 	private int hostId;
 	private int tableId;
 	private ArrayList<Player> players;
 	private int ante;
 	private double bringIn;
 	private static int numberOfTables = 0;
 	private Game curGame;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param host
 	 * @param ante
 	 * @param game
 	 * @param bringIn
 	 * @param players
 	 * @author Peter, mouhyi
 	 */
 	public GameTable(int ante, int hostId, ArrayList<Integer> playersId,
 			double bringIn) {
 		this.hostId = hostId;
 		this.ante = ante;
 		this.tableId = numberOfTables + 1;
		this.players = players;
 		this.bringIn = bringIn;
 		this.numberOfTables++;
 		new Thread(this).run();
 		
 		// register table with rmi
 	}
 
 	/**
 	 * returns the hostId
 	 * 
 	 * @return
 	 * @author Peter
 	 */
 	public int getHostId() {
 		return this.hostId;
 	}
 
 	/**
 	 * Returns the game name
 	 * 
 	 * @return
 	 * @author Peter
 	 */
 	public int getTableId() {
 		return this.tableId;
 	}
 
 	/**
 	 * Returns the list of players in the game
 	 * 
 	 * @return
 	 * @author Peter
 	 */
 	public ArrayList<Player> getPlayers() {
 		return this.players;
 	}
 
 	/**
 	 * Returns the ante of the game
 	 * 
 	 * @return
 	 * @author Peter
 	 */
 	public int getAnte() {
 		return this.ante;
 	}
 
 	// TODO implement in Lobby
 	public boolean requestJoin(int playerId) {
 
 	}
 
 	/**
 	 * This adds a player to the player list
 	 * 
 	 * @param playerId
 	 * @return
 	 * @author Peter, Mouhyi
 	 */
 	public int addPlayer(int playerId) {
 		Player p = null;
 		try {
 			p = new Player(playerId);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		synchronized (players) {
 			if (this.players.size() < 5 && p != null) {
 				this.players.add(p);
 				return 0;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * This removes a player from the player list
 	 * 
 	 * @param player
 	 * @author mouhyi
 	 */
 	public void removePlayer(int userId) {
 		Player player = this.getPlayer(userId);
 		synchronized (players) {
 			if (player != null) {
 				players.remove(player);
 			}
 		}
 	}
 
 	public int changeAnte(int newAnte) {
 		this.ante = newAnte;
 		return this.ante;
 	}
 
 	/**
 	 * This changes the minimum ante of the game
 	 * 
 	 * @param newante
 	 * @author Peter
 	 */
 
 	public double getBringIn() {
 		return this.bringIn;
 	}
 
 	/**
 	 * This returns the bringIn value that the player
 	 * 
 	 * @param newBringIn
 	 * @return
 	 */
 
 	public double changeBringIn(double newBringIn) {
 		this.bringIn = newBringIn;
 		return this.bringIn;
 	}
 
 	public void initiateGame() {
 		try {
 			curGame = new Game(this.ante, this.bringIn, this.players, this.tableId);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void run() {
 		try {
 			curGame.play();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Player getPlayer(int userId) {
 		for (Player p : players) {
 			if (p.getId() == userId) {
 				return p;
 			}
 		}
 		return null;
 	}
 }
