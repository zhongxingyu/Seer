 package com.example.wecharades.model;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 
 import android.content.Context;
 
 import com.example.wecharades.presenter.Database;
 import com.parse.ParseUser;
 
 /**
  * This class stores all the data available in the game.
  * @author Anton Dahlstrm
  *
  */
 public class Model {
 	private static final String SAVE_FILE = "model.save";
 	
 	//Two maps for games for increased speed
 	private HashMap<Game, ArrayList<Turn>> gameList = new HashMap<Game, ArrayList<Turn>>();
 	private HashMap<String, Game> gameIdList = new HashMap<String, Game>();
 	
 	//Two maps for player names and id:s. The second one is used for increased speed
 	private HashMap<String, Player> storedPlayers = new HashMap<String, Player>();
 	private HashMap<String, String> storedPlayerNames = new HashMap<String, String>();
 	private Player currentPlayer = null;
 	
 	/*
 	 * Invitations are stored locally, in order to check that two invites aren't sent to one person (weak check).
 	 */
 	private ArrayList<Invitation> sentInvitations = new ArrayList<Invitation>(); 
 	
 
 	//Singleton
 	private static Model singleModel;
 
 	private Model(Context context){
 		loadModel(context);
 	}
 
 	/**
 	 * Use this method to get the singleton instance of the model where necessary.
 	 * @return the Model
 	 */
 	public static Model getModelInstance(Context context){
 		if (singleModel == null){
 			singleModel = new Model(context);
 		}
 		return singleModel;
 	}
 	
 	/**
 	 * A method to save the current model to memory.
 	 * 	This should be done on every onDestroy
 	 * @param context
 	 */
 	public void saveModel(Context context){
 		try {
 			ObjectOutputStream oOut = new ObjectOutputStream(
 						context.openFileOutput(SAVE_FILE, Context.MODE_PRIVATE)
 					);
 			oOut.writeObject(singleModel);
 			oOut.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private static void loadModel(Context context){
 		try {
 			ObjectInputStream oIn = new ObjectInputStream(context.openFileInput(SAVE_FILE));
 			Object obj = oIn.readObject();
 			if (obj.getClass().equals(Model.class)){
 				singleModel = (Model) obj;
 			}
 		} catch (IOException e){
 			//TODO ndra hr ocks
 		} catch (ClassNotFoundException e2){
 			//TODO ndra ven hr
 		}
 	}
 
 	//Games ---------------------------------------------------------------
 
 	/**
 	 * Updates a list of games. If a game is not existant, it will be added to the list. 
 	 * @param games
 	 */
 	public void putGameList(ArrayList<Game> games){
 		for(Game game : games){
 			putGame(game);
 		}
 	}
 
 	/**
 	 * Updates a game in the internal list of games. Will also create new games that does not exist.
 	 * @param game - the game to be updated
 	 */
 	public void putGame(Game game){
 		//This is actually kind of fast, although it might look a bit weird.
 		ArrayList<Turn> tempTurns;
 		if(gameList.containsKey(game)){
 			tempTurns = gameList.get(game);
 			gameList.remove(game);
 			gameList.put(game,tempTurns);
 			gameIdList.put(game.getGameId(), game);
 		} else{
			gameList.put(game, new ArrayList<Turn>(6));
 			gameIdList.put(game.getGameId(), game);
 		}
 
 	}
 
 	/**
 	 * Return an ArrayList with current games
 	 * @return - an arraylist containing games
 	 */
 	public ArrayList<Game> getGames(){
 		return new ArrayList<Game>(gameList.keySet());
 	}
 	
 	/**
 	 * Gets a game from its game id
 	 * @param parseId
 	 * @return a Game, or null it does not exist
 	 */
 	public Game getGame(String parseId){
 		return gameIdList.get(parseId);
 	}
 
 	/**
 	 * Removes a game form the model
 	 * @param game - the game to be deleted
 	 * @return - true if the game was in the list, false otherwise
 	 */
 	public void removeGame(Game game){
 		gameIdList.remove(game.getGameId());
 		gameList.remove(game);
 	}
 
 	/**
 	 * Use to update a single turn of a game. This will add a turn if it does not exist,
 	 * 	as well as update its state if it is existant.
 	 * @param game - the game in question
 	 * @param turn - the turn of the game
 	 * @throws NoSuchElementException if no game is found
 	 */
 	public void putTurn(Turn turn){
 		Game game = getGame(turn.getGameId());
 		if( !gameList.containsKey(game))
 			throw new NoSuchElementException();
 		ArrayList<Turn> listOfTurns = gameList.get(game);
		if(listOfTurns.contains(turn)) //Removes the old copy of the turn
			listOfTurns.remove(turn.getTurnNumber()-1); 
		listOfTurns.add(turn.getTurnNumber()-1, turn); //Adds the new copy of the game
 	}
 
 	/**
 	 * Updates a list of turns at once
 	 * @param game - 
 	 * @param turnList
 	 * @throws NoSuchElementException if no game is found
 	 */
 	public void putTurns(ArrayList<Turn> turnList) throws NoSuchElementException{
 		for(Turn turn : turnList) {
 			putTurn(turn);
 		}
 	}
 
 	/**
 	 * Get a list of turns for a game
 	 * @param game - the game
 	 * @return - an arraylist of turns
 	 */
 	public ArrayList<Turn> getTurns(Game game){
 		return gameList.get(game);
 	}
 	
 	/**
 	 * Returns the current turn from the model
 	 * @param game - the game to fetch from
 	 * @return a Turn
 	 */
 	public Turn getCurrentTurn(Game game){
 		return gameList.get(game).get(game.getTurn()-1);
 	}
 
 	//Players ---------------------------------------------------------------
 	
 	public boolean playerIsCached(Player player){
 		return storedPlayers.containsKey(player.getParseId());
 	}
 	
 	/**
 	 * Puts a player in stored players 
 	 * @param player - the player to be stored
 	 * @return if the player was added or not
 	 */
 	public void putPlayer(Player player){
 		if(!playerIsCached(player)){
 			storedPlayers.put(player.getParseId(),player);
 			storedPlayerNames.put(player.getName(), player.getParseId());
 		}
 	}
 	
 	/**
 	 * Puts a collection of players into the model
 	 * @param players - a collection of players
 	 */
 	public void putPlayers(Collection<Player> players){
 		for(Player player : players){
 			putPlayer(player);
 		}
 	}
 
 	/**
 	 * Used to get a player representation from a username
 	 * @param username - the player username
 	 * @return a Player
 	 */
 	public Player getPlayer(String username){
 		Player retPlayer = null;
 		if(storedPlayerNames.containsKey(username)){
 			retPlayer = storedPlayers.get(storedPlayerNames.get(username));
 		}
 		return retPlayer;
 	}
 
 	/**
 	 * Used to get a player representation from a username
 	 * @param parseId - the player id
 	 * @return a Player or null if not found
 	 */
 	public Player getPlayerById(String parseId){
 		return storedPlayers.get(parseId);
 	}
 
 	/**
 	 * Designates a player as the current player. If the player does not exist,  
 	 *  it gets added.
 	 */
 	public void setCurrentPlayer(Player player){
 		ParseUser parseUser = ParseUser.getCurrentUser();
 		currentPlayer = new Player(parseUser.getObjectId(), 
 				parseUser.getString(Database.PLAYER_USERNAME_NATURAL), 
 				parseUser.getInt(Database.PLAYER_GLOBALSCORE));
 		putPlayer(currentPlayer);
 	}
 
 	/**
 	 * Returns the logged in player player (ParseUser)
 	 * @return A Player representation of The current player, or null if this player does not exist.
 	 */
 	public Player getCurrentPlayer(){
 		return currentPlayer;
 	}
 	
 	/**
 	 * Deletes the current player entirely from the model. Should be done when user logs out.
 	 */
 	public void logOutCurrentPlayer(){
 		storedPlayers.remove(currentPlayer.getParseId());
 		storedPlayerNames.remove(currentPlayer.getName());
 		currentPlayer = null;
 	}
 	
 	//Invitations ---------------------------------------------------------------
 	//Received invitations are not needed here, as they should allways be fetched from the database.
 	
 	/**
 	 * 
 	 * @param invitation
 	 */
 	public void setSentInvitation(Invitation invitation){
 		sentInvitations.add(invitation);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ArrayList<Invitation> getSentInviations(){
 		return sentInvitations;
 	}
 
 }
