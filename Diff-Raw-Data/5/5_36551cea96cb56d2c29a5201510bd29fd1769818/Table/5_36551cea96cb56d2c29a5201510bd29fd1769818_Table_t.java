 package model.game;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import model.player.iPlayer;
 import model.player.hand.*;
 
 /**
  * A class that represent table at which a poker game takes place. This class 
  * has a central role in Dragon. It has access to all the other classes in the 
  * application and is the class through which the game is controlled.
  * 
  * @author Mattias Henriksson
  * @author lisastenberg
  * 
  */
 
 public class Table {
 	private Round round;
 	private Dealer dealer;
 	private List<Card> tableCards;
 	private List<iPlayer> players;
 	private int indexOfCurrentPlayer;
 	
 	/**
 	 * Creates a new Table.
 	 */
 	public Table() {
 		this(new Round(),new Dealer());
 	}
 	
 	/**
 	 * Creates a new Table.
 	 */
 	public Table(Round round, Dealer dealer) {
 		this.round = round;
 		this.dealer = dealer;
 		tableCards = new ArrayList<Card>();
 		players = new ArrayList<iPlayer>();
 		indexOfCurrentPlayer = 0;
 	}
 	
 	/**
 	 * Adds a player to the table.
 	 * @param p The player that will be added to the list of players
 	 * @throws IllegalArgumentException if there are all ready ten players at the table
 	 */
 	public void addPlayer(iPlayer p) throws IllegalArgumentException {
 		if (players.size() < 10) {
 			players.add(p);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	/**
 	 * Sets the turn to the next player in order.
 	 */
 	public void nextPlayer() {
 		if (indexOfCurrentPlayer < players.size() - 1) {
 			indexOfCurrentPlayer++;
 		} else {
 			indexOfCurrentPlayer = 0;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return The player who's turn it currently is
 	 */
 	public iPlayer getCurrentPlayer() {
 		return players.get(indexOfCurrentPlayer);
 	}
 	
 	/**
 	 * Adds a card to the "table cards" 
 	 * @param c The card which will be added
 	 * @throws IllegalArgumentException if there are all ready five cards on the table 
 	 */
 	public void addTableCard(Card c) throws IllegalArgumentException {
 		if (tableCards.size() < 5) {
 			tableCards.add(c);
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 	
 	/**
 	 * Clears all "table cards" from the table.
 	 */
 	public void clearTableCards() {
 		tableCards.clear();
 	}
 	
 	/**
 	 * Makes a players cards visible
 	 * @param p The player which cards will be set visible
 	 */
 	public void makeHandVisible(iPlayer p) {
 		p.getHand().setVisible(true); //TODO bttre att gra en metod i Player makeHandVisble fr att undvika lnga rader av metodanrop?
 	}
 	
 	/**
 	 * @author Mattias Henriksson
 	 * @author lisastenberg
 	 * 
 	 * Calculates the amount of chips the winner(s) will get and distributes it to him.
 	 * After the pot is distributed equally among the winner(s), the pot is emptied. 
 	 */
 	public void distributePot(List<iPlayer> winners) {
 		// This assumes that the pot can be distributed equally.
 		// TODO: How to do?
 		int winnerAmount = round.getPot().getValue() / winners.size();
 		
 		for (iPlayer p: winners) {
 			p.getBalance().addToBalance(winnerAmount);
 		}
 		round.getPot().emptyPot();
 	}
 	
     /**
      * @author Oscar Stigter
      * @author lisastenberg
      * 
      * Performs the Showdown.
      */
 	
 
     public List<iPlayer> doShowdown() {
         // Look at each hand value (calculated in HandEvaluator), sorted from highest to lowest.
         Map<HandValue, List<iPlayer>> rankedPlayers = getRankedPlayers();
         for (HandValue handValue : rankedPlayers.keySet()) {
             // Get players with winning hand value.
             List<iPlayer> winners = rankedPlayers.get(handValue);
             distributePot(winners);
             return winners;
         }
         // No person is the winner. This should never happen.
         return null;
     }
     
     /**
      * @author Oscar Stigter
      * @author lisastenberg
      * Returns the active players mapped and sorted by their hand value.
      * 
      * The players are sorted in descending order (highest value first).
      * 
      * @return The active players mapped by their hand value (sorted). 
      */
     private Map<HandValue, List<iPlayer>> getRankedPlayers() {
         Map<HandValue, List<iPlayer>> winners = new TreeMap<HandValue, List<iPlayer>>();
 		for (iPlayer player : players) {
 			if (player.isActive()) {
 				// Create a hand with the community cards and the player's hole
 				// cards.
 				FullTHHand hand = new FullTHHand(tableCards);
 				hand.addCards(player.getHand());
 				// Store the player together with other players with the same
 				// hand value.
 				HandValue handValue = new HandValue(hand);
 				List<iPlayer> playerList = winners.get(handValue);
 				if (playerList == null) {
 					playerList = new LinkedList<iPlayer>();
 				}
 				playerList.add(player);
 				winners.put(handValue, playerList);
 			}
 		}
         return winners;
     }
 	
 	/**
 	 * 
 	 * @return The current round
 	 */
 	public Round getRound() {
 		return round;
 	}
 	
 	/**
 	 * 
 	 * @return The table's dealer
 	 */
 	public Dealer getDealer() {
 		return dealer;
 	}
 	
 	/**
 	 * This method is used only for testing of the class.
 	 * @return A list of players at the table.
 	 */
 	public List<iPlayer> getPlayers() {
 		return players;
 	}
 	
 	/**
 	 * This method is used only for testing of the class.
 	 * @return The "table cards" represented as a list of cards.
 	 */
 	public List<Card> getTableCards() {
 		return tableCards;
 	}
 	
 	/**
 	 * Tostring method for the Table class
 	 * @author Mattias Forssen
 	 * @author mattiashenriksson
 	 * @return Returns a string containing the names of all players, cards, who the current player is
 	 * and what cards are shown
 	 */
 	@Override
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		result.append("Players at table: ");
 		for(iPlayer p : this.players) {
 			result.append(p.getName() + "\t\t");
 		}
 		result.append("\nCards: ");
 		for(iPlayer p: players) {
 			List<Card> hand = p.getHand().getCards();
 			for(Card c : hand) {
 				result.append(c.toString() + "\t");
 			}
 		}
 		result.append("\nCurrent player is " + getCurrentPlayer().getName() + "\n");
 		result.append("Table cards are:" + "\n");
 		for(Card c : this.tableCards) {
 			result.append(c.toString() + "\t\t");
 		}
 		return result.toString();
 	}
 	
 	/**
 	 * Equals method for the Table class
 	 * @author forssenm
 	 * @param Object to compare with
 	 * @return returns true if they are the same object otherwise false
 	 */
 	@Override
 	public boolean equals(Object o) {
 		return (o == this);
 	}
 	
 	//Since we at the current state aren't planning on using any hashtables this code was added
 	//for the cause of good practice
 	public int hashCode() {
 		  assert false : "hashCode not designed";
 		  return 42; // any arbitrary constant will do
 	}
 	
 }
