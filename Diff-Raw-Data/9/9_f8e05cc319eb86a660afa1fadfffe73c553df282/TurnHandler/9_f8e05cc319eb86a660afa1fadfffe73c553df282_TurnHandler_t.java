 package game;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Observable;
 
 import exception.NoPlayersLeftException;
 
 /**
  * This class handles everything involving the turns in the game
  * 
  * @invar	The turnhandler has a game to handle the turns for.
  * 			| game != null
  * 
  * @author 	Group 8
  * 
  * @version	May 2013
  */
 public class TurnHandler extends Observable {
 	
 	/**
 	 * The game which the TurnHandler handles the turns of
 	 */
 	private Game game;
 
 	/**
 	 * The turn queue of players in the game.
 	 */
 	private LinkedList<Player> playerQueue = new LinkedList<Player>() ;
 	
 	/**
 	 * Returns the game currently being played.
 	 * 
 	 * @return	The game.
 	 */
 	public Game getGame() {
 		return game;
 	}
 
 	/**
 	 * This method sets the game.
 	 * 
 	 * @param 	game
 	 * 			The game being played.
 	 * 
 	 * @effect	The game has been set.
 	 * 
 	 * @note	This method is used to maintain a bidirectional association
 	 * 			and should not be used in other cases.
 	 */
 	public void setGame(Game game) {
 		this.game = game;
 	}
 	
 	/**
 	 * Adds a player to the player queue.
 	 * 
 	 * @param 	player
 	 * 			The player to add.
 	 */
 	void addPlayerToQueue(Player player) {
 		playerQueue.add(player);
 	}
 
 	/**
 	 * Removes a player from the player queue.
 	 * 
 	 * @param 	player
 	 * 			The player to remove.
 	 */
 	void removePlayerFromQueue(Player player) {
 		playerQueue.remove(player);
 	}
 	
 	/**
 	 * This method initializes the players in the turnhandler.
 	 * 
 	 * @param 	players
 	 * 			The players in the game.
 	 * 
 	 * @post	The players are added to the playerQueue.
 	 * 			| playerQueue.addAll(players)
 	 * 
 	 * @effect	The players have a turnhandler
 	 * 			| for each player in players do
 	 * 			| 	 player.setTurnhandler(this)
 	 */
 	public void initializeTurnQueue(Player... players) {
 		// Add to the turn queue
 		playerQueue.addAll(Arrays.asList(players));
 		
 		// Set this turn handler
 		for(Player p: players)
 			p.setTurnHandler(this);
 	}
 
 	/**
 	 * Return who's turn it is.
 	 * 
 	 * @return	The current player.
 	 * 			| playerQueue.getFirst()
 	 */
 	public Player getCurrentPlayer() {
 		return playerQueue.getFirst();
 	}
 
 	/**
 	 * Return an array of active players in this game.
 	 * 
 	 * @return 	The array of active players.
 	 */
 	public Player[] getActivePlayers() {
 		return playerQueue.toArray(new Player[getNbActivePlayers()]);
 	}
 	
 	/**
 	 * Return the number of active players in this game.
 	 * 
 	 * @return 	The number of players in this game.
 	 * 			| result == players.size();
 	 */
 	public int getNbActivePlayers(){
 		return playerQueue.size();
 	}
 	
 	/*********************
 	 * CORE FUNCTIONALITY
 	 *********************/
 
 	/**
 	 * Go to the next player in the queue.
 	 * 
 	 * @effect 	The first player in the list of players is removed and added to the end of the list.
 	 * 			| players.addLast(players.removeFirst())
 	 * @effect	The turn of the next player is set.
 	 * 			| playerQueue.getFirst().startTurn()
 	 * 
 	 * @throws 	NoPlayersLeftException
 	 * 			If there are no players left.
 	 */
 	private void goToNextPlayer() throws NoPlayersLeftException {
 		Player currentPlayer = playerQueue.removeFirst();
 		playerQueue.addLast(currentPlayer);
 		
 		playerQueue.getFirst().startTurn();
 	}
 
 	/**
 	 * Check if the player who's turn it currently is, has more than 1 action left.
 	 * Also, check if he must not lose or win.
 	 * If he doesn't have any actions left, the turn goes to another player.
 	 * 
 	 * @effect	If there are actions left, do an after-action check.
 	 * 			| if (getCurrentPlayer().getActionsLeft() > 0)
 	 * 			|	checkStateAfterAction()
 	 * @effect 	If there are no actions left, do a after-turn check.
 	 * 			| if (getCurrentPlayer().getActionsLeft() <= 0)
 	 * 			|	checkStateAfterTurn()
 	 * @effect	The turns get switched when the current player has no actions left.
 	 * 			| if (getCurrentPlayer().getActionsLeft() <= 0)
 	 * 			|	switchTurn()
 	 * 
 	 * @throws 	NoPlayersLeftException 
 	 * 			If there are no players left.
 	 */
 	public void checkTurn() throws NoPlayersLeftException {
 		if(getCurrentPlayer().getActionsLeft() > 0){
 			checkStateAfterAction();
 			checkOnePlayerLeft();
 		} else {
 			checkStateAfterTurn();
 			checkOnePlayerLeft();
 			switchTurn();
 		}
 	}
 	
 	/**
 	 * Check if any of the players just won or lost the game.
 	 * 
 	 * @effect	Check the after-own-action lose condition for
 	 * 			the current player.
 	 * @effect	Check the after-each-action lose condition
 	 * 			for each player.
 	 * @effect	If one player is left and he must not lose,
 	 * 			then let him win.
 	 * 
 	 * @throws 	NoPlayersLeftException
 	 * 			If there are no players left.
 	 */
 	private void checkStateAfterAction() throws NoPlayersLeftException {
 		// lose check after action of current player
 		if(game.getMode().checkLoseAfterOwnAction(getCurrentPlayer())) {
 			loseGame(getCurrentPlayer());
 		} else if(game.getMode().checkWin(getCurrentPlayer())) {
 			game.winGame(getCurrentPlayer());
 		}
 		
 		// lose check after action of each player
 		LinkedList<Player> clone = new LinkedList<Player>(playerQueue);
 		for(Player player : clone) {
 			if(game.getMode().checkLoseAfterEachAction(player)) {
 				loseGame(player);
 			}
 		}
 	}
 	
 	/**
 	 * Checks the state of the game after each turn.
 	 * 
 	 * @effect	If the current player loses after his action, make the current player lose.
 	 * 			| if(game.getMode().checkLoseAfterOwnAction(getCurrentPlayer()))
 	 *			|	loseGame(getCurrentPlayer())
 	 * @effect	If the current player loses after his turn, make the current player lose.
 	 * 			|if(game.getMode().checkLoseAfterOwnTurn(getCurrentPlayer()))
 	 * 			|	loseGame(getCurrentPlayer());
 	 * 
 	 * @throws 	NoPlayersLeftException
 	 * 			If there are no players left.
 	 */
 	private void checkStateAfterTurn() throws NoPlayersLeftException {
 		if(game.getMode().checkLoseAfterOwnAction(getCurrentPlayer())) {
 			loseGame(getCurrentPlayer());
 		} else if(game.getMode().checkLoseAfterOwnTurn(getCurrentPlayer())){
 			loseGame(getCurrentPlayer());
 		}
 	}
 	
 	/**
 	 * Checks whether there is one player left and if he wins the game.
 	 * 
 	 * @effect	If the player is the only one left and does not lose the game,
 	 * 			he wins the game.
 	 */
 	private void checkOnePlayerLeft() {
 		// if one player is left and he must not lose, let him win
 		if(getNbActivePlayers() == 1)  {
 			Player remainingPlayer = playerQueue.get(0);
 			if(!game.getMode().checkLoseAfterEachAction(remainingPlayer)) {
 				if(playerQueue.get(0) == getCurrentPlayer()) {
 					if(!game.getMode().checkLoseAfterOwnAction(remainingPlayer))
 						game.winGame(playerQueue.get(0));
 				} else {
 					game.winGame(playerQueue.get(0));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Switch turns.
 	 * 
 	 * @effect	Go to the next player in the queue.
 	 * 			| if getCurrentPlayer().hasMoved() || getCurrentPlayer().skipsTurn() || getCurrentPlayer().hasReceivedDamage() then
 	 * 			| 	 goToNextPlayer()
 	 * @effect 	The subscribers are notified of the change of the turn.
 	 * 			| setChanged()
 	 * 			| notifyObservers()
 	 * @effect	Check the turn again, to be sure.
 	 * 			| checkTurn();
 	 * 
 	 * @throws 	NoPlayersLeftException 
 	 * 			If there are no players left.
 	 */
 	private void switchTurn() throws NoPlayersLeftException {		
 		goToNextPlayer();
 		
 		setChanged();
 		notifyObservers();
 		
 		checkTurn();
 	}
 	
 	/**
 	 * Let a player lose the game.
 	 * 
 	 * @param 	loser
 	 * 			The player who loses the game.
 	 * 
 	 * @effect	The loser is removed from the game.
 	 * 			| loser.terminate()
	 * @effect	If it is the loser's turn, then the next player
	 * 			must still start its turn.
	 * 			| if losersTurn then
	 * 			| 	 getCurrentPlayer().startTurn()
 	 *
 	 * @throws 	NoPlayersLeftException 
 	 * 			If there are no players left.
 	 */
 	private void loseGame(Player loser) throws NoPlayersLeftException {
 		boolean losersTurn = (loser == getCurrentPlayer());
 		loser.terminate();
 		
 		if(getNbActivePlayers() == 0) 
 			throw new NoPlayersLeftException();
 		
 		if(losersTurn) {
 			getCurrentPlayer().startTurn();
 		}
 	}
 	
 }
