 package de.htwg.kalaha.controller;
 
 import java.util.List;
 
 import de.htwg.kalaha.model.Board;
 import de.htwg.kalaha.model.Hollow;
 import de.htwg.kalaha.model.KalahaHollow;
 import de.htwg.kalaha.model.Marble;
 import de.htwg.kalaha.model.Player;
 import de.htwg.util.observer.Observable;
 
 public class KalahaController extends Observable {
 	
 	private String statusMessage = "Welcome!";
 	private Board board;
 	private Player player1, player2;
 	
 	/**
 	 * Instantiates a new kalaha controller.
 	 */
 	public KalahaController() {
 		this.player1 = new Player("Player 1");
 		this.player2 = new Player("Player 2");
 		this.board = new Board(this.player1,this.player2);	
 	}
 	
 	/**
 	 * Prepare the board for a new game.
 	 */
 	public void prepareNewGame() {
 		board.prepareBoard();
 		setStatusMessage("New game started");
 		notifyObservers();
 	}
 	
 	
 	/**
 	 * Take marbles out of a hollow and distribute them.
 	 *
 	 * @param playernum the player number
 	 * @param number the hollow number
 	 */
 	public void takeMarbles(int playernum, int number) {
 		Player player;
 		if (playernum == 1) {
 			player = player1;
 		} else {
 			player = player2;
 		}
 		
 		Hollow hollow = board.getHollow(player, number);
 		
 		if (!hollow.isEmpty()) {
 			List<Marble> marbles = hollow.getMarbles();
 			Hollow current = hollow;
 			int count = marbles.size();
 			for (int i = 1; i <= count; i++) {
 				current = board.getNextHollow(current);
 				current.addMarble(marbles.get(0));
 				marbles.remove(0);
 			}
 			setStatusMessage("The hollow p" + playernum + "," + number + " is now empty");
 			catchMarbles(current);
 			
 			// Switch the active player after a turn
 			board.switchActivePlayer();
 		} else {
 			setStatusMessage("The hollow p" + playernum + "," + number + " is already empty");
 		}
 		
 		
 		
 		
 		
 		
 		notifyObservers();
 	}
 	
 	public void catchMarbles(Hollow current) {
 		KalahaHollow  playerKalaha = board.getKalaha(board.getActivePlayer());
		if (!current.equals(playerKalaha)) {
		if (current.getMarbleCount() == 1 && current.getOwner() == board.getActivePlayer() &&
               !board.getOppositeHollow(current).isEmpty()) {
 			List<Marble> marbles = board.getOppositeHollow(current).getMarbles();
 			marbles.addAll(current.getMarbles());
 			playerKalaha.addMarbles(marbles);
		}
 		}
 	}
 	
 	
 	
 	/**
 	 * Gets the status message.
 	 *
 	 * @return the status message
 	 */
 	public String getStatus() {
 		return statusMessage;
 	}
 
 	private void setStatusMessage(String statusMessage) {
 		this.statusMessage = statusMessage;
 	}
 	
 	/**
 	 * Gets the game board as a string.
 	 *
 	 * @return the game board as a string
 	 */
 	public String getBoardString() {
 		return board.toString();
 	}
 
 }
