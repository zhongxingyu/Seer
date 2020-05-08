 package com.example.wecharades.model;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.TreeMap;
 
 import android.content.Context;
 import android.util.Log;
 
 /**
  * This class stores all the data available in the game locally.
  * 	This class exist to reduce the number or request to parse.com
  *
  */
 public class Model implements Serializable{
 
 	private static final long serialVersionUID = -8167671678222883966L;
 	//The name of our model save file
 	private static final String 	SAVE_FILE = "model.save";
 	//Stored variables to use in other classes - should maybe be in another class.
 	public static final int 		
 	FINISHEDGAMES_SAVETIME 			= 168
 	, FINISHEDGAMES_NUMBERSAVED 	= 10
 	, INVITATIONS_SAVETIME 			= 72;
 
 	/*
 	 * A variable that can be changed in order to purge the model - this is done manually when needed!
 	 * 	When this is set to true, the model will be forced to be recreated. This is done to purge the
 	 * 	Model and retrieve a mirror of the database information, while preserving login status 
 	 * 	(and avoid having to reinstall and log in and out again). This MIGHT be implemented as a feature later. 
 	 * 
 	 * 	-- DO NOT FORGET TO RESET THIS AFTERWARDS! --
 	 */
 	private static boolean			PURGE = false;
 
 	//A variable to check if model is already saved.
 	private boolean					SAVED = false;
 	//A variable which is called when a user logs out 
 	// - the model exists a moment so we may finish any queries first
 	private static boolean 			RECREATE = false;
 
 	//Two maps for games for increased speed and ease of use
 	private TreeMap<Game, ArrayList<Turn>> gameList = new TreeMap<Game, ArrayList<Turn>>();
 	private TreeMap<String, Game> gameIdList = new TreeMap<String, Game>();
 
 	//Two maps for player names and id:s. The second one is used for increased speed and ease of use
 	private TreeMap<String, Player> storedPlayers = new TreeMap<String, Player>();
 	private TreeMap<String, String> storedPlayerNames = new TreeMap<String, String>();
 	private Player currentPlayer = null;
 
 	// Invitations are stored locally in two lists
 	private LinkedList<Invitation> sentInvitations = new LinkedList<Invitation>();
 	private LinkedList<Invitation> receiveInvitations = new LinkedList<Invitation>();
 
 	//Singleton
 	private static Model singleModel;
 
 	private Model(Context context){
 		//Creating a file to save to
 		if(context != null){
 			saveModel(context);
 		}
 	}
 
 	/**
 	 * Use this method to get the singleton instance of the model where necessary.
 	 * @return the Model
 	 */
 	public static Model getModelInstance(Context context){
 		if(PURGE){
 			//If the PURGE variable is set to true (done manually), the model will be recreated
 			eraseModel(context);
 			singleModel = null;
 			PURGE = false;
 		}
 		if(singleModel == null){
 			//Try to load from storage
 			singleModel = loadModel(context);
 		}
 		if(singleModel == null || RECREATE){
 			//If there were no previous models present, create a new one
 			singleModel = new Model(context);
 			RECREATE = false;
 		}
 		return singleModel;
 	}
 
 	/**
 	 * A method to save the current model to memory.
 	 * @param context - used to retrieve a save location
 	 */
 	public void saveModel(Context context){
 		if(!SAVED && context != null){
 			try {
 				FileOutputStream ops = context.openFileOutput(SAVE_FILE, Context.MODE_PRIVATE);
 				ObjectOutputStream oOut = new ObjectOutputStream(ops);
 				oOut.writeObject(singleModel);
 				oOut.close();
 				SAVED = true;
 			} catch (IOException e) {
 				Log.d("IO - Model save", e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Method to load a model form memory
 	 * @param context
 	 * @return
 	 */
 	private static Model loadModel(Context context){
 		Model singleModel = null;
 		if(context != null){
 			try {
 				ObjectInputStream oIn = new ObjectInputStream(context.openFileInput(SAVE_FILE));
 				Object obj = oIn.readObject();
 				if (obj != null && obj.getClass().equals(Model.class)){
 					singleModel = (Model) obj;
 				}
 			} catch (FileNotFoundException e1){
 				Log.d("IO - Model load", "No file found");
 			} catch (IOException e2){
 				Log.d("IO - Model load", "IOException");
 			} catch (ClassNotFoundException e3){
 				Log.d("IO - Model load", "ClassNotFound");
 			}
 		}
 		return singleModel;
 	}
 
 	/**
 	 * Called to erase the current model from memory and disk.
 	 * @param context
 	 */
 	private static void eraseModel(Context context){
 		if(context != null){
 			File modelFile = new File(context.getFilesDir(), SAVE_FILE);
 			if(modelFile.delete()){
 				Log.d("Model - File:","Removed file");
 			}
 			RECREATE = true;
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
 		SAVED = false;
 	}
 
 	/**
 	 * Updates a game in the internal list of games. Will also create new games that does not exist.
 	 * @param game - the game to be updated
 	 */
 	public void putGame(Game game){
 		//This is actually kind of fast, although it might look a bit weird.
 		ArrayList<Turn> tempTurns;
 		if(gameList.containsKey(game) && gameList.get(game) != null){
 			tempTurns = gameList.get(game);
 			gameList.remove(game);
 			gameList.put(game,tempTurns);
 			gameIdList.put(game.getGameId(), game);
 		} else{
 			gameList.put(game, null);
 			gameIdList.put(game.getGameId(), game);
 		}
 		SAVED = false;
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
 		SAVED = false;
 	}
 
 	/**
 	 * Use to update a single turn of a game. This will add a turn if it does not exist,
 	 * 	as well as update its state if it is existant.
 	 * @param game - the game in question
 	 * @param turn - the turn of the game
 	 * @throws NoSuchElementException if no game is found
 	 */
 	public void putTurn(Turn turn){
 		if(turn != null){
 			if(!gameIdList.containsKey(turn.getGameId()))
 				throw new NoSuchElementException();
 			Game game = getGame(turn.getGameId());
 			ArrayList<Turn> listOfTurns = gameList.get(game);
 			if(listOfTurns == null){
 				listOfTurns = new ArrayList<Turn>();
 				gameList.put(game, listOfTurns);
 			} else if(listOfTurns.contains(turn)){
 				//If the turn contains the turn, we must delete it first
 				listOfTurns.remove(turn);
 			}
 			listOfTurns.add(turn);
 		}
 		SAVED = false;
 	}
 
 	/**
 	 * Updates a list of turns at once - the existing list will be overwritten.
 	 * @param turnList
 	 * @throws NoSuchElementException if no game is found
 	 */
 	public void putTurns(ArrayList<Turn> turnList) throws NoSuchElementException{
 		//Do not simply replace the list, as this might cause problems with the amount of turns etc.
 		for(Turn turn : turnList){
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
 	public Turn getCurrentTurn(Game game) {
 		if(game != null){
 			ArrayList<Turn> turns = getTurns(game);
 			if(turns != null){
 				for(Turn t : turns){
 					//Find the turn with CurrentTurnNumber
 					if(t.getTurnNumber() == game.getTurnNumber()){
 						return t;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	//Players ---------------------------------------------------------------
 
 	/**
 	 * Puts a player in stored players 
 	 * @param player - the player to be stored
 	 */
 	public void putPlayer(Player player){
 		//The data for a player should always be updated
 		storedPlayerNames.put(player.getName(), player.getParseId());
 		storedPlayers.put(player.getParseId(),player);
 		SAVED = false;
 	}
 
 	/**
 	 * Puts a collection of players into the model
 	 * @param players - a collection of players
 	 */
 	public void putPlayers(Collection<Player> players){
 		storedPlayers.clear();
 		storedPlayerNames.clear();
 		for(Player player : players){
 			putPlayer(player);
 		}
 	}
 
 	/**
 	 * Used to get a player representation from a username
 	 * @param username - the player username
 	 * @return a Player, or null if no player was found
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
 	 * Designates a player as the current player. If the player does not exist in cache,  
 	 *  it gets added.
 	 */
 	public void setCurrentPlayer(Player player){
 		currentPlayer = player;
 		putPlayer(player);
 		SAVED = false;
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
 	public void logOutCurrentPlayer(Context context){
 		eraseModel(context);
 	}
 
 	//Invitations ---------------------------------------------------------------
 	//Received invitations are not needed here, as they should allways be fetched from the database.
 
 	/**
 	 * Set all sent invitations from this player. This replaces the local version of this game.
 	 * @param invitations - The invitations to add
 	 */
 	public void setSentInvitations(LinkedList<Invitation> invitations){
 		if(invitations != null){
 			sentInvitations = invitations;
 		} else{
 			sentInvitations.clear();
 		}
 		SAVED = false;
 	}
 
 	/**
 	 * Set all received invitations to this player
 	 * @param invitations - The invitations to add
 	 */
 	public void setReceivedInvitations(LinkedList<Invitation> invitations){
 		if(invitations != null){
 			receiveInvitations = invitations;
 		} else{
 			receiveInvitations.clear();
 		}
 		SAVED = false;
 	}
 
 	/**
 	 * Retrieve a list of Invitations sent from this device.
 	 * @return A List containing invitations.
 	 */
 	public List<Invitation> getSentInvitations(){
 		return sentInvitations;
 	}
 
 	/**
 	 * Retrieve a list of Invitations the current player has received
 	 * @return A List containing invitations.
 	 */
 	public List<Invitation> getReceivedInvitations(){
 		return receiveInvitations;
 	}
 
 }
