 package com.jaredpearson.puzzlestrike;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.jaredpearson.puzzlestrike.actions.Action;
 import com.jaredpearson.puzzlestrike.states.State;
 
 public class Game {
 	private Integer id;
 	private Player owner;
 	private boolean started;
 	private int maxPlayerCount = 2;
 	private List<PlayerGameContext> players = new ArrayList<PlayerGameContext>();
 	private ChipPile chipPile;
 	private Integer currentPlayerIndex;
 	private Integer winner;
 	private StateManager stateManager;
 	
 	public Game() {
 		stateManager = new StateManager(this);
 		chipPile = new ChipPile();
 	}
 	
 	public Game(Integer id, Player owner) {
 		this();
 		this.id = id;
 		this.owner = owner;
 	}
 	
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Gets the ID of the game.
 	 */
 	public Integer getId() {
 		return id;
 	}
 	
 	/**
 	 * Gets the person who is the owner of the game.
 	 */
 	public Player getOwner() {
 		return owner;
 	}
 	
 	public void setOwner(Player owner) {
 		this.owner = owner;
 	}
 	
 	public boolean isOwner(Player player) {
 		return this.owner.equals(player);
 	}
 	
 	/**
 	 * Returns the maximum number of players allowed in the game.
 	 */
 	public int getMaxPlayerCount() {
 		return maxPlayerCount;
 	}
 	
 	public boolean isFull() {
 		return this.players.size() == maxPlayerCount;
 	}
 	
 	public boolean isOpen() {
 		return !isFull() && !this.isStarted();
 	}
 	
 	public boolean isStarted() {
 		return this.started;
 	}
 	
 	public void addPlayer(Player player) {
 		if(isFull()) {
 			throw new IllegalStateException("Game is full");
 		}
 		if(isStarted()) {
 			throw new IllegalStateException("Game has already started");
 		}
 		
 		if(player != null) {
 			this.players.add(new PlayerGameContext(player));
 		}
 	}
 	
 	public int getPlayerCount() {
 		return this.players.size();
 	}
 	
 	public List<PlayerGameContext> getPlayers() {
 		return this.players;
 	}
 	
 	public boolean isActivePlayer(Player player) {
 		return this.isStarted() && getActivePlayerContext().getPlayer().equals(player);
 	}
 	
 	/**
 	 * Determines if the specified player is in the game.
 	 */
 	public boolean containsPlayer(Player player) {
		for(PlayerGameContext context : this.players) {
			if(context.getPlayer().getId().equals(player.getId()))
			{
				return true;
			}
		}
		return false;
 	}
 	
 	public Player getActivePlayer() {
 		return getActivePlayerContext().getPlayer();
 	}
 	
 	private PlayerGameContext getPlayerContext(Player player) {
 		for(PlayerGameContext context : this.players) {
 			if(player.equals(context.getPlayer())) {
 				return context;
 			}
 		}
 		throw new IllegalArgumentException();
 	}
 	
 	public PlayerGameContext getActivePlayerContext() {
 		if(!this.isStarted()) {
 			throw new IllegalStateException("Game has not been started so no active player has been selected.");
 		}
 		return this.players.get(currentPlayerIndex);
 	}
 	
 	public Integer getCurrentPlayerIndex() {
 		return currentPlayerIndex;
 	}
 	
 	public void setCurrentPlayerIndex(Integer value) {
 		if(value < 0 || value >= this.players.size()) {
 			throw new IllegalStateException("Player index is out of bounds");
 		}
 		
 		this.currentPlayerIndex = value;
 	}
 	
 	/**
 	 * Goes to the next player
 	 */
 	public void goToNextPlayer() {
 		if(this.getCurrentPlayerIndex() + 1 >= this.getPlayerCount()) {
 			this.setCurrentPlayerIndex(0);
 		} else {
 			this.setCurrentPlayerIndex(this.getCurrentPlayerIndex() + 1);
 		}
 	}
 	
 	/**
 	 * Sets the winner of the game.
 	 */
 	public void setWinner(Player player) {
 		this.winner = this.players.indexOf(player);
 	}
 	
 	public PlayerGameContext getWinner() {
 		if(this.winner == null) {
 			return null;
 		} else {
 			return this.players.get(this.winner);
 		}
 	}
 	
 	public void selectCharacter(Player player, GameCharacter character) {
 		getActivePlayerContext().setCharacter(character);
 	}
 	
 	/**
 	 * Gets the list of actions available to the player
 	 */
 	public List<Action> getActions(Player player) {
 		return stateManager.getCurrentState().getActions(this, player);
 	}
 	
 	/**
 	 * Gets the hand of the player
 	 */
 	public List<Chip> getHand(Player player) {
 		return this.getPlayerContext(player).getHand();
 	}
 	
 	public String getStatus(Player player) {
 		if(!isStarted() && this.isOwner(player) && this.isFull()) {
 			return "Waiting for another player to join";
 		}
 		
 		if(isActivePlayer(player)) {
 			return "";
 		} else {
 			return "Waiting for " + this.players.get(currentPlayerIndex).getPlayer().getName();
 		}
 	}
 	
 	public void start() {
 		//TODO: randomly select the first player
 		this.currentPlayerIndex = 0;
 		this.started = true;
 	}
 	
 	public ChipPile getChipPile() {
 		return chipPile;
 	}
 	
 	/**
 	 * Tells the game to go to the specified state.
 	 */
 	public void setState(State state) {
 		this.stateManager.setState(state);
 	}
 	
 	/**
 	 * Applies the action to this game for the given player.
 	 * @param player The player that is invoking this action.
 	 * @param action The action to perform.
 	 */
 	public void applyAction(Player player, Action action) {
 		stateManager.handle(player, action);
 	}
 }
