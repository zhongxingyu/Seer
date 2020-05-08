 package com.example.wecharades.presenter;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.TreeSet;
 
 import android.content.Context;
 
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.model.Invitation;
 import com.example.wecharades.model.Model;
 import com.example.wecharades.model.Player;
 import com.example.wecharades.model.Turn;
 
 /**
  * This is a class intended as an interface to the model and database.
  * 	All requests for DATA should be run through this class, which will handle
  * 	the logic of fetching cached data and retrieving form the database.
  * 	This class will also house the logic for pushing and updating data to both  
  * @author Anton Dahlstrm
  *
  */
 public class DataController {
 	
 	private static DataController dc = null;
 	private Model m;
 	private Database db;
 	
 	private DataController(Context context){
 		m = Model.getModelInstance(context);
 		db = Database.getDatabaseInstance(context);
 	}
 	
 	public static DataController getDataController(Context context){
 		if(dc == null){
 			dc = new DataController(context);
 		}
 		return dc;
 	}
 	
 	public void saveState(Context context){
 		m.saveModel(context);
 	}
 	
 	//Session handling -----------------------------------------------------------
 	
 	/**
 	 * Log in a player
 	 * @param username - The username (case insensitive)
 	 * @param password - The password
 	 * @throws DatabaseException - if the connection to the database fails 
 	 */
 	public void loginPlayer(String username, String password) throws DatabaseException{
 		db.loginPlayer(username, password);
 		m.setCurrentPlayer(db.getCurrentPlayer());
 	}
 	
 	/**
 	 * Log out the current player
 	 */
 	public void logOutPlayer(){
 		db.logOut();
 		m.logOutCurrentPlayer();
 	}
 	
 	/**
 	 * returns the current user
 	 * @return
 	 */
 	public Player getCurrentPlayer(){
 		return db.getCurrentPlayer();
 	}
 	
 	/**
 	 * Register a player
 	 * @param inputNickname - The player
 	 * @param inputEmail - The registered email
 	 * @param inputPassword - the password
 	 * @param inputRepeatPassword - the password, repeated
 	 * @throws DatabaseException - if the connection fails
 	 */
 	public void registerPlayer(
 			String inputNickname, 
 			String inputEmail, 
 			String inputPassword, 
 			String inputRepeatPassword
 			) throws DatabaseException{
 		db.registerPlayer(inputNickname, inputEmail, inputPassword,inputRepeatPassword);
 	}
 	
 	/**
 	 * Resets the password connected to the provided email address
 	 * @param email - The email address connected to an account.
 	 * @throws DatabaseException - if the connection fails 
 	 */
 	public void resetPassword(String email) throws DatabaseException{
 		db.resetPassword(email);
 	}
 	
 	//Players -----------------------------------------------------------
 	
 	/**
 	 * Get a user by its ParseId
 	 * @param parseId - the players ParseId
 	 * @return A player
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public Player getPlayerById(String parseId) throws DatabaseException {
 		Player p = m.getPlayerById(parseId);
 		if(p == null){
 			p = db.getPlayerById(parseId);
 			m.putPlayer(p);
 		}
 		return p;
 	}
 	
 	/**
 	 * Get a user by its username
 	 * @param username - the players username
 	 * @return A player
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public Player getPlayer(String username) throws DatabaseException {
 		Player p = m.getPlayer(username);
 		if(p == null){
 			p = db.getPlayer(username);
 			m.putPlayer(p);
 		}
 		return p;
 	}
 	
 	/**
 	 * Returns a list with all player names. This list will also be cached locally.
 	 * @return an ArrayList containing 
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public TreeSet<String> getAllOtherPlayerNames() throws DatabaseException {
 		ArrayList<Player> players = db.getPlayers();
 		m.putPlayers(players);
 		TreeSet<String> nameList = new TreeSet<String>();
 		for(Player p : players){
 			nameList.add(p.getName());
 		}
 		return nameList;
 	}
 	
 	//Games -----------------------------------------------------------
 	
 	/**
 	 * Create a game. The local storage will not be updated
 	 * @param p1 - player 1
 	 * @param p2 - palyer 2
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public void createGame(Player p1, Player p2) throws DatabaseException{
 		db.createGame(p1, p2);
 	}
 	
 	/**
 	 * Gets a list of current games. This should only be called from the StartPresenter,
 	 * 	as it updates the game-list from the database. If a game has changed, its current turn will be updated.
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public ArrayList<Game> getGames() throws DatabaseException{
 		ArrayList<Game> games = db.getGames(getCurrentPlayer());
 		Game localGame;
 		for(Game game : games){
 			localGame = m.getGame(game.getGameId());
 			//If the local game hasn't been created locally, fetch all turns and create the game
 			if(localGame == null){
 				m.putGame(game);
 				m.putTurns(db.getTurns(game));
 			} else if(Game.hasChanged(game, localGame)){
 				//Updates the current turn from the database
 				m.putTurn(db.getTurn(game, game.getTurn()));
 				//If the last turn was finished
 				if(game.getTurn() > localGame.getTurn()){
 					m.putTurn(db.getTurn(game, game.getTurn()-1));
 				}
 			}
 		}
 		m.putGameList(games);
 		return games;
 	}
 	
 	/**
 	 * Returns a game from its parseId
 	 * @param parseId - the games parseId
 	 * @return A Game, or null if no such game exist
 	 * @throws DatabaseException - if the connection to the database fails
 	 */
 	public Game getGame(String parseId) throws DatabaseException{
 		Game game = m.getGame(parseId);
 		if(game == null){
 			game = db.getGame(parseId);
 			m.putGame(game);
 		}
 		return game;
 	}
 	
 	/**
 	 * Updates the database for the game. 
 	 * 	CALL THIS METHOD BEFORE INCREMENTING THE GAME TURN!
 	 * 	if the turn is finished, this will also be set here.
 	 * @param game - the game to be updated
 	 * @throws DatabaseException
 	 */
 	public void updateGame(Game game) throws DatabaseException{
 		if(isFinished(game)){
 			game.setFinished();
 		}
 		db.updateGame(game);
 		db.updateTurn(m.getCurrentTurn(game));
 	}
 	/*
 	 * Helper method for updateGame()
 	 */
 	private boolean isFinished(Game game){
 		return (game.getTurn() == 6) && (m.getCurrentTurn(game).getState() == Turn.FINISH);
 	}
 	
 	//Turn -----------------------------------------------------------
 	/**
 	 * Get all turns for a game. These are all collected from the stored instance - updated at startscreen.
 	 * @param game - The game who's turns to fetch
 	 * @return An ArrayList of turns
 	 */
 	public ArrayList<Turn> getTurns(Game game){
 		return m.getTurns(game);
 	}
 	
 	
 	//Invitation -----------------------------------------------------------
 	
 	/**
 	 * A method to get all current invitations from the database
 	 * @return
 	 * @throws DatabaseException
 	 */
 	public ArrayList<Invitation> getInvitations() throws DatabaseException{
 		ArrayList<Invitation> invitations = db.getInvitations(getCurrentPlayer());
 		Date currentTime = new Date();
 		long timeDifference;
 		ArrayList<Invitation> oldInvitations = new ArrayList<Invitation>();
 		for(Invitation inv : invitations){
 			timeDifference = (currentTime.getTime() - inv.getTimeOfInvite().getTime()) / (1000L*3600L);
 			if(timeDifference > 72){ //if the invitations are considered to old
 				oldInvitations.add(inv);
 				invitations.remove(inv);
 			}
 		}
 		db.removeInvitations(oldInvitations);
 		return invitations;
 	}
 	
 	/**
 	 * Retrieves a list of all invitations sent form this device.
 	 * @return An ArrayList containing Invitations
 	 */
 	public ArrayList<Invitation> getSentInvitations(){
 		return m.getSentInviations();
 	}
 	
 	/**
 	 * Send an invitation to another player
 	 * @param invitation
 	 */
 	public void sendInvitation(Invitation invitation){
 		m.setSentInvitation(invitation);
 		db.sendInvitation(invitation);
 	}
 	
 	/**
 	 * Send an invitation to another Player (based on the Player class)
 	 * @param player The player-representation of the player
 	 */
 	public void sendInvitation(Player player){
 		sendInvitation(new Invitation(getCurrentPlayer(), player, new Date()));
 	}
 	
 }
