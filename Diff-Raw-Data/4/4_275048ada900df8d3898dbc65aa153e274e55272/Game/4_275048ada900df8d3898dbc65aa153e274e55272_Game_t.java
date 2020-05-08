 package game;
 
 import exception.*;
 import grid.*;
 
 /**
  * This class represents a game.
  * 
  * @invar	The game has a valid gridbuilder
  * 			| canHaveAsGridBuilder(this.getGridBuilder())
  * @invar	The game has a valid grid.
  * 			| canHaveAsGrid(this.grid())
  * 
  * @author 	Groep 8
  * 
  * @version	February 2013
  */
 public class Game {
 	
 	/**
 	 * The grid of the game.
 	 */
 	private Grid grid;
 	
 	/**
 	 * Players that take part in the game.
 	 */
 	private Player[] players = new Player[PlayerColour.values().length];
 		
 	/**
 	 * Indicate if the game is ended.
 	 */
 	private boolean gameEnded; 
 	
 	/**
 	 * Represents the winner of the game.
 	 */
 	private Player winner;
 	
 	/**
 	 * Represents the gridCreator, which is responsible for creating the grid.
 	 */
 	private GridBuilder gridBuilder;
 	
 	/**
 	 * Represents the turnHandler, which is responsible for handling the turns.
 	 */
 	private TurnHandler turnHandler;
 	
 	/**
 	 * Constructor for a game.
 	 * 
 	 * @param 	width
 	 * 			The width of the grid/game.
 	 * @param 	height
 	 * 			The height of the grid/game.
 	 * 
 	 * @effect	The turnhandler is initialized.
 	 * 			| initializeTurnHandler()
 	 * @effect	The grid is set.
 	 * 			| getGrid() == new GridCreator(width, height).getGrid()
 	 * @effect 	The players are initialized.
 	 * 			| initializePlayers();
 	 * @effect	The game is set not ended.
 	 * 			| getGameEnded() == false;
 	 * 
 	 * @throws	InvalidDimensionException  [must]
 	 * 			Dimension of the width and the height are invalid.
 	 */
 	@Deprecated
 	public Game(int width, int height) throws InvalidDimensionException
 	{
 		initializeTurnHandler();
 		setGridBuilder(new GeneratedRandomGridBuilder(width, height));
 		setGrid(gridBuilder.getGrid());
 		initializePlayers();
 		setGameEnded(false);
 	}
 	
 	/**
 	 * Initialize a new game.
 	 * 
 	 * @param	GridBuilder
 	 * 			The gridbuilder for the game that must be set.
 	 * 
 	 * @effect	The turnhandler is initialized.
 	 * 			| initializeTurnHandler()
 	 * @effect  The gridbuilder is set.
 	 * 			| setGridBuilder(gridBuilder)
 	 * @effect	The grid is set.
 	 * 			| setGrid(gridBuilder.getGrid());
 	 * @effect 	The players are initialized.
 	 * 			| initializePlayers();
 	 * @effect	The game is set not ended.
 	 * 			| getGameEnded() == false;
 	 */
 	public Game(GridBuilder gridBuilder) {
 		initializeTurnHandler();
 		setGridBuilder(gridBuilder);
 		setGrid(gridBuilder.getGrid());
 		initializePlayers();
 		setGameEnded(false);
 	}
 	
 	/********************
 	 * GETTERS & SETTERS
 	 ********************/
 	
 	/**
 	 * Returns the turnhandler for this game.
 	 * 
 	 * @return 	The turnhanlder for this game.
 	 * 			| result == turnhandler
 	 */
 	public TurnHandler getTurnHandler() {
 		return turnHandler;
 	}
 	
 	/**
 	 * Initializes the turnhandler.
 	 * 
 	 * @effect	The turnhandler is initialized.
 	 * 			| new.turnhandler = new TurnHandler
 	 * @effect	This game is set in the turnhandler.
 	 * 			| turnHandler.setGame(this)
 	 */
 	private void initializeTurnHandler() {
 		this.turnHandler = new TurnHandler();
 		turnHandler.setGame(this);
 	}
 
 	/**
 	 * Initialize the players in this game.
 	 * Starting from the fact their is for the moment only a red and a blue player.
 	 * 
 	 * @Effect	Player Red is constructed and placed on his starting position.  
 	 * @Effect 	Player Blue is constructed and placed on his starting position.
 	 * @Effect	Both players are added to the list of players.
 	 * 			| players.add(players)
 	 * @Effect	The current player is set (which is player Red).
 	 * 			| getCurrentPlayer() == playerRed
 	 * @effect	The turn queue in turn handler is initialized.
 	 * 			| turnHandler.initializeTurnQueue(players)
 	 */
 	private void initializePlayers() {
 		InnerSquare redSquare = null;
 		InnerSquare blueSquare = null;
 		try {
			redSquare = getGrid().getInnerSquareAtCoordinate(getGrid().getStartingPositions()[0]);
			blueSquare = getGrid().getInnerSquareAtCoordinate(getGrid().getStartingPositions()[1]);
 		} catch (OutsideTheGridException e) {
 			// Can not occur.
 		}	
 		// Add the players to the game
 		players[0] = new Player(redSquare,PlayerColour.RED);		
 		players[1] = new Player(blueSquare,PlayerColour.BLUE);
 		
 		// Add to turn handler
 		turnHandler.initializeTurnQueue(players[0], players[1]);
 	}
 
 	/**
 	 * Return the grid in this game.
 	 * 
 	 * @return 	The grid in this game.
 	 */
 	public Grid getGrid()
 	{
 		return grid;
 	}
 
 	/**
 	 * Set the grid in this game (More information inside Grid).
 	 * 
 	 * @param	grid
 	 * 			The given grid to be set.
 	 */
 	protected void setGrid(Grid grid)
 	{
 		this.grid = grid;
 	}
 
 	/**
 	 * Return an array of the players in this game.
 	 * 
 	 * @return 	An array of the players in this game.
 	 * 			| result == players
 	 */
 	public Player[] getPlayers(){
 		return players;
 	}
 		
 	/**
 	 * Return the winner of the game.
 	 * 
 	 * @return	The winner of the game.
 	 */
 	public Player getWinner() {
 		return winner;
 	}
 
 	/**
 	 * Set the winner of the game.
 	 * 
 	 * @param 	winner
 	 * 			The winner of the game.
 	 * 
 	 * @Post	The given winner is the winner of the game,
 	 * 			if the game hasn't been won yet.
 	 * 			| getWinner() == winner
 	 */
 	private void setWinner(Player winner) {
 		if(getWinner() == null)
 			this.winner = winner;
 	}
 	
 	/**
 	 * Return the gridbuilder.
 	 * 
 	 * @return	The gridbuilder.
 	 */
 	public GridBuilder getGridBuilder() {
 		return gridBuilder;
 	}
 	
 	/**
 	 * Set the gridbuilder.
 	 * 
 	 * @param 	gridBuilder
 	 * 			The gridBuilder to be set.
 	 * 
 	 * @Post	The gridbuilder is set.
 	 * 			| this.gridBuilder = gridBuilder
 	 */
 	private void setGridBuilder(GridBuilder gridBuilder) {
 		this.gridBuilder = gridBuilder;
 	}
 
 	/**
 	 * Return whether the game is ended.
 	 * 
 	 * @return	True if the game is ended. False if not.
 	 */
 	public boolean isGameEnded() {
 		return gameEnded;
 	}
 
 	/**
 	 * Set the game to be ended.
 	 * 
 	 * @param 	gameEnded
 	 * 			True or false.
 	 * @Post	The game is ended (or the game is not ended).
 	 * 			| getGameEnded == true (or == false)
 	 */
 	private void setGameEnded(boolean gameEnded) {
 		this.gameEnded = gameEnded;
 	}
 	
 	/**
 	 * Let a player win the game.
 	 * 
 	 * @param 	winner
 	 * 			The player who wins the game.
 	 * @effect	The game is ended.
 	 * 			| getGameEnded() == true
 	 * @effect	The player who wins is the winner.
 	 * 			| getWinner() == winner
 	 */
 	protected void winGame(Player winner) {
 		setGameEnded(true);
 		setWinner(winner);
 	}
 		
 }
